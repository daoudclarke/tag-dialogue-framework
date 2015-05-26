package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.taxiServiceHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.TaxiServiceHandler;

import java.util.List;

/**
 * Created by juliewe on 26/05/2015.
 */
public class OutOfDomainHandler implements Handler.ProblemHandler {
    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        return intents.stream().anyMatch(i->TaxiServiceHandler.outOfDomainIntents.contains(i.getName()))&&dialogue.getStates().contains("initial");
    }

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        return null;
    }

    @Override
    public boolean subhandle(List<Intent> intents, Dialogue dialogue, Object resource) {
        System.err.println("Out of Domain handler fired.");
        dialogue.pushFocus(TaxiServiceHandler.unknownResponse);
        return true;
    }
}
