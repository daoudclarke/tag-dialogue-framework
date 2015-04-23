package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.WitAiAnalyser;

import java.io.IOException;

/**
 * Creates an instance of the WitAiAnalyser.
 *
 * User: Andrew D. Robertson
 * Date: 13/04/2015
 * Time: 11:24
 */
public class WitAiAnalyserFactory implements AnalyserFactory {

    @Override
    public Analyser readJson(String resourcePath) throws IOException {
        return Dialoguer.readObjectFromJsonResourceOrFile(resourcePath, WitAiAnalyser.class);
    }

    @Override
    public String getName() {
        return "wit.ai";
    }
}
