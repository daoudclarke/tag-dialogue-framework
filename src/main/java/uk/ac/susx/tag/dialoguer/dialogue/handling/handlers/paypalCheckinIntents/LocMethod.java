package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntents;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

import com.google.common.collect.Multimap;

import java.util.Collection;

/**
 * Created by juliewe on 21/04/2015.
 */
public class LocMethod implements IntentMethod{

    public static final String locationSlot="wit/local_search_query";
    public static Response execute(Intent i, Dialogue d){
        //we think that the user message has some information about location
        //we also need to consider the user's geolocation information

        //Multimap<String, Intent.Slot> slots = i.getSlots();

        Collection<Intent.Slot> locations = i.getSlotByType(locationSlot);
        for(Intent.Slot location: locations){
            System.out.println(location.name+" : "+location.value);
        }
        return new Response("confirmLocation");}
}
