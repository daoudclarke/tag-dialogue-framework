package uk.ac.susx.tag.dialoguer.dialogue.handling.factories;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.CheckInHandler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.PaypalCheckinHandler;

import java.io.File;
import java.io.IOException;

/**
 * Created by juliewe on 20/04/2015.
 */
public class PaypalCheckinHandlerFactory implements HandlerFactory{

    @Override
    public Handler readJson(File json) throws IOException {
        if (json == null)
            return new PaypalCheckinHandler();
        else
            return Dialoguer.readFromJsonFile(json, PaypalCheckinHandler.class);
    }

    @Override
    public String getName() {
        return "paypal_checkin";
    }


}
