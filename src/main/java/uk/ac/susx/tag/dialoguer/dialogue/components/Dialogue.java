package uk.ac.susx.tag.dialoguer.dialogue.components;

import com.google.common.collect.Lists;
import uk.ac.susx.tag.dialoguer.Dialoguer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * NOTE TO DEVS: This class MUST be serialisable and deserialisable using Dialoguer.gson
 *
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
 *   isComplete         : whether this dialogue has been marked as complete. It is mostly the duty of the Dialoguer or Handler to decide this.
 *
 *   intents            : the list of intents the user has expressed that are still pertinent to the dialogue Handler. Intents that
 *                        it has yet to act upon or needs to keep track of.
 *
 *   autoQueryTracker   : Tracks intents with unfilled necessary slots, allowing the Dialoguer to auto query/fill them. The analysers
 *                        will get a look at the user response before the autoquerier does. So if you want to stop the auto-querying
 *                        mechanic after a particular user message, then have your Analyser make a "cancel_auto_querying" intent
 *                        by using Intent.buildCancelAutoQueryIntent()
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
    private boolean isComplete;

    private List<Intent> intents;              // Pertinent user intents, still requiring attention by the handler
    private AutoQueryTracker autoQueryTracker; // Track status of auto-queries for necessary slots on intents

    private Map<String, String> workingMemory; // Data about the current dialogue, e.g. partially filled slots that could be useful for subsequent intents
    private List<String> states;               // States the dialogue is actually IN. Think: all the states passed to a single Wit.Ai query
    private List<String> questionFocusStack;   // IDs/Names of questions that the Handler wants to queue for asking the user

    private List<String> choices;     // Choices currently presented to the user (remember to clear it in the handler if you no longer expect to user to answer them)
    private boolean requestingYesNo;  // Whether or not the system is currently requesting a Yes/No answer

    private List<Message> history;    // Log of the user and system messages in chronological order (oldest first) (including user data)
    private User user;                // Data about the user with which we're interacting

    private Dialogue(){
        this("*UNDEFINED*"); // This allows Gson to use default values upon deserialisation.
    }

    public Dialogue(String id) {
        this.id = id;
        isComplete = false;
        intents = new ArrayList<>();
        autoQueryTracker = new AutoQueryTracker();
        workingMemory = new HashMap<>();
        states = new ArrayList<>();
        questionFocusStack = new ArrayList<>();
        choices = new ArrayList<>();
        requestingYesNo = false;
        history = new ArrayList<>();
        user = null;
    }

    public String getId() { return id; }

    public void complete() { isComplete = true; }
    public void setComplete(boolean isComplete) { this.isComplete = isComplete; }
    public boolean isComplete() { return isComplete; }

/***********************************************
 * Intent management
 ***********************************************/
    public List<Intent> getCurrentIntents() { return intents; }
    public void addToCurrentIntents(Intent i) { intents.add(i); }
    public void addToCurrentIntents(List<Intent> intents){ this.intents.addAll(intents); }
    public void clearCurrentIntents() { intents.clear(); }
    public void replaceCurrentIntents(List<Intent> intents){ this.intents = intents; }

/***********************************************
 * Question focus stack
 ***********************************************/

    public String peekTopFocus() { return questionFocusStack.get(questionFocusStack.size()-1);}
    public String popTopFocus() { return questionFocusStack.remove(questionFocusStack.size()-1);}
    public void pushFocus(String newTopFocus) { questionFocusStack.add(newTopFocus); }
    public boolean isFocusPresent(String focus) { return questionFocusStack.contains(focus); }
    public void removeFocus(String focus) { questionFocusStack.remove(focus); }
    public void clearFocusStack() { questionFocusStack.clear();}

/***********************************************
 * Choice / Confirmation management
 ***********************************************/
    public boolean isChoicesPresented(){return !choices.isEmpty();}
    public void clearChoices(){ choices.clear(); }
    public List<String> getChoices() { return choices; }
    public boolean isRequestingYesNo() { return requestingYesNo; }
    public void setRequestingYesNo(boolean requestingYesNo){ this.requestingYesNo = requestingYesNo; }

/***********************************************
 * State management
 ***********************************************/
    public void setStates(List<String> states){ this.states = states; }
    public void setState(String state){ this.states = Lists.newArrayList(state); }
    public List<String> getStates(){ return states; }
    public void clearStates() { states.clear(); }

/***********************************************
 * Working memory management
 ***********************************************/
    public void putToWorkingMemory(String key, String dataValue) { workingMemory.put(key, dataValue);}
    public boolean isInWorkingMemory(String key, String value){
        return workingMemory.containsKey(key) && workingMemory.get(key).equals(value);
    }
    public void appendToWorkingMemory(String key, String appendValue){
        appendToWorkingMemory(key, appendValue, "");
    }

    /**
     * Append *appendValue* to whatever string is in working memory for *key*, separated by *separator*.
     * If *key* is not in working memory, then this method is the same as putToWorkingMemory.
     */
    public void appendToWorkingMemory(String key, String appendValue, String separator){
        if (workingMemory.containsKey(key)){
            workingMemory.put(key, workingMemory.get(key)+separator+appendValue);
        } else {
            putToWorkingMemory(key, appendValue);
        }
    }
    public String getFromWorkingMemory(String key) { return workingMemory.get(key); }
    public String getStrippedText(){ return getFromWorkingMemory("stripped"); }
    public String getStrippedNoStopwordsText(){ return getFromWorkingMemory("strippedNoStopwords"); }

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

    public Message getLastMessage(){
        return history.isEmpty()? null : history.get(history.size()-1);
    }

    public User getUserData(){ return user; }
    public List<Message> getMessageHistory(){ return history; }
    public int getCurrentMessageNumber(){ return history.size(); }
    public List<Message> getUserMessageHistory(){
        return history.stream()
                .filter(Message::isUserMessage)
                .collect(Collectors.toList());
    }

    public boolean isLastMessageByUser() {
        return !history.isEmpty() && getLastMessage().isUserMessage();
    }


/***********************************************
 * Auto-querying management
 ***********************************************/
    public void trackNewAutoQueryList(List<IntentMatch> intentMatches){
        autoQueryTracker.intents = intentMatches;
        autoQueryTracker.currentIntentIndex = 0;
    }

    /**
     * Check isExpectingAutoRequestResponse is true before calling.
     * This will fill the slot of the intent expecting its value with the text of the user message.
     */
    public void fillAutoRequest(String userMessage){
        autoQueryTracker.getCurrentIntent().fillNextNecessarySlot(userMessage);
    }

    /**
     * Return true if there is an intent awaiting the fill of a necessary slot.
     * Will return false if there are no intents being tracked, or if all tracked intents are filled.
     */
    public boolean isExpectingAutoRequestResponse(){
        if (!autoQueryTracker.intents.isEmpty()){
            while (autoQueryTracker.currentIntentIndex < autoQueryTracker.intents.size()){
                if (!autoQueryTracker.getCurrentIntent().areSlotsFilled()){
                    return true;
                } else {
                    autoQueryTracker.currentIntentIndex++;
                }
            } return false;
        } return false;
    }

    /**
     * Get and remove all tracked intents in whatever state they are.
     */
    public List<Intent> popAutoQueriedIntents(){
        List<Intent> autoQueriedIntents = autoQueryTracker.intents.stream()
                                              .map(IntentMatch::getIntent)
                                              .collect(Collectors.toList());
        autoQueryTracker.reset();
        return autoQueriedIntents;
    }


    public String getNextAutoQuery(){
        if (isExpectingAutoRequestResponse())
            return autoQueryTracker.getCurrentIntent().peekNextNecessarySlot();
        else throw new NoSuchElementException();
    }

    // Basic structure allowing easy tracking of IntentMatches
    public static class AutoQueryTracker {
        public List<IntentMatch> intents = new ArrayList<>(); // Track status of auto-queries for necessary slots on intents
        public int currentIntentIndex = 0;

        public IntentMatch getCurrentIntent() {
            if (intents.isEmpty())
                throw new NoSuchElementException();
            else return intents.get(currentIntentIndex);
        }

        public void reset(){
            intents.clear();
            currentIntentIndex = 0;
        }
    }

/***********************************************
 * Utility
 ***********************************************/

    @Override
    public String toString(){ return Dialoguer.gson.toJson(this); }

}
