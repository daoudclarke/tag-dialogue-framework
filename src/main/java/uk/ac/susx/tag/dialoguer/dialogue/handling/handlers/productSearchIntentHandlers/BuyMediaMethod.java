package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.productSearchIntentHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.ProductSearchHandler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;

/**
 * Created by juliewe on 11/05/2015.
 */
public class BuyMediaMethod implements Handler.IntentHandler{
    public Response handle(Intent i, Dialogue d, Object resource){
        //grab db
        ProductMongoDB db = ProductSearchHandler.castDB(resource);

        //appears to be new request to buy media.  Create intent


        Response r = new Response("unknown");
        return r;

    }

    private void processStack(Intent i, Dialogue d, ProductMongoDB db){


    }

}
