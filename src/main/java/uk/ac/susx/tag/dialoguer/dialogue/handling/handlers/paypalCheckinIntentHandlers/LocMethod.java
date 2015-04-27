package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.User;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.PaypalCheckinHandler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Merchant;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;

import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by juliewe on 21/04/2015.
 */
public class LocMethod implements Handler.ProblemHandler {

    public static final String locationSlot="local_search_query";
    public static final String userSlot="user";
    public static final String yes_no_slot = "yes_no";
    public static final int searchradius = 50;
    public static final int limit = 50;
    public static final String confirmLoc = "confirm_loc";
    public static final String checkinLoc = "check_in_loc";
    public static final String loc = "loc";



    public boolean isInHandleableState(List<Intent> intents, Dialogue d){
        ArrayList<String> locIntents = new ArrayList<>();
        locIntents.add(confirmLoc);
        locIntents.add(checkinLoc);
        locIntents.add(loc);
        boolean response=false;
        for(Intent i:intents){
            if (locIntents.contains(i.getName())){
                response=true;
            }
        }
        return response;
    }

    public Response handle(List<Intent> intents, Dialogue d, Object resource){
        //we think that the user message has some information about location
        //System.err.println("Using problem handler: locMethod()");
        handleLocation(intents.get(0),d,resource); // must actually find correct intent
        return processStack(d);

    }

    public static void handleLocation(Intent i, Dialogue d, Object resource){
        ProductMongoDB db=null;
        if (resource instanceof ProductMongoDB){
            db=(ProductMongoDB) resource;
        }

        //possible confirm slot
        Collection<Intent.Slot> answers = i.getSlotByType(yes_no_slot);
        boolean accept=false;
        for(Intent.Slot answer:answers){

            if(answer.value.equals("yes")){
                accept=true;
            }
        }
        if(accept){
            ConfirmMethod.accept(d);
        } else {
            ConfirmMethod.reject(d, resource);
        }

        //definite location slot
        Collection<Intent.Slot> locations = i.getSlotByType(locationSlot);
        ArrayList location_list = new ArrayList<>();
        for(Intent.Slot location: locations){
            location_list.add(location.value);

        }
        d.putToWorkingMemory("locationList",StringUtils.join(location_list));
        List<Merchant> possibleMerchants = filterRejected(matchNearbyMerchants(location_list, db, d.getUserData()), d.getFromWorkingMemory("rejectedlist"));
        processMerchantList(possibleMerchants,d);

    }

    public static void processMerchantList(List<Merchant> possibleMerchants, Dialogue d){
        //System.err.println("Processing possible merchants");
        if(possibleMerchants.size()==0){
            //newStates.add("confirm_loc");
            //responseVariables.put(locationSlot, StringUtils.join(location_list));
            //System.err.println("No matching merchants");
            if(d.getFromWorkingMemory("location_list")==null){
                d.pushFocus("request_location");
            } else {
                d.pushFocus("repeat_request_loc");
            }
            //return new Response("repeat_request_location",responseVariables,newStates);
        } else {
            if(possibleMerchants.size()==1){
                //newStates.add("confirm_loc");
                //responseVariables.put(locationSlot, possibleMerchants.get(0).getName());
                d.pushFocus("confirm_loc");
                updateMerchant(possibleMerchants.get(0),d);
                //return new Response("confirm_location",responseVariables,newStates);
            }
            else{
                //may want to do something different if multiple merchants returned but currently assume first is best and just offer this one
                //newStates.add("confirm_loc");
                //responseVariables.put(locationSlot, possibleMerchants.get(0).getName());
                d.pushFocus("confirm_loc");
                updateMerchant(possibleMerchants.get(0),d);
                //return new Response("confirm_location",responseVariables,newStates);

            }
        }

    }


    public static void updateMerchant(Merchant m, Dialogue d){
        if(m==null){
            d.putToWorkingMemory("merchantId","");
            d.putToWorkingMemory("merchantName","");
        } else {
            d.putToWorkingMemory("merchantId", m.getMerchantId());
            d.putToWorkingMemory("merchantName", m.getName());
        }

    }

    public static List<Merchant> findNearbyMerchants(ProductMongoDB db, User user){

        List<Merchant> merchants = new ArrayList<>();
        if(db!=null){
            merchants = db.merchantQueryByLocation(user.getLatitude(),user.getLongitude(),searchradius,limit);

        } else {
            System.err.println("No database specified");
        }
        return merchants;
    }

    public static List<Merchant> filterRejected(List<Merchant> merchants, String rejectedlist){
        System.err.println("rejected: "+rejectedlist);
        if(rejectedlist==null){
            return merchants;
        } else {
            String[] rejected = rejectedlist.split(" ");
            List<Merchant> newmerchants = new ArrayList<>();
            for (Merchant m : merchants) {
                boolean reject=false;
                for (String r : rejected) {
                    if (m.getMerchantId().equals(r)) {
                        reject=true;

                    }
                }
                if(!reject){
                    newmerchants.add(m);
                }
            }
            return newmerchants;
        }
    }

    public static List<Merchant> matchNearbyMerchants(List<String> location_list, ProductMongoDB db,User user){
        if(db!=null) {
            List<Merchant> merchants = findNearbyMerchants(db,user);
            merchants.retainAll(db.merchantQuery(StringUtils.phrasejoin(location_list)));
            return merchants;
        } else{
            System.err.println("No database specified");
            return new ArrayList<>();
        }
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

        Response r=  new Response(focus,responseVariables,newStates);
        //if(r==null){
        //    System.err.println("No response generated");}
        //else {
        //    System.err.println("Generated non-null response");
        //}
        return r;

    }



}
