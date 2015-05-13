package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

import com.google.gson.Gson;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.WitAiAnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Determine user intent using a WitAi instance.
 *
 * User: Andrew D. Robertson
 * Date: 17/03/2015
 * Time: 14:17
 */
public class WitAiAnalyser extends Analyser {

    private static final String witApi = "https://api.wit.ai/message";

    private transient Client client;
    private String serverAccessToken;

    private WitAiAnalyser(){
        client = ClientBuilder.newClient();
        serverAccessToken = null;
    }

    public WitAiAnalyser(String serverAccessToken){
        client = ClientBuilder.newClient();
        this.serverAccessToken = serverAccessToken;
    }

    @Override
    public List<Intent> analyse(String message, Dialogue dialogue) {
        WitAiResponse r = queryAPI(message, dialogue.getStates(), serverAccessToken, client);

        WitAiResponse.Outcome o = r.getMostLikelyOutcome();

        Intent i = new Intent(o.getIntent(), o.getText());

        for (Map.Entry<String, List<WitAiResponse.EntityDefinition>> entry : o.getEntities().entrySet()){
            String name = entry.getKey();
            for (WitAiResponse.EntityDefinition e : entry.getValue()){
                i.fillSlot(name, e.value, e.start, e.end);
            }
        }

        return i.toList();
    }

    @Override
    public AnalyserFactory getFactory() {
        return new WitAiAnalyserFactory();
    }

    public static WitAiResponse queryAPI(String message,  List<String> states, String serverAccessToken, Client client){

        WebTarget target = client.target(witApi);

        // Turn this on to getValue info about the API calls that are being made through Jersey (into std.err)
//        target.register(new LoggingFilter());

        target = target
                .queryParam("v", 20150512)
                .queryParam("q", message)
                .queryParam("n", 3);


        if (states != null && !states.isEmpty()){
            target = target
                    .queryParam("context", "{context}")
                    .resolveTemplate("context", new Gson().toJson(new Context(states)));
        }

        return WitAiResponse.build(
                target.request()
                        .header("Authorization", "Bearer " + serverAccessToken)
                        .header("Accept",  "application/json")
                        .buildGet().invoke(String.class));
    }

    @Override
    public void close() throws Exception {
        client.close();
    }

    public static class Context {
        private final List<String> state;
        public Context(List<String> states){
            state = states;
        }
    }

    public static class WitAiResponse {

        //  It may look like these are never assigned, but they are externally. Since the Wit.Ai API sends us a JSON object response , which is converted using GSON into an instance of this class
        private String msg_id;
        private String _text;
        private List<Outcome> outcomes = new LinkedList<>();

        public static WitAiResponse build(String jsonDefinition){
            return new Gson().fromJson(jsonDefinition, WitAiResponse.class);
        }

        public String toString() {
            return new Gson().toJson(this);
        }

        public String getMessageID() { return msg_id; }

        public List<Outcome> getOutcomes() { return outcomes; }

        public Outcome getMostLikelyOutcome() {
            return outcomes.isEmpty()? null : outcomes.get(0);
        }

        public String getMostLikelyIntent() {
            return outcomes.isEmpty()? null : outcomes.get(0).getIntent();
        }

        public double getConfidenceOfMostLikelyOutcome(){
            return outcomes.isEmpty()? 0.0 : outcomes.get(0).getConfidence();
        }

        /**
         * return true if 1 or more intents were identified by Wit.Ai in this response
         */
        public boolean isIntentIdentified() {
            return !outcomes.isEmpty();
        }

        public String getText() { return _text; }

        public List<String> getIntents() {
            List<String> intents = new ArrayList<>();
            for (Outcome o : outcomes){
                intents.add(o.intent);
            } return intents;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WitAiResponse that = (WitAiResponse) o;

            if (_text != null ? !_text.equals(that._text) : that._text != null) return false;
            if (outcomes != null ? !outcomes.equals(that.outcomes) : that.outcomes != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = _text != null ? _text.hashCode() : 0;
            result = 31 * result + (outcomes != null ? outcomes.hashCode() : 0);
            return result;
        }

        /**
         * Defines an intent and the corresponding entities discovered.
         */
        public static class Outcome {

            private String _text;
            private String intent;  // The user intent predicted
            private Map<String, List<EntityDefinition>> entities = new HashMap<>();  // ENTITY_TYPE : [ENTITY_INSTANCE-1, ... , ENTITY_INSTANCE-n]
            private double confidence;  // The wit.ai confidence rating in this classification
            public double getConfidence(){ return confidence;}

            public String getIntent() { return intent; }
            public String getText() {return _text; }
            public Map<String, List<EntityDefinition>> getEntities() { return entities; }

            /**
             * Get a simple mapping between each entity type and all of the values it occurred as in the text.
             * (therefore ignoring whether it was a new suggestion by wit.ai, and any other (probably unimportant)
             * details.
             */
            public Map<String, List<String>> getEntityKeyValues() {
                Map<String, List<String>> kvs = new HashMap<>();
                for (Map.Entry<String, List<EntityDefinition>> entry : entities.entrySet()){
                    String entityName = entry.getKey();
                    kvs.put(entityName, new ArrayList<String>());
                    for (EntityDefinition entityDef : entry.getValue()) {
                        kvs.get(entityName).add(entityDef.value);
                    }
                } return kvs;
            }

            /**
             * Get the values of a particular entity type in the intent. E.g. if you ask for the "product_query"
             * entity, then any product_query identified in the text (usually 1, but possibly more) will be returned.
             */
            public List<String> getEntityValues(String entityType){
                List<String> entityValues = new ArrayList<>();
                if (entities.containsKey(entityType)){
                    for (EntityDefinition  e : entities.get(entityType)){
                        entityValues.add(e.value);
                    }
                } return entityValues;
            }

            public Outcome addEntity(String key, EntityDefinition e) {
                if(entities.containsKey(key)) {

                    entities.get(key).add(e);
                } else {
                    List<EntityDefinition> ents = new LinkedList<>();
                    ents.add(e);
                    entities.put(key, ents);
                }
                return this;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Outcome outcome = (Outcome) o;

                if (Double.compare(outcome.confidence, confidence) != 0) return false;
                if (_text != null ? !_text.equals(outcome._text) : outcome._text != null) return false;
                if (entities != null ? !entities.equals(outcome.entities) : outcome.entities != null) return false;
                if (intent != null ? !intent.equals(outcome.intent) : outcome.intent != null) return false;

                return true;
            }

            @Override
            public int hashCode() {
                int result;
                long temp;
                result = _text != null ? _text.hashCode() : 0;
                result = 31 * result + (intent != null ? intent.hashCode() : 0);
                result = 31 * result + (entities != null ? entities.hashCode() : 0);
                temp = Double.doubleToLongBits(confidence);
                result = 31 * result + (int) (temp ^ (temp >>> 32));
                return result;
            }
        }

        /**
         * Defines an instance of an entity
         */
        public static class EntityDefinition {

            public String value;   // The value of this entity type in this instance
            public boolean suggested; // True if wit.ai hasn't seen this value before, but thinks it belongs to this entity.
            public String body; // Not sure if wit.ai uses this all the time
            public int start; // Not sure if wit.ai uses this all the time
            public int end; // Not sure if wit.ai uses this all the time


            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                EntityDefinition that = (EntityDefinition) o;

                if (end != that.end) return false;
                if (start != that.start) return false;
                if (suggested != that.suggested) return false;
                if (body != null ? !body.equals(that.body) : that.body != null) return false;
                if (value != null ? !value.equals(that.value) : that.value != null) return false;

                return true;
            }

            @Override
            public int hashCode() {
                int result = value != null ? value.hashCode() : 0;
                result = 31 * result + (suggested ? 1 : 0);
                result = 31 * result + (body != null ? body.hashCode() : 0);
                result = 31 * result + start;
                result = 31 * result + end;
                return result;
            }
        }
    }

}
