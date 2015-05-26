package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.taxiServiceHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.TaxiServiceHandler;

import java.util.List;

/**
 * Created by juliewe on 19/05/2015.
 */
public class AcceptProblemHandler implements Handler.ProblemHandler {


    /**
     *
     * @param intents
     * @param dialogue
     * @return
     * Is it possible that the order is completed.  Check whether there are intents matching orderTaxiIntent and yes
     * Check whether the topFocus is confirmCompletionReponse or empty stack
     * Check whether the intents are valid for completion (optional)
     */
    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        //need to have a orderTaxi working intent, a yes_intent and an empty focus stack?
        //probably should check that there is no other information and that the orderTaxiIntent is valid (as may have been merged)
        boolean intentmatch = (intents.stream().anyMatch(i-> i.isName(TaxiServiceHandler.orderTaxiIntent))&&intents.stream().anyMatch(i->i.isName(Intent.yes)));
        boolean statematch=dialogue.isEmptyFocusStack()||dialogue.peekTopFocus().equals(TaxiServiceHandler.confirmCompletionResponse);
        return intentmatch&&statematch&&validAcceptIntents(intents);
    }

    @Override
    public void handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        System.err.println("Accept Problem Handler fired");
        Intent intent = intents.stream().filter(i->i.isName(TaxiServiceHandler.orderTaxiIntent)).findFirst().orElse(null);
        TaxiServiceHandler.allSlots.stream().forEach(s->OrderTaxiMethod.handleEntity(intent,dialogue,s));//check individual components of order still valid - may not be if the person has said "Yes I want a ...."
        dialogue.addToWorkingIntents(intent);

    }

    /**
     *
     * @param intents
     * @param dialogue
     * @param resource
     * @return
     * Get the orderTaxiIntent and revalidate all of the slots (in case the user has added extra information at the completion point)
     * Add it to the working intents and return true
     */
    @Deprecated
    @Override
    public boolean subhandle(List<Intent> intents, Dialogue dialogue, Object resource) {
        System.err.println("Accept Problem Handler fired");
        Intent intent = intents.stream().filter(i->i.isName(TaxiServiceHandler.orderTaxiIntent)).findFirst().orElse(null);
        TaxiServiceHandler.allSlots.stream().forEach(s->OrderTaxiMethod.handleEntity(intent,dialogue,s));//check individual components of order still valid - may not be if the person has said "Yes I want a ...."
        dialogue.addToWorkingIntents(intent);
        return true;
        //return null;
    }

    private boolean validAcceptIntents(List<Intent> intents){
        //check the intents more thoroughly if required
        return true;
    }

    /**
     *
     * @param d
     * Side effect of completion.  Will be called when the stack is processed and confirmCompletionResponse is found
     */
    public static void complete(Dialogue d) {
        System.out.println("Ordering taxi for " + d.getId());
        display(d.peekTopIntent());
        d.setComplete(true);

    }
    private static void display(Intent i){
        i.getSlotCollection().stream().forEach(slot -> System.out.println(slot.name+" : "+slot.value));
    }
}
