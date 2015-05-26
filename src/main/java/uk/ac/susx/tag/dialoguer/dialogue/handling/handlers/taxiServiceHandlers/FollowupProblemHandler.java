package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.taxiServiceHandlers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.ProductSearchHandler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.TaxiServiceHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by juliewe on 19/05/2015.
 */
public class FollowupProblemHandler implements Handler.ProblemHandler {
    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        Intent intent= intents.stream().filter(i-> TaxiServiceHandler.followupIntents.contains(i.getName())).findFirst().orElse(null);
        if(intent==null){
            return false;
        } else {
            return intent.isAnySlotFilled();
        }
    }

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        return null;
    }

    @Override
    public boolean subhandle(List<Intent> intents, Dialogue dialogue, Object resource) {
        System.err.println("Followup Problem Handler fired");
        int accepting = determineAccepting(intents);
        Intent followup = intents.stream().filter(i->TaxiServiceHandler.followupIntents.contains(i.getName())).findFirst().orElse(null); // will not be null as otherwise not inHandleableState
        dialogue.addToWorkingIntents(intents.stream().filter(i->i.isName(TaxiServiceHandler.orderTaxiIntent)).collect(Collectors.toList())); //save any orderTaxiIntents to working intents
        if(dialogue.isEmptyWorkingIntents()){ // this should not happen because this intents require the "followup" state to be set
            throw new Dialoguer.DialoguerException("Follow up intent generated when no orderTaxiIntents present");
        }
        switch (followup.getName()){
            case TaxiServiceHandler.followupCapacityIntent:
                handleEntity(followup,dialogue,accepting, TaxiServiceHandler.capacitySlot);
                break;
            case TaxiServiceHandler.followupTimeIntent:
                handleEntity(followup,dialogue,accepting, TaxiServiceHandler.timeSlot);
                break;
            case TaxiServiceHandler.followupLocationIntent:
                if(followup.areSlotsFilled(Sets.newHashSet(TaxiServiceHandler.destinationSlot))){
                    handleEntity(followup,dialogue,accepting,TaxiServiceHandler.destinationSlot);
                } else {
                    if(followup.areSlotsFilled(Sets.newHashSet(TaxiServiceHandler.pickupSlot))){
                        handleEntity(followup,dialogue,accepting, TaxiServiceHandler.pickupSlot);
                    }
                }
                break;

        }
        //return TaxiServiceHandler.processStack(dialogue);
        return true;
    }

    private static int determineAccepting(List<Intent> intents){
        int res=0;
        Intent confirmation = Intent.getFirstIntentFromSource(ProductSearchHandler.yesNoAnalyser, intents);
        if(confirmation!=null) {
            if (confirmation.isName(Intent.yes)) {
                res = 1;
            } else {
                if (confirmation.isName(Intent.no)) {
                    res = -1;
                }
            }
        }
        return res;
    }

    static void handleEntity(Intent i, Dialogue d, int accepting, String slotname){
        List<String> values = validate(i,slotname);
        update(accepting,values,slotname,d);
    }


    public static List<String> validate(Intent i, String slotname){
        List<String> values = i.getSlotValuesByType(slotname);
        if (values.isEmpty()){
            if(defaultvalue(slotname)!=null) {
                i.fillSlot(slotname, defaultvalue(slotname));
                values.add(defaultvalue(slotname));
            }
        } else {
            //check values are valid - currently assume all ok
            values.stream().forEach(value -> System.err.println(slotname+" : " + value));
            values=values.stream().filter(value->isValidValue(slotname, value)).collect(Collectors.toList());
        }
        return values;
    }


    private static void update(int accepting, List<String> values, String slotname, Dialogue d) {

        if(values.isEmpty()){
            d.pushFocus(TaxiServiceHandler.respecifyResponse);
            d.putToWorkingMemory("slot_to_choose",slotname);
        } else {
            if (accepting > 0) {
                //check matches working intent
                if (values.size()==1&&d.peekTopIntent().getSlotValuesByType(slotname).stream().filter(value -> value.equals(values.get(0))).count() > 0) {
                    //ok
                    d.peekTopIntent().replaceSlot(new Intent.Slot(slotname, values.get(0), 0, 0)); //replace multiple options if present
                } else {
                    values.stream().forEach(newvalue->d.peekTopIntent().fillSlot(new Intent.Slot(slotname, newvalue, 0, 0)));
                    d.pushFocus(TaxiServiceHandler.chooseResponse); //multiple possibilities for capacity so choose
                    d.putToWorkingMemory("slot_to_choose",slotname);
                }

            } else {
                //replace info in working intent - if accepting not known, assuming rejection
                d.peekTopIntent().clearSlots(slotname);
                values.stream().forEach(newvalue -> d.peekTopIntent().fillSlot(new Intent.Slot(slotname, newvalue, 0, 0)));
                if (values.size() > 1) {
                    d.pushFocus(TaxiServiceHandler.chooseResponse);
                    d.putToWorkingMemory("slot_to_choose", slotname);
                }
                if (d.peekTopFocus().equals(TaxiServiceHandler.confirmCompletionResponse)) {
                    d.pushFocus(TaxiServiceHandler.confirmResponse);
                }
            }

        }
    }
    private static String defaultvalue(String slotname){
        switch(slotname){
            case TaxiServiceHandler.capacitySlot:
                return "4";
            case TaxiServiceHandler.timeSlot:
                return "ASAP";
            default:
                return null;
        }

    }
    private static boolean isValidValue(String slotname, String value){
        switch(slotname){
            case TaxiServiceHandler.capacitySlot:
                return isValidCapacity(value);
            case TaxiServiceHandler.timeSlot:
                return isValidTime(value);
            default:
                return isValidLocation(value);
        }
    }

    private static boolean isValidLocation(String value){
        return true;
    }
    private static boolean isValidTime(String value){
        return true;
    }
    private static boolean isValidCapacity(String value){
        try {
            int number = Integer.parseInt(value);
            if(number>0&&number<8){
                return true;
            } else {
                return false;
            }
        }
        catch(NumberFormatException e){
                return false;
            }
    }
}
