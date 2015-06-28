package uk.ac.susx.tag.dialoguer.dialogue.handling.factories;

import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.InteractiveHandler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;

import java.io.IOException;

/**
 * Created by Daniel Saska on 6/25/2015.
 */
public class InteractiveHandlerFactory implements HandlerFactory {
    @Override
    public Handler readJson(String resourcePath) throws IOException {
        return new InteractiveHandler();//TODO
    }

    @Override
    public String getName() {
        return "question_handler";
    }
}
