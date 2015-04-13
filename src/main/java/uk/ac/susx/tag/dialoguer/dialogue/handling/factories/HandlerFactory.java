package uk.ac.susx.tag.dialoguer.dialogue.handling.factories;

import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 13/04/2015
 * Time: 11:36
 */
public interface HandlerFactory {

    Handler readJson(File json) throws IOException;

    String getName();
}
