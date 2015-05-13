package uk.ac.susx.tag.dialoguer;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.common.reflect.TypeToken;
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
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.RuleBasedHandler;
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
 * Each response is an object that needs that one "templates" attribute. However, there are two further attributes that
 * can optionally be specified:
 *
 *   newStates : A list of string states that the dialogue should be placed in when this response is given.
 *   requestingYesNo : Either string "yes" or "no" to put the dialogue in the state of having requested a yes/no from the user
 *
 * When these options are not specified, the current states will be left untouched.
 *
 * These can be overriden even if present in the handler when it creates a response, by specifying what the states should be.
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

    private static final Random random = new Random();
    public static final Gson gson = new GsonBuilder().setPrettyPrinting()
                                        .registerTypeAdapter(Analyser.class, new JsonUtils.AnalyserAdaptor().nullSafe()) // Custom deserialisation for analysers
                                        .registerTypeAdapter(Handler.class, new JsonUtils.HandlerAdaptor().nullSafe())   // Custom deserialisation for handlers
                                        .registerTypeAdapter(Multimap.class, JsonUtils.multimapJsonSerializer())         // Custom serialisation for multimap
                                        .registerTypeAdapter(Multimap.class, JsonUtils.multimapJsonDeserializer())       // Custom deserialisation for multimap
                                        .registerTypeAdapter(ImmutableSet.class, JsonUtils.immutableSetJsonDeserializer()) // Custom deserialisation for immutableset
                                        .registerTypeAdapter(Pattern.class, new JsonUtils.PatternAdaptor().nullSafe())
                                        .registerTypeAdapter(Expression.class, new JsonUtils.ExpressionAdaptor().nullSafe())
                                        .registerTypeAdapter(RuleBasedHandler.ResponseRule.class, new JsonUtils.ResponseRuleAdaptor().nullSafe())
                                        .registerTypeAdapter(new TypeToken<List<String>>() { }.getType(), new JsonUtils.ArrayListAdaptor().nullSafe())
                                        .registerTypeAdapter(new TypeToken<ArrayList<String>>() { }.getType(), new JsonUtils.ArrayListAdaptor().nullSafe())
                                    .create();
    private Handler handler;
    private List<Analyser> analysers;

    private Map<String, Set<String>> necessarySlotsPerIntent;
    private Map<String, String> humanReadableSlotNames;
    private Map<String, ResponseTemplate> responseTemplates;

    private Dialoguer(){
        handler = null;
        analysers = new ArrayList<>();
        necessarySlotsPerIntent = new HashMap<>();
        humanReadableSlotNames = new HashMap<>();
        responseTemplates = new HashMap<>();
    }

    public static void main(String[] args) throws IOException {
        Dialoguer d = Dialoguer.loadDialoguerFromJsonResourceOrFile("example_dialoguer.json");
        System.out.println();
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
            //System.err.println("Using analyser: "+analyser.getName()+" generated intents: "+analysis.size());
            for (Intent intent : analysis)
                intent.setSource(analyser.getSourceId());  // Source of the intent is the position of the analyser that produced it in the array of analysers
            intents.addAll(analysis);
        }

        List<IntentMatch> intentMatches = intents.stream()
                .map(intent -> intent.getIntentMatch(necessarySlotsPerIntent.get(intent.getName())))
                .collect(Collectors.toList());

        intents = handler.preProcessIntents(intents, intentMatches, dialogue);

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
                    if (!dialogue.isExpectingAutoRequestResponse()) {

                        r = handler.handle(dialogue.popAutoQueriedIntents(), dialogue);
                    }

                    // 15. Otherwise build the next auto query
                    else {

                        r = Response.buildAutoQueryResponse(getHumanReadableSlotNameIfPresent(dialogue.getNextAutoQuery()));

                    }
                }
            }
            // 16. Otherwise pay attention to what the analysers decide on the intents that the user is trying to convey, let handler deal so long as necessary slots are filled
            else r = handleNewIntents(intents, dialogue, !isCancelAutoQueryPresent);
        }

        // 17. Add the response to the dialogue object and update states
        applyResponse(r, dialogue);

        // Return the updated dialogue
        return dialogue;
    }

    private Response handleNewIntents(List<Intent> intents, Dialogue dialogue, boolean autoQueryTracking){
        // 9. Find which necessary slots are not filled (re-process because there could be more intents)
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
        validateDialoguerConfig(dialoguerDefinition);
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

    public static class ResponseTemplate {

        public List<String> templates;
        public List<String> newStates;
        public String requestingYesNo;

        public ResponseTemplate(){
            templates = new ArrayList<>();
            newStates = null;
            requestingYesNo = null;
        }

        public boolean areNewStates() { return newStates != null; }
        public List<String> getNewStates() {return newStates; }
        public boolean isSettingRequestingYesNoState() { return requestingYesNo != null; }
        public boolean getRequestingYesNo() {
            switch (requestingYesNo) {
                case "yes": return true;
                case "no" : return false;
                default: throw new DialoguerException("The 'requestingYesNo' property of a response template must be equals to 'yes' or 'no', or be non-existent");
            }
        }
        public boolean hasMultipleAlternatives() { return templates.size() > 1; }
        public String getRandomTemplateExample() {
            //System.err.println(templates.size());
            if (hasMultipleAlternatives()){
                String t = templates.get(random.nextInt(templates.size()));
                //System.err.println(t);
                return t;
            } else {
                return templates.get(0);
            }
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

    private void applyResponse(Response r, Dialogue dialogue){
        // If there's a template for this response
        if (responseTemplates.containsKey(r.getResponseName())){
            // Get the template
            ResponseTemplate template = responseTemplates.get(r.getResponseName());
            //System.err.println(template.toString());
            dialogue.addNewSystemMessage(r.fillTemplate(template.getRandomTemplateExample()));

            if (template.areNewStates())
                dialogue.setStates(template.newStates);
            if (template.isSettingRequestingYesNoState())
                dialogue.setRequestingYesNo(template.getRequestingYesNo());
        }
        // Otherwise check for default response templates
        else if (r.getResponseName().equals(Response.defaultConfirmCancelResponseId)) {
            dialogue.addNewSystemMessage(r.fillTemplate("Cancelled. Thank you!"));
        } else if (r.getResponseName().equals(Response.defaultCompletionResponseId)) {
            dialogue.addNewSystemMessage(r.fillTemplate("Thanks, goodbye!"));
        } else if (r.getResponseName().equals(Response.defaultAutoQueryResponseId)) {
            dialogue.addNewSystemMessage(r.fillTemplate("Please specify {query}."));
        } else if (r.getResponseName().equals(Response.defaultUnableToProcessResponseId)) {
            dialogue.addNewSystemMessage(r.fillTemplate("I'm sorry; I don't understand."));
        }
        //Otherwise give up
        else throw new DialoguerException("No response template found for this response name: " + r.getResponseName());

        // Response can override the states specified by the template
        if (r.isOverridingTemplate()){
            dialogue.setStates(r.getNewStates());
            dialogue.setRequestingYesNo(r.isRequestingYesNo());
        }
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

    private static void validateDialoguerConfig(String resourcePath) throws IOException {
        boolean valid = true;

        Map<String, Object> obj = Dialoguer.readObjectFromJsonResourceOrFile(resourcePath, new TypeToken<Map<String, Object>>(){}.getType());
        Set<String> allowableTopLevelFields = Sets.newHashSet("handler", "analysers", "humanReadableSlotNames", "necessarySlotsPerIntent", "responseTemplates");
        Set<String> necessaryTopLevelFields = Sets.newHashSet("handler", "analysers");
        Set<String> handlerAnalyserFields = Sets.newHashSet("name", "path", "sourceId");
        Set<String> allowableResponseTemplateFields = Sets.newHashSet("templates", "newStates", "requestingYesNo");


        if (!Sets.difference(necessaryTopLevelFields, obj.keySet()).isEmpty()) {
            System.err.println("Config object must define the following fields: " + necessaryTopLevelFields); valid = false;
        }

        if (!Sets.difference(obj.keySet(), allowableTopLevelFields).isEmpty()) {
            System.err.println("Config has unexpected fields: " + Sets.difference(obj.keySet(), allowableTopLevelFields)); valid = false;
        }

        if (obj.containsKey("handler")){
            Map<String, String> handler = (Map<String, String>) obj.get("handler");
            if (!Sets.difference(handler.keySet(), handlerAnalyserFields).isEmpty()) {
                System.err.println("Handler has unexpected fields: " + Sets.difference(handler.keySet(), handlerAnalyserFields)); valid = false;
            }
            if (!Sets.difference( Sets.newHashSet("name"), handler.keySet()).isEmpty()) {
                System.err.println("Handler requires the following fields: " + Sets.newHashSet("name")); valid = false;
            }
        }

        if (obj.containsKey("analysers")){
            if (!(obj.get("analysers") instanceof List)) {
                System.err.println("Analysers must be list of objects.");
                throw new DialoguerException("Config file malformed.");
            }
            List<Map<String, String>> analysers = (List<Map<String, String>>) obj.get("analysers");
            for (Map<String, String> analyser : analysers){
                if (!Sets.difference(analyser.keySet(), handlerAnalyserFields).isEmpty()) {
                    System.err.println("Analyser has unexpected fields: " + Sets.difference(analyser.keySet(), handlerAnalyserFields)); valid = false;
                }
                if (!Sets.difference(Sets.newHashSet("name"), analyser.keySet()).isEmpty()) {
                    System.err.println("Analyser requires the following fields: " + Sets.newHashSet("name")); valid = false;
                }
            }
        }

        if (obj.containsKey("responseTemplates")){
            if (!(obj.get("responseTemplates") instanceof Map)) {
                System.err.println("responseTemplates must be a map from response name strings, to response template objects.");
                throw new DialoguerException("Config file malformed.");
            }
            Map<String, Map<String, Object>> responseTemplates = (Map<String, Map<String, Object>>) obj.get("responseTemplates");
            for (Map<String, Object> responseAtts : responseTemplates.values()){
                if (!Sets.difference(responseAtts.keySet(), allowableResponseTemplateFields).isEmpty()) {
                    System.err.println("Response templates has unexpected fields: " + Sets.difference(responseAtts.keySet(), allowableResponseTemplateFields)); valid = false;
                }
                if (!Sets.difference(Sets.newHashSet("templates"), responseAtts.keySet()).isEmpty()) {
                    System.err.println("Analyser requires the following fields: " + Sets.newHashSet("templates")); valid = false;
                }
            }
        }

        if (!valid) throw new DialoguerException("Config file malformed.");
    }


}
