package uk.ac.susx.tag.dialoguer;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.User;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 17/03/2015
 * Time: 14:13
 */
public class DialogueTracker {

    private Dialoguer dialoguer;
    private Map<String, Dialogue> dialogues;
    private Map<String, LocalDateTime> lastUpdated;
    private Duration trackedTimeLimit;

    public DialogueTracker(Dialoguer dialoguer){
        this.dialoguer = dialoguer;
        this.dialogues = new HashMap<>();
    }

    public String getResponse(String dialogueId, String userMessage, User userData){
        if (isTracked(dialogueId, trackedTimeLimit)){
            lastUpdated.put(dialogueId, LocalDateTime.now());

        }
        return null;
    }

    public void untrackDialogue(String id){
        dialogues.remove(id);
        lastUpdated.remove(id);
    }

    public boolean isTracked(String dialogueId) {
        return isTracked(dialogueId, null);
    }

    public boolean isTracked(String dialogueId, Duration timeLimit){
        return dialogues.containsKey(dialogueId) && (timeLimit == null || isTimeSinceLastUpdateLessThan(timeLimit, dialogueId));
    }

    public boolean isTracked(String dialogueID, long timeLimitInHours){
        return isTracked(dialogueID, Duration.ofHours(timeLimitInHours));
    }

    public boolean isTimeSinceLastUpdateLessThan(Duration d, String dialogueId){
        Duration timeSinceLastUpdate = Duration.between(lastUpdated.get(dialogueId), LocalDateTime.now()).abs();
        return timeSinceLastUpdate.compareTo(d) < 0;
    }

    public void log(String dialogueId){
        //TODO
    }
}
