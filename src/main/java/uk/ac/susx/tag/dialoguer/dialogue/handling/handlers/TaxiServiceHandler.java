package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.IntentMatch;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.IntentMerger;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.HandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.TaxiServiceHandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.taxiServiceHandlers.AcceptProblemHandler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.taxiServiceHandlers.OrderTaxiMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by juliewe on 19/05/2015.
 */
public class TaxiServiceHandler extends Handler{


    //analyser names
    public static final String mainAnalyser="wit.ai";
    public static final String yesNoAnalyser="simple_yes_no";
    public static final String merged="merged";
    public static final List<String> analysers = Lists.newArrayList(mainAnalyser, yesNoAnalyser);

    //intent names
    public static final String orderTaxiIntent="order_taxi";

    //slot names
    public static final String destinationSlot="from";
    public static final String pickupSlot="to";
    public static final String timeSlot="datetime";
    public static final String capacitySlot="persons";
    public static final List<String> allSlots=Lists.newArrayList(destinationSlot,pickupSlot,timeSlot,capacitySlot);

    //response/focus/state names
    public static final String confirmResponse = "request_confirm";
    public static final String chooseCapacityResponse ="choose_capacity";
    public static final String chooseTimeResponse="choose_time";
    public static final String chooseDestinationResponse="choose_destination";
    public static final String choosePickupResponse="choose_pickup";
    public static final String respecifyDestinationResponse="respecify_destination";
    public static final String respecifyPickupResponse="respecify_pickup";
    public static final String confirmCompletionResponse="confirm_completion";

    public TaxiServiceHandler(){
        //register problem handlers and intent handlers here
        super.registerIntentHandler(orderTaxiIntent, new OrderTaxiMethod());
        super.registerProblemHandler(new AcceptProblemHandler());
    }

    public List<Intent> preProcessIntents(List<Intent> intents, List<IntentMatch> intentmatches, Dialogue d){
        //modify list of intents before the dialoguer gets to auto-query/cancel

        //add working intents to the list of intents
        if(!d.getWorkingIntents().isEmpty()){
            // System.err.println(d.peekTopIntent().toString());
            intents.addAll(d.getWorkingIntents());
            d.clearWorkingIntents();  //must re-add if you want to keep them
        }

        //merge intents of the same name
        intents = new IntentMerger(intents)
                    .merge(Sets.newHashSet(orderTaxiIntent), (intentsToBeMerged) -> {
                        Intent output = new Intent(orderTaxiIntent);
                        output.copySlots(intentsToBeMerged);
                        return output;
                        })
                    .getIntents();


        intents.stream().forEach(intent->System.err.println(intent.toString()));
        return intents;
    }

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue) {


        Response r=applyFirstProblemHandlerOrNull(intents, dialogue, null); //is there a problem handler

        if(r==null){
            Intent i = Intent.getFirstIntentFromSource(merged,intents); //look for pre-processed/merged intents first
            if(i!=null){
                r=applyIntentHandler(i,dialogue,null);
            }
        }

        for(String analyser:analysers) { //try each analyser in order of priority for a non-null response
            if(r==null) {
                Intent i = Intent.getFirstIntentFromSource(analyser, intents);
                if (i != null) {
                    r = applyIntentHandler(i, dialogue, null);//
                }
            }
        }
        if(r==null){ //no problem handler or intent handler
            dialogue.pushFocus("unknown");
            r=processStack(dialogue);
        }
        return r;
    }

    @Override
    public Dialogue getNewDialogue(String dialogueId) {
        Dialogue d = new Dialogue(dialogueId);
        d.setState("initial");
        return d;
    }

    @Override
    public HandlerFactory getFactory() {
        return new TaxiServiceHandlerFactory();
    }

    @Override
    public void close() throws Exception {

    }

    public static Response processStack(Dialogue d){
        String focus="unknown";
        if (!d.isEmptyFocusStack()) {
            focus = d.popTopFocus();
        }
        Map<String, String> responseVariables = new HashMap<>();
        System.err.println(focus);
        try {
            switch (focus) {
                case confirmResponse:
                    allSlots.stream().forEach(slot->responseVariables.put(slot,d.peekTopIntent().getSlotValuesByType(slot).stream().collect(Collectors.joining(" "))));
                    break;
                case confirmCompletionResponse:
                    AcceptProblemHandler.complete(d);
                case chooseCapacityResponse:
                    break;
                case chooseTimeResponse:
                    break;
                case chooseDestinationResponse:
                    break;
                case choosePickupResponse:
                    break;
                case respecifyDestinationResponse:
                    break;
                case respecifyPickupResponse:
                    break;

            }

        } catch(ArrayIndexOutOfBoundsException e){
            throw new Dialoguer.DialoguerException("Error with response variables: "+e.toString());
        }
        return new Response(focus,responseVariables);

    }
}
