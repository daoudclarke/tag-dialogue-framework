package uk.ac.susx.tag.dialoguer;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;

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

    public DialogueTracker(Dialoguer dialoguer){
        this.dialoguer = dialoguer;
        this.dialogues = new HashMap<>();
    }

    public String getResponse(String id, String userMessage){
        return null;
    }

    public void untrackDialogue(String id){
        //TODO
    }

    public boolean isTracked(String id) {
        return false;

    }

    public boolean isTracked(String dialogueId, Duration timeLimit, boolean removeIfExpired){
        if (dialogues.containsKey(dialogueId)){
            if (timeLimit == null || isTimeSinceLastUpdateLessThan(timeLimit, dialogueId)){
                return true;
            } else {
                if (removeIfExpired){
                    untrackDialogue(dialogueId);
                }
                return false;
            }
        } return false;
    }

    public boolean isTimeSinceLastUpdateLessThan(Duration d, String dialogueId){
        Duration timeSinceLastUpdate = Duration.between(lastUpdated.get(dialogueId), LocalDateTime.now()).abs();
        return timeSinceLastUpdate.compareTo(d) < 0;
    }

    public void log(String dialogueId){
        //TODO
    }

    public static void main(String[] args){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);
        LocalDateTime fourWeeksAgo = LocalDateTime.now().minusWeeks(4);

        Duration d2 = Duration.between(now, twoWeeksAgo).abs();
        Duration d4 = Duration.between(now, fourWeeksAgo).abs();

        System.out.println(d2.compareTo(d4));



        System.out.println("Done");
    }
}
