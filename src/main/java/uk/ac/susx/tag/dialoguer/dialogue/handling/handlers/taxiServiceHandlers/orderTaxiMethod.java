package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.taxiServiceHandlers;

import com.google.common.collect.Lists;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.TaxiServiceHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by juliewe on 19/05/2015.
 */
public class OrderTaxiMethod implements Handler.IntentHandler{
    @Override
    public Response handle(Intent intent, Dialogue dialogue, Object resource) {
        System.err.println("orderTaxi intent handler fired");
        dialogue.pushFocus(TaxiServiceHandler.confirmCompletionResponse);
        dialogue.pushFocus(TaxiServiceHandler.confirmResponse);
        handleCapacity(intent,dialogue);
        handleTime(intent,dialogue);
        handleDestination(intent,dialogue);
        handlePickup(intent,dialogue);
        dialogue.addToWorkingIntents(intent);
        return TaxiServiceHandler.processStack(dialogue);
    }

    static void handleCapacity(Intent i, Dialogue d){
        //check for multiple and empty slots
        List<String> values = FollowupProblemHandler.validate(i,TaxiServiceHandler.capacitySlot);
        //System.err.println(values);
        generateResponse(values,TaxiServiceHandler.capacitySlot, Lists.newArrayList(TaxiServiceHandler.chooseCapacityResponse),d);
    }

    static void handleTime(Intent i, Dialogue d){
        List<String> values = FollowupProblemHandler.validate(i,TaxiServiceHandler.timeSlot);
        generateResponse(values,TaxiServiceHandler.timeSlot,Lists.newArrayList(TaxiServiceHandler.chooseTimeResponse),d);
    }

    static void handleDestination(Intent i, Dialogue d){
        List<String> values = FollowupProblemHandler.validate(i,TaxiServiceHandler.destinationSlot);
        generateResponse(values,TaxiServiceHandler.destinationSlot,Lists.newArrayList(TaxiServiceHandler.chooseDestinationResponse,TaxiServiceHandler.respecifyDestinationResponse),d);
    }
    static void handlePickup(Intent i, Dialogue d){
        List<String> values = FollowupProblemHandler.validate(i,TaxiServiceHandler.pickupSlot);
        generateResponse(values,TaxiServiceHandler.pickupSlot,Lists.newArrayList(TaxiServiceHandler.choosePickupResponse,TaxiServiceHandler.respecifyPickupResponse),d);
    }

    static void generateResponse(List<String> values,String slotname, List<String> responsenames, Dialogue d){
        if(values.isEmpty()){
            d.pushFocus(responsenames.get(1));
        }
        if(values.size()>1){
            d.pushFocus(responsenames.get(0));
            d.putToWorkingMemory("slot_to_choose",slotname);
        }

    }

}
