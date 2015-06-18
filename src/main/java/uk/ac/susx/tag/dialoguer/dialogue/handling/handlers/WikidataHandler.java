package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers;

import com.google.gson.Gson;

import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.WikidataHandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Determine user intent using a WitAi instance.
 *
 * User: Andrew D. Robertson
 * Date: 17/03/2015
 * Time: 14:17
 */

public class WikidataHandler extends Analyser {

    private static final String wdApi = "https://www.wikidata.org/w/api.php";
    private static final String wdqApi = "https://wdq.wmflabs.org/api";

    private transient Client client;

    public WikidataHandler() {
        client = ClientBuilder.newClient();
    }

    @Override
    public List<Intent> analyse(String message, Dialogue dialogue) {
        //TODO: PROCESS


        //TODO: Call queryAPI
        Intent intent = queryAPI(message, null, client);
        List<Intent> intents = new ArrayList<>();
        intents.add(intent);
        return null;
    }


    public static Intent queryAPI(String message,  List<String> states/*TODO*/, Client client){

        WebTarget target = client.target(wdApi);

        message = "country";
        //Search for the term
        String entityStr = message;
        target = target
                .queryParam("format", "json")
                .queryParam("language", "en")
                .queryParam("action", "wbsearchentities")
                .queryParam("search", entityStr);


        //Get results
        String s = target.request()
                .header("Accept",  "application/json")
                .buildGet().invoke(String.class);
        WDSearch wds = new Gson().fromJson(s, WDSearch.class);

        //Get ID of the result
        //TODO: Use all matches/searches until property justifies users query
        //TODO: Determine distance of found term from search term and reject it if it does not match sufficiently
        if (wds.search.size() == 0) {
            //TODO: Throw error
        }

        String entityId = wds.search.get(0).id.substring(1); //Strip first character


        //TODO: Perform a search first to disambiguate between entities
        /*target = client.target(wdApi);
        target = target
                .queryParam("format", "json")
                .queryParam("language", "en")
                .queryParam("action", "wbsearchentities")
                .queryParam("search", message);*/

        target = client.target(wdqApi);
        String relationId = "31";
        target = target
                .queryParam("q","CLAIM[" + relationId + ":" + entityId + "]");
        s = target.request()
                .header("Accept",  "application/json")
                .buildGet().invoke(String.class);
        WDQResult wdqr = new Gson().fromJson(s, WDQResult.class);

        //Use the IDs to obtain entity names
        List<String> results = new ArrayList<>();
        int nRes = wdqr.items.size();
        if (nRes == 0) {
            //TOOD: ERROR
        }
        while (nRes --> 0) {
            target = client.target(wdApi);
            target = target
                    .queryParam("format", "json")
                    .queryParam("sites", "itwiki")
                    .queryParam("action", "wbgetentities")
                    .queryParam("ids", "Q"+wdqr.items.get(nRes));
            s = target.request()
                    .header("Accept", "application/json")
                    .buildGet().invoke(String.class);
            WDResult wdr = new Gson().fromJson(s, WDResult.class);
            if ( wdr.entities.entrySet().iterator().next().getValue().labels != null
                    && wdr.entities.entrySet().iterator().next().getValue().labels.en != null ) {
                results.add(wdr.entities.entrySet().iterator().next().getValue().labels.en.value); //Use first entry for now
            }
        }


        //TODO: Extract data from response

        Intent intent = new Intent("factual_question", message);
        intent.fillSlot("entity_str", entityStr);
        intent.fillSlot("entity_id", entityId);
        intent.fillSlot("relation_id", relationId);

        return intent;
    }

    @Override
    public AnalyserFactory getFactory() {
        return new WikidataHandlerFactory();
    }

    @Override
    public void close() throws Exception {
        client.close();
    }

    private class WDSearch {
        public WDSearchInfo searchinfo;
        public List<WDSearchResult> search;
        public String success;

        class WDSearchInfo {
            String search;
        }
        class WDSearchResult {
            String id;
            String url;
            String description;
            String label;
            List<String> aliases;
        }
    }
    private class WDQResult {
        public WDStatus status;
        public List<String> items;

        public class WDStatus {
            public String error;
            public String items;
            public String querytime;
            public String parsed_query;
        }
    }
    private class WDResult {
        public Map<String, WDEntity> entities;
        public class WDEntity {
            public String pageid;
            public String ns;
            public String title;
            public String lastrevid;
            public String modified;
            public String id;
            public String type;
            public WDLabel labels;

            public class WDLabel {
                public WDLabelEntry en;

                public class WDLabelEntry {
                    public String language;
                    public String value;
                }
            }
        }
    }
}
