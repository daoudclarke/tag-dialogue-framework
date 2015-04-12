package uk.ac.susx.tag.dialoguer.dialogue.analisers.simple;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import uk.ac.susx.tag.dialoguer.dialogue.analisers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.SimplePatterns;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 31/03/2015
 * Time: 10:52
 */
public class CancellationAnalyserStringMatching implements Analyser {

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
    public String getName() {
        return "simple_cancel";
    }

    @Override
    public Analyser readJson(InputStream json) throws IOException {
        return new CancellationAnalyserStringMatching();
    }

    @Override
    public void close() throws Exception {
        // No resources to close
    }
}
