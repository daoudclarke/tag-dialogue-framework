package uk.ac.susx.tag.dialoguer.dialogue.components;

import uk.ac.susx.tag.dialoguer.Dialoguer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents an intent of the user. An intent has a name, and potentially text associated with it (optional).
 *
 * It also has a number of filled slots.
 *
 * Each slot has a type and a value (and optionally, a start and end span in the text).
 *
 * There can be multiple values for a single slot type.
 *
 * There are a number of default intents. See the public static fields.
 *
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 14:58
 */
public class Intent {

    // Default intents names that may have default behaviour
    public static final String nullChoice = "null_choice"; // User is explicitly rejecting a list of choices
    public static final String noChoice = "no_choice";     // User is ignoring the presented choices
    public static final String allChoice = "all_choice";   // User is selects everything in multi-choice
    public static final String choice = "choice";          //User is making a choice. The choice will be in the "choice" slot.
    public static final String no = "no";                  //User wishes to say no or decline or is ignoring a request for confirmation
    public static final String yes = "yes";                //User wishes to say yes or confirm
    public static final String cancel = "cancel";          //User wishes to cancel
    public static final String cancelAutoQuery = "cancel_auto_query";  // User message implies that we should cancel auto-querying

    private String name;
    private String text;
    private HashMap<String, HashSet<Slot>> slots;
    private String source;


    public Intent(String name) {
        this(name, "");
    }

    public Intent(String name, String text){
        this(name, text, new HashMap<String, HashSet<Slot>>());
    }

    public Intent(String name, String text, HashMap<String, HashSet<Slot>> slots){
        this.name = name;
        this.text = text;
        this.slots = slots;
        this.source = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isName(String name) { return this.name.equals(name); }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSource(String sourceId){
        source = sourceId;
    }

    public String getSource() { return source; }

/**********************************************
 * Slot management
 **********************************************/

    public void copySlots(List<Intent> intents){
        for (Intent i : intents) {
            Collection<Slot> slots = i.getSlotCollection();
            for (Slot slot : slots) {
                fillSlot(slot);
            }
        }
    }

    public IntentMatch getIntentMatch(Set<String> requiredSlotNames){
        return new IntentMatch(this, requiredSlotNames);
    }

    public boolean isSlotTypeFilledWith(String type, String value){
        Collection<Slot> slots = getSlotByType(type);
        for (Slot slot : slots) {
            if (slot.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

//    public static boolean areSlotsFilled(List<Intent> intents, Map<String, Set<String>> necessarySlotsPerIntent){
//        // Only return true if all slots are field on all intents
//        return intents.stream().allMatch(i -> i.areSlotsFilled(necessarySlotsPerIntent.get(i.getName())));
//    }

    public HashSet<String> getUnfilledSlotNames(Set<String> requiredSlotNames){
        HashSet<String> results = new HashSet<>(requiredSlotNames);
        results.removeAll(slots.keySet());
        return results;
    }

    public boolean areSlotsFilled(Set<String> requiredSlotNames){
        return getUnfilledSlotNames(requiredSlotNames).isEmpty();
    }

    public Intent fillSlot(String name, String value, int start, int end){
        HashSet<Slot> slotsToFill = ensureSlotExists(name);
        slotsToFill.add(new Slot(name, value, start, end));
        return this;
    }
    public Intent fillSlot(String name, String value){
        return fillSlot(name, value, 0, 0);
    }

    public Intent fillSlot(Slot s){
        HashSet<Slot> slotToFill = ensureSlotExists(s.name);
        slotToFill.add(s);
        return this;
    }

    public Intent replaceSlot(Slot s){
        slots.remove(s.name);
        return fillSlot(s);
    }

    public Intent fillSlots(Collection<Slot> slotlist){
        for (Slot s : slotlist) {
            fillSlot(s);
        }
        return this;
    }

    public Intent clearSlots(String name){
        slots.remove(name);
        return this;
    }

    public Collection<Slot> getSlotByType(String slotType){ return slots.get(slotType);}

    public List<String> getSlotValuesByType(String slotType){
        ArrayList<String> results = new ArrayList<>();
        for (Slot slot : slots.get(slotType)) {
            results.add(slot.value);
        }
        return results;
    }

//    public Multimap<String, Slot> getSlots() { return slots; }
    public Collection<Slot> getSlotCollection() {
        HashSet<Slot> allSlots = new HashSet<>();
        for (HashSet<Slot> slotSet : slots.values()) {
            allSlots.addAll(slotSet);
        }
        return allSlots;
    }

//    public void setSlots(Multimap<String, Slot> slots) { this.slots = slots; }
    public boolean isAnySlotFilled() { return !slots.isEmpty(); }

    public static class Slot {

        public String name;
        public String value;
        public int start;
        public int end;

        public Slot(String name, String value, int start, int end) {
            this.name = name;
            this.value = value;
            this.start = start;
            this.end = end;
        }

        public String toString() {
            return name + ":" + value + "(" + start + ":" + end + ")";
        }
    }

/**********************************************
 * Default intents
 **********************************************/
    public static Intent buildNullChoiceIntent(String userMessage){ return new Intent(nullChoice, userMessage);}
    public static Intent buildNoChoiceIntent(String userMessage){
        return new Intent(noChoice, userMessage);
    }
    public static Intent buildCancelIntent(String userMessage){ return new Intent(cancel, userMessage); }
    public static Intent buildNoIntent(String userMessage) { return new Intent(no, userMessage);}
    public static Intent buildYesIntent(String userMessage) { return new Intent(yes, userMessage); }
    public static Intent buildCancelAutoQueryIntent(String userMessage) { return new Intent(cancelAutoQuery, userMessage);}
    public static Intent buildChoiceIntent(String userMessage, int choiceNumber){
        return new Intent(choice, userMessage)
                .fillSlot("choice", Integer.toString(choiceNumber), 0, 0);
    }
    public static Intent buildMultichoiceIntent(String userMessage, List<Integer> choices){
        Intent intent = new Intent(choice, userMessage);
        for (int choiceNumber : choices) {
            intent = intent.fillSlot("choice", Integer.toString(choiceNumber), 0, 0);
        }
        return intent;
    }

/**********************************************
 * Utility
 **********************************************/
    public List<Intent> toList(){
        ArrayList<Intent> results = new ArrayList<>();
        results.add(this);
        return results;
    }

    /**
     * Return true if an intent with name=*name* is in *intents*.
     */
    public static boolean isPresent(String name, List<Intent> intents){
        for (Intent intent : intents) {
            if (intent.isName(name)) {
                return true;
            }
        }
        return false;
    }

//    public static Intent getIfPresentElseNull(String name, List<Intent> intents){
//        for (Intent intent : intents) {
//            if (intent.isName(name)) {
//                return intent;
//            }
//        }
//        return null;
//    }

    /**
     * Get first intent from *intents* which has the source specified.
     * If no such intent, then return null.
     */
    public static Intent getFirstIntentFromSource(String source, List<Intent> intents){
        for (Intent intent : intents) {
            if (intent.getSource().equals(source)) {
                return intent;
            }
        }
        return null;
    }

//    /**
//     * Create a new list from those elements in *intents* which have the source specified.
//     * Empty list will be returned if there are no such intents.
//     */
//    public static List<Intent> getAllIntentsFromSource(String source, List<Intent> intents){
//        ArrayList<Intent> results = new ArrayList<>();
//        for (Intent intent : intents) {
//            if (intent.getSource().equals(source)) {
//                results.add(intent);
//            }
//        }
//        return results;
//    }

//    @Override
//    public String toString(){
//        return Dialoguer.gson.toJson(this);
//    }

    /**
     * Create a set of slots for the given name if it doesn't exist, and return it,
     * or return the one that's already there.
     * @param name The name of the slot
     * @return The associated set of slots
     */
    private HashSet<Slot> ensureSlotExists(String name) {
        HashSet<Slot> slotsToFill;
        if (!slots.containsKey(name)) {
            slotsToFill = new HashSet<>();
            slots.put(name, slotsToFill);
        } else {
            slotsToFill = slots.get(name);
        }
        return slotsToFill;
    }
}
