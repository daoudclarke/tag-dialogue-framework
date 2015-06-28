package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.interactiveIntentHandlers;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.InteractiveHandler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.TaxiServiceHandler;

import java.util.List;

/**
 * Created by Daniel Saska on 6/27/2015.
 */
public class HelpProblemHelper implements Handler.ProblemHandler {
    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        boolean intentmatch = intents.stream().filter(i->i.getName().equals(InteractiveHandler.helpIntent)).count()>0;
        boolean statematch = dialogue.isEmptyFocusStack()||dialogue.peekTopFocus().equals(InteractiveHandler.initial);
        return intentmatch && statematch;
    }

    @Override
    public void handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        System.err.println("help intent handler fired");
        dialogue.pushFocus(InteractiveHandler.aLocation);
        dialogue.pushFocus(InteractiveHandler.qLocation);
    }
}
