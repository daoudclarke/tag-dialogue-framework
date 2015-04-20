package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.CheckInHandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.HandlerFactory;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 20/04/2015
 * Time: 11:28
 */
public class CheckInHandler extends Handler {

    public static final String checkinIntent = "check_in";

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue) {
        return null; // Return response ID here
    }

    @Override
    public HandlerFactory getFactory() {
        return new CheckInHandlerFactory();
    }

    @Override
    public void close() throws Exception {
        // Close any resources here (like database)
    }
}
