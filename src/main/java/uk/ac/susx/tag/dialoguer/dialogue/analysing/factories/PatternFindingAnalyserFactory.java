package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.PatternFindingAnalyser;

import java.io.IOException;

/**
 * Config file example:
 *
 *  {
 *    "regex": "(send|gift|present)",
 *    "intentName": "gift"
 *  }
 *
 * The above would create an analyser that fires the "gift" intent when it sees one of the three words "send", "gift", or "present".
 *
 * User: Andrew D. Robertson
 * Date: 16/04/2015
 * Time: 12:00
 */
public class PatternFindingAnalyserFactory implements AnalyserFactory{

    @Override
    public Analyser readJson(String resourcePath) throws IOException {
        return Dialoguer.readObjectFromJsonResourceOrFile(resourcePath, PatternFindingAnalyser.class);
    }

    @Override
    public String getName() {
        return "pattern_finder";
    }
}
