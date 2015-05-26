package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.productSearchIntentHandlers;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.ProductSearchHandler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Product;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;

import java.util.List;

/**
 * Created by juliewe on 12/05/2015.
 */
public class ChoiceProblemHandler implements Handler.ProblemHandler {
    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        return intents.stream().anyMatch(i-> (ProductSearchHandler.choiceIntents.contains(i.getName())&&i.getSource().equals(ProductSearchHandler.simpleChoiceAnalyser)));

    }

    @Override
    public void handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        Intent i = intents.stream().filter(intent->intent.getSource().equals(ProductSearchHandler.simpleChoiceAnalyser)).findFirst().orElse(null);
        try {
            switch (i.getName()) {
                case Intent.choice:
                    handleChoice(i, dialogue, ProductSearchHandler.castDB(resource));
                    break;
                case Intent.nullChoice:
                    handleNullChoice(intents, dialogue, ProductSearchHandler.castDB(resource));
                    break;
                case Intent.noChoice:
                    handleNoChoice(dialogue);



            }
        } catch (Exception e){
            throw new Dialoguer.DialoguerException("Not in handleable state for ChoiceProblemHandler "+e.toString());
        }

    }

    private void handleNullChoice(List<Intent> intents, Dialogue d, ProductMongoDB db){
        d.clearChoices();
        ConfirmProductHandler.handleReject(d,ProductSearchHandler.productIdSlot); //add current productIds to rejected list
        boolean updated = ConfirmProductHandler.handleUpdate(intents,d,db,-1);
        if(!updated){
            d.peekTopIntent().fillSlots(BuyProblemHandler.handleProduct(d.peekTopIntent(), d, db));
            if(d.getFromWorkingMemory("focus")!=null){
                d.pushFocus(d.getFromWorkingMemory("focus"));
                d.putToWorkingMemory("focus",null);
            }
        }


    }
    private void handleNoChoice(Dialogue d){
        d.pushFocus("repeat_choice");
    }
    private void handleChoice(Intent i,Dialogue d,ProductMongoDB db){
        String choice=i.getSlotValuesByType("choice").stream().findFirst().orElse(null);
        try{
            String chosenText=d.getChoices().get(Integer.parseInt(choice));
            Intent workingIntent=d.popTopIntent();
            Product pr =db.getProductList(workingIntent.getSlotValuesByType(ProductSearchHandler.productIdSlot)).stream().filter(p->p.toShortString().equals(chosenText)).findFirst().orElse(null);
            workingIntent.replaceSlot(new Intent.Slot(ProductSearchHandler.productIdSlot,pr.getProductId(),0,0));
            d.addToWorkingIntents(workingIntent);
            d.clearChoices();
            //shouldn't need to push a focus - should be there from before!

        } catch (Exception e){

            throw new Dialoguer.DialoguerException("Cannot match chosen product "+e.toString());
        }


    }

}
