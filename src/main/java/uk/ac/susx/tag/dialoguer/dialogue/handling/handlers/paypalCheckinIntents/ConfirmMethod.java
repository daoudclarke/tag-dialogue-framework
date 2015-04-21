package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntents;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;

/**
 * Created by juliewe on 21/04/2015.
 */
public class ConfirmMethod implements IntentMethod {
    public static Response execute(Dialogue d){return new Response("confirm_completion");}
}
