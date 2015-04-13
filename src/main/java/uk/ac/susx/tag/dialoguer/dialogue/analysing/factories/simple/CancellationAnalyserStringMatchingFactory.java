package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.simple;

import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.simple.CancellationAnalyserStringMatching;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 13/04/2015
 * Time: 11:29
 */
public class CancellationAnalyserStringMatchingFactory implements AnalyserFactory {

    @Override
    public String getName() {
        return "simple_cancel";
    }

    @Override
    public Analyser readJson(File json) throws IOException {
        return new CancellationAnalyserStringMatching();
    }
}
