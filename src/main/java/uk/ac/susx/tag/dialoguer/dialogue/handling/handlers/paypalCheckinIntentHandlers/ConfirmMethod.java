package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;

/**
 * Created by juliewe on 21/04/2015.
 */
public class ConfirmMethod implements Handler.IntentHandler {

    public Response handle(Intent i, Dialogue d, Object r) {
        //perform side effects
        System.out.println("Contacting paypal for user "+d.getId()+" to checkin at "+d.getFromWorkingMemory("merchantId")+"....");
        //return Response
        return new Response("confirm_completion");
    }
}

