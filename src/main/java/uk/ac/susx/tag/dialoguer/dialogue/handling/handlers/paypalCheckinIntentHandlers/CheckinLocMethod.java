package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;

/**
 * Created by juliewe on 21/04/2015.
 */
public class CheckinLocMethod implements Handler.IntentHandler{
    public Response handle(Intent i,Dialogue d, Object r){
        //handle both checkin and location intents
        Response checkin = new CheckinMethod().handle(i,d,r);
        Response location = new LocMethod().handle(i,d,r);
        return location;
    }
}
