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
public class LocMethod implements Handler.IntentHandler {

    public static final String locationSlot="local_search_query";
    public static final int searchradius = 50;
    public static final int limit = 50;

    public Response handle(Intent i, Dialogue d, Object resource){
        //we think that the user message has some information about location
        handleLocation(i,d,resource);
        return new Response("");

    }

    public static void handleLocation(Intent i, Dialogue d, Object resource){
        ProductMongoDB db=null;
        if (resource instanceof ProductMongoDB){
            db=(ProductMongoDB) resource;
        }

        Collection<Intent.Slot> locations = i.getSlotByType(locationSlot);
        ArrayList location_list = new ArrayList<>();
        for(Intent.Slot location: locations){
            location_list.add(location.value);

        }
        d.putToWorkingMemory("locationList",StringUtils.join(location_list));
        List<Merchant> possibleMerchants = matchNearbyMerchants(location_list,db, d.getUserData());
        processMerchantList(possibleMerchants,d);

    }

    public static void processMerchantList(List<Merchant> possibleMerchants, Dialogue d){
        System.err.println("Processing possible merchants");
        if(possibleMerchants.size()==0){
            //newStates.add("confirm_loc");
            //responseVariables.put(locationSlot, StringUtils.join(location_list));
            if(d.getStates().contains("initial")){
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


    private static void updateMerchant(Merchant m, Dialogue d){
        d.putToWorkingMemory("merchantId",m.getMerchantId());
        d.putToWorkingMemory("merchantName",m.getName());

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



}
