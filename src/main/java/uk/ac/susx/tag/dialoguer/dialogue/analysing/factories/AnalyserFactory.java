package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories;

import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 13/04/2015
 * Time: 11:17
 */
public interface AnalyserFactory {

    Analyser readJson(File json) throws IOException;

    String getName();
}
