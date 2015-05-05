package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.IntentMatch;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.HandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.ProductSearchHandlerFactory;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;

import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by juliewe on 05/05/2015.
 */
public class ProductSearchHandler extends Handler {

    //db settings provided by the json file product_search_handler.json
    private String dbHost;
    private String dbPort;
    private String dbName;
    protected transient ProductMongoDB db;

    //analyser names


    //intent names


    //slot names

    public ProductSearchHandler(){
        //register problem and intent handlers here
    }

    @Override
    public HandlerFactory getFactory() {
        return new ProductSearchHandlerFactory();
    }

    @Override
    public Dialogue getNewDialogue(String dialogueId){

        Dialogue d = new Dialogue(dialogueId);
        //d.setState("initial");

        return d;
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
    public void close() throws Exception {
        // Close any resources here (like database)
        db.close();
    }

    @Override
    public List<Intent> preProcessIntents(List<Intent> intents, List<IntentMatch> matches, Dialogue d){
        //do any preprocessing/filtering of intents here before the dialoguer gets to auto-query etc
        return intents;
    }

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue){
        //how to handle a list of intents
        return new Response("unknown");
    }
}
