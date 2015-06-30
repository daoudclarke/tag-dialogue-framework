package uk.ac.susx.tag.dialoguer.dialogue.components;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import uk.ac.susx.tag.dialoguer.Dialoguer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private Multimap<String, Slot> slots;
    private String source;


    public Intent(String name) {
        this(name, "");
    }

    public Intent(String name, String text){
        this(name, text, ArrayListMultimap.create());
    }

    public Intent(String name, String text, Multimap<String, Slot> slots){
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
            i.getSlotCollection().forEach(this::fillSlot);
        }
    }

    public IntentMatch getIntentMatch(Set<String> requiredSlotNames){
        return new IntentMatch(this, requiredSlotNames);
    }

    public boolean isSlotTypeFilledWith(String type, String value){
        return getSlotByType(type).stream()
                .anyMatch(slot -> slot.value.equals(value));
    }

    public static boolean areSlotsFilled(List<Intent> intents, Map<String, Set<String>> necessarySlotsPerIntent){
        // Only return true if all slots are field on all intents
        return intents.stream().allMatch(i -> i.areSlotsFilled(necessarySlotsPerIntent.get(i.getName())));
    }

    public Sets.SetView<String> getUnfilledSlotNames(Set<String> requiredSlotNames){
        return Sets.difference(requiredSlotNames, slots.keySet());
    }

    public boolean areSlotsFilled(Set<String> requiredSlotNames){
        return getUnfilledSlotNames(requiredSlotNames).isEmpty();
    }

    public Intent fillSlot(String name, String value, int start, int end){
        slots.put(name, new Slot(name, value, start, end));  return this;
    }
    public Intent fillSlot(String name, String value){
        return fillSlot(name, value, 0, 0);
    }

    public Intent fillSlot(Slot s){
        slots.put(s.name, s); return this;
    }

    public Intent replaceSlot(Slot s){
        slots.removeAll(s.name);
        slots.put(s.name, s);
        return this;
    }

    public Intent fillSlots(Collection<Slot> slotlist){
        slotlist.stream().filter(s->!slots.values().contains(s)).forEach(s -> slots.put(s.name, s));
        return this;
    }

    public Intent clearSlots(String name){
        slots.removeAll(name);
        return this;
    }

    public Collection<Slot> getSlotByType(String slotType){ return slots.get(slotType);}
    public List<String> getSlotValuesByType(String slotType){return this.getSlotByType(slotType).stream().map(slot->slot.value).collect(Collectors.toList());}
    public Multimap<String, Slot> getSlots() { return slots; }
    public Collection<Slot> getSlotCollection() {
        return slots.values();
    }
    public void setSlots(Multimap<String, Slot> slots) { this.slots = slots; }
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
        return Lists.newArrayList(this);
    }

    /**
     * Return true if an intent with name=*name* is in *intents*.
     */
    public static boolean isPresent(String name, List<Intent> intents){
        return intents.stream().anyMatch(i -> i.isName(name));
    }

    public static Intent getIfPresentElseNull(String name, List<Intent> intents){
        return intents.stream().filter(i -> i.isName(name)).findFirst().orElse(null);
    }

    /**
     * Get first intent from *intents* which has the source specified.
     * If no such intent, then return null.
     */
    public static Intent getFirstIntentFromSource(String source, List<Intent> intents){
        return intents.stream()
                .filter(i -> i.getSource().equals(source))
                .findFirst()
                .orElse(null);
    }

    /**
     * Create a new list from those elements in *intents* which have the source specified.
     * Empty list will be returned if there are no such intents.
     */
    public static List<Intent> getAllIntentsFromSource(String source, List<Intent> intents){
        return intents.stream()
                .filter(i -> i.getSource().equals(source))
                .collect(Collectors.toList());
    }

    @Override
    public String toString(){
        return Dialoguer.gson.toJson(this);
    }
}
