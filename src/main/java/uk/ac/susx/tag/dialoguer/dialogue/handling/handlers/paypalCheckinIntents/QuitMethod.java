package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntents;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

/**
 * Created by juliewe on 21/04/2015.
 */
public class QuitMethod implements IntentMethod {
    public static Response execute(Intent i, Dialogue d){return new Response("confirm_cancellation");}
}
