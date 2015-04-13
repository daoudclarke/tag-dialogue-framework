package uk.ac.susx.tag.dialoguer.dialogue.components;

/**
 * An individual message within a Dialogue.
 *
 * A message can be from the user or from the system.
 *
 * If the message is from a user, it should be accompanied with user data (e.g. geo data).
 *
 * User the intents array on the Dialogue to track past user intents.
 *
 * User: Andrew D. Robertson
 * Date: 13/04/2015
 * Time: 15:09
 */

public class Message {

    private String text;
    private User userData;

    public Message(String text){
        this.text = text;
        this.userData = null;
    }

    public Message(String text, User userData) {
        this.text = text;
        this.userData = userData;
    }

    public String getText() {
        return text;
    }

    public User getUserData() {
        return userData;
    }

    public boolean isUserMessage(){
        return userData != null;
    }

    public boolean isSystemMessage(){
        return !isUserMessage();
    }
}
