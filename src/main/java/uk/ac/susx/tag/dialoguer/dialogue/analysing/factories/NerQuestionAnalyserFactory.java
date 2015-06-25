package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.NerQuestionAnalyser;

import java.io.IOException;

/**
 * Created by User on 6/18/2015.
 */
public class NerQuestionAnalyserFactory implements AnalyserFactory {
    @Override
    public Analyser readJson(String resourcePath) throws IOException {
        return new NerQuestionAnalyser(Dialoguer.readObjectFromJsonResourceOrFile(resourcePath, NerQuestionAnalyser.class));
    }

    @Override
    public String getName() {
        return "ner_question";
    }
}
