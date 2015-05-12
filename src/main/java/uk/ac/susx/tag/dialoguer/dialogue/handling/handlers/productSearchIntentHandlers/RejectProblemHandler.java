package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.productSearchIntentHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.ProductSearchHandler;

import java.util.List;

/**
 * Created by juliewe on 12/05/2015.
 */
public class RejectProblemHandler implements Handler.ProblemHandler{
    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        return intents.stream().anyMatch(i->(i.getName().equals(ProductSearchHandler.confirm)&&i.getSlotValuesByType(ProductSearchHandler.yes_no_slot).contains("no"))||i.getName().equals(Intent.no));
    }

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        //will need to check for other information in the other intents e.g., a "no Julie"

        return null;
    }
}
