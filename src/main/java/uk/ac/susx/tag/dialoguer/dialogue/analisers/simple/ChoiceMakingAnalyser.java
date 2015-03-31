package uk.ac.susx.tag.dialoguer.dialogue.analisers.simple;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 31/03/2015
 * Time: 13:44
 */
public interface ChoiceMakingAnalyser {

    /**
     * Given a list of choices that were presented to the user, and the user response, determine whether or not
     * the user response was in fact making a choice.
     */
    public boolean isChoice(String userMessage, List<String> choices);

    /**
     * Given a list of choices that were presented to the user, and the user response, determine the index of the
     * choice that the user is most likely to have selected.
     */
    public int whichChoice(String userMessage, List<String> choices);

    /**
     * Given a user message and choices that were presented to him/her, did the user explicitly reject all the choices?
     */
    public boolean isNullChoice(String userMessage, List<String> choices);

    /**
     * Did the user make an affirmation in response to a yes/no question, or request for confirmation?
     */
    public boolean isConfirmed(String userMessage);
}
