package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.PaypalCheckinHandler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Merchant;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;

import java.util.*;

/**
 * Created by juliewe on 21/04/2015.
 */
public class ConfirmMethod implements Handler.IntentHandler {



    public Response handle(Intent i, Dialogue d, Object r){
        switch(i.getName()){
            case PaypalCheckinHandler.yes:
                return accept(d);
            case PaypalCheckinHandler.no:
                return reject(d,r);
            case PaypalCheckinHandler.confirm:
                return handleConfirm(i,d,r);
            default:
                return new Response("unknown");
        }
    }

    private Response handleConfirm(Intent i, Dialogue d, Object r) {
        Collection<Intent.Slot> answers = i.getSlotByType(PaypalCheckinHandler.yes_no_slot);
        boolean accept = answers.stream().anyMatch(answer->answer.value.equals("yes"));
        if(accept){
            return accept(d);
        } else {
            return reject(d, r);
        }
    }

    private Response accept(Dialogue d){
        handleAccept(d);
        return processStack(d);
    }

    private Response reject(Dialogue d, Object resource){
        //rejecting the merchant currently in working memory.  Move to rejection list
        handleReject(d);

        ProductMongoDB db;
        try {
            db = (ProductMongoDB) resource;
        } catch (ClassCastException e){
            throw new Dialoguer.DialoguerException("Resource should be mongo db", e);
        }

        List<Merchant> possibleMerchants;
        if(d.getFromWorkingMemory("location_list")==null) {
            possibleMerchants = LocMethod.filterRejected(LocMethod.findNearbyMerchants(db, d.getUserData()), d.getFromWorkingMemory("rejectedlist"));
        } else {
            possibleMerchants = LocMethod.matchNearbyMerchants(db, d.getUserData(), d);//will use workingmemory's location_list
        }

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
                responseVariables.put(PaypalCheckinHandler.merchantSlot, d.getFromWorkingMemory("merchantName"));
                d.setRequestingYesNo(true);
                break;
            case "repeat_request_loc":
                newStates.add("confirm_loc");
                responseVariables.put(PaypalCheckinHandler.locationSlot, d.getFromWorkingMemory("location_list"));
                d.setRequestingYesNo(false);
                break;
            case "request_location":
                newStates.add("confirm_loc");
                d.setRequestingYesNo(false);
                break;

            //case "confirm_completion":




        }
        return new Response(focus,responseVariables,newStates);

    }
    public static void handleReject(Dialogue d){
        //rejecting the merchant currently in working memory.  Move to rejection list
        String rejectedlist=d.getFromWorkingMemory("rejectedlist");
        if(rejectedlist==null){rejectedlist="";}
        String rejected = d.getFromWorkingMemory("merchantId");
        if(rejected==null){rejected="";}
        rejectedlist+=rejected+" ";
        LocMethod.updateMerchant(null,d);
        d.putToWorkingMemory("rejectedlist",rejectedlist);
    }

    public static void handleAccept(Dialogue d){
        //perform side effects
        System.out.println("Contacting paypal for user "+d.getId()+" to checkin at "+d.getFromWorkingMemory("merchantId")+" ("+d.getFromWorkingMemory("merchantName")+")....");
        //return Response
        d.setComplete(true);
        d.pushFocus("confirm_completion");
    }
}

