package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Try to detemine whether the user is attempting to cancel a dialogue.
 *
 * Produces the default cancel intent. See Intent documentation.
 *
 * User: Andrew D. Robertson
 * Date: 31/03/2015
 * Time: 10:52
 */
public class CancellationAnalyserStringMatching extends Analyser {

    // When a message wholly consists of any of these phrases, it is immediately considered a cancellation message
    private static final String[] cancellationPhraseArray = {
            "nevermind",
            "exit",
            "changed my mind",
            "i changed my mind",
            "ive changed my mind",
            "dont worry",
            "dont worry about it",
            "q",
            "quit",
            "bye",
            "cancel",
            "cancel that",
            "bye bye",
            "goodbye",
            "restart",
            "forget that",
            "forget it",
            "no bye",
            "stop",
            "end"};
    public static Set<String> cancellationPhrases =
            new HashSet<>(Arrays.asList(cancellationPhraseArray));


    public boolean isCancellation(Dialogue d) {
        // If after trimming unnecessary information from the message, it now exactly matches a cancellation phrase, then return true else false
        return cancellationPhrases.contains(d.getFromWorkingMemory("stripped"));
    }

    @Override
    public List<Intent> analyse(String message, Dialogue d) {
        return isCancellation(d)? Intent.buildCancelIntent(message).toList() : new ArrayList<Intent>();
    }

//    @Override
//    public AnalyserFactory getFactory() {
//        return new CancellationAnalyserStringMatchingFactory();
//    }

    @Override
    public void close() throws Exception {
        // No resources to close
    }
}
