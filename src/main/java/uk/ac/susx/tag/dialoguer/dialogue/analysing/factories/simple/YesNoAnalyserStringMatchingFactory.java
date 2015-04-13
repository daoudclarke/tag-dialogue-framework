package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.simple;

import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.simple.YesNoAnalyserStringMatching;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 13/04/2015
 * Time: 11:27
 */
public class YesNoAnalyserStringMatchingFactory implements AnalyserFactory{

    @Override
    public String getName() {
        return "yes_no_simple";
    }

    @Override
    public Analyser readJson(File json) throws IOException {
        return new YesNoAnalyserStringMatching();
    }
}
