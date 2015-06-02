package uk.ac.susx.tag.dialoguer.dialogue.handling.factories;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.TaxiServiceHandler;

import java.io.IOException;

/**
 * Created by juliewe on 19/05/2015.
 */
public class TaxiServiceHandlerFactory implements HandlerFactory{
    @Override
    public Handler readJson(String resourcePath) throws IOException {
            TaxiServiceHandler h = Dialoguer.readObjectFromJsonResourceOrFile(resourcePath, TaxiServiceHandler.class);
            h.setupDatabase();
            return h;

    }

    @Override
    public String getName() {
        return "taxi_service";
    }
}
