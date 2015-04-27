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

    protected ProductMongoDB db;

    public void setDB(ProductMongoDB db){this.db=db;}

    public Response handle(Intent i, Dialogue d, Object resource){
        //we think that the user message has some information about location
        //we also need to consider the user's geolocation information

        //Multimap<String, Intent.Slot> slots = i.getSlots();


        //grab db
        if (resource instanceof ProductMongoDB){
            setDB((ProductMongoDB) resource);
        }

        List<String> newStates = new ArrayList<>();
        Map<String, String> responseVariables = new HashMap<>();

        Collection<Intent.Slot> locations = i.getSlotByType(locationSlot);
        String location_string="";
        ArrayList location_list = new ArrayList<>();
        for(Intent.Slot location: locations){
            location_list.add(location.value);
        }


        List<Merchant> possibleMerchants = matchNearbyMerchants(location_list,db, d.getUserData());

        if(possibleMerchants.size()==0){
            newStates.add("confirm_loc");
            responseVariables.put(locationSlot, StringUtils.join(location_list));
            return new Response("repeat_request_location",responseVariables,newStates);
        } else {
            if(possibleMerchants.size()==1){
                newStates.add("confirm_loc");
                responseVariables.put(locationSlot, possibleMerchants.get(0).getName());
                d.putToWorkingMemory("merchantId",possibleMerchants.get(0).getMerchantId());
                return new Response("confirm_location",responseVariables,newStates);
            }
            else{
                //may want to do something different if multiple merchants returned but currently assume first is best and just offer this one
                newStates.add("confirm_loc");
                responseVariables.put(locationSlot, possibleMerchants.get(0).getName());
                d.putToWorkingMemory("merchantId",possibleMerchants.get(0).getMerchantId());
                return new Response("confirm_location",responseVariables,newStates);

            }
        }





    }

    private List<Merchant> matchNearbyMerchants(List<String> location_list, ProductMongoDB db,User user){
        List<Merchant> merchants = new ArrayList<>();
        if(db!=null){
            merchants = db.merchantQueryByLocation(user.getLatitude(),user.getLongitude(),searchradius,limit);
            merchants.retainAll(db.merchantQuery(StringUtils.phrasejoin(location_list)));
        } else {
            System.err.println("No database specified");
        }
        return merchants;
    }



}
