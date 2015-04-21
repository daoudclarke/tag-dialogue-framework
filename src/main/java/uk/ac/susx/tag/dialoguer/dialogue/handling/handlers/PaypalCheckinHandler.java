package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.HandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.PaypalCheckinHandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntents.IntentMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntents.CheckinMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntents.CheckinLocMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntents.ConfirmMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntents.ConfirmLocMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntents.LocMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntents.QuitMethod;


import java.util.List;

/**
 * Created by juliewe on 20/04/2015.
 */
public class PaypalCheckinHandler extends Handler{


    public static final String checkinIntent = "check_in"; //check this against wit
    public static final String otherIntent = "other";
    public static final String confirmLoc = "confirm_loc";
    public static final String checkinLoc = "checkin_loc";
    public static final String loc = "loc";
    public static final String confirm = "confirm";
    public static final String quit = "quit";

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue) {

        /*NEED TO IMPLEMENT THIS!*/

        System.err.println("Using PaypalCheckinHandler");
        String intent = intents.get(0).getName();
        Response r;
        switch(intent) {
            case checkinIntent:
                r=CheckinMethod.execute(dialogue);
                break;
            case loc:
                r = LocMethod.execute(dialogue);
                break;
            case confirmLoc:
                r = ConfirmLocMethod.execute(dialogue);
                break;
            case confirm:
                r = ConfirmMethod.execute(dialogue);
                break;
            case quit:
                r= QuitMethod.execute(dialogue);
                break;
            case checkinLoc:
                r=CheckinLocMethod.execute(dialogue);
                break;
            default:
                r=IntentMethod.execute(dialogue);
        }

        System.err.println(r.getResponseName());
        return r; // Return response ID here
    }

    @Override
    public HandlerFactory getFactory() {
        return new PaypalCheckinHandlerFactory();
    }

    @Override
    public void close() throws Exception {
        // Close any resources here (like database)
    }



}
