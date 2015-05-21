package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.productSearchIntentHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.ProductSearchHandler;

/**
 * Created by juliewe on 21/05/2015.
 */
public class noChoiceMethod implements Handler.IntentHandler {
    @Override
    public Response handle(Intent intent, Dialogue dialogue, Object resource) {
        dialogue.pushFocus("repeat_choice");
        return ProductSearchHandler.processStack(dialogue, ProductSearchHandler.castDB(resource));
    }

    @Override
    public boolean subhandle(Intent intent, Dialogue dialogue, Object resource) {
        return false;
    }
}
