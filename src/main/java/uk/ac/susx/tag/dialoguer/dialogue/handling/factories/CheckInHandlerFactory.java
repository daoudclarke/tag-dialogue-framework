package uk.ac.susx.tag.dialoguer.dialogue.handling.factories;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.CheckInHandler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 20/04/2015
 * Time: 11:27
 */
public class CheckInHandlerFactory implements HandlerFactory
{
    @Override
    public Handler readJson(File json) throws IOException {
        if (json == null)
            return new CheckInHandler();
        else
            return Dialoguer.readFromJsonFile(json, CheckInHandler.class);
    }

    @Override
    public String getName() {
        return "check_in";
    }
}
