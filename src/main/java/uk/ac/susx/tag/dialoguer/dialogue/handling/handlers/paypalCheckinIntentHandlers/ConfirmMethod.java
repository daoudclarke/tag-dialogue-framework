package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Merchant;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;

import java.util.*;

/**
 * Created by juliewe on 21/04/2015.
 */
public class ConfirmMethod implements Handler.IntentHandler {

    public static final String yes_no_slot = "yes_no";
    public static final String locationSlot="local_search_query";
    public static final String userSlot="user";

    public Response handle(Intent i, Dialogue d, Object r) {
        Collection<Intent.Slot> answers = i.getSlotByType(yes_no_slot);
        boolean accept=false;
        for(Intent.Slot answer:answers){

            if(answer.value.equals("yes")){
                accept=true;
            }
        }
        if(accept){
            return accept(d);
        } else {
            return reject(d, r);
        }
    }

    public static Response accept(Dialogue d){
        //perform side effects
        System.out.println("Contacting paypal for user "+d.getId()+" to checkin at "+d.getFromWorkingMemory("merchantId")+" ("+d.getFromWorkingMemory("merchantName")+")....");
        //return Response
        d.setComplete(true);
        d.pushFocus("confirm_completion");
        return processStack(d);
    }

    public static Response reject(Dialogue d, Object resource){
        //rejecting the merchant currently in working memory.  Move to rejection list
        String rejectedlist=d.getFromWorkingMemory("rejectedlist");
        if(rejectedlist==null){rejectedlist="";}
        String rejected = d.getFromWorkingMemory("merchantId");
        if(rejected==null){rejected="";}
        rejectedlist+=rejected+" ";
        LocMethod.updateMerchant(null,d);
        d.putToWorkingMemory("rejectedlist",rejectedlist);
        ProductMongoDB db=null;
        if (resource instanceof ProductMongoDB){
            db=(ProductMongoDB) resource;
        }

        List<Merchant> possibleMerchants;
        if(d.getFromWorkingMemory("location_list")==null) {
            possibleMerchants = LocMethod.filterRejected(LocMethod.findNearbyMerchants(db, d.getUserData()), d.getFromWorkingMemory("rejectedlist"));
        } else {
            possibleMerchants = LocMethod.matchNearbyMerchants(d.getFromWorkingMemory("location_list"),db, d.getUserData(), d);
        }

        LocMethod.processMerchantList(possibleMerchants, d);
        return processStack(d);
    }

    private static Response processStack(Dialogue d){
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

