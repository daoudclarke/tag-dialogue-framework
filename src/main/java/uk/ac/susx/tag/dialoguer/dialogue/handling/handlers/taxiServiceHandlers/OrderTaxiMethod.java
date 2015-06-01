package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.taxiServiceHandlers;

import com.google.common.collect.Lists;
import uk.ac.susx.tag.dialoguer.Dialoguer;
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
public class OrderTaxiMethod implements Handler.ProblemHandler{


    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        return intents.stream().filter(i->i.getName().equals(TaxiServiceHandler.orderTaxiIntent)).count()>0;
    }

    /***
     *
     * @param intents
     * @param dialogue
     * @param resource
     * @return
     * Not used by this dialoguer instance
     */
    @Override
    public void handle(List<Intent> intents, Dialogue dialogue, Object resource) {

        System.err.println("orderTaxi intent handler fired");
        Intent intent = intents.stream().filter(i->i.getName().equals(TaxiServiceHandler.orderTaxiIntent)).findFirst().orElse(null);
        if(intent==null){
            throw new Dialoguer.DialoguerException("Not in handleable state");
        }
        dialogue.pushFocus(TaxiServiceHandler.confirmCompletionResponse);
        dialogue.pushFocus(TaxiServiceHandler.confirmResponse);
        TaxiServiceHandler.allSlots.stream().forEach(s->handleEntity(intent,dialogue,s));
        dialogue.addToWorkingIntents(intent);
    }


    /***
     *
     * @param intent
     * @param dialogue
     * @param resource
     * @return true
     *
     * Update a dialogue based on a given intent (which is the OrderTaxi intent).
     * Push the required focuses on to the stack in the reverse order.  Check the entities/slots.  Add the current intent to the working intents.  Return true because it has fired
     */

    /***
     *
     * @param i
     * @param d
     * @param slotname
     * Validate slots and generateResponse
     */

    static void handleEntity(Intent i, Dialogue d, String slotname){
        //check for multiple and empty slots
        List<String> values = TaxiServiceHandler.validate(i,slotname);
        //System.err.println(values);
        generateResponse(values, slotname, d);
    }


    /**
     *
     * @param values
     * @param slotname
     * @param d
     * If a particular slot is empty, this needs to be respecified
     * If it contains more than 1 value, the user needs to choose.
     * 1 value: good! The confirmResponse addled by subhandle will be the top focus
     */
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
