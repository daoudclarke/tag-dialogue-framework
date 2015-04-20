package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.WitAiAnalyser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Creates an instance of the WitAiAnalyser.
 *
 * User: Andrew D. Robertson
 * Date: 13/04/2015
 * Time: 11:24
 */
public class WitAiAnalyserFactory implements AnalyserFactory {

    @Override
    public Analyser readJson(File json) throws IOException {
        return Dialoguer.readFromJsonFile(json, WitAiAnalyser.class);
    }

    @Override
    public String getName() {
        return "wit.ai";
    }
}
