package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories;

import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.YesNoAnalyserStringMatching;

import java.io.IOException;

public class YesNoAnalyserStringMatchingFactory implements AnalyserFactory{

    @Override
    public String getName() {
        return "simple_yes_no";
    }

    @Override
    public Analyser readJson(String resourcePath) throws IOException {
        return new YesNoAnalyserStringMatching();
    }
}
