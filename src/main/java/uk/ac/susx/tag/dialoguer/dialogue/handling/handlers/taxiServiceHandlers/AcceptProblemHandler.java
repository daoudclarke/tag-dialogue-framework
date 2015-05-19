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
    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        //need to have a orderTaxi working intent, a yes_intent and an empty focus stack?
        //probably should check that there is no other information and that the orderTaxiIntent is valid (as may have been merged)
        boolean intentmatch = (intents.stream().anyMatch(i-> i.isName(TaxiServiceHandler.orderTaxiIntent))&&intents.stream().anyMatch(i->i.isName(Intent.yes)));
        boolean statematch=dialogue.isEmptyFocusStack()||dialogue.peekTopFocus().equals(TaxiServiceHandler.confirmCompletionResponse);
        return intentmatch&&statematch&&validAcceptIntents(intents);
    }

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        System.err.println("Accept Problem Handler fired");
        Intent intent = intents.stream().filter(i->i.isName(TaxiServiceHandler.orderTaxiIntent)).findFirst().orElse(null);
        dialogue.pushFocus(TaxiServiceHandler.confirmCompletionResponse);
        OrderTaxiMethod.handleCapacity(intent, dialogue);  //check individual components of order still valid - may not be if the person has said "Yes I want a ...."
        OrderTaxiMethod.handleTime(intent, dialogue);
        OrderTaxiMethod.handleDestination(intent, dialogue);
        OrderTaxiMethod.handlePickup(intent, dialogue);
        dialogue.addToWorkingIntents(intent);
        return TaxiServiceHandler.processStack(dialogue);
    }

    private boolean validAcceptIntents(List<Intent> intents){
        //check the intents more thoroughly if required
        return true;
    }

    public static void complete(Dialogue d) {
        System.out.println("Ordering taxi for " + d.getId());
        display(d.peekTopIntent());
        d.setComplete(true);

    }
    private static void display(Intent i){
        i.getSlotCollection().stream().forEach(slot -> System.out.println(slot.name+" : "+slot.value));
    }
}
