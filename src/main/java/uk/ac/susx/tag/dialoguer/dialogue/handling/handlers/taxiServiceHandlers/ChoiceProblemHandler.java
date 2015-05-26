package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.taxiServiceHandlers;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.TaxiServiceHandler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Product;


import java.util.List;
import java.util.stream.Collectors;

import static uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler.ProblemHandler;

/**
 * The user has chosen between options offered.
 * Created by juliewe on 19/05/2015.
 */
public class ChoiceProblemHandler implements ProblemHandler {

    /**
     *
     * @param intents
     * @param dialogue
     * @return
     * Do any of the intents match one of the choiceIntents from the simpleChoiceAnalyser?
     */
    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        return intents.stream().anyMatch(i-> (TaxiServiceHandler.choiceIntents.contains(i.getName())&&i.getSource().equals(TaxiServiceHandler.simpleChoiceAnalyser)));
    }

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        return null;
    }


    /**
     *
     * @param intents
     * @param dialogue
     * @param resource
     * @return
     * Get the relevant orderTaxi intent from the list and add it to working intents
     * Handle the choice intent according to its name
     */
    @Override
    public boolean subhandle(List<Intent> intents, Dialogue dialogue, Object resource) {
        Intent i = intents.stream().filter(intent->intent.getSource().equals(TaxiServiceHandler.simpleChoiceAnalyser)).findFirst().orElse(null);
        dialogue.addToWorkingIntents(intents.stream().filter(intent->intent.isName(TaxiServiceHandler.orderTaxiIntent)).collect(Collectors.toList())); //save any orderTaxiIntents to working intents
        if(dialogue.isEmptyWorkingIntents()){ // this should not happen because this intents require the "followup" state to be set
            throw new Dialoguer.DialoguerException("Choice intent generated when no orderTaxiIntents present");
        }

        try {
            switch (i.getName()) {
                case Intent.choice:
                    handleChoice(i, dialogue);
                    break;
                case Intent.nullChoice:
                    handleNullChoice(i, dialogue);
                    break;
                case Intent.noChoice:
                    handleNoChoice(i,dialogue);



            }
        } catch (Exception e){
            throw new Dialoguer.DialoguerException("Not in handleable state for ChoiceProblemHandler "+e.toString());
        }
        return true;

    }


    /**
     *
     * @param i
     * @param d
     *
     * Find the chosenText from the choices stored in the dialogue.  Match this to one of the values to be chosen from in the top working intent.
     * Replace the slot and clear choices.
     * If there are no other items on the stack other than completion, request a confimation before completion
     *
     */
    private static void handleChoice(Intent i, Dialogue d){
        String choice=i.getSlotValuesByType(TaxiServiceHandler.choiceSlot).stream().findFirst().orElse(null);
        try{
            String chosenText=d.getChoices().get(Integer.parseInt(choice));
            String value = d.peekTopIntent().getSlotValuesByType(d.getFromWorkingMemory("slot_to_choose")).stream().filter(v->v.equals(chosenText)).findFirst().orElse(null);
            d.peekTopIntent().replaceSlot(new Intent.Slot(d.getFromWorkingMemory("slot_to_choose"),value,0,0));
            d.clearChoices();
            if(d.peekTopFocus().equals(TaxiServiceHandler.confirmCompletionResponse)){ // if this is the last question, rerequest final confirmation
                d.pushFocus(TaxiServiceHandler.confirmResponse);
            }
        } catch (Exception e){

            throw new Dialoguer.DialoguerException("Cannot match choice "+e.toString());
        }


    }

    /***
     *
     * @param i
     * @param d
     * If the user rejects all choices, ask them to respecify the slot under consideration
     */
    private static void handleNullChoice(Intent i, Dialogue d){
        d.pushFocus(TaxiServiceHandler.respecifyResponse);
        d.clearChoices();

    }

    /***
     *
     * @param i
     * @param d
     * If the user does not choose, repeat the request for a choice (along with an instruction as to how to quit)
     */
    private static void handleNoChoice(Intent i, Dialogue d){
        d.pushFocus(TaxiServiceHandler.repeatChoiceResponse);

    }
}
