package uk.ac.susx.tag.dialoguer.dialogue.analisers;

import com.google.common.collect.Sets;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 18:02
 */
public interface  Analyser extends AutoCloseable {

    public abstract List<Intent> analise(String message, Dialogue dialogue);

    public String getName();
}
