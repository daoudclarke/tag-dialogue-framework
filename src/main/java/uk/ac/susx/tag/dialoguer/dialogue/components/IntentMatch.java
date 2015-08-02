package uk.ac.susx.tag.dialoguer.dialogue.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Represents an Intent matched with a set of necessary slots.
 *
 * Tracks which necessary slots still need filling.
 *
 * User: Andrew D. Robertson
 * Date: 15/04/2015
 * Time: 09:44
 */
public class IntentMatch {

    private Intent intent;
    private Set<String> necessarySlotsRemaining;
    private String nextSlotRequired;

    private IntentMatch(){
        this(null, new HashSet<String>());
    }

    public IntentMatch(Intent intent, Set<String> necessarySlots) {
        this.intent = intent;
        this.necessarySlotsRemaining = necessarySlots == null
                ? new HashSet<String>() : copyAndTrimFilledSlots(intent, necessarySlots);
        this.nextSlotRequired = null;
        readyNextSlot();
    }

    public Intent getFilledIntentOrNull(){
        if (!areSlotsFilled()) // If there's more slots that need filling return null
            return null;
        else return intent;
    }

    public Intent getIntent(){
        return intent;
    }

    public String peekNextNecessarySlot(){
        return nextSlotRequired;
    }

    public String popNextNecessarySlot(){
        if (!areSlotsFilled()){
            String toBeReturned = nextSlotRequired;
            readyNextSlot();
            return toBeReturned;
        }
        else throw new NoSuchElementException();
    }

    public void fillNextNecessarySlot(String message){
        intent.fillSlot(popNextNecessarySlot(), message, 0, message.length());
    }

    public boolean areSlotsFilled(){
        return nextSlotRequired == null;
    }

    public static boolean areSlotsFilled(List<IntentMatch> intentMatches){
        for (IntentMatch match : intentMatches) {
            if (!match.areSlotsFilled()) {
                return false;
            }
        }
        return true;
    }

    public List<IntentMatch> toList(){
        ArrayList<IntentMatch> matches = new ArrayList<>();
        matches.add(this);
        return matches;
    }

    private void readyNextSlot(){
        if (nextSlotRequired != null)
            necessarySlotsRemaining.remove(nextSlotRequired);
        if (!necessarySlotsRemaining.isEmpty())
            nextSlotRequired = necessarySlotsRemaining.iterator().next();
        else nextSlotRequired = null;
    }

    private Set<String> copyAndTrimFilledSlots(Intent i, Set<String> necessarySlots){
        Set<String> result = new HashSet<>();
        for (String name : i.getUnfilledSlotNames(necessarySlots)) {
            result.add(name);
        }
        return result;
    }
}
