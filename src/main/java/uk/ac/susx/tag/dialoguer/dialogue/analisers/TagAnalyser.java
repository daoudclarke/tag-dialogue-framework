package uk.ac.susx.tag.dialoguer.dialogue.analisers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

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
    public void close() throws Exception {

    }
}
