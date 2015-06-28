package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.interactiveIntentHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.InteractiveHandler;

import java.util.List;

/**
 * Created by Daniel Saska on 6/26/2015.
 */
public class UnknownProblemHandler implements Handler.ProblemHandler {

    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {

        boolean intentmatch = (intents.stream().anyMatch(i-> i.isName(InteractiveHandler.unknownIntent))&&intents.stream().anyMatch(i->i.isName(Intent.yes)));

        return  intentmatch;
    }

    @Override
    public void handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        System.err.println("Unknwon meaning");
        dialogue.pushFocus(InteractiveHandler.unknownResponse);
    }
}
