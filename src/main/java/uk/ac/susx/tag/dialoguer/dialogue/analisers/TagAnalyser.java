package uk.ac.susx.tag.dialoguer.dialogue.analisers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 17/03/2015
 * Time: 14:18
 */
public class TagAnalyser implements Analyser {
    @Override
    public List<Intent> analise(String message, Dialogue dialogue) {
        return null;
    }

    @Override
    public String getName() {
        return "tag";
    }

    @Override
    public Analyser readJson(InputStream json) throws IOException {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
