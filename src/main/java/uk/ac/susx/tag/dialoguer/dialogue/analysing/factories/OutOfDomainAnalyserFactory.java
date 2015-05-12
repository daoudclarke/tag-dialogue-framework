package uk.ac.susx.tag.dialoguer.dialogue.analysing.factories;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.OutOfDomainAnalyser;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 12/05/2015
 * Time: 13:32
 */
public class OutOfDomainAnalyserFactory implements AnalyserFactory {
    @Override
    public Analyser readJson(String resourcePath) throws IOException {
        return Dialoguer.readObjectFromJsonResourceOrFile(resourcePath, OutOfDomainAnalyser.class);
    }

    @Override
    public String getName() {
        return "out_of_domain";
    }
}
