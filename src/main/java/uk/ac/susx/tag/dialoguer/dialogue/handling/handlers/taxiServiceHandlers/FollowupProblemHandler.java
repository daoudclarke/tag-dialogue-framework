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
        int accepting = determineAccepting(intents);
        Intent followup = intents.stream().filter(i->TaxiServiceHandler.followupIntents.contains(i.getName())).findFirst().orElse(null); // will not be null as otherwise not inHandleableState
        dialogue.addToWorkingIntents(intents.stream().filter(i->i.isName(TaxiServiceHandler.orderTaxiIntent)).collect(Collectors.toList())); //save any orderTaxiIntents to working intents
        if(dialogue.isEmptyWorkingIntents()){ // this should not happen because this intents require the "followup" state to be set
            throw new Dialoguer.DialoguerException("Follow up intent generated when no orderTaxiIntents present");
        }
        switch (followup.getName()){
            case TaxiServiceHandler.followupCapacityIntent:
                handleCapacity(followup,dialogue,accepting);
                break;
            case TaxiServiceHandler.followupTimeIntent:
                handleTime(followup,dialogue,accepting);
                break;
            case TaxiServiceHandler.followupLocationIntent:
                if(followup.areSlotsFilled(Sets.newHashSet(TaxiServiceHandler.destinationSlot))){
                    handleDestination(followup,dialogue,accepting);
                } else {
                    if(followup.areSlotsFilled(Sets.newHashSet(TaxiServiceHandler.pickupSlot))){
                        handlePickup(followup,dialogue,accepting);
                    }
                }
                break;

        }
        return TaxiServiceHandler.processStack(dialogue);
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

    static void handleCapacity(Intent i, Dialogue d, int accepting){

        List<String> values=validate(i,TaxiServiceHandler.capacitySlot);
        update(accepting, values, TaxiServiceHandler.capacitySlot, Lists.newArrayList(TaxiServiceHandler.chooseCapacityResponse),d);
    }

    static void handleTime(Intent i, Dialogue d, int accepting){
        List<String> values=validate(i,TaxiServiceHandler.timeSlot);
        update(accepting,values,TaxiServiceHandler.timeSlot,Lists.newArrayList(TaxiServiceHandler.chooseTimeResponse),d);
    }

    static void handleDestination(Intent i, Dialogue d, int accepting){
        List<String> values = validate(i, TaxiServiceHandler.destinationSlot);
        update(accepting,values,TaxiServiceHandler.destinationSlot,Lists.newArrayList(TaxiServiceHandler.chooseDestinationResponse,TaxiServiceHandler.respecifyDestinationResponse),d);
    }

    static void handlePickup(Intent i, Dialogue d, int accepting){
        List<String> values = validate(i, TaxiServiceHandler.pickupSlot);
        update(accepting,values,TaxiServiceHandler.pickupSlot,Lists.newArrayList(TaxiServiceHandler.choosePickupResponse,TaxiServiceHandler.respecifyPickupResponse),d);
    }

    public static List<String> validate(Intent i, String slotname){
        List<String> values = i.getSlotValuesByType(slotname);
        if (values.isEmpty()){
            //insert default - this should not have happened in a followup though so print warning
            if(defaultvalue(slotname)!=null) {
                i.fillSlot(slotname, defaultvalue(slotname));
            }
        } else {
            //check values are valid - currently assume all ok
            values.stream().forEach(value -> System.err.println(slotname+" : " + value));
            values.stream().filter(value->isValidValue(slotname, value)).collect(Collectors.toList());
        }
        return values;
    }


    private static void update(int accepting, List<String> values, String slotname, List<String> responsenames, Dialogue d) {

        if(values.isEmpty()){
            d.pushFocus(responsenames.get(1));
        } else {

            if (values.size() > 1) {
                d.pushFocus(responsenames.get(0));
                d.putToWorkingMemory("slot_to_choose",slotname);
            } else {
                String newvalue = values.get(0);
                if (accepting > 0) {
                    //check matches working intent
                    if (d.peekTopIntent().getSlotValuesByType(slotname).stream().filter(value -> value.equals(newvalue)).count() > 0) {
                        //ok
                        d.peekTopIntent().replaceSlot(new Intent.Slot(slotname, newvalue, 0, 0)); //replace multiple options if present
                    } else {
                        d.peekTopIntent().fillSlot(new Intent.Slot(slotname, newvalue, 0, 0));
                        d.pushFocus(responsenames.get(0)); //multiple possibilities for capacity so choose
                        d.putToWorkingMemory("slot_to_choose",slotname);
                    }

                } else {
                    //replace info in working intent - if accepting not known, assuming rejection
                    d.peekTopIntent().replaceSlot(new Intent.Slot(slotname, newvalue, 0, 0));
                    if(d.peekTopFocus().equals(TaxiServiceHandler.confirmCompletionResponse)){
                        d.pushFocus(TaxiServiceHandler.confirmResponse);
                    }
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
        return true;
    }
}
