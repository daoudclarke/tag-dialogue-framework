package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.productSearchIntentHandlers;

import com.google.common.collect.Lists;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;

import java.util.List;

/**
 * Created by juliewe on 12/05/2015.
 */
public class ConfirmProductHandler implements Handler.ProblemHandler{

    public static final String requiredState="confirm_product";
    public static final List<String> mystates= Lists.newArrayList(requiredState, "confirm_yes_no");
    
    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        //this should only fire if user is definitiely accepting or rejecting the selected product

        boolean statematch=(dialogue.getStates().stream().anyMatch(s->s.equals(requiredState))&&dialogue.getStates().stream().allMatch(s->mystates.contains(s)));
        boolean intentmatch=false; //TODO
        return statematch||intentmatch;
    }

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        return null;
    }
}
