package uk.ac.susx.tag.dialoguer.dialogue.handling.factories;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.RuleBasedHandler;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 30/04/2015
 * Time: 16:44
 */
public class RuleBasedHandlerFactory implements HandlerFactory {
    @Override
    public Handler readJson(String resourcePath) throws IOException {
        return Dialoguer.readObjectFromJsonResourceOrFile(resourcePath, RuleBasedHandler.class);
    }

    @Override
    public String getName() {
        return "rule_based";
    }
}
