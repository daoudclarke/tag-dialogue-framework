package uk.ac.susx.tag.dialoguer.dialogue.handling.factories;

import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;

import java.io.IOException;

/**
 * It is the responsibility of the HandlerFactory to be able to create a new instance of a specific type of Handler,
 * setting it up using a JSON setup file. It also must provide a name for that type of Handler.
 *
 * User: Andrew D. Robertson
 * Date: 13/04/2015
 * Time: 11:36
 */
public interface HandlerFactory {

    /**
     * Open the file. Expect that it is in JSON format. So use Gson to deserialise it.
     * Create and return an instance of the appropriate Handler, with the settings in the JSON file.
     */
    Handler readJson(String resourcePath) throws IOException;

    /**
     * Return a simple name for this type of handling task.
     */
    String getName();
}
