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
public class ConfirmProblemHandler implements Handler.ProblemHandler{
    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        boolean intentMatch=intents.stream().anyMatch(i->(i.getName().equals(ProductSearchHandler.confirm)&&i.getSlotValuesByType(ProductSearchHandler.yes_no_slot).contains("yes"))||i.getName().equals(Intent.yes));
        boolean stateMatch=false;
        try {
            stateMatch = isComplete(dialogue.peekTopIntent());
        } catch(ArrayIndexOutOfBoundsException e){
            stateMatch=false;
        }
        return intentMatch&&stateMatch;
    }

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        //this is to handle a positive confirmation only
        handleAccept(dialogue);
        return ProductSearchHandler.processStack(dialogue,ProductSearchHandler.castDB(resource));

    }

    private boolean isComplete(Intent i){
        if (i.getName().equals(ProductSearchHandler.buy)){
            return true;
        } else {
            return false;
        }
    }
    public static void handleAccept(Dialogue d){
        //perform side effects
        System.out.println("Making purchase for "+d.getId());

        System.out.println( d.peekTopIntent().toString());
        //may want to check for other working intents
        //return Response
        d.setComplete(true);
        d.pushFocus("confirm_completion");
    }
}
