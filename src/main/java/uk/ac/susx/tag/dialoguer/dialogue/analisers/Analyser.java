package uk.ac.susx.tag.dialoguer.dialogue.analisers;

import com.google.common.collect.Sets;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 18:02
 */
public abstract class Analyser implements AutoCloseable {

    private static final Set<String> cancelMessages = Sets.newHashSet("nevermind", "exit", "changed my mind", "q", "quit", "bye", "cancel", "exit", "bye bye", "goodbye", "none of them", "restart", "forget that", "forget it", "cancel", "cancel that", "no bye", "stop", "quit");

    public abstract List<Intent> analise(String message, Dialogue dialogue);

    protected boolean isCancelIntent(String message){
        return cancelMessages.contains(message.toLowerCase());
    }
}
