package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.OutOfWitDomainAnalyser;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * See documentation in OutOfWitDomainAnalyser for function and purpose of such an analyser.
 *
 * Config example:
 *
 *  {
 *    "serverAccessToken": "QJG7W4SON6THNBPGMBQFPFZT5OKEBGVE",
 *    "ngramOrder": 3,
 *    "excludedIntents": ["other"]
 *  }
 *
 * Explanation:
 *
 *   serverAccessToken = the access token for the Wit.Ai instance with which you wish to create a language model
 *   modelName = an arbitrary string used for naming files that are created
 *
 *
 * User: Andrew D. Robertson
 * Date: 12/05/2015
 * Time: 13:32
 */
public class OutOfWitDomainAnalyserFactory implements AnalyserFactory {
    @Override
    public Analyser readJson(String resourcePath) throws IOException {
        OutOfDomainAnalyserDefinition def = Dialoguer.readObjectFromJsonResourceOrFile(resourcePath, OutOfDomainAnalyserDefinition.class);

        return new OutOfWitDomainAnalyser(def.ngramOrder, def.serverAccessToken, def.excludedIntents);
    }

    @Override
    public String getName() {
        return "out_of_wit_domain";
    }

    public static class OutOfDomainAnalyserDefinition {
        public int ngramOrder = 3;
        public String serverAccessToken = null;
        public Set<String> excludedIntents = new HashSet<>();
    }
}
