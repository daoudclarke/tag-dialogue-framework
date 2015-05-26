package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.IntentMatch;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.HandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.PaypalCheckinHandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers.*;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;

import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by juliewe on 20/04/2015.
 *
 *
 */
public class PaypalCheckinHandler extends Handler{


    private String dbHost;
    private String dbPort;
    private String dbName;
    protected transient ProductMongoDB db;

    //intent names
    public static final String checkinIntent = "check_in"; //check this against wit
    public static final String otherIntent = "other";
    public static final String confirm = "confirm";
    public static final String quit = "quit";

    public static final String checkinLoc = "check_in_loc";
    public static final String loc = "loc";
    public static final String nochoice="no_choice";
    public static final String neg_loc="neg_loc";

    public static final List<String> locIntents = Lists.newArrayList(PaypalCheckinHandler.checkinLoc, PaypalCheckinHandler.loc, neg_loc);
    public static final List<String> confirmIntents = Lists.newArrayList(confirm,Intent.yes);


    //analyser names
    public static final String mainAnalyser="wit.ai";
    public static final String yesNoAnalyser="simple_yes_no";

    //slot names
    public static final String yes_no_slot = "yes_no";
    public static final String locationSlot="local_search_query";
    public static final String merchantSlot="merchant";
    public static final String productSlot="product";

    public PaypalCheckinHandler(){
        super.registerProblemHandler(new LocMethod()); //this deals with loc, check_in_loc and confirm_loc from the wit.analyser
        super.registerProblemHandler(new ConfirmMethod()); //deals with confirm from wit and yes/no from yes_no analyser
        super.registerProblemHandler(new RejectMethod());
        super.registerProblemHandler(new CheckinMethod());
        super.registerProblemHandler(new UnknownMethod());
    }

    public void setupDatabase()throws Dialoguer.DialoguerException {
        try {
            if(!dbHost.equals("")) {
                db = new ProductMongoDB(dbHost, Integer.parseInt(dbPort), dbName);
            } else {
                db= new ProductMongoDB();

            }
        } catch (UnknownHostException e) {
            throw new Dialoguer.DialoguerException("Cannot connect to database host", e);
        }
    }



    @Override
    public Dialogue getNewDialogue(String dialogueId){

        Dialogue d = new Dialogue(dialogueId);
        d.setState("initial");

        return d;
    }

    @Override
    public List<Intent> preProcessIntents(List<Intent> intents, List<IntentMatch> matches, Dialogue d){
        //remove any intents returned by the wit analyser which should have entities but don't
        //this actually makes autoquerying pointless and would actually be better not to have them as necessary slots
        //however this is actually better because it actually removes these intents

        List<Intent> filtered = new ArrayList<>();
        for(Intent i:intents){
            if(i.getSource().equals(mainAnalyser)) {
                if (locIntents.contains(i.getName())) {
                    if (i.areSlotsFilled(Sets.newHashSet(locationSlot))) {
                        filtered.add(i);
                    }
                } else {
                    if (confirmIntents.contains(i.getName())) {
                        if (i.areSlotsFilled(Sets.newHashSet(yes_no_slot))) {
                            filtered.add(i);
                        }
                    } else {
                        filtered.add(i);
                    }
                }
            } else {
                filtered.add(i);
            }

        }
        //for(Intent i: filtered){
        //    System.err.println(i.toString());
        //}
        return filtered;
    }


    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue) {

        Boolean complete=useFirstProblemHandler(intents, dialogue, this.db);//first check whether there is a specific problemHandler associated with these intents

//        if(r==null) {
//            Intent i = Intent.getFirstIntentFromSource(mainAnalyser,intents);
//            if(!(i ==null)) {
//                r = applyIntentHandler(Intent.getFirstIntentFromSource(mainAnalyser, intents), dialogue, this.db);//get wit's response
//            }
//        }
        if(!complete){//no intent handler - don't really need this as have the UnknownProblemHandler now
            if(dialogue.getStates().contains("initial")) {
                dialogue.pushFocus("unknown_hello");
            } else {
                dialogue.pushFocus("unknown_request_location");
            }

        }

        return processStack(dialogue);
    }

    @Override
    public HandlerFactory getFactory() {
        return new PaypalCheckinHandlerFactory();
    }

    @Override
    public void close() throws Exception {
        // Close any resources here (like database)
        db.close();
    }

    @Override
    public Set<String> getRequiredAnalyserSourceIds(){
        return Sets.newHashSet(mainAnalyser, yesNoAnalyser);
    }

    private Response processStack(Dialogue d){
        String focus="unknown_hello";
        if (!d.isEmptyFocusStack()) {
            focus = d.popTopFocus();
        }
        Map<String, String> responseVariables = new HashMap<>();
        switch(focus){
            case "confirm_loc":
                responseVariables.put(PaypalCheckinHandler.merchantSlot, d.getFromWorkingMemory("merchantName"));
                break;
            case "confirm_loc_product":
                responseVariables.put(PaypalCheckinHandler.merchantSlot, d.getFromWorkingMemory("merchantName"));
                responseVariables.put(PaypalCheckinHandler.productSlot,d.getFromWorkingMemory("product"));
                break;
            case "repeat_request_loc":
                responseVariables.put(PaypalCheckinHandler.locationSlot, d.getFromWorkingMemory("location_list"));
                break;
            case "repeat_request_loc_rejects":
                responseVariables.put(PaypalCheckinHandler.locationSlot, d.getFromWorkingMemory("location_list"));
                break;
            //case "request_location":
            //  break;
            case "reconfirm_loc":
                responseVariables.put(PaypalCheckinHandler.merchantSlot, d.getFromWorkingMemory("merchantName"));
                responseVariables.put(PaypalCheckinHandler.locationSlot, d.getFromWorkingMemory("location_list"));
                //case "confirm_completion":
            case "request_location":
                d.setRequestingYesNo(false);
                break;

        }

        Response r=  new Response(focus,responseVariables);

        return r;

    }



}
