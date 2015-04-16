package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.PatternFindingAnalyser;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 16/04/2015
 * Time: 12:00
 */
public class PatternFindingAnalyserFactory implements AnalyserFactory{

    @Override
    public Analyser readJson(File json) throws IOException {
        return Dialoguer.readFromJsonFile(json, PatternFindingAnalyser.class);
    }

    @Override
    public String getName() {
        return "pattern_finder";
    }
}
