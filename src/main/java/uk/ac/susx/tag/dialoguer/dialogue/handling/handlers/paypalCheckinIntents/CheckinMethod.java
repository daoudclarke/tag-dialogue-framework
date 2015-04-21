package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntents;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
/**
 * Created by juliewe on 21/04/2015.
 */
public class CheckinMethod implements IntentMethod {
    public static Response execute(Dialogue d){
        //generate response to request Location
        Response r = new Response("requestLocation");
        System.err.println(r.getResponseName());
        //update Dialogue state to know that confirm_loc is expected next
        d.setState("confirm_loc");
        return r; // Return response ID here

    }
}
