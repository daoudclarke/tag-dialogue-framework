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
 * Created by juliewe on 19/05/2015.
 */
public class ChoiceProblemHandler implements ProblemHandler {
    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        return intents.stream().anyMatch(i-> (TaxiServiceHandler.choiceIntents.contains(i.getName())&&i.getSource().equals(TaxiServiceHandler.simpleChoiceAnalyser)));
    }

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue, Object resource) {
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
        return TaxiServiceHandler.processStack(dialogue);
    }
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
    private static void handleNullChoice(Intent i, Dialogue d){

    }
    private static void handleNoChoice(Intent i, Dialogue d){
        d.pushFocus(TaxiServiceHandler.repeatChoiceResponse);

    }
}
