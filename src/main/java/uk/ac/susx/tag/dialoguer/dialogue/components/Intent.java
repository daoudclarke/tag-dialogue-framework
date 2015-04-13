package uk.ac.susx.tag.dialoguer.dialogue.components;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;
import uk.ac.susx.tag.dialoguer.Dialoguer;

import java.util.Collection;
import java.util.List;
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
 * There are a number of default intents:
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
 * ----User wishes to say yes or confirm--------------:
 *
 *  {
 *      name = "yes"
 *      slots = {}
 *  }
 *
 * ---User wishes to say no or decline --------------:
 * {
 *     name = "no"
 *     slots = {}
 * }
 *
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 14:58
 */
public class Intent {

    private String name;
    private String text;
    private Multimap<String, Slot> slots;

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

    public Intent fillSlot(String name, String value, int start, int end){
        slots.put(name, new Slot(name, value, start, end));  return this;
    }
    public Intent fillSlot(String name, String value){
        return fillSlot(name, value, 0, 0);
    }

    public Collection<Slot> getSlotByType(String slotType){ return slots.get(slotType);}
    public Multimap<String, Slot> getSlots() { return slots; }
    public void setSlots(Multimap<String, Slot> slots) { this.slots = slots; }

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
        return new Intent("choice", userMessage)
                     .fillSlot("choice", Integer.toString(choiceNumber), 0, 0);
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

    @Override
    public String toString(){
        return Dialoguer.gson.toJson(this);
    }
}
