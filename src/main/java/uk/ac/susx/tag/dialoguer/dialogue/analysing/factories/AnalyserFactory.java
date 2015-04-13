package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories;

import org.reflections.Reflections;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * It is the responsibility of the AnalyserFactory to be able to create a new instance of a specific type of Analyser,
 * setting it up using a JSON setup file. It also must provide a name for that type of Analyser.
 *
 * User: Andrew D. Robertson
 * Date: 13/04/2015
 * Time: 11:17
 */
public interface AnalyserFactory {

    Analyser readJson(File json) throws IOException;

    String getName();
}
