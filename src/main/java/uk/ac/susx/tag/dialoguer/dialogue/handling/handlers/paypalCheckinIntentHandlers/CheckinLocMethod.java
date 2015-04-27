package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;

/**
 * Created by juliewe on 21/04/2015.
 */
public class CheckinLocMethod implements Handler.IntentHandler{
    //should not be needed with loc problem handler
    public Response handle(Intent i,Dialogue d, Object r){
        System.err.println("Using intent handler: checkinLocMethod");
        LocMethod.handleLocation(i,d,r);
        return new Response("unknown");

    }
}
