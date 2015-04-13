package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.simple;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.simple.CancellationAnalyserStringMatchingFactory;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

import java.util.ArrayList;
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
    public static Set<String> cancellationPhrases = Sets.newHashSet(
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
            "none",
            "restart",
            "forget that",
            "forget it",
            "no bye",
            "stop"
    );

    public boolean isCancellation(Dialogue d) {
        // If after trimming unnecessary information from the message, it now exactly matches a cancellation phrase, then return true else false
        return cancellationPhrases.contains(d.getFromWorkingMemory("stripped"));
    }

    @Override
    public List<Intent> analise(String message, Dialogue d) {
        return isCancellation(d)? Lists.newArrayList(Intent.buildCancelIntent(message)) : new ArrayList<>();
    }

    @Override
    public AnalyserFactory getFactory() {
        return new CancellationAnalyserStringMatchingFactory();
    }

    @Override
    public void close() throws Exception {
        // No resources to close
    }
}
