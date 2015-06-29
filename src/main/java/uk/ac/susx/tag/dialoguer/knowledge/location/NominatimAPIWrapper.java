package uk.ac.susx.tag.dialoguer.knowledge.location;

import com.google.gson.Gson;
import com.jcabi.immutable.Array;
import edu.berkeley.nlp.util.ArrayUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.List;
import java.util.Map;

//TODO: Javadoc

/**
 * Created by Daniel Saska on 6/26/2015.
 */
public class NominatimAPIWrapper implements AutoCloseable {
    private static final String nominatimApi = "http://nominatim.openstreetmap.org/search";
    private static final String nominatimRApi = "http://nominatim.openstreetmap.org/reverse";

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
        if (limit > 50 && results.length == 50) {
            //Possibility of more results
            String exclude = "";
            for (NomResult nr : results) {
                exclude += nr.place_id + ",";
            }
            exclude = exclude.substring(0, exclude.length()-1);
            return getMore(query, polygonGeojson, addressDetails, exclude, results, limit - 50);
        }
        return results;
    }

    private NomResult[] getMore(String query,  int polygonGeojson, int addressDetails, String foundId, NomResult results[], int remainingLimit) {
        WebTarget target = client.target(nominatimApi);


        target = target
                .queryParam("q", query)
                .queryParam("email", clientEmail)
                .queryParam("format", "json")
                .queryParam("exclude_place_ids", foundId)
                .queryParam("limit", remainingLimit);
        if (polygonGeojson == 0 || polygonGeojson == 1) {
            target = target.queryParam("polygon_geojson", polygonGeojson);
        }
        if (addressDetails == 0 || addressDetails == 1) {
            target = target.queryParam("addressdetails", addressDetails);
        }
        String s = target.request()
                .header("Accept", "application/json")
                .buildGet().invoke(String.class);

        NomResult results2[] = new Gson().fromJson(s, NomResult[].class);
        if (results2.length > 0) {
            int aLen = results.length;
            int bLen = results2.length;
            NomResult[] c= new NomResult[aLen+bLen];
            System.arraycopy(results, 0, c, 0, aLen);
            System.arraycopy(results2, 0, c, aLen, bLen);
            if (remainingLimit > 50 && results2.length == 50) {
                for (NomResult nr : results2) {
                    foundId += "," + nr.place_id;
                }
                return getMore(query, polygonGeojson, addressDetails, foundId, c, remainingLimit - 50);
            }
            return c;
        }
        return results;
    }


    public NomResult queryReverseAPI(double lat, double lon) {
        return queryReverseAPI(lat, lon, 18);
    }
    public NomResult queryReverseAPI(double lat, double lon, int zoom) {
        return queryReverseAPI(lat, lon, zoom, 0);
    }
    public NomResult queryReverseAPI(double lat, double lon, int zoom, int addressDetails) {
        WebTarget target = client.target(nominatimRApi);

        target = target
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("email", clientEmail)
                .queryParam("format", "json")
                .queryParam("zoom", zoom);

        if (addressDetails == 0 || addressDetails == 1) {
            target = target.queryParam("addressdetails", addressDetails);
        }

        String s = target.request()
                .header("Accept", "application/json")
                .buildGet().invoke(String.class);

        NomResult result = new Gson().fromJson(s, NomResult.class);
        return result;
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
