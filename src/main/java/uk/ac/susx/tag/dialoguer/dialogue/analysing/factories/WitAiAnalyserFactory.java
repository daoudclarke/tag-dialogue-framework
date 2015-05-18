package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.OutOfWitDomainAnalyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.WitAiAnalyser;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Creates an instance of the WitAiAnalyser.
 *
 * The setup config should look like:
 *
 *  {
 *    "serverAccessToken" : "MLLG4RQ4CHCEL6I4O4T7SABCDXYEVBT5",
 *    "outOfDomainAnalyser" : {
 *        "ngramOrder" : 3,
 *        "excludedIntents" : [ "other" ]
 *    }
 *  }
 *
 * The out of domain analyser bit is optional.
 *
 * User: Andrew D. Robertson
 * Date: 13/04/2015
 * Time: 11:24
 */
public class WitAiAnalyserFactory implements AnalyserFactory {

    @Override
    public Analyser readJson(String resourcePath) throws IOException {
        return new WitAiAnalyser(Dialoguer.readObjectFromJsonResourceOrFile(resourcePath, WitAiAnalyserDefinition.class));
    }

    @Override
    public String getName() {
        return "wit.ai";
    }

    public static class WitAiAnalyserDefinition {

        public String serverAccessToken = null;
        public OutOfWitDomainAnalyserDefinition outOfDomainAnalyser = null;

        public boolean hasServerAccessToken() {return serverAccessToken != null; }
        public boolean hasOutOfDomainAnalyser() {return outOfDomainAnalyser != null;}
    }

    public static class OutOfWitDomainAnalyserDefinition {
        public int ngramOrder = 3;
        public Set<String> excludedIntents = new HashSet<>();
    }
}
