package uk.ac.susx.tag.dialoguer.dialogue.handling.factories;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.WikidataAnalyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;

import java.io.IOException;

/**
 * Created by User on 6/15/2015.
 */
public class WikidataHandlerFactory implements AnalyserFactory {
    @Override
    public Analyser readJson(String resourcePath) throws IOException {
        return new WikidataAnalyser();
    }

    @Override
    public String getName() {
        return "Wikidata";
    }

}
