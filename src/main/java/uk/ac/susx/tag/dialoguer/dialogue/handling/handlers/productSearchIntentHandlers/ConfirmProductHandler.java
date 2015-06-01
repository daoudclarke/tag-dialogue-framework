package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.productSearchIntentHandlers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.ProductSearchHandler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by juliewe on 12/05/2015.
 */
public class ConfirmProductHandler implements Handler.ProblemHandler {

    public static final String requiredState = "confirm_product_query";
    public static final List<String> mystates = Lists.newArrayList(requiredState, "confirm_yes_no");
    public static final List<String> myintents = Lists.newArrayList(ProductSearchHandler.confirm, Intent.yes, Intent.no, ProductSearchHandler.confirmProduct, ProductSearchHandler.buy);
    public static final Set<String> updateIntents = Sets.newHashSet(ProductSearchHandler.buy,ProductSearchHandler.confirmProduct);


    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        //this should only fire if user is definitely accepting or rejecting the selected product

        boolean statematch1 = dialogue.getStates().stream().anyMatch(s -> s.equals(requiredState));
        boolean statematch2 = dialogue.getStates().stream().allMatch(s-> mystates.contains(s));
        boolean intentmatch1 = intents.stream().anyMatch(s -> myintents.contains(s.getName()));//any of these intents along with the other state
        boolean intentmatch = intents.stream().anyMatch(s->s.getName().equals(ProductSearchHandler.confirmProduct));//or just this intent (which should only fire in presence of other state anyway) so probably don't need this

        //dialogue.getStates().stream().forEach(s->System.err.println(s));
        //System.err.println("Handleable by the ConfirmProductHandler: "+statematch1+", "+statematch2+", "+intentmatch1);
        return (statematch1 &&statematch2 && intentmatch1) || intentmatch ;
    }

    @Override
    public void handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        //need to check for yes or no and handle accordingly.  Accept/reject.  Then update
        System.err.println("ConfirmProductHandler has fired");
        int accepting = determineAccepting(intents);
        if(accepting>0){handleAccept(dialogue, ProductSearchHandler.productIdSlot);}
        if(accepting<0){handleReject(dialogue,ProductSearchHandler.productIdSlot);}
        boolean updated=handleUpdate(intents, dialogue, ProductSearchHandler.castDB(resource),accepting);
        if(!updated && accepting <0){handleNoInfo(dialogue,ProductSearchHandler.castDB(resource));}
    }


    public static int determineAccepting(List<Intent> intents) {
        int accepting = 0; //1=accepting, -1=rejecting, 0=don't know
        Intent confirmation = Intent.getFirstIntentFromSource(ProductSearchHandler.yesNoAnalyser, intents);
        if (confirmation == null) {
            confirmation = Intent.getFirstIntentFromSource(ProductSearchHandler.mainAnalyser, intents);
            if (confirmation != null) {
                if (confirmation.getName().equals(ProductSearchHandler.confirm)) {
                    if (confirmation.isSlotTypeFilledWith(ProductSearchHandler.yes_no_slot, "yes")) {
                        accepting = 1;
                    } else {
                        if (confirmation.isSlotTypeFilledWith(ProductSearchHandler.yes_no_slot, "no")) {
                            accepting = -1;
                        }
                    }
                }
            }
        } else {
            if (confirmation.getName().equals(Intent.yes)) {
                accepting = 1;
            } else {
                accepting = -1;
            }
        }
        return accepting;
    }

    public static void handleAccept(Dialogue d, String slot){
        //should also check for "Yes Aphrodite"
        d.putToWorkingMemory(slot,"confirmed");
    }
    public static void handleReject(Dialogue d, String slot){
        //should also check for "No not Aphrodite"
        //get rejectedproductlist from working memory, add current product(s)
        String rejectedlist=d.getFromWorkingMemory("rejected"+slot);
        if(rejectedlist==null){rejectedlist="";}
        Intent workingIntent=d.popTopIntent();
        for(String id: workingIntent.getSlotValuesByType(slot)){
            rejectedlist+=id+" ";
        }
        workingIntent.clearSlots(slot);
        d.putToWorkingMemory("rejected"+slot,rejectedlist);
        d.addToWorkingIntents(workingIntent);
        d.clearChoices();
    }
    public static boolean handleUpdate(List<Intent> intents, Dialogue d, ProductMongoDB db, int accepting){
        //find the intent which offers more positive information
        Intent i = intents.stream().filter(intent->updateIntents.contains(intent.getName())).findFirst().orElse(null);
        if(i==null){
            return false;
        } else {
            System.err.println("Updating working intent with new info");
            Intent workingIntent=d.popTopIntent();
            if(i.isName(ProductSearchHandler.buy)) {
                workingIntent.clearSlots(ProductSearchHandler.productSlot);
            }
            workingIntent.fillSlots(i.getSlotByType(ProductSearchHandler.productSlot)); //add info
            List<Intent.Slot> queries= BuyProblemHandler.handleProduct(workingIntent, d, db);
            if(accepting>0) {
                //check whether new slots contain productIdSlot
                String currentId=workingIntent.getSlotValuesByType(ProductSearchHandler.productIdSlot).get(0); //should only be 1 if accepting
                boolean matches = queries.stream()
                        .filter(slot->slot.name.equals(ProductSearchHandler.productIdSlot))
                        .map(slot->slot.value)
                        .collect(Collectors.toList())
                        .contains(currentId);
                if(!matches)
                    {
                        d.pushFocus("no_match_respecify");}
                else {
                    d.addToWorkingIntents(workingIntent);
                    if(d.isEmptyFocusStack()){
                        AcceptProblemHandler.handleAccept(d);
                    }
                    return false;}
            } else {
                //add in the new products found and update focus
                workingIntent.clearSlots(ProductSearchHandler.productSlot);
                workingIntent.fillSlots(queries);
                if(d.getFromWorkingMemory("focus")!=null){
                    d.pushFocus(d.getFromWorkingMemory("focus"));
                    d.putToWorkingMemory("focus",null);
                }
            }
            if(d.isEmptyFocusStack()){
                d.pushFocus("confirm_buy");}
            d.addToWorkingIntents(workingIntent);
            return true;
        }

    }
    private void handleNoInfo(Dialogue d, ProductMongoDB db){
        //search for alternative products?
        d.peekTopIntent().fillSlots(BuyProblemHandler.handleProduct(d.peekTopIntent(), d, db));
        if(d.getFromWorkingMemory("focus")!=null){
            d.pushFocus(d.getFromWorkingMemory("focus"));
            d.putToWorkingMemory("focus",null);
        }

    }
}