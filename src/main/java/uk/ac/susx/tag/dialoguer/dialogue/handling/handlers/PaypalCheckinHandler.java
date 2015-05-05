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
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers.ConfirmMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers.CheckinMethod;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers.LocMethod;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by juliewe on 20/04/2015.
 *
 * TODO: check null pointer on "fab thats right"
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
    public static final String yes = "yes";
    public static final String no = "no";
    public static final String checkinLoc = "check_in_loc";
    public static final String loc = "loc";

    public static final List<String> locIntents = Lists.newArrayList(PaypalCheckinHandler.checkinLoc, PaypalCheckinHandler.loc);
    public static final List<String> confirmIntents = Lists.newArrayList(confirm,yes,no);


    //analyser names
    public static final String mainAnalyser="wit.ai";
    public static final String yesNoAnalyser="simple_yes_no";

    //slot names
    public static final String yes_no_slot = "yes_no";
    public static final String locationSlot="local_search_query";
    public static final String merchantSlot="merchant";

    public PaypalCheckinHandler(){

        super.registerIntentHandler(quit, (i, d, r) -> new Response("confirm_cancellation"));
        super.registerIntentHandler(confirm, new ConfirmMethod());
        super.registerIntentHandler(yes,new ConfirmMethod());
        super.registerIntentHandler(no,new ConfirmMethod());
        super.registerIntentHandler(checkinIntent,new CheckinMethod());
        super.registerIntentHandler(otherIntent, (i,d, r) -> new Response("unknown"));
        super.registerProblemHandler(new LocMethod()); //this deals with loc, check_in_loc and confirm_loc from the wit.analyser
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
        for(Intent i: filtered){
            System.err.println(i.toString());
        }
        return filtered;
    }


    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue) {

        Response r=applyFirstProblemHandlerOrNull(intents, dialogue, this.db);//first check whether there is a specific problemHandler associated with these intents

        if(r==null) {
            Intent mainIntent = Intent.getFirstIntentFromSource(yesNoAnalyser,intents);//then check for a response from yes_no
            if(mainIntent==null){
                mainIntent=Intent.getFirstIntentFromSource(mainAnalyser,intents);//then get wit's response
            }
            r=applyIntentHandler(mainIntent, dialogue, this.db);
        }
        return r;
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


}
