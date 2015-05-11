package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.productSearchIntentHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.PaypalCheckinHandler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.ProductSearchHandler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by juliewe on 11/05/2015.
 */
public class BuyMethod implements Handler.IntentHandler{
    public Response handle(Intent i, Dialogue d, Object resource){
        //grab db
        ProductMongoDB db = ProductSearchHandler.castDB(resource);

        //intent to buy.  Should have been auto-filled with product and recipient.
        //Need to check each slot and add to working memory
        //need to check what we have in workingintents already and clear

        Intent workingIntent = new Intent(ProductSearchHandler.buy);
        d.pushFocus("confirm_buy");
        handleMessage(i,d,db);
        handleRecipient(i,d,db);
        workingIntent.fillSlot(handleProduct(i, d, db));
        d.addToWorkingIntents(workingIntent);
        return processStack(d,db);

    }

    public Intent.Slot handleMessage(Intent i,Dialogue d, ProductMongoDB db){
        List<String> messages = i.getSlotValuesByType(ProductSearchHandler.messageSlot);
        String messagestring=StringUtils.detokenise(messages);
        if(messagestring.equals("none")){
            d.pushFocus("confirm_buy_no_message");
        }
        return new Intent.Slot(ProductSearchHandler.messageSlot,messagestring,0,0);
    }
    public Intent.Slot handleRecipient(Intent i,Dialogue d, ProductMongoDB db){
        List<String> recipients = i.getSlotValuesByType("recipient");
        String recipientstring = StringUtils.detokenise(recipients);
        Intent.Slot s=null;
        if(ProductSearchHandler.recipients.contains(recipientstring)) {  //better test for recipient required - db matching
            s = new Intent.Slot(ProductSearchHandler.recipientSlot, recipientstring, 0, 0);
        } else {
            d.pushFocus("unknown_recipient");
            d.putToWorkingMemory("recipient",recipientstring);
        }
        return s;
    }
    public Intent.Slot handleProduct(Intent i, Dialogue d,ProductMongoDB db){
        //basic db look up
        List<String> queries = i.getSlotValuesByType("title");
        String searchstring=StringUtils.detokenise(queries);
        System.err.println(searchstring);
        Intent.Slot s = new Intent.Slot(ProductSearchHandler.productSlot,searchstring,0,0);
        return s;
    }

    private Response processStack(Dialogue d, ProductMongoDB db){
        String focus="unknown";
        if (!d.isEmptyFocusStack()) {
            focus = d.popTopFocus();
        }
        Map<String, String> responseVariables = new HashMap<>();
        switch(focus) {
            case "confirm_buy":
                responseVariables.put(ProductSearchHandler.productSlot, StringUtils.detokenise(d.peekTopIntent().getSlotValuesByType(ProductSearchHandler.productSlot)));
                responseVariables.put(ProductSearchHandler.recipientSlot,StringUtils.detokenise(d.peekTopIntent().getSlotValuesByType(ProductSearchHandler.recipientSlot)));
                responseVariables.put(ProductSearchHandler.messageSlot,StringUtils.detokenise(d.peekTopIntent().getSlotValuesByType(ProductSearchHandler.messageSlot)));
                break;
            case "confirm_buy_no_message":
                responseVariables.put(ProductSearchHandler.productSlot, StringUtils.detokenise(d.peekTopIntent().getSlotValuesByType(ProductSearchHandler.productSlot)));
                responseVariables.put(ProductSearchHandler.recipientSlot,StringUtils.detokenise(d.peekTopIntent().getSlotValuesByType(ProductSearchHandler.recipientSlot)));
                break;
            case "unknown_recipient":
                break;
            case "unknown_product":
                break;
        }
        return new Response(focus);

    }

}
