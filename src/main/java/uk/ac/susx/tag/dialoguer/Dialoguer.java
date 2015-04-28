package uk.ac.susx.tag.dialoguer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.IntentMatch;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.User;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.SimplePatterns;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.Stopwords;
import uk.ac.susx.tag.dialoguer.utils.JsonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The definition of a Dialoguer task.
 *
 * Should be in a JSON file. Should be a large JSON object, with all the following properties.
 * So all of the following properties appear inside one set of curly braces.
 *
 * I.e. the skeleton looks like:
 *
 * {
 *   handler : {},
 *   analysers : [],
 *   responseTemplates : {},
 *   necessarySlotsPerIntent: {},
 *   humanReadableSlotNames: {}
 * }
 *
 * Definition includes:
 *
 * --------- Setup -----------------------------------
 *
 * handler  : {
 *   name : <string name of the handler>
 *   path : <path to JSON file definition for handler>
 * }
 *
 * analysers : [
 *   {
 *     name: <String name of analyser>,
 *     path: <string path to JSON file definition for handler
 *     sourceId : <String ID that is given as the source to any intent produced by this analyser. Optional.>
 *   },
 *   ... (as many analysers as you like)...
 * ]
 * --------- Responses -------------------------------
 *
 * Available responses are defined in a JSON object:
 *
 *   responseTemplates : {
 *     responseName1 : {
 *       templates : [string, alternative_1, alternative2, ...]
 *     },
 *     ... (as many responses as you like, each with a unique response name (notice this is an object map, not an array)...
 *   }
 *
 * Each response name refers to a type of response that the system can make. Each response has a list of
 * one or more templates, which are simply alternative ways of expressing that response (among which the system
 * will choose randomly). One may define variables to be filled in the template using curly braces. For example:
 *
 *   "Are you sure you want to purchase {product name}?"
 *
 * The system will require a response that contains the variable "product name", and will replace "{product name}"
 * with its value when using the template.
 *
 * There are some responses that have default templates, you can override them by specifying responses for them:
 *
 *   confirm_cancellation : the response given to the user when they have requested a cancellation of the dialogue
 *
 *
 * --------- Intents & Slots ------------------------
 *
 *   You can specify that certain slots must be filled in for a given intent. The system will automatically
 *   re-query for them if they are missing from a user message.
 *
 *   You can specify a mapping between slot names and a more human-readable portion. If so, this is the phrase
 *   that will be used when automatically querying the user about that slot.
 *
 *   necessarySlotsPerIntent : {
*       intentName1 : [ requiredSlot1, requiredSlot2, ...],
*       intentName2 : [ requiredSlot1, requiredSlot2, ...],
*       ...
 *   },
 *   humanReadableSlotNames : {
 *      slotName1 : readableSlotName1,
 *      slotName2 : readableSlotName2,
 *      ...
 *      }
 *   }
 *
 * ---------------------------------------------------
 *
 *
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 15:28
 */
public class Dialoguer implements AutoCloseable {

    public static final Random random = new Random();
    public static final Gson gson = new GsonBuilder().setPrettyPrinting()
                                        .registerTypeAdapter(Analyser.class, new JsonUtils.AnalyserAdaptor().nullSafe()) // Custom deserialisation for analysers
                                        .registerTypeAdapter(Handler.class, new JsonUtils.HandlerAdaptor().nullSafe())   // Custom deserialisation for handlers
                                        .registerTypeAdapter(Multimap.class, JsonUtils.multimapJsonSerializer())         // Custom serialisation for multimap
                                        .registerTypeAdapter(Multimap.class, JsonUtils.multimapJsonDeserializer())       // Custom deserialisation for multimap
                                        .registerTypeAdapter(ImmutableSet.class, JsonUtils.immutableSetJsonDeserializer()) // Custom deserialisation for immutableset
                                        .registerTypeAdapter(Pattern.class, new JsonUtils.PatternAdaptor().nullSafe())
                                        .create();
    private Handler handler;
    private List<Analyser> analysers;

    private Map<String, Set<String>> necessarySlotsPerIntent;
    private Map<String, String> humanReadableSlotNames;
    private Map<String, List<String>> responseTemplates;

    private Dialoguer(){
        handler = null;
        analysers = new ArrayList<>();
        necessarySlotsPerIntent = new HashMap<>();
        humanReadableSlotNames = new HashMap<>();
        responseTemplates = new HashMap<>();
    }

    public Dialogue startNewDialogue(String dialogueId){
        return handler.getNewDialogue(dialogueId);
    }

    public Dialogue interpret(String message, User user, Dialogue dialogue){

        // 0. Cache some simple string processing of the user message in the working memory of the dialogue object
        String stripped = SimplePatterns.stripAll(message);
        dialogue.putToWorkingMemory("stripped", stripped);
        dialogue.putToWorkingMemory("strippedNoStopwords", Stopwords.removeStopwords(stripped));

        // 1. Add the new user message to the dialogue object
        dialogue.addNewUserMessage(message, user);

        Response r; // To be filled with system response

        // 2. Determine user intent (largely ignored if we're auto-querying, see below)
        List<Intent> intents = new ArrayList<>();
        for (Analyser analyser : analysers) {

            List<Intent> analysis = analyser.analyse(message, dialogue);
            System.err.println("Using analyser: "+analyser.getName()+" generated intents: "+analysis.size());
            for (Intent intent : analysis)
                intent.setSource(analyser.getSourceId());  // Source of the intent is the position of the analyser that produced it in the array of analysers
            intents.addAll(analysis);
        }
        intents = handler.preProcessIntents(intents, dialogue);

        // 3. Check to see if there is a cancellation intent, short-circuiting and finishing the dialogue
        if (isCancellationPresent(intents)){

            // 4. Complete and cancel dialogue
            dialogue.complete();
            r = Response.buildCancellationResponse();
        } else {
            boolean isCancelAutoQueryPresent = isCancelAutoQueryPresent(intents);
            // 5. If dialogue was waiting for auto query response
            if (dialogue.isExpectingAutoRequestResponse()){

                // 6. If any of the analysers decided that it was appropriate to short-circuit the auto-query process
                if (isCancelAutoQueryPresent){

                    // 7. Add all incomplete/complete intents being tracked to the intents list
                    intents.addAll(dialogue.popAutoQueriedIntents());

                    // 8. Let the handler deal with the intents
                    r = handleNewIntents(intents, dialogue, false);
                }
                // 13. Otherwise fill the appropriate slot on the awaiting intent
                else {
                    dialogue.fillAutoRequest(message);

                    // 14. If all waiting intents are now complete, pass the the finished intents to the handler for an appropriate response (ignoring the other intents found by analysers)
                    if (!dialogue.isExpectingAutoRequestResponse())
                        r = handler.handle(dialogue.popAutoQueriedIntents(), dialogue);

                    // 15. Otherwise build the next auto query
                    else r = Response.buildAutoQueryResponse(getHumanReadableSlotNameIfPresent(dialogue.getNextAutoQuery()));
                }
            }
            // 16. Otherwise pay attention to what the analysers decide on the intents that the user is trying to convey, let handler deal so long as necessary slots are filled
            else r = handleNewIntents(intents, dialogue, !isCancelAutoQueryPresent);
        }

        // 17. Add the response to the dialogue object

        dialogue.addNewSystemMessage(fillTemplateWithResponse(r));

        // 18. Extract the new states from the response if there is one and put the dialogue in those states
        if (r.areNewStates())
            dialogue.setStates(r.getNewStates());

        // Return the updated dialogue
        return dialogue;
    }

    private Response handleNewIntents(List<Intent> intents, Dialogue dialogue, boolean autoQueryTracking){
        // 9. Find which necessary slots are not filled
        List<IntentMatch> intentMatches = intents.stream()
                .map(intent -> intent.getIntentMatch(necessarySlotsPerIntent.get(intent.getName())))
                .collect(Collectors.toList());

        // 10. If all the necessary slots for the intents are filled, and the analysers haven't disabled auto-querying
        if (!autoQueryTracking || IntentMatch.areSlotsFilled(intentMatches)){

            // 11. Ask the handler for a response to these intents
            return handler.handle(intents, dialogue);
        }
        // 12. otherwise track intents and produce auto query
        else {
            dialogue.trackNewAutoQueryList(intentMatches);
            return Response.buildAutoQueryResponse(getHumanReadableSlotNameIfPresent(dialogue.getNextAutoQuery()));
        }
    }

    public static Dialoguer loadDialoguerFromJsonResourceOrFile(String dialoguerDefinition) throws IOException {
        Dialoguer d =  readObjectFromJsonResourceOrFile(dialoguerDefinition, Dialoguer.class);
        d.validateAnalyserIdsOrThrow(d.handler.getRequiredAnalyserSourceIds());
        return d;
    }

    /**
     * Read an arbitrary object from a JSON file using the Dialoguer's custom Gson instance.
     *
     * DEPRECATED WARNING: use the more general purpose readObjectFromJsonResourceOrFile() method.
     */
    @Deprecated
    public static <T> T readFromJsonFile(File json, Class<T> klazz) throws IOException {
        try (JsonReader r = new JsonReader(new BufferedReader(new InputStreamReader(new FileInputStream(json), "UTF8")))) {
            return gson.fromJson(r, klazz);
        }
    }

    /**
     * Read an arbitrary object from a JSON file using the Dialoguer's custom Gson instance.
     *
     * The aim is for JSON files to be able to specify paths to resources, and have them easily be found wherever they
     * may be. So whereas readFromJsonFile() only supported proper paths to actual files, this method will be extended to
     * have more power.
     *
     * It currently searches for the resource using the resource path in 2 ways:
     *
     *   1. A path to a normal file
     *   2. A path to a resource in the classpath
     */
    public static <T> T readObjectFromJsonResourceOrFile(String resourcePath, Class<T> klazz) throws IOException{
        try (JsonReader r = new JsonReader(new BufferedReader(new InputStreamReader(getResourceOrFileStream(resourcePath), "UTF8")))) {
            return gson.fromJson(r, klazz);
        }
    }

    /**
     * Use exactly like the Gsonvariant. In other words, if you are trying to deserialise a generic type, you must pass that type in.
     * You can do that using Gson's TypeToken. E.g.
     *
     * Type type = new TypeToken<Map<String, String>>(){}.getType();
     * Map<String, String> map = readObjectFromJsonResourceOrFile("resource/path.json", type);
     */
    public static <T> T readObjectFromJsonResourceOrFile(String resourcePath, Type typeOfT) throws IOException {
        try (JsonReader r = new JsonReader(new BufferedReader(new InputStreamReader(getResourceOrFileStream(resourcePath), "UTF8")))) {
            return gson.fromJson(r, typeOfT);
        }
    }

    public static InputStream getResourceOrFileStream(String resourcePath) throws IOException {
        try {
            return Resources.getResource(resourcePath).openStream();
        } catch (IllegalArgumentException e){
            return new FileInputStream(new File(resourcePath));
        }
    }

    @Override
    public void close() throws Exception {
        handler.close();
        for (Analyser a : analysers)
            a.close();
    }

    public static class DialoguerException extends RuntimeException{
        public DialoguerException(String msg) {
            super(msg);
        }
        public DialoguerException(Throwable cause){
            super(cause);
        }
        public DialoguerException(String msg, Throwable cause){
            super(msg, cause);
        }
    }

    /**
     * Return true if one of the intents is the default cancel intent
     */
    private boolean isCancellationPresent(List<Intent> intents){
        return intents.stream().anyMatch((intent) -> intent.isName(Intent.cancel));
    }

    /**
     * Return true if one of the intents is the default cancel auto query intent
     */
    private boolean isCancelAutoQueryPresent(List<Intent> intents){
        return intents.stream().anyMatch((intent) -> intent.isName(Intent.cancelAutoQuery));
    }

    private String getHumanReadableSlotNameIfPresent(String slotName){
        return humanReadableSlotNames.containsKey(slotName) ? humanReadableSlotNames.get(slotName) : slotName;
    }

    private String fillTemplateWithResponse(Response r){
        List<String> alternatives = responseTemplates.get(r.getResponseName());
        // If the user has defined an appropriate template in the Dialoguer JSON config file, use it.
        if (alternatives != null){
            if (alternatives.size() == 1)
                return r.fillTemplate(alternatives.get(0));
            else
                return r.fillTemplate(alternatives.get(random.nextInt(alternatives.size())));
        }
        // Otherwise see if it's a default response that we can handle.
        else if (r.getResponseName().equals(Response.defaultConfirmCancelResponseId)) {
            return r.fillTemplate("Cancelled. Thank you!");
        } else if (r.getResponseName().equals(Response.defaultCompletionResponseId)) {
            return r.fillTemplate("Thanks, goodbye!");
        } else if (r.getResponseName().equals(Response.defaultAutoQueryResponseId)) {
            return r.fillTemplate("Please specify {query}.");
        }
        // Otherwise give up
        else throw new DialoguerException("No response template found for this response name: " + r.getResponseName());
    }

    private void validateAnalyserIdsOrThrow(Set<String> requiredSourceIds){
        if (!Sets.difference(
                requiredSourceIds,
                Sets.newHashSet(analysers.stream()
                        .map(Analyser::getSourceId)
                        .collect(Collectors.toList())))
             .isEmpty())
            throw new DialoguerException("Handler requires Analysers with the following source IDs: "
                    + requiredSourceIds.stream().collect(Collectors.joining(", ")));
    }
}
