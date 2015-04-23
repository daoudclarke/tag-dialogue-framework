package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Merchant;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;

import java.util.*;

/**
 * Created by juliewe on 21/04/2015.
 */
public class LocMethod implements Handler.IntentHandler {

    public static final String locationSlot="local_search_query";
    public Response handle(Intent i, Dialogue d){
        //we think that the user message has some information about location
        //we also need to consider the user's geolocation information

        //Multimap<String, Intent.Slot> slots = i.getSlots();
        List<String> newStates = new ArrayList<>();
        Map<String, String> responseVariables = new HashMap<>();

        Collection<Intent.Slot> locations = i.getSlotByType(locationSlot);
        String location_string="";
        int count=0;
        for(Intent.Slot location: locations){
            //System.out.println(location.name+" : "+location.value);
            if (count>0){location_string+=" ";}
            location_string+=location.value;
            count++;
        }

        List<Merchant> possibleMerchants = matchNearbyMerchants(locations);

        if(possibleMerchants.size()==0){
            newStates.add("confirm_loc");
            responseVariables.put(locationSlot, location_string);
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

    private List<Merchant> matchNearbyMerchants(Collection<Intent.Slot> locations){
        List<Merchant> merchants = new ArrayList<>();
        return merchants;
    }

}