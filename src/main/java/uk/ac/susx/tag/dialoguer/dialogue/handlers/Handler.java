package uk.ac.susx.tag.dialoguer.dialogue.handlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 15:00
 */
public interface Handler extends AutoCloseable {

    public Response handle(Dialogue dialogue);

    public String getName();

}
