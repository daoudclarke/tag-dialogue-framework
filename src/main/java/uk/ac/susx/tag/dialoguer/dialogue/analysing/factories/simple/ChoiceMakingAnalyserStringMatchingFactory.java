package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.simple;

import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.simple.ChoiceMakingAnalyserStringMatching;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;

import java.io.File;
import java.io.IOException;

public class ChoiceMakingAnalyserStringMatchingFactory implements AnalyserFactory{

    @Override
    public String getName() {
        return "simple_choice";
    }

    @Override
    public Analyser readJson(File json) throws IOException {
        return new ChoiceMakingAnalyserStringMatching(0.5);
    }
}
