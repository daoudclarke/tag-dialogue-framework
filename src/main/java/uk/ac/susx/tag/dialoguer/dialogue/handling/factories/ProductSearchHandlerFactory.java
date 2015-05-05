package uk.ac.susx.tag.dialoguer.dialogue.handling.factories;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.PaypalCheckinHandler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.ProductSearchHandler;

import java.io.IOException;

/**
 * Created by juliewe on 05/05/2015.
 */
public class ProductSearchHandlerFactory implements HandlerFactory{



    @Override
    public Handler readJson(String json) throws IOException {
        if (json == null||json.equals("")){
            ProductSearchHandler h = new ProductSearchHandler();
            h.setupDatabase();
            return h;
        }
        else {
            ProductSearchHandler h = Dialoguer.readObjectFromJsonResourceOrFile(json, ProductSearchHandler.class);
            h.setupDatabase();
            return h;
        }

    }

    @Override
    public String getName() {
        return "product_search";
    }

}
