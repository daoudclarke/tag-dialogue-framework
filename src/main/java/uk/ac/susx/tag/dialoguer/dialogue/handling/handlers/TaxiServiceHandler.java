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
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.taxiServiceHandlers.ChoiceProblemHandler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.taxiServiceHandlers.FollowupProblemHandler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.taxiServiceHandlers.OrderTaxiMethod;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;

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
    public static final String simpleChoiceAnalyser="simple_choice";
    public static final String merged="merged";
    public static final List<String> analysers = Lists.newArrayList(mainAnalyser, yesNoAnalyser,simpleChoiceAnalyser);

    //intent names
    public static final String orderTaxiIntent="order_taxi";
    public static final String followupCapacityIntent="followup_people";
    public static final String followupTimeIntent="followup_time";
    public static final String followupLocationIntent="followup_location";
    public static final List<String> followupIntents=Lists.newArrayList(followupCapacityIntent,followupLocationIntent,followupTimeIntent);
    public static final List<String> choiceIntents=Lists.newArrayList(Intent.choice,Intent.noChoice,Intent.nullChoice);

    //slot names
    public static final String destinationSlot="to";
    public static final String pickupSlot="from";
    public static final String timeSlot="datetime";
    public static final String altTimeSlot="timeref";
    public static final String capacitySlot="number";
    public static final String choiceSlot="choice";
    public static final String choiceNameSlot="choice_name";
    public static final List<String> allSlots=Lists.newArrayList(destinationSlot,pickupSlot,timeSlot,capacitySlot);

    private Map<String, String> humanReadableSlotNames; //read from config file

    //response/focus/state names
    public static final String confirmResponse = "request_confirm";
    public static final String chooseResponse = "choose";
    public static final String respecifyResponse="respecify";
    public static final String confirmCompletionResponse="confirm_completion";
    public static final String repeatChoiceResponse="repeat_choice";

    public TaxiServiceHandler(){
       // humanReadableSlotNames = new HashMap<>();
        //register problem handlers and intent handlers here
        super.registerIntentHandler(orderTaxiIntent, new OrderTaxiMethod());
        super.registerProblemHandler(new ChoiceProblemHandler());
        super.registerProblemHandler(new FollowupProblemHandler());
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

        //merge timeSlots
        intents=intents.stream().map(i->{if(i.areSlotsFilled(Sets.newHashSet(altTimeSlot))){
                                            i.fillSlot(timeSlot,i.getSlotValuesByType(altTimeSlot).get(0));}
                                        return i;})
                    .collect(Collectors.toList());

        intents.stream().forEach(intent->System.err.println(intent.toString()));
        return intents;
    }

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue) {

        //System.err.println(humanReadableSlotNames.keySet());
        boolean complete=applyFirstProblemSubHandlerOrNull(intents, dialogue, null); //is there a problem handler

        if(!complete){
            Intent i = Intent.getFirstIntentFromSource(merged,intents); //look for pre-processed/merged intents first
            if(i!=null){
                complete=applyIntentSubHandler(i,dialogue,null);
            }
        }

        for(String analyser:analysers) { //try each analyser in order of priority for a non-null response
            if(!complete) {
                Intent i = Intent.getFirstIntentFromSource(analyser, intents);
                if (i != null) {
                    complete = applyIntentSubHandler(i, dialogue, null);//
                }
            }
        }
        if(!complete){ //no problem handler or intent handler
            dialogue.pushFocus("unknown");
        }
        return processStack(dialogue);
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

    public Response processStack(Dialogue d){
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
                case chooseResponse:
                    d.setChoices(d.peekTopIntent().getSlotValuesByType(d.getFromWorkingMemory("slot_to_choose")));
                    responseVariables.put(choiceNameSlot,humanReadableSlotNames.get(d.getFromWorkingMemory("slot_to_choose")));
                    responseVariables.put(choiceSlot,StringUtils.numberList(d.getChoices()));
                case respecifyResponse:
                    responseVariables.put(choiceNameSlot,humanReadableSlotNames.get(d.getFromWorkingMemory("slot_to_choose")));
                    break;
                case repeatChoiceResponse:
                    responseVariables.put(choiceSlot,StringUtils.numberList(d.getChoices()));

            }

        } catch(ArrayIndexOutOfBoundsException e){
            throw new Dialoguer.DialoguerException("Error with response variables: "+e.toString());
        }
        return new Response(focus,responseVariables);

    }
}
