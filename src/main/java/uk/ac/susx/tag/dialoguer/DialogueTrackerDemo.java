package uk.ac.susx.tag.dialoguer;

import uk.ac.susx.tag.dialoguer.dialogue.components.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 28/05/2015
 * Time: 13:05
 */
public class DialogueTrackerDemo extends DialogueTracker {

    public static final User clocktower = new User(50.823694, -0.143587, 3.0);
    public static final User waterstones = new User(50.823538, -0.143807, 3.0);
    public static final User bench = new User(50.823471, -0.143441, 3.0);
    public static final User indulge = new User(50.823838, -0.144005, 3.0);
    public static final User boots = new User(50.823732, -0.143193, 3.0);
    public static final User eat = new User(50.823589, -0.143933, 3.0);
    public static final User pret = new User(50.823609, -0.144029, 3.0);
    public static final User superdry = new User(50.823843, -0.143801, 3.0);
    public static final User quadrant = new User(50.8240815, -0.1434446, 3.0);

    private Map<String, User> dialogueLocations = new HashMap<>();

    public DialogueTrackerDemo(String resourcePath) throws IOException {
        super(resourcePath);
    }

    public String getResponse(String dialogueId, String userMessage, User userData){
        if (isMoveStatement(userMessage)){
            return moveDialogueLocationAndConfirm(dialogueId, userMessage, userData.getAttribute("name"));
        } else {
            return super.getResponse(dialogueId, userMessage, getUserData(dialogueId, userData.getAttribute("name")));
        }
    }

    private String moveDialogueLocationAndConfirm(String dialogueId, String userMessage, String userName){
        userMessage = userMessage.toLowerCase().trim();
        if (userMessage.contains("clock") || userMessage.contains("tower")){
            updateDialogueLocation(dialogueId, clocktower.newCopy().setAttribute("name", userName));
            return "You are now at the Clock Tower.";
        } else if (userMessage.contains("waterstones")){
            updateDialogueLocation(dialogueId, waterstones.newCopy().setAttribute("name", userName));
            return "You are now at Waterstones.";
        } else if (userMessage.contains("bench")){
            updateDialogueLocation(dialogueId, bench.newCopy().setAttribute("name", userName));
            return "You are now at Bench.";
        } else if (userMessage.contains("indulge")){
            updateDialogueLocation(dialogueId, indulge.newCopy().setAttribute("name", userName));
            return "You are now at Indulge.";
        } else if (userMessage.contains("boots")){
            updateDialogueLocation(dialogueId, boots.newCopy().setAttribute("name", userName));
            return "You are now at Boots.";
        } else if (userMessage.contains("eat")){
            updateDialogueLocation(dialogueId, eat.newCopy().setAttribute("name", userName));
            return "You are now at Eat.";
        } else if (userMessage.contains("pret")){
            updateDialogueLocation(dialogueId, pret.newCopy().setAttribute("name", userName));
            return "You are now at Pret a Manger.";
        } else if (userMessage.contains("superdry")){
            updateDialogueLocation(dialogueId, superdry.newCopy().setAttribute("name", userName));
            return "You are now at Superdry.";
        } else if (userMessage.contains("quadrant")){
            updateDialogueLocation(dialogueId, quadrant.newCopy().setAttribute("name", userName));
            return "You are now at The Quadrant.";
        } else {
            updateDialogueLocation(dialogueId, clocktower.newCopy().setAttribute("name", userName));
            return "You get lost, but manage to find your way back to the Clock Tower.";
        }
    }

    private void updateDialogueLocation(String dialogueId, User location){
        dialogueLocations.put(dialogueId, location);
    }

    private User getUserData(String dialogueId, String userName){
        if (!dialogueLocations.containsKey(dialogueId)){
            dialogueLocations.put(dialogueId, clocktower.newCopy().setAttribute("name", userName));
        } return dialogueLocations.get(dialogueId);
    }

    private boolean isMoveStatement(String userMessage){
        userMessage = userMessage.toLowerCase().trim();
        return userMessage.startsWith("go to") || userMessage.startsWith("walk to") || userMessage.startsWith("move to") || userMessage.startsWith("goto");
    }
}
