package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;

import java.awt.*;

/**
 * Created by juliewe on 30/04/2015.
 */
public class UnknownMethod implements Handler.IntentHandler {

    public Response handle(Intent i, Dialogue d, Object res){
        Response r;
        if(d.getStates().contains("initial")) {
            r = new Response("unknown_hello");
        } else {
            r= new Response("unknown_request_location");
        }
        return r;
    }

}
