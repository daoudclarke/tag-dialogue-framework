package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers;

import org.reflections.Reflections;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.HandlerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * It is the function of the Handler to determine what response to give to a user's intents, and what side-effects to
 * perform (e.g. product search/purchase).
 *
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 15:00
 */
public abstract class Handler implements AutoCloseable {

    private Map<String, IntentHandler> intentHandlers = new HashMap<>();

    /**
     * Given a list of new intents, and the dialogue thusfar, determine the response to give.
     *
     * You can store intents on the dialogue (e.g. add or replace these new intents), and otherwise modify the dialogue
     * to track choices you're giving to the user, and other data.
     *
     * Build a response object (see Response documentation), with the appropriate template name and variables, and any
     * new states that the dialogue should be put it (done automatically for you).
     */
    public abstract Response handle(List<Intent> intents, Dialogue dialogue);

    /**
     * This is called by the Dialoguer at the initiation of a new user Dialogue, in order to obtain the initial state
     * of the Dialogue object for this particular dialogue task.
     */
    public abstract Dialogue getNewDialogue(String dialogueId);

    /**
     * The handler must be able to provide a new instance of the type of HandlerFactory that can produce this type
     * of handler.
     */
    public abstract HandlerFactory getFactory();

    public static interface IntentHandler {
        public Response handle(Intent intent, Dialogue dialogue);
    }

    protected void registerIntentHandler(String intentName, IntentHandler h){
        intentHandlers.put(intentName, h);
    }

    protected Response applyIntentHandler(Intent intent, Dialogue d){
        if (intentHandlers.containsKey(intent.getName())){
            return intentHandlers.get(intent.getName()).handle(intent, d);
        } else return null;
    }

    protected Map<Integer, Response> applyIntentHandlers(List<Intent> intents, Dialogue d){
        Map<Integer, Response> intentIndexToResponse = new HashMap<>();
        for (int i = 0; i < intents.size(); i++){
            Intent intent = intents.get(i);
            if (intentHandlers.containsKey(intent.getName())){
                intentIndexToResponse.put(i, intentHandlers.get(intent.getName()).handle(intent, d));
            }
        } return intentIndexToResponse;
    }


    /**
     * Get the name of the factory that can produce this type of handler.
     */
    public String getName(){
        return getFactory().getName();
    }

    /**
     * Get a new instance of a handler with a specified name, using the setup file given.
     */
    public static Handler getHandler(String handlerName, File handlerSetupJson) throws IOException, IllegalAccessException, InstantiationException {
        Reflections reflections = new Reflections("uk.ac.susx.tag.dialoguer.dialogue.handling.factories");

        Set<Class<? extends HandlerFactory>> foundHandlerFactories = reflections.getSubTypesOf(HandlerFactory.class);

        for (Class<? extends HandlerFactory> klass : foundHandlerFactories){
            HandlerFactory handlerFactory = klass.newInstance();
            if (handlerFactory.getName().equals(handlerName)) {
                return handlerFactory.readJson(handlerSetupJson);
            }
        } throw new IOException("Unable to load handler; handler name not found.");
    }
}
