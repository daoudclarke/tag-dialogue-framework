package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories;

import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.CancellationAnalyserStringMatching;

import java.io.IOException;

public class CancellationAnalyserStringMatchingFactory implements AnalyserFactory {

    @Override
    public String getName() {
        return "simple_cancel";
    }

    @Override
    public Analyser readJson(String resourcePath) throws IOException {
        return new CancellationAnalyserStringMatching();
    }
}
