package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;
import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.PaypalCheckinHandler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Merchant;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;

import java.util.*;

/**
 * Created by juliewe on 21/04/2015.
 */
public class CheckinMethod implements Handler.IntentHandler {

    public Response handle(Intent i,Dialogue d, Object resource){
        //generate response to request Location
        ProductMongoDB db;
        try {
            db = (ProductMongoDB) resource;
        } catch (ClassCastException e){
            throw new Dialoguer.DialoguerException("Resource should be mongo db", e);
        }

        d.putToWorkingMemory("rejectionlist",null); // in case restarting
        List<Merchant> possibleMerchants = LocMethod.findNearbyMerchants(db, d.getUserData());

        LocMethod.processMerchantList(possibleMerchants, d);
        return processStack(d);



    }

    private Response processStack(Dialogue d){
        String focus="unknown_hello";
        if (!d.isEmptyFocusStack()) {
            focus = d.popTopFocus();
        }
        Map<String, String> responseVariables = new HashMap<>();
        switch(focus){
            case "confirm_loc":
                responseVariables.put(PaypalCheckinHandler.merchantSlot, d.getFromWorkingMemory("merchantName"));
                break;
            case "repeat_request_loc":
                responseVariables.put(PaypalCheckinHandler.locationSlot, d.getFromWorkingMemory("location_list"));
                break;
            case "repeat_request_loc_rejects":
                responseVariables.put(PaypalCheckinHandler.locationSlot, d.getFromWorkingMemory("location_list"));
                d.setRequestingYesNo(false);
                break;
            //case "request_location":
               // break;


        }
        return new Response(focus,responseVariables);

    }




}
