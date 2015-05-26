package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import java.util.stream.Collectors;


/**
 * Created by juliewe on 21/04/2015.
 */
public class LocMethod implements Handler.ProblemHandler {

    public static final int searchradius = 50;
    public static final int limit = 50;


    public boolean isInHandleableState(List<Intent> intents, Dialogue d){
        return intents.stream().anyMatch(i -> PaypalCheckinHandler.locIntents.contains(i.getName()));
    }

    public void handle(List<Intent> intents, Dialogue d, Object resource){
        //we think that the user message has some information about location
        //System.err.println("Using problem handler: locMethod()");

        //first of all check for yes/no/confirm intents
        d.putToWorkingMemory("accepting","no_choice");
        Optional<Intent> confirm = intents.stream().filter(intent->PaypalCheckinHandler.confirmIntents.contains(intent.getName())).findFirst();
        if(confirm.isPresent()){
            handleConfirm(confirm.get(),d,resource);
        }

        //now get location intent
        Optional<Intent> location = intents.stream().filter(intent->PaypalCheckinHandler.locIntents.contains(intent.getName())).findFirst();
        if(location.isPresent()){
            if(location.get().getName().equals(PaypalCheckinHandler.neg_loc)){handleNegLocation(location.get(),d,resource);}
            else {
                handleLocation(location.get(), d, resource);
            }
        }

    }

    public void handleConfirm(Intent i, Dialogue d, Object resource){
        //possible confirm slot
        boolean accept=false;
        if(i.getName().equals(Intent.yes)){
            accept=true;
        } else {
            if(i.getName().equals(Intent.no)){
                accept=false;
            } else {
                accept = i.isSlotTypeFilledWith(PaypalCheckinHandler.yes_no_slot,"yes");
            }
        }


        if(!accept){//need to add the current selection to rejected_list
            d.putToWorkingMemory("accepting","no");
            RejectMethod.handleReject(d);
        } else {
            d.putToWorkingMemory("accepting","yes");
            //need to handle location as well before going through accept method
        }
        //System.err.println(d.getFromWorkingMemory("accepting"));
    }

    public static void handleNegLocation(Intent i, Dialogue d, Object resource){
        //System.err.println("Negative location!");
        ProductMongoDB db;
        try {
            db = (ProductMongoDB) resource;
        } catch (ClassCastException e){
            throw new Dialoguer.DialoguerException("Resource should be mongo db", e);
        }

        //definite location slot
        List<String> location_list = i.getSlotByType(PaypalCheckinHandler.locationSlot).stream()
                .map(location->location.value)
                .collect(Collectors.toList());
        String cacheLocationList=d.getFromWorkingMemory("location_list");
        String cacheTagList=d.getFromWorkingMemory("tag_list");
        List<Merchant> possibleMerchants = matchNearbyMerchants(location_list, db, d.getUserData(), d);
        if(possibleMerchants.size()>0&&d.isInWorkingMemory("merchantId",possibleMerchants.get(0).getMerchantId())) {

            //this is just a standard reject of the current suggestion
            //System.err.println("Rejecting current suggestion");
            RejectMethod.handleReject(d);
            //reinstate previous location_list
            d.putToWorkingMemory("location_list",cacheLocationList);
            d.putToWorkingMemory("tag_list",cacheTagList);
            if(d.getFromWorkingMemory("location_list")==null) {

                possibleMerchants = LocMethod.filterRejected(LocMethod.findNearbyMerchants(db, d.getUserData()), d.getFromWorkingMemory("rejectedlist"));
            } else {
                System.err.println(d.getFromWorkingMemory("location_list"));
                possibleMerchants = LocMethod.matchNearbyMerchants(db, d.getUserData(), d);//will use workingmemory's location_list
            }

            LocMethod.processMerchantList(possibleMerchants, d,db);

        }
        else {
            //avoid trying to handle logic of negation - ignore and ask user to specify location
            d.pushFocus("request_location");

        }

    }

    public void handleLocation(Intent i, Dialogue d, Object resource){
        //DialogueTracker.logger.log(Level.INFO, "Im in handleLocation");

        ProductMongoDB db;
        try {
            db = (ProductMongoDB) resource;
        } catch (ClassCastException e){
            throw new Dialoguer.DialoguerException("Resource should be mongo db", e);
        }


        //definite location slot
        List<String> location_list = i.getSlotByType(PaypalCheckinHandler.locationSlot).stream()
                                                .map(location->location.value)
                                                .collect(Collectors.toList());




        List<Merchant> possibleMerchants = matchNearbyMerchants(location_list, db, d.getUserData(), d);
        //System.err.println(d.getFromWorkingMemory("accepting"));
        if(d.isInWorkingMemory("accepting","yes")||(d.isInWorkingMemory("accepting","no_choice")&&d.getFromWorkingMemory("merchantId")!=null)){
            //check that the accepted location is in possiblemerchants
            boolean match=false;
            for(Merchant m:possibleMerchants){
                if(m.getMerchantId().equals(d.getFromWorkingMemory("merchantId"))){
                    match=true;
                    if(d.isInWorkingMemory("accepting","no_choice")&&d.isRequestingYesNo()) {
                        //d.putToWorkingMemory("accepting","yes"); //shouldn't upgrade this to a definite yes.
                        d.pushFocus("confirm");
                    } else {
                        d.pushFocus("confirm_loc");
                    }
                }
            }
            if(!match){
                //problem - accepted merchant does not appear to match the location given
                if(d.isInWorkingMemory("accepting","yes")) {
                    d.pushFocus("reconfirm_loc");
                    d.putToWorkingMemory("accepting", "no");
                } else {
                    //System.err.println(possibleMerchants);
                    processMerchantList(possibleMerchants, d, db); //assume it was a rejection of the previous suggestion
                }
            }
        }else {
            //System.err.println(possibleMerchants);
            processMerchantList(possibleMerchants, d, db);
        }
        if(d.isInWorkingMemory("accepting","yes")){//still accepting after checking possible location
            ConfirmMethod.handleAccept(d);
        }

    }

    public static void processMerchantList(List<Merchant> possibleMerchants, Dialogue d, ProductMongoDB db){

        if(possibleMerchants.size()==0){
            if(d.getFromWorkingMemory("location_list")==null){
                d.pushFocus("request_location");
            } else {
                if(d.getFromWorkingMemory("rejectedlist")==null) {
                    d.pushFocus("repeat_request_loc");
                } else {
                    d.pushFocus("repeat_request_loc_rejects");
                    d.putToWorkingMemory("rejectedlist",null); //clear this for a restart
                }
            }

        } else {
            if(possibleMerchants.size()>0) { //may want to do something different if multiple merchants returned but currently assume first is best and just offer this one
                if (d.getFromWorkingMemory("productSearch") == null) {
                    d.pushFocus("confirm_loc");
                } else {
                    d.pushFocus("confirm_loc_product");
                    List<Merchant> mymerchant = Lists.newArrayList(possibleMerchants.get(0));
                    List<Product> products = db.productQueryWithMerchants(d.getFromWorkingMemory("location_list"),mymerchant,new HashSet<>(),limit);
                    d.putToWorkingMemory("product",products.get(0).propertyDescription());
                }
                updateMerchant(possibleMerchants.get(0), d);


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

        return db.merchantQueryByLocation(user.getLatitude(), user.getLongitude(), searchradius+user.getUncertaintyRadius(), limit);


    }

    public static List<Merchant> filterRejected(List<Merchant> merchants, String rejectedlist){
       // System.err.println("rejected: "+rejectedlist);
        if(rejectedlist==null){
            return merchants;
        } else {
            String[] rejected = rejectedlist.split(" ");
            List<Merchant> newmerchants = new ArrayList<>();
            for (Merchant m : merchants) {
                boolean reject=false;
                for (String r : rejected) {
                    //System.err.println(":"+r+":"+m.getMerchantId()+":");
                    if (m.getMerchantId().equals(r)) {
                        reject=true;

                    }
                }
                //System.err.println(m.getMerchantId()+":"+reject);
                if(!reject){
                    newmerchants.add(m);
                }
            }
            return newmerchants;
        }
    }

    public static List<Merchant> matchNearbyMerchants(List<String> location_list, ProductMongoDB db,User user, Dialogue d){
        d.putToWorkingMemory("productSearch",null);
        List<Merchant> merchants = filterRejected(findNearbyMerchants(db, user), d.getFromWorkingMemory("rejectedlist"));
        d.putToWorkingMemory("location_list",StringUtils.phrasejoin(location_list));
        merchants.retainAll(db.merchantQuery(StringUtils.phrasejoin(location_list)));
        if(merchants.size()==0){
            merchants = filterRejected(findNearbyMerchants(db, user), d.getFromWorkingMemory("rejectedlist"));
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
        d.putToWorkingMemory("productSearch",null);
        List<Merchant> merchants = filterRejected(findNearbyMerchants(db, user), d.getFromWorkingMemory("rejectedlist"));
        merchants.retainAll(db.merchantQuery(d.getFromWorkingMemory("location_list")));
        if(merchants.size()==0){//try a product match instead
            merchants=productMatchNearbyMerchants(db,user, d);
        }
        return merchants;

    }

    public static List<Merchant> productMatchNearbyMerchants(List<String> location_list, ProductMongoDB db, User user, Dialogue d){

        List<Merchant> merchants;
        if(d.isInWorkingMemory("accepting","yes")){
            merchants=new ArrayList<>();
            merchants.add(db.getMerchant(d.getFromWorkingMemory("merchantId")));
        }
        else{
            merchants = filterRejected(findNearbyMerchants(db, user), d.getFromWorkingMemory("rejectedlist"));
        }

        d.putToWorkingMemory("location_list",StringUtils.phrasejoin(location_list));
        d.putToWorkingMemory("tag_list",StringUtils.detokenise(location_list));
        List<Product> products = db.productQueryWithMerchants(d.getFromWorkingMemory("location_list"),merchants, Sets.newHashSet(d.getFromWorkingMemory("tag_list")),limit);//quotes + tags

        ProductSet ps = new ProductSet(user.getLocationData(),products);
        Set<Merchant> merchantSet= ps.fetchMerchants();
        if(merchantSet.isEmpty()) {
            products = db.productQueryWithMerchants(d.getFromWorkingMemory("location_list"), merchants, new HashSet<>(), limit);//quotes, no tags
            ps = new ProductSet(user.getLocationData(), products);
            merchantSet = ps.fetchMerchants();
            if (merchantSet.size() == 0) {
                d.putToWorkingMemory("location_list", StringUtils.detokenise(location_list));
                products=db.productQueryWithMerchants(d.getFromWorkingMemory("location_list"),merchants,Sets.newHashSet(d.getFromWorkingMemory("tag_list")),limit);//no quotes + tags
                ps= new ProductSet(user.getLocationData(),products);
                merchantSet=ps.fetchMerchants();
                if(merchantSet.isEmpty()) {
                    products = db.productQueryWithMerchants(d.getFromWorkingMemory("location_list"), merchants, new HashSet<>(), limit);//no quotes + no tags
                    ps = new ProductSet(user.getLocationData(), products);
                    merchantSet = ps.fetchMerchants();
                }
            }
        }
        merchants.retainAll(merchantSet);

        d.putToWorkingMemory("productSearch","yes");
        return merchants;

    }

    public static List<Merchant> productMatchNearbyMerchants(ProductMongoDB db, User user, Dialogue d){
        //used when location_list is retrieved from workingmemory

        List<Merchant> merchants = filterRejected(findNearbyMerchants(db, user), d.getFromWorkingMemory("rejectedlist"));

        List<Product> products = db.productQueryWithMerchants(d.getFromWorkingMemory("location_list"),merchants,Sets.newHashSet(d.getFromWorkingMemory("tag_list")),limit);
        ProductSet ps = new ProductSet();
        ps.updateProducts(products);
        Set<Merchant> merchantSet= ps.fetchMerchants();
        if(merchantSet.isEmpty()) {
            products = db.productQueryWithMerchants(d.getFromWorkingMemory("location_list"), merchants, new HashSet<>(), limit);
            ps = new ProductSet();
            ps.updateProducts(products);
            merchantSet = ps.fetchMerchants();
        }
        merchants.retainAll(merchantSet);

        d.putToWorkingMemory("productSearch","yes");
        return merchants;

    }




}
