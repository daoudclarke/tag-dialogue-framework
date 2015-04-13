package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 17/03/2015
 * Time: 14:18
 */
public class TagAnalyser extends Analyser {
    @Override
    public List<Intent> analise(String message, Dialogue dialogue) {
        return null;
    }

    @Override
    public AnalyserFactory getFactory() {
        return null;//TODO
    }

    @Override
    public void close() throws Exception {

    }
}
