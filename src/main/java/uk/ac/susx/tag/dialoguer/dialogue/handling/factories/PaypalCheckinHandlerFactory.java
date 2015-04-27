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
        if (json == null||json.equals("")){
            PaypalCheckinHandler h = new PaypalCheckinHandler();
            h.setupDatabase();
            return h;
        }
        else {
//            Map<String, String> config =  Dialoguer.readObjectFromJsonResourceOrFile(json,  new TypeToken<Map<String, String>>(){}.getType());
//            return new PaypalCheckinHandler(config.get("dbHost"), Integer.parseInt(config.get("dbPort")), config.get("dbName"));
            PaypalCheckinHandler h = Dialoguer.readObjectFromJsonResourceOrFile(json, PaypalCheckinHandler.class);
            h.setupDatabase();
            return h;
        }

}

    @Override
    public String getName() {
        return "paypal_checkin";
    }


}
