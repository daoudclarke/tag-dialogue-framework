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
        return null;
    }

    @Override
    public boolean subhandle(Intent intent, Dialogue dialogue, Object resource) {
        System.err.println("orderTaxi intent handler fired");
        dialogue.pushFocus(TaxiServiceHandler.confirmCompletionResponse);
        dialogue.pushFocus(TaxiServiceHandler.confirmResponse);
        TaxiServiceHandler.allSlots.stream().forEach(s->handleEntity(intent,dialogue,s));
        dialogue.addToWorkingIntents(intent);
        return true;
    }

    static void handleEntity(Intent i, Dialogue d, String slotname){
        //check for multiple and empty slots
        List<String> values = FollowupProblemHandler.validate(i,slotname);
        //System.err.println(values);
        generateResponse(values, slotname, d);
    }

    static void generateResponse(List<String> values,String slotname, Dialogue d){
        if(values.isEmpty()){
            d.pushFocus(TaxiServiceHandler.respecifyResponse);
            d.putToWorkingMemory("slot_to_choose",slotname);
        }
        if(values.size()>1){
            d.pushFocus(TaxiServiceHandler.chooseResponse);
            d.putToWorkingMemory("slot_to_choose",slotname);
        }

    }

}
