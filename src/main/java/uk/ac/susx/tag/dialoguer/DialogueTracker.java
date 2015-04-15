package uk.ac.susx.tag.dialoguer;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.User;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Most high-level class. The main entry point into the dialogueing system is the getResponse() method
 * which takes a message, user info and an Id, maps it to the appropriate dialogue and uses the Dialoguer
 * to produce a response from the system.
 *
 * Tracks incomplete dialogues and handles logging.
 *
 * It is possible to register a CompletedDialogueHandler, which will be given a chance to play with Dialogue objects
 * when they are completed, just before they get untracked and forgotten (or logged).
 *
 *
 * User: Andrew D. Robertson
 * Date: 17/03/2015
 * Time: 14:13
 */
public class DialogueTracker implements AutoCloseable {

    private Dialoguer dialoguer;
    private Map<String, Dialogue> dialogues;
    private Map<String, LocalDateTime> lastUpdated;
    private Duration trackingTimeLimit;
    private boolean logDialogues;
    private CompletedDialogueHandler cdHandler;

    public DialogueTracker(String jsonDefinition){
        this(Dialoguer.loadJson(jsonDefinition));
    }

    public DialogueTracker(File jsonDefinition) throws IOException {
        this(Dialoguer.loadJson(jsonDefinition));
    }

    public DialogueTracker(Dialoguer dialoguer){
        this.dialoguer = dialoguer;
        this.dialogues = new HashMap<>();
        lastUpdated = new HashMap<>();
        trackingTimeLimit = Duration.ofDays(1);
        logDialogues = false;
        cdHandler = null;
    }

    public void registerCompletedDialogueHandler(CompletedDialogueHandler h){
        cdHandler = h;
    }

    public void removeTrackingTimeLimit(){
        trackingTimeLimit = null;
    }

    public void setTrackingTimeLimit(Duration d){
        trackingTimeLimit = d;
    }

    public String getResponse(String dialogueId, String userMessage, User userData){
        return getResponse(dialogueId, userMessage, userData, trackingTimeLimit);
    }

    public String getResponse(String dialogueId, String userMessage, User userData, Duration trackedTimeLimit){
        Dialogue d;
        if (isTracked(dialogueId, trackedTimeLimit)){
            d = dialoguer.interpret(userMessage, userData, dialogues.get(dialogueId));
        } else {
            d = dialoguer.interpret(userMessage, userData, new Dialogue(dialogueId));
        }
        lastUpdated.put(dialogueId, LocalDateTime.now());
        dialogues.put(dialogueId, d);
        if (d.isComplete()) {
            if (logDialogues)
                log(dialogueId);
            if (cdHandler != null)
                cdHandler.handle(d);
            unTrackDialogue(dialogueId);
        }
        return d.isLastMessageByUser()? null : d.getLastMessage().getText();
    }

    public void unTrackDialogue(String id){
        dialogues.remove(id);
        lastUpdated.remove(id);
    }

    public boolean isTracked(String dialogueId) {
        return isTracked(dialogueId, trackingTimeLimit);
    }

    public boolean isTracked(String dialogueId, Duration timeLimit){
        return dialogues.containsKey(dialogueId) && (timeLimit == null || isTimeSinceLastUpdateLessThan(timeLimit, dialogueId));
    }

    public boolean isTracked(String dialogueID, long timeLimitInHours){
        return isTracked(dialogueID, Duration.ofHours(timeLimitInHours));
    }

    public boolean isTrackedNoTimeLimit(String dialogueId){
        return isTracked(dialogueId, null);
    }

    public boolean isTimeSinceLastUpdateLessThan(Duration d, String dialogueId){
        Duration timeSinceLastUpdate = Duration.between(lastUpdated.get(dialogueId), LocalDateTime.now()).abs();
        return timeSinceLastUpdate.compareTo(d) < 0;
    }

    public void log(String dialogueId){
        // TODO
    }

    public void logAll(){
        // TODO
    }

    @Override
    public void close() throws Exception {
        dialoguer.close();
        if (logDialogues)
            logAll();
    }

    public static interface CompletedDialogueHandler {
        public void handle(Dialogue d);
    }
}
