package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;


import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.User;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.PaypalCheckinHandler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Merchant;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Product;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductSet;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;
import java.util.*;


/**
 * Created by juliewe on 21/04/2015.
 */
public class LocMethod implements Handler.ProblemHandler {

    public static final int searchradius = 50;
    public static final int limit = 50;


    public boolean isInHandleableState(List<Intent> intents, Dialogue d){
        return intents.stream().anyMatch(i -> PaypalCheckinHandler.locIntents.contains(i.getName()));
    }

    public Response handle(List<Intent> intents, Dialogue d, Object resource){
        //we think that the user message has some information about location
        //System.err.println("Using problem handler: locMethod()");
        for(Intent i:intents){
            if(PaypalCheckinHandler.locIntents.contains(i.getName())){
                handleLocation(i,d,resource); // first matching intent
            }
        }
      //  intents.stream().filter(i->PaypalCheckinHandler.locIntents.contains(i.getName()))
        //        .findFirst()

        return processStack(d);

    }

    public static void handleLocation(Intent i, Dialogue d, Object resource){
        //DialogueTracker.logger.log(Level.INFO, "Im in handleLocation");

        ProductMongoDB db;
        try {
            db = (ProductMongoDB) resource;
        } catch (ClassCastException e){
            throw new Dialoguer.DialoguerException("Resource should be mongo db", e);
        }

        //possible confirm slot
        Collection<Intent.Slot> answers = i.getSlotByType(PaypalCheckinHandler.yes_no_slot);
        boolean accept =answers.stream().anyMatch(answer->answer.value.equals("yes")); //are any of the answers "yes"
        if(!accept){//need to add the current selection to rejected_list

            ConfirmMethod.handleReject(d);
        }

        //definite location slot
        Collection<Intent.Slot> locations = i.getSlotByType(PaypalCheckinHandler.locationSlot);
        ArrayList<String> location_list = new ArrayList<>();
        for(Intent.Slot location: locations){
            location_list.add(location.value);

        }

        List<Merchant> possibleMerchants = matchNearbyMerchants(location_list, db, d.getUserData(), d);

        if(accept){
            //check that the accepted location is in possiblemerchants
            boolean match=false;
            for(Merchant m:possibleMerchants){
                if(m.getMerchantId().equals(d.getFromWorkingMemory("merchantId"))){
                    match=true;
                }
            }
            if(!match){
                //problem - accepted merchant does not appear to match the location given
                d.pushFocus("reconfirm_loc");
                accept=false;
            }
        }else {
            processMerchantList(possibleMerchants, d);
        }
        if(accept){//still accepting after checking possible location
            ConfirmMethod.handleAccept(d);
        }

    }

    public static void processMerchantList(List<Merchant> possibleMerchants, Dialogue d){

        if(possibleMerchants.size()==0){
            if(d.getFromWorkingMemory("location_list")==null){
                d.pushFocus("request_location");
            } else {
                d.pushFocus("repeat_request_loc");
            }

        } else {
            if(possibleMerchants.size()==1){

                d.pushFocus("confirm_loc");
                updateMerchant(possibleMerchants.get(0),d);

            }
            else{
                //may want to do something different if multiple merchants returned but currently assume first is best and just offer this one
                d.pushFocus("confirm_loc");
                updateMerchant(possibleMerchants.get(0),d);


            }
        }

    }


    public static void updateMerchant(Merchant m, Dialogue d){
        if(m==null){
            d.putToWorkingMemory("merchantId","");
            d.putToWorkingMemory("merchantName","");
        } else {
            d.putToWorkingMemory("merchantId", m.getMerchantId());
            d.putToWorkingMemory("merchantName", m.getInfo(d.getUserData().getLocationData()));
        }

    }

    public static List<Merchant> findNearbyMerchants(ProductMongoDB db, User user){

        return db.merchantQueryByLocation(user.getLatitude(),user.getLongitude(),searchradius,limit);


    }

    public static List<Merchant> filterRejected(List<Merchant> merchants, String rejectedlist){
        //System.err.println("rejected: "+rejectedlist);
        if(rejectedlist==null){
            return merchants;
        } else {
            String[] rejected = rejectedlist.split(" ");
            List<Merchant> newmerchants = new ArrayList<>();
            for (Merchant m : merchants) {
                boolean reject=false;
                for (String r : rejected) {
                    if (m.getMerchantId().equals(r)) {
                        reject=true;

                    }
                }
                if(!reject){
                    newmerchants.add(m);
                }
            }
            return newmerchants;
        }
    }

    public static List<Merchant> matchNearbyMerchants(List<String> location_list, ProductMongoDB db,User user, Dialogue d){
        List<Merchant> merchants = filterRejected(findNearbyMerchants(db, user), d.getFromWorkingMemory("rejectedlist"));
        d.putToWorkingMemory("location_list",StringUtils.phrasejoin(location_list));
        merchants.retainAll(db.merchantQuery(StringUtils.phrasejoin(location_list)));
        if(merchants.size()==0){
            merchants = findNearbyMerchants(db,user);
            merchants.retainAll(db.merchantQuery(StringUtils.detokenise(location_list)));
            d.putToWorkingMemory("location_list",StringUtils.detokenise(location_list));
        }
        if(merchants.size()==0){
            merchants=productMatchNearbyMerchants(location_list,db,user, d);
        }
        return merchants;

    }

    public static List<Merchant> matchNearbyMerchants(ProductMongoDB db,User user, Dialogue d){
        //used when location_list is retrieved from working memory

        List<Merchant> merchants = filterRejected(findNearbyMerchants(db, user), d.getFromWorkingMemory("rejectedlist"));
        merchants.retainAll(db.merchantQuery(d.getFromWorkingMemory("location_list")));
        if(merchants.size()==0){//try a product match instead
            merchants=productMatchNearbyMerchants(db,user, d);
        }
        return merchants;

    }

    public static List<Merchant> productMatchNearbyMerchants(List<String> location_list, ProductMongoDB db, User user, Dialogue d){

        List<Merchant> merchants;
        try{//if one merchant under consideration, only consider this for product match
            //System.err.println(d.getFromWorkingMemory("merchantId"));
            merchants=new ArrayList<>();
            merchants.add(db.getMerchant(d.getFromWorkingMemory("merchantId")));
        }
        catch(IllegalArgumentException e) {
            merchants = filterRejected(findNearbyMerchants(db, user), d.getFromWorkingMemory("rejectedlist"));
        }
        List<Product> products = db.productQueryWithMerchants(StringUtils.phrasejoin(location_list),merchants,new HashSet<>(),limit);
        d.putToWorkingMemory("location_list",StringUtils.phrasejoin(location_list));
        ProductSet ps = new ProductSet(user.getLocationData(),products);
        Set<Merchant> merchantSet= ps.fetchMerchants();
        if(merchantSet.size()==0){
            products=db.productQueryWithMerchants(StringUtils.detokenise(location_list),merchants,new HashSet<>(),limit);
            d.putToWorkingMemory("location_list",StringUtils.detokenise(location_list));
            ps=new ProductSet();
            ps.updateProducts(products);
            merchantSet=ps.fetchMerchants();
        }
        merchants.retainAll(merchantSet);

        return merchants;

    }

    public static List<Merchant> productMatchNearbyMerchants(ProductMongoDB db, User user, Dialogue d){
        //used when location_list is retrieved from workingmemory

        List<Merchant> merchants = filterRejected(findNearbyMerchants(db, user), d.getFromWorkingMemory("rejectedlist"));
        List<Product> products = db.productQueryWithMerchants(d.getFromWorkingMemory("location_list"),merchants,new HashSet<>(),limit);
        ProductSet ps = new ProductSet();
        ps.updateProducts(products);
        Set<Merchant> merchantSet= ps.fetchMerchants();

        merchants.retainAll(merchantSet);
        return merchants;

    }

    private Response processStack(Dialogue d){
        String focus="hello";
        if (!d.isEmptyFocusStack()) {
            focus = d.popTopFocus();
        }
        List<String> newStates = new ArrayList<>();
        Map<String, String> responseVariables = new HashMap<>();
        switch(focus){
            case "confirm_loc":
                newStates.add(focus);
                responseVariables.put(PaypalCheckinHandler.merchantSlot, d.getFromWorkingMemory("merchantName"));
                d.setRequestingYesNo(true);
                break;
            case "repeat_request_loc":
                newStates.add("confirm_loc");
                responseVariables.put(PaypalCheckinHandler.locationSlot, d.getFromWorkingMemory("location_list"));
                d.setRequestingYesNo(false);
                break;
            case "request_location":
                newStates.add("confirm_loc");
                d.setRequestingYesNo(false);
                break;

            case "reconfirm_loc":
                newStates.add("confirm_loc");
                responseVariables.put(PaypalCheckinHandler.merchantSlot, d.getFromWorkingMemory("merchantName"));
                responseVariables.put(PaypalCheckinHandler.locationSlot,d.getFromWorkingMemory("location_list"));
                d.setRequestingYesNo(true);
            //case "confirm_completion":




        }

        Response r=  new Response(focus,responseVariables,newStates);

        return r;

    }



}
