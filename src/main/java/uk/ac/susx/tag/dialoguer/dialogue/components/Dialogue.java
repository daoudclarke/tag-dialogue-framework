package uk.ac.susx.tag.dialoguer.dialogue.components;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import uk.ac.susx.tag.dialoguer.Dialoguer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class representing the ongoing dialogue with a user.
 *
 * Any information that must be persistent throughout that dialogue (like choices presented to the user, or user's
 * previous location) gets put on this dialogue. Message history, and recent pertinent user intents are here.
 *
 * This is the object passed through the system, where information is collected and passed on.
 *
 * Full description of information stored:
 *
 *   id                 : the ID of this dialogue. This is what the DialogueTracker pays attention to in order to match new messages
 *                        to this dialogue. A sensible value for this could be the user's twitter handle for example.
 *
 *   intents            : the list of intents the user has expressed that are still pertinent to the dialogue Handler. Intents that
 *                        it has yet to act upon or needs to keep track of.
 *
 *   workingMemory      : A mapping of extra useful information for the current dialogue state, that will be available for
 *                        any subsequent analysis. This could track entities or variables outside of intents that the system
 *                        wishes to track.
 *
 *   states             : the CURRENT states of the dialogue. Much like the states passed to a Wit.Ai system as any one time. In
 *                        fact the WitAiAnalyser directly passes all of these to Wit.Ai when it makes a query. These states are
 *                        set using the states specified in the Response object produced by the Handler.
 *
 *   questionFocusStack : A stack of strings (e.g. IDs/names) that a handler wishes to track, in order to track a sequence
 *                        of questions to ask the user, or messages to send to the user.
 *
 *   choices            : these are choices that the handler has declared that it has presented to the user in its responses. These
 *                        should be cleared when they are no longer needed to get best performance from the choice analyser.
 *
 *   requestingYesNo    : the handler should set this to true if you're expecting a yes/no or confirmation response.
 *
 *   history            : the system and user message history. Each user message contains data about the user at the time
 *                        of that message.
 *
 *   user               : the current user data (e.g. geo data).
 *
 *
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 15:29
 */
public class Dialogue {

    private String id;
    private List<Intent> intents;
    private Map<String, String> workingMemory; // Data about the current dialogue, e.g. partially filled slots that could be useful for subsequent intents
    private List<String> states;               // States the dialogue is actually IN. Think: all the states passed to a single Wit.Ai query
    private List<String> questionFocusStack;   // IDs/Names of questions that the Handler wants to queue for asking the user
    private List<String> choices;     // Choices currently presented to the user (remember to clear it in the handler if you no longer expect to user to answer them)
    private boolean requestingYesNo;  // Whether or not the system is currently requesting a Yes/No answer
    private List<Message> history;    // Log of the user and system messages in chronological order (oldest first) (including user data)
    private User user;                // Data about the user with which we're interacting

    public Dialogue(String id) {
        this.id = id;
        intents = new ArrayList<>();
        workingMemory = new HashMap<>();
        states = new ArrayList<>();
        questionFocusStack = new ArrayList<>();
        choices = new ArrayList<>();
        requestingYesNo = false;
        history = new ArrayList<>();
        user = null;
    }

/***********************************************
 * Intent management
 ***********************************************/
    public List<Intent> getCurrentIntents() { return intents; }
    public void addToCurrentIntents(Intent i) { intents.add(i); }
    public void addToCurrentIntents(List<Intent> intents){ intents.addAll(intents); }
    public void clearCurrentIntents() { intents.clear(); }
    public void replaceCurrentIntents(List<Intent> intents){ this.intents = intents; }

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
    public void addNewUserMessage(String message, User user) {
        this.user = user;
        history.add(new Message(message, user));
    }

    public void addNewSystemMessage(String message) {
        history.add(new Message(message));
    }

    public Message getLatestUserMessage(){
        for (int i = history.size()-1; i >= 0; i--){
            if (history.get(i).isUserMessage())
                return history.get(i);
        } return null;
    }

    public User getUserData(){ return user; }
    public List<Message> getMessageHistory(){ return history; }
    public int getCurrentMessageNumber(){return history.size(); }
    public List<Message> getUserMessageHistory(){
        return history.stream()
                .filter(Message::isUserMessage)
                .collect(Collectors.toList());
    }

/***********************************************
 * Utility
 ***********************************************/

    @Override
    public String toString(){
        return Dialoguer.gson.toJson(this);
    }
}
