package uk.ac.susx.tag.dialoguer.dialogue.handling.factories;

import com.google.gson.reflect.TypeToken;
import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.PaypalCheckinHandler;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by juliewe on 20/04/2015.
 */
public class PaypalCheckinHandlerFactory implements HandlerFactory{

    @Override
    public Handler readJson(String json) throws IOException {
        if (json == null||json.equals(""))
            return new PaypalCheckinHandler();
        else {

            return Dialoguer.readObjectFromJsonResourceOrFile(json, PaypalCheckinHandler.class);
        }
    }

    @Override
    public String getName() {
        return "paypal_checkin";
    }


}
