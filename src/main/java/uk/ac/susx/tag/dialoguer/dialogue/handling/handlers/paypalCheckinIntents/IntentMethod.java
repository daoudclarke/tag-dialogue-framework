package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntents;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

/**
 * Created by juliewe on 21/04/2015.
 */

public interface IntentMethod {
    public static Response execute(Intent i, Dialogue d){
        //leave state the same - return unknown intent response
        return new Response("unknown");}
}
