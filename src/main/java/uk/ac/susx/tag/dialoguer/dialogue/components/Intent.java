package uk.ac.susx.tag.dialoguer.dialogue.components;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 14:58
 */
public class Intent {

    private String name;
    private String text;
    private Multimap<String, Slot> slots;

    public Set<String> getUnfilledSlotNames(Set<String> requiredSlotNames){
        return Sets.difference(requiredSlotNames, slots.keySet());
    }

    public boolean areSlotsFilled(Set<String> requiredSlotNames){
        return !getUnfilledSlotNames(requiredSlotNames).isEmpty();
    }

    public void fillSlot(String name, String value, int start, int end){
        slots.put(name, new Slot(name, value, start, end));
    }

    public void fillSlot(String value, String type){
        fillSlot(value, type, 0, 0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
}
