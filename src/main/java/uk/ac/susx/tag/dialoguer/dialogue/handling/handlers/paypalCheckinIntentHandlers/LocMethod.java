package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;

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

        Collection<Intent.Slot> locations = i.getSlotByType(locationSlot);
        String location_string="";
        int count=0;
        for(Intent.Slot location: locations){
            //System.out.println(location.name+" : "+location.value);
            if (count>0){location_string+=", ";}
            location_string+=location.value;
            count++;
        }

        List<String> newStates = new ArrayList<>();
        newStates.add("confirm_loc");

        Map<String, String> responseVariables = new HashMap<>();
        responseVariables.put(locationSlot, location_string);
        d.putToWorkingMemory(locationSlot,location_string);
        return new Response("confirm_location",responseVariables,newStates);}
}
