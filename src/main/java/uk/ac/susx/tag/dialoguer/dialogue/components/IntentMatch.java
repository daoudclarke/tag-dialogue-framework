package uk.ac.susx.tag.dialoguer.dialogue.components;

import com.google.common.collect.Lists;

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
        this(null, new HashSet<>());
    }

    public IntentMatch(Intent intent, Set<String> necessarySlots) {
        this.intent = intent;
        this.necessarySlotsRemaining = copyAndTrimFilledSlots(intent, necessarySlots);
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
        return intentMatches.stream().allMatch(IntentMatch::areSlotsFilled);
    }

    public List<IntentMatch> toList(){
        return Lists.newArrayList(this);
    }

    private void readyNextSlot(){
        necessarySlotsRemaining.remove(nextSlotRequired);
        if (!necessarySlotsRemaining.isEmpty()) {
            nextSlotRequired = necessarySlotsRemaining.iterator().next();
        }
        else nextSlotRequired = null;
    }

    private Set<String> copyAndTrimFilledSlots(Intent i, Set<String> necessarySlots){
        return i.getUnfilledSlotNames(necessarySlots).copyInto(new HashSet<>());
    }
}
