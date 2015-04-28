package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;

import com.sun.jdi.request.MonitorContendedEnteredRequest;
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

import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by juliewe on 21/04/2015.
 */
public class LocMethod implements Handler.ProblemHandler {

    public static final String locationSlot="local_search_query";
    public static final String userSlot="user";
    public static final String merchantSlot="merchant";
    public static final String yes_no_slot = "yes_no";
    public static final int searchradius = 50;
    public static final int limit = 50;
    public static final String confirmLoc = "confirm_loc";
    public static final String checkinLoc = "check_in_loc";
    public static final String loc = "loc";


    public static List<String> getIntents(){
        ArrayList<String> locIntents = new ArrayList<>();
        locIntents.add(confirmLoc);
        locIntents.add(checkinLoc);
        locIntents.add(loc);
        return locIntents;
    }


    public boolean isInHandleableState(List<Intent> intents, Dialogue d){
        List<String> locIntents = getIntents();
        boolean response=false;
        for(Intent i:intents){
            if (locIntents.contains(i.getName())){
                response=true;
            }
        }
        return response;
    }

    public Response handle(List<Intent> intents, Dialogue d, Object resource){
        //we think that the user message has some information about location
        //System.err.println("Using problem handler: locMethod()");
        List<String> locIntents = getIntents();
        for(Intent i:intents){
            if(locIntents.contains(i.getName())){
                handleLocation(i,d,resource); // first matching intent
            }
        }

        return processStack(d);

    }

    public static void handleLocation(Intent i, Dialogue d, Object resource){
        ProductMongoDB db=null;
        if (resource instanceof ProductMongoDB){
            db=(ProductMongoDB) resource;
        }

        //possible confirm slot
        Collection<Intent.Slot> answers = i.getSlotByType(yes_no_slot);
        boolean accept=false;
        for(Intent.Slot answer:answers){

            if(answer.value.equals("yes")){
                accept=true;
            }
        }
        if(!accept){//need to add the current selection to rejected_list

            ConfirmMethod.reject(d, resource);
        }

        //definite location slot
        Collection<Intent.Slot> locations = i.getSlotByType(locationSlot);
        ArrayList location_list = new ArrayList<>();
        for(Intent.Slot location: locations){
            location_list.add(location.value);

        }
        d.putToWorkingMemory("location_list",StringUtils.join(location_list));
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
            ConfirmMethod.accept(d);
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

        List<Merchant> merchants = new ArrayList<>();
        if(db!=null){
            merchants = db.merchantQueryByLocation(user.getLatitude(),user.getLongitude(),searchradius,limit);

        } else {
            System.err.println("No database specified");
        }
        return merchants;
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
        if(db!=null) {
            List<Merchant> merchants = filterRejected(findNearbyMerchants(db,user), d.getFromWorkingMemory("rejectedlist"));
            merchants.retainAll(db.merchantQuery(StringUtils.phrasejoin(location_list)));
            if(merchants.size()==0){
                merchants = findNearbyMerchants(db,user);
                merchants.retainAll(db.merchantQuery(StringUtils.join(location_list)));
            }
            if(merchants.size()==0){
                merchants=productMatchNearbyMerchants(location_list,db,user, d);
            }
            return merchants;
        } else{
            System.err.println("No database specified");
            return new ArrayList<>();
        }
    }

    public static List<Merchant> matchNearbyMerchants(String location_list, ProductMongoDB db,User user, Dialogue d){
        if(db!=null) {
            List<Merchant> merchants = filterRejected(findNearbyMerchants(db,user),d.getFromWorkingMemory("rejectedlist"));
            merchants.retainAll(db.merchantQuery(location_list));
            if(merchants.size()==0){//try a product match instead
                merchants=productMatchNearbyMerchants(location_list,db,user, d);
            }
            return merchants;
        } else{
            System.err.println("No database specified");
            return new ArrayList<>();
        }
    }

    public static List<Merchant> productMatchNearbyMerchants(List<String> location_list, ProductMongoDB db, User user, Dialogue d){
        if(db!=null){
            List<Merchant> merchants = filterRejected(findNearbyMerchants(db,user),d.getFromWorkingMemory("rejectedlist"));
            List<Product> products = db.productQueryWithMerchants(StringUtils.phrasejoin(location_list),merchants,new HashSet<>(),limit);
            ProductSet ps = new ProductSet();
            ps.updateProducts(products);
            Set<Merchant> merchantSet= ps.fetchMerchants();
            if(merchantSet.size()==0){
                products=db.productQueryWithMerchants(StringUtils.join(location_list),merchants,new HashSet<>(),limit);
                ps=new ProductSet();
                ps.updateProducts(products);
                merchantSet=ps.fetchMerchants();
            }
            merchants.retainAll(merchantSet);

            return merchants;
        } else {
            System.err.println("No database specified");
            return new ArrayList<>();
        }
    }

    public static List<Merchant> productMatchNearbyMerchants(String location_list, ProductMongoDB db, User user, Dialogue d){
        if(db!=null){
            List<Merchant> merchants = filterRejected(findNearbyMerchants(db,user),d.getFromWorkingMemory("rejectedlist"));
            List<Product> products = db.productQueryWithMerchants(location_list,merchants,new HashSet<>(),limit);
            ProductSet ps = new ProductSet();
            ps.updateProducts(products);
            Set<Merchant> merchantSet= ps.fetchMerchants();

            merchants.retainAll(merchantSet);
            return merchants;
        } else {
            System.err.println("No database specified");
            return new ArrayList<>();
        }
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
                responseVariables.put(locationSlot, d.getFromWorkingMemory("merchantName"));
                break;
            case "repeat_request_loc":
                newStates.add("confirm_loc");
                responseVariables.put(locationSlot, d.getFromWorkingMemory("location_list"));
                break;
            case "request_location":
                newStates.add("confirm_loc");
                break;
            case "hello":
                newStates.add("initial");
                responseVariables.put(userSlot,d.getId());
                break;
            case "reconfirm_loc":
                newStates.add("confirm_loc");
                responseVariables.put(merchantSlot, d.getFromWorkingMemory("merchantName"));
                responseVariables.put(locationSlot,d.getFromWorkingMemory("location_list"));
            //case "confirm_completion":




        }

        Response r=  new Response(focus,responseVariables,newStates);

        return r;

    }



}
