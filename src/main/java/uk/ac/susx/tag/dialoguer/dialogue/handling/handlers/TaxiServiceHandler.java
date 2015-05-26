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
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.taxiServiceHandlers.*;
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
    public static final String followupNegativeIntent="followup_negative";
    public static final List<String> outOfDomainIntents=Lists.newArrayList("out_of_domain","UNKNOWN");
    public static final List<String> followupIntents=Lists.newArrayList(followupCapacityIntent,followupLocationIntent,followupTimeIntent,followupNegativeIntent);
    public static final List<String> choiceIntents=Lists.newArrayList(Intent.choice,Intent.noChoice,Intent.nullChoice);

    //slot names
    public static final String destinationSlot="to";
    public static final String pickupSlot="from";
    public static final String timeSlot="datetime";
    public static final String altTimeSlot="timeref";
    public static final String capacitySlot="number";
    public static final String altCapacitySlot="persons";
    public static final String choiceSlot="choice";
    public static final String choiceNameSlot="choice_name";
    public static final List<String> allSlots=Lists.newArrayList(capacitySlot,timeSlot,destinationSlot,pickupSlot);

    private Map<String, String> humanReadableSlotNames; //read from config file

    //response/focus/state names
    public static final String confirmResponse = "request_confirm";
    public static final String chooseResponse = "choose";
    public static final String respecifyResponse="respecify";
    public static final String confirmCompletionResponse="confirm_completion";
    public static final String repeatChoiceResponse="repeat_choice";
    public static final String unknownResponse="unknown";

    public TaxiServiceHandler(){
       // humanReadableSlotNames = new HashMap<>();
        //register problem handlers and intent handlers here
        super.registerIntentHandler(orderTaxiIntent, new OrderTaxiMethod());
        super.registerProblemHandler(new OutOfDomainHandler());
        super.registerProblemHandler(new ChoiceProblemHandler());
        super.registerProblemHandler(new FollowupProblemHandler());
        super.registerProblemHandler(new AcceptProblemHandler());
    }

    /****
     *
     * @param intents
     * @param intentmatches
     * @param d
     * @return
     *
     * This specifies how the list of intents should be modified before the dialoguer does any autoquerying etc.
     * In this case, we add the working intents from the dialogue.  Then merge intents of the same name (to cater for multiple orderTaxiIntents in working intents and newly recieved).
     * Then merge the two different types of time slot (to cater for time references like ASAP)

     */
    @Override
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
                                            i.fillSlot(timeSlot,i.getSlotValuesByType(altTimeSlot).get(0));
                                            i.clearSlots(altTimeSlot);}
                                        return i;})
                    .collect(Collectors.toList());

        //merge capacitySlots
        intents=intents.stream().map(i->{if(i.areSlotsFilled(Sets.newHashSet(altCapacitySlot))){
                                            i.fillSlot(capacitySlot,i.getSlotValuesByType(altCapacitySlot).get(0));
                                            i.clearSlots(altCapacitySlot);}
                                            return i;})
                        .collect(Collectors.toList());


        intents.stream().forEach(intent->System.err.println(intent.toString()));
        return intents;
    }

    /*******
     *
     * @param intents
     * @param dialogue
     * @return
     *
     * This specifies how a set of intents should be handled in the context of the dialogue.
     * In this case, we use the subhandle methods which return a boolean (if they fire) rather than a response
     * Specifically, we look for a ProblemHandler that matches, then we look for any merged intents, then we turn to each individual analyser in turn.  No match leads to unknown response
     * We then call processStack to actually generate the response
     */

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue) {

        //System.err.println(humanReadableSlotNames.keySet());
        boolean complete=applyFirstProblemSubHandlerOrNull(intents, dialogue, null); //is there a problem handler

        if(!complete){
            Intent i = Intent.getFirstIntentFromSource(merged,intents); //look for pre-processed/merged intents first
            if(i!=null){
                complete=applyIntentSubHandler(i, dialogue, null);
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
            dialogue.pushFocus(unknownResponse);
        }
        return processStack(dialogue);
    }

    /****
     *
     * @param dialogueId
     * @return
     * This specifies how a dialogue should be intialised.
     * This is the generic case of setting the state as "initial"
     *
     */

    @Override
    public Dialogue getNewDialogue(String dialogueId) {
        Dialogue d = new Dialogue(dialogueId);
        d.setState("initial");
        return d;
    }


    /*****
     *
     * @return
     * Get the handler factory associated with this handler
     */
    @Override
    public HandlerFactory getFactory() {
        return new TaxiServiceHandlerFactory();
    }


    /****
     *
     * @throws Exception
     * Close any resources that have been opened.
     * None in this case
     */
    @Override
    public void close() throws Exception {

    }

    /****
     *
     * @param d
     * @return
     * Generate a response based on the current state of the dialogue (most specifically the FocusStack)
     * Pop the focus stack, add responseVariables which are required by this focus, generate the Response associated with this focus and responseVariables
     */
    public Response processStack(Dialogue d){
        String focus=unknownResponse;
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

    /***
     *
     * @param i : intent to check
     * @param slotname : slot we are concerned with
     * @return List<String> values: these are valid values associated with slotname (which may be used in update or add)
     * Check whether the slot values associated with the specified slotname are valid
     * Specifically, add default values if none are present.  Filter out any which do not pass IsValidValue()
     */
    public static List<String> validate(Intent i, String slotname){
        List<String> values = i.getSlotValuesByType(slotname);
        if (values.isEmpty()){
            if(defaultvalue(slotname)!=null) {
                i.fillSlot(slotname, defaultvalue(slotname));
                values.add(defaultvalue(slotname));
            }
        } else {
            //check values are valid - currently assume all ok
            values.stream().forEach(value -> System.err.println(slotname+" : " + value));
            values=values.stream().filter(value->isValidValue(slotname, value)).collect(Collectors.toList());
        }
        return values;
    }

    /****
     *
     * @param accepting : >0 => yes, <0 => no, 0 => don't know
     * @param values : new values
     * @param slotname
     * @param d
     * update the top working intent with the list of values given for the specified slot.
     * accepting is used to check whether the list of values should match those already there (in which case they will be added and the user asked to choose) or automatically replace them
     *
     */
    public static void update(int accepting, List<String> values, String slotname, Dialogue d) {

        if(values.isEmpty()){
            d.pushFocus(TaxiServiceHandler.respecifyResponse);
            d.putToWorkingMemory("slot_to_choose",slotname);
        } else {
            if (accepting > 0) {
                //check matches working intent
                if (values.size()==1&&d.peekTopIntent().getSlotValuesByType(slotname).stream().filter(value -> value.equals(values.get(0))).count() > 0) {
                    //ok
                    d.peekTopIntent().replaceSlot(new Intent.Slot(slotname, values.get(0), 0, 0)); //replace multiple options if present
                } else {
                    values.stream().forEach(newvalue->d.peekTopIntent().fillSlot(new Intent.Slot(slotname, newvalue, 0, 0)));
                    d.pushFocus(TaxiServiceHandler.chooseResponse); //multiple possibilities for capacity so choose
                    d.putToWorkingMemory("slot_to_choose",slotname);
                }

            } else {
                //replace info in working intent - if accepting not known, assuming rejection
                d.peekTopIntent().clearSlots(slotname);
                values.stream().forEach(newvalue -> d.peekTopIntent().fillSlot(new Intent.Slot(slotname, newvalue, 0, 0)));
                if (values.size() > 1) {
                    d.pushFocus(TaxiServiceHandler.chooseResponse);
                    d.putToWorkingMemory("slot_to_choose", slotname);
                }
                if (d.peekTopFocus().equals(TaxiServiceHandler.confirmCompletionResponse)) {
                    d.pushFocus(TaxiServiceHandler.confirmResponse);
                }
            }

        }
    }

    /**
     *
     * @param slotname
     * @return
     * Simple method to provide default values for some slots
     */
    private static String defaultvalue(String slotname){
        switch(slotname){
            case TaxiServiceHandler.capacitySlot:
                return "4";
            case TaxiServiceHandler.timeSlot:
                return "ASAP";
            default:
                return null;
        }

    }

    /**
     *
     * @param slotname
     * @param value
     * @return
     * Simple method to validate values for some slots
     */
    private static boolean isValidValue(String slotname, String value){
        switch(slotname){
            case TaxiServiceHandler.capacitySlot:
                return isValidCapacity(value);
            case TaxiServiceHandler.timeSlot:
                return isValidTime(value);
            default:
                return isValidLocation(value);
        }
    }

    private static boolean isValidLocation(String value){
        return true;
    }

    private static boolean isValidTime(String value){
        return true;
    }

    private static boolean isValidCapacity(String value){
        try {
            int number = Integer.parseInt(value);
            if(number>0&&number<8){
                return true;
            } else {
                return false;
            }
        }
        catch(NumberFormatException e){
            return false;
        }
    }
}
