package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;
import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.PaypalCheckinHandler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Merchant;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;

import java.util.*;

/**
 * Created by juliewe on 21/04/2015.
 */
public class CheckinMethod implements Handler.ProblemHandler {

    public void handle(List<Intent> intents,Dialogue d, Object resource){
        //generate response to request Location
        ProductMongoDB db;
        try {
            db = (ProductMongoDB) resource;
        } catch (ClassCastException e){
            throw new Dialoguer.DialoguerException("Resource should be mongo db", e);
        }

        d.putToWorkingMemory("rejectionlist",null); // in case restarting
        List<Merchant> possibleMerchants = LocMethod.findNearbyMerchants(db, d.getUserData());
        LocMethod.processMerchantList(possibleMerchants,d,db);
    }

    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        return intents.stream().anyMatch(i->i.isName(PaypalCheckinHandler.checkinIntent));
    }
}
