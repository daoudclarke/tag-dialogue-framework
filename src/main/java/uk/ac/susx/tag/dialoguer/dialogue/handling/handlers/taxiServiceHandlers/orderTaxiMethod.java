package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.taxiServiceHandlers;

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
public class orderTaxiMethod implements Handler.IntentHandler{
    @Override
    public Response handle(Intent intent, Dialogue dialogue, Object resource) {
        System.err.println("orderTaxi intent handler fired");
        dialogue.pushFocus(TaxiServiceHandler.confirmResponse);
        handleCapacity(intent,dialogue);
        handleTime(intent,dialogue);
        handleDestination(intent,dialogue);
        handlePickup(intent,dialogue);
        dialogue.addToWorkingIntents(intent);
        return TaxiServiceHandler.processStack(dialogue);
    }

    private void handleCapacity(Intent i, Dialogue d){
        //check for multiple and empty slots
        List<String> values = i.getSlotValuesByType(TaxiServiceHandler.capacitySlot);
        if (values.isEmpty()){
            //insert default
            i.fillSlot(TaxiServiceHandler.capacitySlot,"4");
        } else {
            //check values are valid - currently assume all ok
            values.stream().forEach(value -> System.err.println("Capacity: " + value));
        }
        if(values.size()>1){
            d.pushFocus(TaxiServiceHandler.chooseCapacityResponse);
        }

    }
    private void handleTime(Intent i, Dialogue d){
        List<String> values = i.getSlotValuesByType(TaxiServiceHandler.timeSlot);
        if(values.isEmpty()){
            //insert default
            i.fillSlot(TaxiServiceHandler.timeSlot,"ASAP");
        } else {
            //check values are valid - currently assume all ok
            values.stream().forEach(value->System.err.println("Time: "+value));
        }
        if(values.size()>1){
            d.pushFocus(TaxiServiceHandler.chooseTimeResponse);
        }

    }
    private void handleDestination(Intent i, Dialogue d){
        List<String> values = i.getSlotValuesByType(TaxiServiceHandler.destinationSlot);
        //check values are valid-currently assume all ok but will do filter
        values.stream().forEach(value->System.err.println("Destination: "+value));
        values.stream().filter(value->validLocation(value)).collect(Collectors.toList());

        if(values.isEmpty()){
            d.pushFocus(TaxiServiceHandler.respecifyDestinationResponse);
        }
        if(values.size()>1){
            d.pushFocus(TaxiServiceHandler.chooseDestinationResponse);
        }

    }
    private void handlePickup(Intent i, Dialogue d){
        List<String> values = i.getSlotValuesByType(TaxiServiceHandler.pickupSlot);
        //check values are valid-currently assume all ok but will do filter
        values.stream().forEach(value->System.err.println("Pickup: "+value));
        values.stream().filter(value->validLocation(value)).collect(Collectors.toList());

        if(values.isEmpty()){
            d.pushFocus(TaxiServiceHandler.respecifyPickupResponse);
        }
        if(values.size()>1){
            d.pushFocus(TaxiServiceHandler.choosePickupResponse);
        }
    }

    private boolean validLocation(String value){
        return true;
    }
}
