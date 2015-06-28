package uk.ac.susx.tag.dialoguer.knowledge.questionanswering;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.*;

/**
 * Created by Daniel Saska on 6/22/2015.
 */

/**
 * Wikidata Interface provides means of interaction with Wikidata through different APIs
 */
public class WikidataAPIWrapper implements AutoCloseable{

    private static final String wdApi = "https://www.wikidata.org/w/api.php";
    private static final String wdqApi = "https://wdq.wmflabs.org/api";
    private static final int maxResponses = 30;

    private transient Client client;

    public WikidataAPIWrapper() {
        client = ClientBuilder.newClient();
    }

    /**
     * Queries https://wdq.wmflabs.org/api with provided entity and relationn, finding all entites that share given
     * relationship with the entityi
     * @param entityId ID of the entity
     * @param relationId ID of the relationship/property the entities should share
     * @return String of concatenated names of up to maxResponses results
     */
    public String wdqQuery(int entityId, int relationId){

        WebTarget target = client.target(wdqApi);
        target = target
                .queryParam("q","CLAIM[" + relationId + ":" + entityId + "]");
        String s = target.request()
                .header("Accept",  "application/json")
                .buildGet().invoke(String.class);
        WDQResult wdqr = new Gson().fromJson(s, WDQResult.class);

        //Use the IDs to obtain entity names
        List<String> results = new ArrayList<>();
        int nRes = wdqr.items.size();
        if (nRes == 0) {
            return "";
        }
        boolean overflow = false;
        if (nRes > maxResponses) {
            overflow = true;
            nRes = maxResponses;
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

        s = "";
        for (String r : results) {
            s += r + ", ";
        }
        if (s.length() >= 2) {
            s = s.substring(0, s.length() - 2);
        }
        if (overflow) {
            s += ", ...";
        }

        return s;
    }

    /**
     * Returns value of some property of some entity
     * @param entityStr Entity for which the property should be explored
     * @param propertyStr Property which should be explored
     * @return
     */
    public String getPropertyValue(String entityStr, String propertyStr){
        return getPropertyValue(getEntityId(entityStr),getPropertyId(propertyStr));
    }

    /**
     * Returns value of some property of some entity
     * @param entityId ID of entity for which the property should be explored
     * @param propertyId ID of property which should be explored
     * @return
     */
    public String getPropertyValue(int entityId, int propertyId){

        WebTarget target = client.target(wdApi);


        //Get data about the entity and extract Relation
        List<String> results = new ArrayList<>();
        {
            target = client.target(wdApi);
            target = target
                    .queryParam("format", "json")
                    .queryParam("sites", "itwiki")
                    .queryParam("action", "wbgetentities")
                    .queryParam("ids", "Q" + entityId);

            String s = target.request()
                    .header("Accept", "application/json")
                    .buildGet().invoke(String.class);
            WDResult wdr = new Gson().fromJson(s, WDResult.class);
            ArrayList<LinkedTreeMap> claim = (ArrayList<LinkedTreeMap>)wdr.entities.entrySet().iterator().next().getValue().claims.get("P"+propertyId);
            if (claim == null) {
                System.out.println("No caims were found");
            }
            for (LinkedTreeMap entry : claim) {
                int id = 0;
                if(entry.containsKey("mainsnak")) {
                    LinkedTreeMap mainsnak = (LinkedTreeMap)entry.get("mainsnak");
                    if (mainsnak.containsKey("datavalue")) {
                        LinkedTreeMap datavalue = (LinkedTreeMap)mainsnak.get("datavalue");
                        if (datavalue.containsKey("type")) {
                            String type = (String) datavalue.get("type");
                            if (!type.equals("wikibase-entityid")) {
                                continue;
                            }
                        }
                        if (datavalue.containsKey("value")) {
                            LinkedTreeMap value = (LinkedTreeMap)datavalue.get("value");
                            if (value.containsKey("numeric-id")) {
                                id = ((Double)value.get("numeric-id")).intValue();
                            }
                        }
                    } else {
                        System.out.print("POSSIBLE ISSUE: 'mainsnack' does not contain 'datavalue' property");
                        System.out.print(entry);
                    }
                } else {
                    System.out.print("POSSIBLE ISSUE: Claim does not contain 'mainsnak' property");
                    System.out.print(entry);
                }
                //Search for the ID and add the entity to results
                target = client.target(wdApi);
                target = target
                        .queryParam("format", "json")
                        .queryParam("sites", "itwiki")
                        .queryParam("action", "wbgetentities")
                        .queryParam("ids", "Q"+Integer.toString(id));
                s = target.request()
                        .header("Accept", "application/json")
                        .buildGet().invoke(String.class);
                WDResult wdrr = new Gson().fromJson(s, WDResult.class);
                if ( wdrr.entities.entrySet().iterator().next().getValue().labels != null
                        && wdrr.entities.entrySet().iterator().next().getValue().labels.en != null ) {
                    results.add(wdrr.entities.entrySet().iterator().next().getValue().labels.en.value); //Use first entry for now
                }
            }

        }

        String s = "";
        for (String r : results) {
            s += r + ", ";
        }
        if (s.length() >= 2) {
            s = s.substring(0, s.length() - 2);
        }
        return s;
    }

    /**
     * Returns set of all valid property for given entity
     * @param entityId ID of entity which should be explored
     * @return Set of Property IDs
     */
    public Set<String> getPropertyIds(int entityId){

        WebTarget target = client.target(wdApi);

        //Get data about the entity and extract Relation
        Set<String> r = new HashSet<>();
        {
            target = client.target(wdApi);
            target = target
                    .queryParam("format", "json")
                    .queryParam("sites", "itwiki")
                    .queryParam("action", "wbgetentities")
                    .queryParam("ids", "Q" + entityId);

            String s = target.request()
                    .header("Accept", "application/json")
                    .buildGet().invoke(String.class);
            WDResult wdr = new Gson().fromJson(s, WDResult.class);
            for (Map.Entry<String, Object> entry : wdr.entities.entrySet().iterator().next().getValue().claims.entrySet())
                r.add(entry.getKey());
        }
        return r;
    }

    /**
     * Returns set of all valid property for given entity
     * @param entityStr Entity which should be explored
     * @return Set of Property IDs
     */
    public Set<String> getPropertyIds(String entityStr){

        WebTarget target = client.target(wdApi);

        //Search for the Entity trem
        String entityId = "";
        {
            target = target
                    .queryParam("format", "json")
                    .queryParam("language", "en")
                    .queryParam("action", "wbsearchentities")
                    .queryParam("search", entityStr);
            //Get results
            String s = target.request()
                    .header("Accept", "application/json")
                    .buildGet().invoke(String.class);
            WDSearch wds = new Gson().fromJson(s, WDSearch.class);
            entityId = wds.search.get(0).id.substring(1); //Strip first character
        }

        return getPropertyIds(Integer.parseInt(entityId));
    }

    /**
     * Finds entity by name and returns its ID
     * @param entityStr Entity name for which the ID is to be found
     * @return ID of the entity
     */
    public int getEntityId(String entityStr) {
        WebTarget target = client.target(wdApi);

        //Search for the Entity trem
        String entityId = "";
        {
            target = target
                    .queryParam("format", "json")
                    .queryParam("language", "en")
                    .queryParam("action", "wbsearchentities")
                    .queryParam("search", entityStr);
            //Get results
            String s = target.request()
                    .header("Accept", "application/json")
                    .buildGet().invoke(String.class);
            WDSearch wds = new Gson().fromJson(s, WDSearch.class);
            if (wds.search.size() == 0) {
                return -1;
            }
            entityId = wds.search.get(0).id.substring(1); //Strip first character
        }

        return Integer.parseInt(entityId);
    }

    /**
     * Finds property by name and returns its ID
     * @param propertyStr Property name for which the ID is to be found
     * @return ID of the entity
     */
    public int getPropertyId(String propertyStr) {
        WebTarget target = client.target(wdApi);

        String propertyId = null;
        {
            target = target
                    .queryParam("format", "json")
                    .queryParam("language", "en")
                    .queryParam("type", "property")
                    .queryParam("action", "wbsearchentities")
                    .queryParam("search", propertyStr);
            //Get results
            String s = target.request()
                    .header("Accept", "application/json")
                    .buildGet().invoke(String.class);
            WDSearch wds = new Gson().fromJson(s, WDSearch.class);
            propertyId = wds.search.get(0).id.substring(1); //Strip first character
        }

        return Integer.parseInt(propertyId);
    }

    public void close() {
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
            public Map<String, Object> claims;

            public class WDLabel {
                public WDLabelEntry en;

                public class WDLabelEntry {
                    public String language;
                    public String value;
                }
            }
            public class WDClaim {
                public String id;
            }
        }
    }
}

