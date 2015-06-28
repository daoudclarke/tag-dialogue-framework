package uk.ac.susx.tag.dialoguer.knowledge.location;

import com.google.gson.Gson;
import com.jcabi.immutable.Array;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel Saska on 6/26/2015.
 */
public class NominatimAPIWrapper implements AutoCloseable {
    private static final String nominatimApi = "http://nominatim.openstreetmap.org/search";

    private String clientEmail;

    private Client client;

    public NominatimAPIWrapper() {
        client = ClientBuilder.newClient();
    }

    @Override
    public void close() throws Exception {
        client.close();
    }


    public NomResult[] queryAPI(String query) {
        return queryAPI(query, -1);
    }
    public NomResult[] queryAPI(String query, int limit) {
       return queryAPI(query, limit, -1, -1);
    }
    public NomResult[] queryAPI(String query, int limit, int polygonGeojson, int addressDetails) {
        WebTarget target = client.target(nominatimApi);

        target = target
                .queryParam("q", query)
                .queryParam("email", clientEmail)
                .queryParam("format", "json");

        if (limit > 0) {
            target = target.queryParam("limit", limit);
        }
        if (polygonGeojson == 0 || polygonGeojson == 1) {
            target = target.queryParam("polygon_geojson", polygonGeojson);
        }
        if (addressDetails == 0 || addressDetails == 1) {
            target = target.queryParam("addressdetails", addressDetails);
        }

        String s = target.request()
                .header("Accept", "application/json")
                .buildGet().invoke(String.class);

        NomResult results[] = new Gson().fromJson(s, NomResult[].class);
        return results;
    }

    public class NomResult {
        public String place_id;
        public String osm_type;
        public String osm_id;
        public String boundingbox[] = new String[4];
        public String lat;
        public String lon;
        public String display_name;
        public String place_rank;
        public String type;
        public String importance;
        public String icon;
        public Map<String, String> address;
        public NomGeojson geojson;

        public class NomGeojson {
            public String type;
            public List<Object> coordinates; //TODO: Fix: Sometimes it is just coordnates sometimes its array of coordnates
        }
    }
}
