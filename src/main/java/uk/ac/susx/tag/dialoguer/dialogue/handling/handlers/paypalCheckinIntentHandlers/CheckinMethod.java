package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by juliewe on 21/04/2015.
 */
public class CheckinMethod implements Handler.IntentHandler {
    public Response handle(Intent i,Dialogue d){
        //generate response to request Location
        List<String> newStates = new ArrayList<>();
        newStates.add("confirm_loc");

        Response r = new Response("request_location",newStates);
        System.err.println(r.getResponseName());
        //update Dialogue state to know that confirm_loc is expected next
        //d.setState("confirm_loc");
        return r; // Return response ID here

    }
}
