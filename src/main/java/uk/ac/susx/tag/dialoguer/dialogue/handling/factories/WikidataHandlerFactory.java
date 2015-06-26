package uk.ac.susx.tag.dialoguer.dialogue.handling.factories;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.WikidataHandler;

import java.io.IOException;

/**
 * Created by User on 6/15/2015.
 */
public class WikidataHandlerFactory implements HandlerFactory {
    @Override
    public Handler readJson(String resourcePath) throws IOException {
        return Dialoguer.readObjectFromJsonResourceOrFile(resourcePath, WikidataHandler.class);
    }

    @Override
    public String getName() {
        return "wikidata";
    }

}
