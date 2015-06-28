package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.interactiveIntentHandlers;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.InteractiveHandler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.TaxiServiceHandler;
import uk.ac.susx.tag.dialoguer.knowledge.location.NominatimAPIWrapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Daniel Saska on 6/26/2015.
 */
public class LocationProblemHandler implements Handler.ProblemHandler {
    NominatimAPIWrapper nom = new NominatimAPIWrapper();

    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        if (dialogue.isEmptyFocusStack()) { return false; }
        boolean intentmatch = intents.stream().filter(i->i.getName().equals(InteractiveHandler.locationIntent)).count()>0;
        boolean intentmatch2 = intents.stream().filter(i->i.getName().equals(InteractiveHandler.locationUnknownIntent)).count()>0;
        boolean statematch = dialogue.peekTopFocus().equals(InteractiveHandler.aLocation);
        return (intentmatch || intentmatch2) && statematch;
    }

    @Override
    public void handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        System.err.println("location intent handler fired");
        Intent intent = intents.stream().filter(i->i.isName(InteractiveHandler.locationIntent)).findFirst().orElse(null);
        if (intent != null) {
            Collection<Intent.Slot> location = intent.getSlotByType(InteractiveHandler.locationSlot);
            String locationStr = location.iterator().next().value;
            //TODO: Validate location and determine whether it is specific enough
            dialogue.appendToWorkingMemory("location_given", locationStr + ", United Kingdom");

            NominatimAPIWrapper.NomResult results[] = nom.queryAPI(locationStr + ", United Kingdom", 100, 0, 1);//For now assume we are in UK

            if (results.length == 0) {
                //TODO: Handle
            }
            //TODO: Find better way to identify ambiguity
            int ambiguous = 0;
            for (NominatimAPIWrapper.NomResult nr : results) {
                if (nr.type == "city") {
                    ++ambiguous;
                }
            }

            //TODO: Handle this better than taking straight first result
            NominatimAPIWrapper.NomResult loc = results[0];
            dialogue.appendToWorkingMemory("location_processed", loc.display_name);

            if(!loc.address.containsKey("road")) { //Not specific enough - need street and house number (or even more)
                dialogue.pushFocus(InteractiveHandler.aEnableGps); //GPS may be easier in this case
                dialogue.pushFocus(InteractiveHandler.qEnableGps);
                return;
            } else if (!loc.address.containsKey("house_number")) {
                dialogue.pushFocus(InteractiveHandler.aLandmarks); //Quickly stating landmark might be easier if we need only house number
                dialogue.pushFocus(InteractiveHandler.qLandmarks);
                return;
            }

            //We should have everything needed in terms of address
            dialogue.pushFocus(InteractiveHandler.aMedicalHelp);
            dialogue.pushFocus(InteractiveHandler.qMedicalHelp);
        }
        else {
            dialogue.appendToWorkingMemory("location_processed", "United Kingdom");
            intent = intents.stream().filter(i->i.isName(InteractiveHandler.locationUnknownIntent)).findFirst().orElse(null);
            dialogue.pushFocus(InteractiveHandler.aEnableGps);
            dialogue.pushFocus(InteractiveHandler.qEnableGps);
        }

    }
}
