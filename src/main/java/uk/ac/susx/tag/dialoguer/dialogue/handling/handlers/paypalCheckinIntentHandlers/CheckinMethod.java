package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Merchant;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by juliewe on 21/04/2015.
 */
public class CheckinMethod implements Handler.IntentHandler {

    public Response handle(Intent i,Dialogue d, Object resource){
        //generate response to request Location
        ProductMongoDB db=null;
        if (resource instanceof ProductMongoDB){
            db=(ProductMongoDB) resource;
        }

        List<Merchant> possibleMerchants = LocMethod.findNearbyMerchants(db, d.getUserData());

        LocMethod.processMerchantList(possibleMerchants,d);
        return new Response("");



    }


}
