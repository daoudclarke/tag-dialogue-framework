package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.PaypalCheckinHandler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Merchant;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;

import java.util.List;

/**
 * Created by juliewe on 26/05/2015.
 */
public class RejectMethod implements Handler.ProblemHandler {
    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        return intents.stream().anyMatch(i->i.isName(Intent.no)||(i.isName(PaypalCheckinHandler.confirm)&&i.isSlotTypeFilledWith(PaypalCheckinHandler.yes_no_slot,"no")));
    }

    @Override
    public void handle(List<Intent> intents, Dialogue d, Object resource){
        //rejecting the merchant currently in working memory.  Move to rejection list
        handleReject(d);

        ProductMongoDB db;
        try {
            db = (ProductMongoDB) resource;
        } catch (ClassCastException e){
            throw new Dialoguer.DialoguerException("Resource should be mongo db", e);
        }

        List<Merchant> possibleMerchants;
        if(d.getFromWorkingMemory("location_list")==null) {
            possibleMerchants = LocMethod.filterRejected(LocMethod.findNearbyMerchants(db, d.getUserData()), d.getFromWorkingMemory("rejectedlist"));
        } else {
            possibleMerchants = LocMethod.matchNearbyMerchants(db, d.getUserData(), d);//will use workingmemory's location_list
        }

        LocMethod.processMerchantList(possibleMerchants, d,db);
    }



    public static void handleReject(Dialogue d){
        //rejecting the merchant currently in working memory.  Move to rejection list
        String rejectedlist=d.getFromWorkingMemory("rejectedlist");
        if(rejectedlist==null){rejectedlist="";}
        String rejected = d.getFromWorkingMemory("merchantId");
        if(rejected==null){rejected="";}
        rejectedlist+=rejected+" ";
        LocMethod.updateMerchant(null,d);
        d.putToWorkingMemory("rejectedlist",rejectedlist);
    }

}
