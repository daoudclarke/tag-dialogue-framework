package uk.ac.susx.tag.dialoguer.dialogue.analisers.simple;

import com.google.common.collect.Sets;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.SimplePatterns;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 31/03/2015
 * Time: 10:52
 */
public class CancellationAnalyserStringMatching implements CancellationAnalyser {

    // When a message wholly consists of any of these phrases, it is immediately considered a cancellation message
    public static Set<String> cancellationPhrases = Sets.newHashSet(
            "nevermind",
            "exit",
            "changed my mind",
            "i changed my mind",
            "i've changed my mind",
            "don't worry",
            "dont worry",
            "dont worry about it",
            "don't worry about it",
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

    @Override
    public boolean isCancellation(String userMessage) {
        // If after trimming unnecessary information from the message, it now exactly matches a cancellation phrase, then return true else false
        return cancellationPhrases.contains(SimplePatterns.stripAll(userMessage));
    }
}
