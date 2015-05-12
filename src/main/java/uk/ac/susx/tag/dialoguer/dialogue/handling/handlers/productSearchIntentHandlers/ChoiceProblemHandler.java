package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.productSearchIntentHandlers;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
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
    public Response handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        Intent i = intents.stream().filter(intent->intent.getSource().equals(ProductSearchHandler.simpleChoiceAnalyser)).findFirst().orElse(null);
        try {
            switch (i.getName()) {
                case Intent.choice:
                    handleChoice(i, dialogue, ProductSearchHandler.castDB(resource));
                    break;
                case Intent.nullChoice:
                    handleNullChoice(dialogue);
                    break;
                case Intent.noChoice:
                    handleNoChoice(dialogue);



            }
        } catch (Exception e){
            throw new Dialoguer.DialoguerException("Not in handleable state for ChoiceProblemHandler "+e.toString());
        }
        return ProductSearchHandler.processStack(dialogue,ProductSearchHandler.castDB(resource));
    }

    private void handleNullChoice(Dialogue d){
        d.clearChoices();
        //probably should add to rejected list and re-search for more potential products
        d.pushFocus("respecify_product");

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
            //shouldn't need to push a focus - should be there from before!

        } catch (Exception e){

            throw new Dialoguer.DialoguerException("Cannot match chosen product "+e.toString());
        }


    }
}
