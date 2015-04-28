package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories;

import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;

import java.io.IOException;

/**
 * It is the responsibility of the AnalyserFactory to be able to create a new instance of a specific type of Analyser,
 * setting it up using a JSON setup file. It also must provide a name for that type of Analyser.
 *
 * User: Andrew D. Robertson
 * Date: 13/04/2015
 * Time: 11:17
 */
public interface AnalyserFactory {

    /**
     * Open the file. Expect that it is in JSON format. So use Gson to deserialise it.
     * Create and return an instance of the appropriate Analyser, with the settings in the JSON file.
     */
    Analyser readJson(String resourcePath) throws IOException;

    /**
     * Return a simple name for this type of analysis.
     */
    String getName();
}
