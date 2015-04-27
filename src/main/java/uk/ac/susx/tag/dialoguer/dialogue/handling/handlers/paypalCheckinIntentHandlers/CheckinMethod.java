package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Merchant;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;

import java.util.*;

/**
 * Created by juliewe on 21/04/2015.
 */
public class CheckinMethod implements Handler.IntentHandler {

    public static final String locationSlot="local_search_query";
    public static final String userSlot="user";

    public Response handle(Intent i,Dialogue d, Object resource){
        //generate response to request Location
        ProductMongoDB db=null;
        if (resource instanceof ProductMongoDB){
            db=(ProductMongoDB) resource;
        }


        d.putToWorkingMemory("rejectionlist",null); // in case restarting
        List<Merchant> possibleMerchants = LocMethod.findNearbyMerchants(db, d.getUserData());

        LocMethod.processMerchantList(possibleMerchants, d);
        return processStack(d);



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
