package uk.ac.susx.tag.dialoguer.dialogue.components;

import com.google.common.collect.*;

import java.util.List;
import java.util.Set;

/**
 * Default intents:
 *
 * ----User wishes to cancel----:
 *
 *  {
 *    name = "cancel"
 *    slots = {}
 *  }
 *
 * ----User is making a choice----:
 *
 *  {
 *    name = "choice"
 *    slots = {
 *        choice_index = <integer>
 *    }
 *  }
 *
 * ----User is explicitly rejecting a list of choices----:
 *  {
 *      name = "null_choice"
 *      slots = {}
 *  }
 *
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 14:58
 */
public class Intent {

    private String name;
    private String text;
    private Multimap<String, Slot> slots;

    public Intent(String name, String text){
        this(name, text, ArrayListMultimap.create());
    }

    public Intent(String name, String text, Multimap<String, Slot> slots){
        this.name = name;
        this.text = text;
        this.slots = slots;
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

/**********************************************
 * Slot management
 **********************************************/
    public Set<String> getUnfilledSlotNames(Set<String> requiredSlotNames){
        return Sets.difference(requiredSlotNames, slots.keySet());
    }

    public boolean areSlotsFilled(Set<String> requiredSlotNames){
        return !getUnfilledSlotNames(requiredSlotNames).isEmpty();
    }

    public void fillSlot(String name, String value, int start, int end){
        slots.put(name, new Slot(name, value, start, end));
    }
    public void fillSlot(String name, String value){
        fillSlot(name, value, 0, 0);
    }

    public Multimap<String, Slot> getSlots() {
        return slots;
    }
    public void setSlots(Multimap<String, Slot> slots) {
        this.slots = slots;
    }

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
    }

/**********************************************
 * Default intents
 **********************************************/
    public static Intent buildNullChoiceIntent(String userMessage){
        return new Intent("null_choice", userMessage);
    }

    public static Intent buildChoiceIntent(String userMessage, int choiceNumber){
        Intent i = new Intent("choice", userMessage);
        i.fillSlot("choice", Integer.toString(choiceNumber), 0, 0);
        return i;
    }

    public static Intent buildNoChoiceIntent(String userMessage){
        return new Intent("no_choice", userMessage);
    }

    public static Intent buildCancelIntent(String userMessage){
        return new Intent("cancel", userMessage);
    }

    public static Intent buildNoIntent(String userMessage) { return new Intent("no", userMessage);}

    public static Intent buildYesIntent(String userMessage) { return new Intent("yes", userMessage); }

/**********************************************
 * Utility
 **********************************************/
    public List<Intent> toList(){
        return Lists.newArrayList(this);
    }

    public static boolean isPresent(String name, List<Intent> intents){
        for (Intent i : intents){
            if (i.isName(name)) return true;
        } return false;
    }
}
