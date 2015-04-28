package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;

/**
 * Created by juliewe on 21/04/2015.
 */
public class ConfirmLocMethod implements Handler.IntentHandler {
    //should not be used
    public Response handle(Intent i, Dialogue d, Object r){return new Response("unknown");}
}
