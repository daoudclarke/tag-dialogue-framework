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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * It is the function of the Handler to determine what response to give to a user's intents, and what side-effects to
 * perform (e.g. product search/purchase) as a result of those intents.
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


    /**
     * Interface defining something which can take an intent and current dialogue, and produce an appropriate response
     * with no other information.
     *
     * These can be registered with the handler in a no-args constructor of a subclassing Handler using the
     * registerIntentHandler method. They are registered with the name of the intent that they can handle.
     *
     * From the handle method of the Handler you can make calls to the applyIntentHandler methods, to farm out the
     * processing of Intents to the IntentHandlers.
     *
     * resource may for example be a DB that is needed to handle some intents
     */
    public static interface IntentHandler {
        public Response handle(Intent intent, Dialogue dialogue, Object resource);
    }

    /**
     * see IntentHandler interface comments.
     */
    protected void registerIntentHandler(String intentName, IntentHandler h){
        intentHandlers.put(intentName, h);
    }

    /**
     * see IntentHandler interface comments.
     */
    protected Response applyIntentHandler(Intent intent, Dialogue d, Object resource){
        if (intentHandlers.containsKey(intent.getName())){
            return intentHandlers.get(intent.getName()).handle(intent, d, resource);
        } else return null;
    }


    /**
     * see IntentHandler interface comments.
     */
    protected Map<Integer, Response> applyIntentHandlers(List<Intent> intents, Dialogue d, Object resource){
        Map<Integer, Response> intentIndexToResponse = new HashMap<>();
        for (int i = 0; i < intents.size(); i++){
            Intent intent = intents.get(i);
            if (intentHandlers.containsKey(intent.getName())){
                intentIndexToResponse.put(i, intentHandlers.get(intent.getName()).handle(intent, d, resource));
            }
        } return intentIndexToResponse;
    }

    /**
     * Return a list that contains those Intents in *intents* that CANNOT be handled by the intent handlers.
     */
    protected List<Intent> filterOutHandleableIntents(List<Intent> intents){
        return intents.stream()
                .filter(i -> !intentHandlers.containsKey(i.getName()))
                .collect(Collectors.toList());
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
    public static Handler getHandler(String handlerName, String handlerSetupJson) throws IOException, IllegalAccessException, InstantiationException {
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
