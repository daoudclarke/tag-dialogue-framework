package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.HandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.PaypalCheckinHandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers.ConfirmMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers.CheckinMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers.CheckinLocMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers.ConfirmLocMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers.LocMethod;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by juliewe on 20/04/2015.
 */
public class PaypalCheckinHandler extends Handler{


    private String dbHost;
    private String dbPort;
    private String dbName;
    protected transient ProductMongoDB db;

    public static final String checkinIntent = "check_in"; //check this against wit
    public static final String otherIntent = "other";
    public static final String confirmLoc = "confirm_loc";
    public static final String checkinLoc = "check_in_loc";
    public static final String loc = "loc";
    public static final String confirm = "confirm";
    public static final String quit = "quit";
    public static final String yes = "yes";
    public static final String no = "no";
    public static final String locationSlot="local_search_query";
    public static final String userSlot="user";


    public PaypalCheckinHandler(){
        super.registerIntentHandler(quit, (i, d, r) -> new Response("confirm_cancellation"));
        super.registerIntentHandler(confirm, new ConfirmMethod());
        super.registerIntentHandler(yes,new ConfirmMethod());
        super.registerIntentHandler(checkinIntent,new CheckinMethod());
        super.registerIntentHandler(loc,new LocMethod());
        super.registerIntentHandler(checkinLoc,new CheckinLocMethod());
        super.registerIntentHandler(confirmLoc,new ConfirmLocMethod());
        super.registerIntentHandler(otherIntent, (i,d, r) -> new Response("unknown"));

    }

    public void setupDatabase(){
        try {
            if(!dbHost.equals("")) {
                db = new ProductMongoDB(dbHost, Integer.parseInt(dbPort), dbName);
            } else {
                db= new ProductMongoDB();

            }
        } catch (UnknownHostException e) {
            System.err.println("Cannot connect to database host");
        }
    }

    @Override
    public Dialogue getNewDialogue(String dialogueId){
        Dialogue d = new Dialogue(dialogueId);
        d.setState("initial");

        return d;
    }


    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue) {
        System.err.println(intents.get(0).getName());
        applyIntentHandler(intents.get(0), dialogue, this.db); //probably not safe just to consider first intent.  Probably should apply all intent handlers or search intents first to find best one
        return processStack(dialogue);
    }

    @Override
    public HandlerFactory getFactory() {
        return new PaypalCheckinHandlerFactory();
    }

    @Override
    public void close() throws Exception {
        // Close any resources here (like database)
        db.close();
    }

    private Response processStack(Dialogue d){
        String focus="hello";
        if (!d.isEmptyFocusStack()) {
            focus = d.popTopFocus();
        }
        List<String> newStates = new ArrayList<>();
        Map<String, String> responseVariables = new HashMap<>();
        switch(focus){
            case "confirm_loc":
                newStates.add(focus);
                responseVariables.put(locationSlot, d.getFromWorkingMemory("merchantName"));
                break;
            case "repeat_request_loc":
                newStates.add("confirm_loc");
                responseVariables.put(locationSlot, d.getFromWorkingMemory("location_list"));
                break;
            case "request_location":
                newStates.add("confirm_loc");
                break;
            case "hello":
                newStates.add("initial");
                responseVariables.put(userSlot,d.getId());
                break;
            //case "confirm_completion":




        }
        return new Response(focus,responseVariables,newStates);

    }

}
