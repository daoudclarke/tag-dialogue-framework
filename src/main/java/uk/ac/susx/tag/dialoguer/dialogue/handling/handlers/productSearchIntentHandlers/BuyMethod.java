package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.productSearchIntentHandlers;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.antlr.misc.MultiMap;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.User;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.PaypalCheckinHandler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.ProductSearchHandler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Merchant;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Product;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by juliewe on 11/05/2015.
 *
 * TODO://Make sure that recipients are not case-sensitive when checked
 */
public class BuyMethod implements Handler.IntentHandler{

    public static final int searchradius=50;
    public static final int limit=3;

    public Response handle(Intent i, Dialogue d, Object resource){
        //grab db
        ProductMongoDB db = ProductSearchHandler.castDB(resource);

        //intent to buy.  Should have been auto-filled with product and recipient.
        //Need to check each slot and add to working memory
        //need to check what we have in workingintents already and clear
        //System.err.println(i.toString());

        Intent workingIntent = new Intent(ProductSearchHandler.buy);
        //d.pushFocus("confirm_buy");
        workingIntent.fillSlot(handleMessage(i, d, db));
        workingIntent.fillSlot(handleRecipient(i, d, db));
        workingIntent.fillSlots(handleProduct(i, d, db));
        d.addToWorkingIntents(workingIntent);
        return ProductSearchHandler.processStack(d, db);

    }

    public static Intent.Slot handleMessage(Intent i,Dialogue d, ProductMongoDB db){
        List<String> messages = i.getSlotValuesByType(ProductSearchHandler.messageSlot);
        String messagestring=StringUtils.detokenise(messages);
        if(messagestring.equals("none")){
            d.pushFocus("confirm_buy_no_message");
        } else {
            messagestring="\""+messagestring+"\"";
            d.pushFocus("confirm_buy");
        }
        return new Intent.Slot(ProductSearchHandler.messageSlot,messagestring,0,0);
    }
    public static Intent.Slot handleRecipient(Intent i,Dialogue d, ProductMongoDB db){
        List<String> recipients = i.getSlotValuesByType(ProductSearchHandler.recipientSlot);
        String recipientstring = StringUtils.detokenise(recipients);
        Intent.Slot s=null;
        if(ProductSearchHandler.recipients.contains(recipientstring.toLowerCase())) {  //better test for recipient required - db matching
            s = new Intent.Slot(ProductSearchHandler.recipientSlot, recipientstring, 0, 0);
        } else {
            d.pushFocus("unknown_recipient");
            //d.putToWorkingMemory("recipient",recipientstring);
            s = new Intent.Slot(ProductSearchHandler.recipientSlot, recipientstring, 0, 0);
        }
        return s;
    }
    public static List<Intent.Slot> handleProduct(Intent i, Dialogue d,ProductMongoDB db){
        //takes the queryMap in the productSlot of the given intent and finds matches in the database which have not been rejected
        //returns a list of slots to be added to productIdSlot
        //processProductList will have added the correct focus to the stack with regard to the length of this list

        //basic db look up

        Map<String, List<String>> queryMap = retrieveQueryMap(i);


        //first make generic searchstring
        String searchstring="";
        List<String> queries=queryMap.values().stream().map(valuelist->StringUtils.phrasejoin(valuelist)).collect(Collectors.toList());
        for(String query:queries){
            searchstring+=query;
        }

        System.err.println(searchstring);
        List<Merchant> merchants = findNearbyMerchants(db,d.getUserData());
        System.err.println("Nearby merchants: "+merchants.size());
        String rejected = d.getFromWorkingMemory("rejected"+ProductSearchHandler.productIdSlot);
        List<String> rejectedlist = new ArrayList<>();
        if(rejected!=null){
            for(String id:rejected.split(" ")){
                rejectedlist.add(id);
            }
        }
        List<Product> products = filterRejected(db.productQueryWithMerchants(searchstring,merchants,new HashSet<>(),limit+rejectedlist.size()),rejectedlist);
        System.err.println("Products found: "+products.size());
        List<Intent.Slot> slotlist = new ArrayList<>();
        //Intent.Slot s = new Intent.Slot(ProductSearchHandler.productSlot,searchstring,0,0);
        slotlist.addAll(i.getSlotByType(ProductSearchHandler.productSlot));
        slotlist.addAll(processProductList(products,d,db));
        return slotlist;
    }

    public static List<Intent.Slot> processProductList(List<Product> products, Dialogue d, ProductMongoDB db){
        //a list of products has been found which match query.  What to do with them?
        List<Intent.Slot> slotlist=new ArrayList<>();

        if(products.size()==0){
            d.pushFocus("respecify_product");
        } else {
            if(products.size()==1){
                slotlist.add(new Intent.Slot(ProductSearchHandler.productIdSlot,products.get(0).getProductId(),0,0));
                //leave focus as is: confirm_completion

            } else {
                slotlist=products.stream().map(p->new Intent.Slot(ProductSearchHandler.productIdSlot,p.getProductId(),0,0)).collect(Collectors.toList());
                d.pushFocus("choose_product");
            }
        }
        return slotlist;
    }



    public static Intent makeQueryMap(Intent i){
        if(i.getName().equals(ProductSearchHandler.buy)||i.getName().equals(ProductSearchHandler.confirmProduct)){//only works on this intent
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

    public static Map<String,List<String>> retrieveQueryMap(Intent i){
        Map<String,List<String>> queryMap=new HashMap<>();
        Map<String, List<String>> aMap;
        Gson gson = new Gson();
        Type termMapType = new TypeToken<Map<String, List<String>>>(){}.getType();
        //System.err.println(i.getSlotValuesByType(ProductSearchHandler.productSlot).stream().findFirst());
        List<String> termJsonList =i.getSlotValuesByType(ProductSearchHandler.productSlot);
        if(!termJsonList.isEmpty()){
            for(String termJson:termJsonList) {
                aMap=gson.fromJson(termJson, termMapType);
                for(String key:aMap.keySet()){
                    List<String> current = queryMap.getOrDefault(key, Lists.newArrayList());
                    current.addAll(aMap.get(key));
                    queryMap.put(key,current);
                }
            }
        }
        return queryMap;
    }

    public static List<Merchant> findNearbyMerchants(ProductMongoDB db, User user){
        return db.merchantQueryByLocation(user.getLatitude(), user.getLongitude(), searchradius, 0);
    }


    public static List<Product> filterRejected(List<Product> products, List<String> rejectedIds){
        List<Product> filtered= products.stream().filter(product->!rejectedIds.contains(product.getProductId())).collect(Collectors.toList());
        if(filtered.size()>limit){
            return filtered.subList(0,limit);
        } else {
            return filtered;
        }
    }
}
