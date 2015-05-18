package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.productSearchIntentHandlers;

import com.google.common.collect.Lists;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.ProductSearchHandler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by juliewe on 12/05/2015.
 */
public class ConfirmMessageHandler implements Handler.ProblemHandler {
    public static final String requiredState = ProductSearchHandler.confirmMessage;
    public static final List<String> mystates = Lists.newArrayList(requiredState, "confirm_yes_no");
    public static final List<String> myintents = Lists.newArrayList(ProductSearchHandler.confirm, Intent.yes, Intent.no, ProductSearchHandler.confirmRecipient);
    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {

        boolean statematch1 = dialogue.getStates().stream().anyMatch(s -> s.equals(requiredState));
        boolean statematch2 = dialogue.getStates().stream().allMatch(s-> mystates.contains(s));
        boolean intentmatch1 = intents.stream().anyMatch(s -> myintents.contains(s.getName()));
        boolean intentmatch = false; //TODO
        //dialogue.getStates().stream().forEach(s->System.err.println(s));
        //System.err.println("Handleable by the ConfirmProductHandler: "+statematch1+", "+statematch2+", "+intentmatch1);
        return (statematch1 &&statematch2 && intentmatch1) || intentmatch;
    }

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue, Object resource) {

        //need to check for yes or no and handle accordingly.  Accept/reject.  Then update
        System.err.println("ConfirmMessageHandler has fired");
        int accepting = ConfirmProductHandler.determineAccepting(intents);
        if(accepting>0){ConfirmProductHandler.handleAccept(dialogue, ProductSearchHandler.messageSlot);}
        if(accepting<0){ConfirmProductHandler.handleReject(dialogue, ProductSearchHandler.messageSlot);}
        boolean updated=handleUpdate(intents, dialogue, ProductSearchHandler.castDB(resource));
        if(!updated && accepting <0){handleNoInfo(dialogue,ProductSearchHandler.castDB(resource));}
        return ProductSearchHandler.processStack(dialogue,ProductSearchHandler.castDB(resource));
    }

    public static boolean handleUpdate(List<Intent> intents, Dialogue d, ProductMongoDB db){

        Intent i = intents.stream().filter(intent->intent.getName().equals(ProductSearchHandler.confirmRecipient)).findFirst().orElse(null);
        if(i==null) {
            return false;
        } else {
            List<Intent.Slot> messages = new ArrayList<>();
            messages.add(BuyMethod.handleMessage(i,d,db));
            d.peekTopIntent().clearSlots(ProductSearchHandler.messageSlot);
            d.peekTopIntent().fillSlots(messages);
            return true;
        }
    }

    public static void handleNoInfo(Dialogue d,ProductMongoDB db){
        d.pushFocus("request_message");

    }
}
