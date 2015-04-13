package uk.ac.susx.tag.dialoguer.dialogue.components;

import com.google.common.collect.Lists;

import java.util.ArrayList;
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
    private Map<String, String> workingMemory; // Data about the current dialogue, e.g. partially filled slots that could be useful for subsequent intents
    private List<String> states;
    private List<String> questionFocusStack;
    private List<String> choices;     // Choices currently presented to the user
    private boolean requestingYesNo;  // Whether or not the system is currently requesting a Yes/No answer
    private List<Message> history;    // Log of the user and system messages in chronological order (oldest first)
    private User user;                // Data about the user with which we're interacting

/***********************************************
 * Intent management
 ***********************************************/
    public List<Intent> getCurrentIntents() {
        //TODO
        return null;
    }

/***********************************************
 * Question focus stack
 ***********************************************/

    public String peekTopFocus() { return questionFocusStack.get(questionFocusStack.size()-1);}
    public String popTopFocus() { return questionFocusStack.remove(questionFocusStack.size()-1);}
    public void pushFocus(String newTopFocus) { questionFocusStack.add(newTopFocus); }
    public void clearFocusStack() { questionFocusStack.clear();}

/***********************************************
 * Choice / Confirmation management
 ***********************************************/
    public boolean isChoicesPresented(){return !choices.isEmpty();}
    public void clearChoices(){ choices.clear(); }
    public List<String> getChoices() { return choices; }
    public boolean isRequestingYesNo() { return requestingYesNo; }
    public void setRequestingYesNo(boolean requestingYesNo){
        this.requestingYesNo = requestingYesNo;
    }

/***********************************************
 * State management
 ***********************************************/
    public void setStates(List<String> states){
        this.states = states;
    }
    public void setState(String state){
        this.states = Lists.newArrayList(state);
    }
    public List<String> getStates(){ return states; }
    public void clearStates() { states.clear(); }

/***********************************************
 * Working memory management
 ***********************************************/
    public void putToWorkingMemory(String key, String dataValue) {
        workingMemory.put(key, dataValue);
    }
    public String getFromWorkingMemory(String key) {
        return workingMemory.get(key);
    }
    public String getStrippedText(){
        return getFromWorkingMemory("stripped");
    }
    public String getStrippedNoStopwordsText(){
        return getFromWorkingMemory("strippedNoStopwords");
    }

/***********************************************
 * Message management
 ***********************************************/
    public void addNewUserMessage(String message, List<Intent> intents, User user) {
        //TODO
    }

    public void addNewSystemMessage(String message) {
        //TODO
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
