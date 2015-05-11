package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.productSearchIntentHandlers;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.PaypalCheckinHandler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.ProductSearchHandler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        workingIntent.fillSlot(handleMessage(i,d,db));
        workingIntent.fillSlot(handleRecipient(i,d,db));
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
        Gson gson = new Gson();
        Type termMapType = new TypeToken<Map<String, List<String>>>(){}.getType();

        Map<String,List<String>> queryMap=new HashMap<>();
        Optional<String> termJson =i.getSlotValuesByType(ProductSearchHandler.productSlot).stream().findFirst();
        if(termJson.isPresent()){
            queryMap=gson.fromJson(termJson.get(),termMapType);
        }

        //first make generic searchstring
        String searchstring="";
        List<String> queries=queryMap.values().stream().map(valuelist->StringUtils.phrasejoin(valuelist)).collect(Collectors.toList());
        for(String query:queries){
            searchstring+=query;
        }

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
        System.err.println(d.peekTopIntent().toString());
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
        return new Response(focus,responseVariables);

    }

    public static Intent makeQueryMap(Intent i){
        if(i.getName().equals(ProductSearchHandler.buy)){//only works on this intent
            Map<String,List<String>> termMap = new HashMap<>();
            if(i.getSlotByType(ProductSearchHandler.witTitle)!=null){termMap.put(ProductSearchHandler.witTitle, i.getSlotValuesByType(ProductSearchHandler.witTitle));}
            if(i.getSlotByType(ProductSearchHandler.witAuthor)!=null){termMap.put(ProductSearchHandler.witAuthor,i.getSlotValuesByType(ProductSearchHandler.witAuthor));}
            if(!termMap.keySet().isEmpty()) {
                Gson gson = new Gson();
                i.fillSlot(ProductSearchHandler.productSlot, gson.toJson(termMap));
            }


        }
        return i;
    }

}
