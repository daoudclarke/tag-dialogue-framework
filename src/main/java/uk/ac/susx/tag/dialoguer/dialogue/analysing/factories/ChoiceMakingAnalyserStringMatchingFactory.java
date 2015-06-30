package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.ChoiceMakingAnalyserStringMatching;

import java.io.IOException;

/**
 * No config file necessary.
 */
public class ChoiceMakingAnalyserStringMatchingFactory implements AnalyserFactory{

    @Override
    public String getName() {
        return "simple_choice";
    }

    public Analyser readJson(String resourcePath) throws IOException {
        if (resourcePath == null) {
            return new ChoiceMakingAnalyserStringMatching(0.5);
        }
        return new ChoiceMakingAnalyserStringMatching(Dialoguer.readObjectFromJsonResourceOrFile(resourcePath, ChoiceMakingAnalyserStringMatching.class));
    }
}
