package uk.ac.susx.tag.dialoguer.dialogue.components;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 15:29
 */
public class Dialogue {

    private String id;
    private Map<String, Object> workingMemory; // Data about the current dialogue, e.g. partially filled slots that could be useful for subsequent intents
    private List<String> states;
    private List<String> choices; // Choices currently presented to the user
    private List<Message> history;    // Log of the user and system messages in chronological order
    private User user;                // Data about the user with which we're interacting


    public void addNewUserMessage(String message, List<Intent> intents) {
        //TODO
    }

    public void addNewSystemMessage(String message) {
        //TODO
    }

    public List<Intent> getCurrentIntents() {
        //TODO
        return null;
    }

    public void setStates(List<String> states){
        this.states = states;
    }

    public void setState(String state){
        this.states = Lists.newArrayList(state);
    }

    public List<String> getStates(){
        return states;
    }

    public void addToWorkingMemory(String key, Object dataValue) {
        workingMemory.put(key, dataValue);
    }

    public <T> T getFromWorkingMemory(String key) {
        return (T)workingMemory.get(key);
    }

    public enum MessageType {SYSTEM, USER}
    public static class Message {

        public String text;
        public MessageType type;

        public boolean isSystemMessage() { return type == MessageType.SYSTEM; }
        public boolean isUserMessage()   { return type == MessageType.USER; }

        public static Message newSystemMessage(String text){
            return new Message(text, MessageType.SYSTEM);
        }

        public static Message newUserMessage(String text){
            return new Message(text, MessageType.USER);
        }

        private Message(String text, MessageType type){
            this.text = text;
            this.type = type;
        }
    }
}
