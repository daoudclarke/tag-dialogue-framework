package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.HandlerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 15:00
 */
public abstract class Handler implements AutoCloseable {

    public abstract Response handle(Dialogue dialogue);

    public abstract HandlerFactory getFactory();

    public String getName(){
        return getFactory().getName();
    }
}
