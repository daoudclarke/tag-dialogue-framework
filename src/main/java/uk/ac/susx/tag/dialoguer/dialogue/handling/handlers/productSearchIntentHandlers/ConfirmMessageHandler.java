package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.productSearchIntentHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;

import java.util.List;

/**
 * Created by juliewe on 12/05/2015.
 */
public class ConfirmMessageHandler implements Handler.ProblemHandler {
    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        return false;
    }

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        return null;
    }
}
