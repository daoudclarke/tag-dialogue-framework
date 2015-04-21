package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.HandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.PaypalCheckinHandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers.IntentMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers.CheckinMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers.CheckinLocMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers.ConfirmMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers.ConfirmLocMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers.LocMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers.QuitMethod;


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

    public PaypalCheckinHandler(){
        super.registerIntentHandler(quit, (i, d) -> {
            return new Response("confirm_cancellation");
                  });
        super.registerIntentHandler(confirm, (i,d) -> {
            return new Response("confirm_completion");

        });
        super.registerIntentHandler(checkinIntent,new CheckinMethod());
        super.registerIntentHandler(loc,new LocMethod());
        super.registerIntentHandler(checkinLoc,new CheckinLocMethod());
        super.registerIntentHandler(confirmLoc,new ConfirmLocMethod());
        super.registerIntentHandler(otherIntent, (i,d) -> {
            return new Response("unknown");
        });
    }


    @Override
    public Dialogue getNewDialogue(String dialogueId){
        Dialogue d = new Dialogue(dialogueId);
        d.setState("initial");
        return d;
    }


    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue) {

        return applyIntentHandler(intents.get(0),dialogue);


        /*NEED TO IMPLEMENT THIS!*/

        //System.err.println("Using PaypalCheckinHandler");
//        Intent myintent = intents.get(0);
//        String intent = myintent.getName();
//        Response r;
//        switch(intent) {
//            case checkinIntent:
//                r=CheckinMethod.execute(myintent, dialogue);
//                break;
//            case loc:
//                r = LocMethod.execute(myintent,dialogue);
//                break;
//            case confirmLoc:
//                r = ConfirmLocMethod.execute(myintent,dialogue);
//                break;
//            case confirm:
//                r = ConfirmMethod.execute(myintent,dialogue);
//                break;
//            case quit:
//                r= QuitMethod.execute(myintent,dialogue);
//                break;
//            case checkinLoc:
//                r=CheckinLocMethod.execute(myintent,dialogue);
//                break;
//            default:
//                r=IntentMethod.execute(myintent,dialogue);
//        }

        //System.err.println(r.getResponseName());
        //return r; // Return response ID here
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
