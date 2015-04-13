package uk.ac.susx.tag.dialoguer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.reflections.Reflections;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.HandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.SimplePatterns;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.Stopwords;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * The definition of a Dialoguer task.
 *
 * Definition includes:
 *
 * --------- Setup -----------------------------------
 *
 * handler  : {
 *     name : string name of the handler
 *     path : path to JSON file definition for handler
 * }
 *
 * analyser : {
 *     name : string name of analyser
 *     path : path to JSON file definition for analyser
 * }
 *
 * cancellation_analyser : {
 *     name : string name of analyser
 *     path : path to JSON file definition for analyser
 * }
 *
 * choice_making_analyser : {
 *     name : string name of analyser
 *     path : path to JSON file definition for analyser
 * }
 *
 * yes_no_analyser : {
 *     name : string name of analyser
 *     path : path to JSON file definition for analyser
 * }
 *
 * --------- Responses -------------------------------
 *
 *    Available responses are defined in a JSON object:
 *
 *    {
 *        responseName1 : {
 *            templates : [string, alternative_1, alternative2, ...]
 *        },
 *        responseName2 : {
 *            templates : [string, alternative_1, alternative2, ...]
 *        },
 *        ...
 *    }
 *
 *    Each response name refers to a type of response that the system can make. Each response has a list of
 *    one or more templates, which are simply alternative ways of expressing that response (among which the system
 *    will choose randomly). One may define variables to be filled in the template using curly braces. For example:
 *
 *      "Are you sure you want to purchase {product name}?"
 *
 *    The system will require a response that contains the variable "product name", and will replace "{product name}"
 *    with its value when using the template.
 *
 * --------- Intents & Slots ------------------------
 *
 *   You can specify that certain slots must be filled in for a given intent. The system will automatically
 *   re-query for them if they are missing from a user message.
 *
 *   You can specify a mapping between slot names and a more human-readable portion. If so, this is the phrase
 *   that will be used when automatically querying the user about that slot.
 *
 *   {
 *      requiredSlots : {
 *         intentName1 : [ requiredSlot1, requiredSlot2, ...],
 *         intentName2 : [ requiredSlot1, requiredSlot2, ...],
 *         ...
 *      },
 *      humanReadableSlotNames : {
 *          slotName1 : readableSlotName1,
 *          slotName2 : readableSlotName2,
 *          ...
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
public class Dialoguer {

    public static final Random random = new Random();
    public static final Gson gson = new GsonBuilder()
                                        .registerTypeAdapter(Analyser.class, new AnalyserAdaptor().nullSafe()) // Custom deserialisation for analysers
                                        .registerTypeAdapter(Handler.class, new HandlerAdaptor().nullSafe())   // Custom deserialisation for handlers
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

    public Dialogue interpret(String message, Dialogue dialogue){

        String stripped = SimplePatterns.stripAll(message);
        dialogue.putToWorkingMemory("stripped", stripped);
        dialogue.putToWorkingMemory("strippedNoStopwords", Stopwords.removeStopwords(stripped));

//        dialogue.addNewUserMessage(message, analyser.analise(message, dialogue), dialog);

        Response r = handler.handle(dialogue);
        dialogue.addNewSystemMessage(fillTemplateWithResponse(r));
        dialogue.setStates(r.getNewStates());

        return dialogue;
    }

    public static Dialoguer loadJson(String dialoguerDefinition) {
        return gson.fromJson(dialoguerDefinition, Dialoguer.class);
    }

    public static Dialoguer loadJson(File dialoguerDefinition) throws FileNotFoundException, UnsupportedEncodingException {
        return gson.fromJson(new JsonReader(new BufferedReader(new InputStreamReader(new FileInputStream(dialoguerDefinition), "UTF8"))), Dialoguer.class);
    }

    private String getHumanReadableSlotNameIfPresent(String slotName){
        return humanReadableSlotNames.containsKey(slotName) ? humanReadableSlotNames.get(slotName) : slotName;
    }

    private String fillTemplateWithResponse(Response r){
        List<String> alternatives = responseTemplates.get(r.getResponseName());
        if (alternatives != null){
            if (alternatives.size() == 1)
                return r.fillTemplate(alternatives.get(0));
            else
                return r.fillTemplate(alternatives.get(random.nextInt(alternatives.size())));
        }
        throw new DialoguerException("No response template found for this response name: " + r.getResponseName());
    }

    public static class HandlerAdaptor extends TypeAdapter<Handler> {
        public void write(JsonWriter out, Handler value) throws IOException {
            throw new UnsupportedOperationException("Cannot be written");
        }

        public Handler read(JsonReader in) throws IOException {
            String handlerName = null;
            String handlerPath = null;

            in.beginObject();
            while (in.hasNext()){
                String name = in.nextName();
                switch (name){
                    case "name": handlerName = in.nextString(); break;
                    case "path": handlerPath = in.nextString(); break;
                }
            } in.endObject();

            if (handlerName==null) throw new DialoguerException("No handler name found");
            return getHandler(handlerName, handlerPath == null ? null : new File(handlerPath));
        }
    }

    public static class AnalyserAdaptor extends TypeAdapter<Analyser> {
        public void write(JsonWriter out, Analyser value) throws IOException {
            throw new UnsupportedOperationException("Cannot be written.");
        }
        public Analyser read(JsonReader in) throws IOException {
            String analyserName = null;
            String analyserPath = null;

            in.beginObject();
            while (in.hasNext()){
                String name = in.nextName();
                switch (name){
                    case "name": analyserName = in.nextString(); break;
                    case "path": analyserPath = in.nextString(); break;
                }
            } in.endObject();

            if (analyserName==null) throw new DialoguerException("No analyser name found");
            return getAnalyser(analyserName, analyserPath==null? null : new File(analyserPath));
        }
    }

    private static Analyser getAnalyser(String analyserName, File analyserSetupJson) {
        Reflections reflections = new Reflections("uk.ac.susx.tag.dialoguer.dialogue.analysing.factories");

        Set<Class<? extends AnalyserFactory>> foundAnalyserFactories = reflections.getSubTypesOf(AnalyserFactory.class);

        for (Class<? extends AnalyserFactory> klass : foundAnalyserFactories){
            try {
                AnalyserFactory analyserFactory = klass.newInstance();
                if (analyserFactory.getName().equals(analyserName)) {
                    return analyserFactory.readJson(analyserSetupJson);
                }
            } catch (IOException | InstantiationException | IllegalAccessException e) {
                throw new DialoguerException("Unable to load analyser", e);
            }
        } throw new DialoguerException("Unable to load analyser; analyser name not found.");
    }

    private static Handler getHandler(String handlerName, File handlerSetupJson) {
        Reflections reflections = new Reflections("uk.ac.susx.tag.dialoguer.dialogue.handling.factories");

        Set<Class<? extends HandlerFactory>> foundHandlerFactories = reflections.getSubTypesOf(HandlerFactory.class);

        for (Class<? extends HandlerFactory> klass : foundHandlerFactories){
            try {
                HandlerFactory handlerFactory = klass.newInstance();
                if (handlerFactory.getName().equals(handlerName)) {
                    return handlerFactory.readJson(handlerSetupJson);
                }
            } catch (IOException | InstantiationException | IllegalAccessException e) {
                throw new DialoguerException("Unable to load handler", e);
            }
        } throw new DialoguerException("Unable to load handler; handler name not found.");
    }

    private static class DialoguerException extends RuntimeException{
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
}
