package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.CheckInHandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.HandlerFactory;

import java.util.List;

/**
 * Created by juliewe on 20/04/2015.
 */
public class PaypalCheckinHandler extends Handler{


    public static final String checkinIntent = "check_in"; //check this against wit

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue) {

        /*NEED TO IMPLEMENT THIS!*/
        System.err.println(intents.get(0).getName());
        Response r = new Response("requestLocation");
        return r; // Return response ID here
    }

    @Override
    public HandlerFactory getFactory() {
        return new CheckInHandlerFactory();
    }

    @Override
    public void close() throws Exception {
        // Close any resources here (like database)
    }


}
