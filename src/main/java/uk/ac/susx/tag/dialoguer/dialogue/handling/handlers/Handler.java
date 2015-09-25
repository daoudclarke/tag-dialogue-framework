package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.IntentMatch;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;

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
    private List<ProblemHandler> problemHandlers = new ArrayList<>();

/********************************************************
 * Functionality to be implemented by subclasses
 ********************************************************/

    /**
     * Override this method if you wish to required that there are Analysers with particular source IDs for a given
     * task. Return a set of the IDs that you require. The Dialoguer will throw an exception if requirements are not met.
     */
    public Set<String> getRequiredAnalyserSourceIds(){
        return new HashSet<>();
    }

    /**
     * This can act as a short circuit for the Dialoguer process. A call to this function is made between steps 2 and 3
     * of the Dialoguer process. I.e. once the analysers have determined the user intents, the handler is asked to
     * take a look at the intent list and perhaps decide to filter out certain intents.
     *
     * This is the best method of circumventing the auto-querying. This should be done if auto-querying is normally the
     * right thing to do, but under certain circumstances it needs to be overridden. You could for example, filter out
     * any incomplete intents, or add in a "cancel auto query" intent (see Intent docs).
     *
     * Another legitimate use of this function, is to merge intents from multiple sources, or merge intents that
     * analysers treat separately but that the handler would rather handle as one. (See IntentMerger class).
     *
     * By default, the list is left untouched.
     */
    public List<Intent> preProcessIntents(List<Intent> intents, List<IntentMatch> intentMatches, Dialogue dialogue){
        return intents;
    }

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

//    /**
//     * The handler must be able to provide a new instance of the type of HandlerFactory that can produce this type
//     * of handler.
//     */
//    public abstract HandlerFactory getFactory();

/********************************************************
 * Simple intent handling
 *
 * Appropriate when one intent maps nicely to a response
 ********************************************************/
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
        //public boolean subhandle(Intent intent, Dialogue dialogue, Object resource);
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

    //protected boolean applyIntentSubHandler(Intent intent, Dialogue d, Object resource){
    //    if (intentHandlers.containsKey(intent.getName())){
    //        return intentHandlers.get(intent.getName()).subhandle(intent, d, resource);
    //    } else return false;
    //}

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

//    /**
//     * Return a list that contains those Intents in *intents* that CANNOT be handled by the intent handlers.
//     */
//    protected List<Intent> filterOutHandleableIntents(List<Intent> intents){
//        return intents.stream()
//                .filter(i -> !intentHandlers.containsKey(i.getName()))
//                .collect(Collectors.toList());
//    }

/********************************************************
 * Problem handling
 *
 * Appropriate when the intents and dialogue state must be checked
 * to see if a particular handling is appropriate. Generally when
 * multiple overlapping intents may contribute to a single logical
 * problem.
 *
 * Generally, if more than one ProblemHandler is appropriate for
 * a problem, you're using them wrong.
 ********************************************************/

    public static interface ProblemHandler {

        /**
         * Return true if the current intents and dialogue should be handled by this handler
         */
        public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue);

        /**
         * Return the appropriate response for this state.
         */
        public void handle(List<Intent> intents, Dialogue dialogue, Object resource);
//    @Deprecated
//    public boolean subhandle(List<Intent> intents, Dialogue dialogue, Object resource);
    }

    protected void registerProblemHandler(ProblemHandler h) {
        problemHandlers.add(h);
    }

//    protected Response applyFirstProblemHandlerOrNull(List<Intent> intents, Dialogue dialogue, Object resource){
//        return problemHandlers.stream()
//                .filter(h -> h.isInHandleableState(intents, dialogue)) // Only allow through handler that can handle this state
//                .map(h -> h.handle(intents, dialogue, resource))  // Map the handler to the response it gives
//                .findFirst().orElse(null);  // Return the first one we see, otherwise if there's none, return null
//    }

//    @Deprecated
//    protected boolean applyFirstProblemSubHandlerOrNull(List<Intent> intents, Dialogue dialogue, Object resource){
//        return problemHandlers.stream()
//                .filter(h -> h.isInHandleableState(intents, dialogue)) // Only allow through handler that can handle this state
//                .map(h -> h.subhandle(intents, dialogue, resource))  // Map the handler to the response it gives
//                .findFirst().orElse(false);  // Return the first one we see, otherwise if there's none, return null
//    }

    protected boolean useFirstProblemHandler(List<Intent> intents, Dialogue dialogue, Object resource){
        ProblemHandler handler = null;
        for (ProblemHandler problemHandler : problemHandlers) {
            if (problemHandler.isInHandleableState(intents, dialogue)) {
                handler = problemHandler;
                break;
            }
        }
        if (handler != null) {
            handler.handle(intents, dialogue, resource);
            return true;
        } else return false;
    }

//    protected boolean useApplicableProblemHandlers(List<Intent> intents, Dialogue dialogue, Object resource){
//        List<ProblemHandler> handlers = problemHandlers.stream()
//                .filter(h -> h.isInHandleableState(intents, dialogue))
//                .collect(Collectors.toList());
//
//        if (!handlers.isEmpty()){
//            handlers.forEach(h -> h.handle(intents, dialogue, resource));
//            return true;
//        } else return false;
//    }
/********************************************************
 * Creation and factory related methods
 ********************************************************/

    /**
     * Get the name of the factory that can produce this type of handler.
     */
    public abstract String getName();

//    /**
//     * Get a new instance of a handler with a specified name, using the setup file given.
//     */
//    public static Handler getHandler(String handlerName, String handlerSetupJson) throws IOException, IllegalAccessException, InstantiationException {
//        Reflections reflections = new Reflections("uk.ac.susx.tag.dialoguer.dialogue.handling.factories");
//
//        Set<Class<? extends HandlerFactory>> foundHandlerFactories = reflections.getSubTypesOf(HandlerFactory.class);
//
//        for (Class<? extends HandlerFactory> klass : foundHandlerFactories){
//            HandlerFactory handlerFactory = klass.newInstance();
//            if (handlerFactory.getName().equals(handlerName)) {
//                return handlerFactory.readJson(handlerSetupJson);
//            }
//        } throw new IOException("Unable to load handler; handler name not found.");
//    }
}
