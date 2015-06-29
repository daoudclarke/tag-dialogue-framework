package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.interactiveIntentHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Message;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.InteractiveHandler;
import uk.ac.susx.tag.dialoguer.knowledge.location.NominatimAPIWrapper;

import java.util.List;

/**
 * Created by Daniel Saska on 6/28/2015.
 */
public class GpsProblemHandler implements Handler.ProblemHandler {
    NominatimAPIWrapper nom = new NominatimAPIWrapper();

    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        return dialogue.isEmptyFocusStack()||dialogue.peekTopFocus().equals(InteractiveHandler.aWaitGps);
    }

    @Override
    public void handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        List<Message> history = dialogue.getMessageHistory();
        double lat = history.get(history.size() - 1).getUserData().getLatitude();
        double lon = history.get(history.size() - 1).getUserData().getLongitude();

        //TODO: Handle no geo-tag (how to determine whether tweeth as one?)
        NominatimAPIWrapper.NomResult loc = nom.queryReverseAPI(lat, lon);
        dialogue.putToWorkingMemory("location_processed", loc.display_name);

        dialogue.pushFocus(InteractiveHandler.aGpsLocConfirm);
        dialogue.pushFocus(InteractiveHandler.qGpsLocConfirm);

    }
}

