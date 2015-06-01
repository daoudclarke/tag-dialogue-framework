package uk.ac.susx.tag.dialoguer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Tweet;
import uk.ac.susx.tag.dialoguer.dialogue.components.User;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;

/**
*
* Doctored by JW on 1/6/2014
*/
@SuppressWarnings("FieldCanBeLocal")
@Path("/")
@Singleton
public class JSONInterpret {

    // --------------- GLOBAL VARIABLES ---------------
    private final double GPS_RADIUS = 3.0;

    // -------------------- FIELDS --------------------
    private final String paypal="paypal_checkin_dialoguer.json";
    private final String product="product_search_dialoguer.json";
    private final String taxi="taxi_service_dialoguer.json";
    private final List<String> apps = Lists.newArrayList(paypal,product,taxi);

    private Map<String, DialogueTracker> mytrackers;

    // ----------------- CONSTRUCTORS -----------------
    public JSONInterpret() throws IOException {
        List<String> s = new ArrayList<>();
        s.add("nodebug");
        mytrackers=new HashMap<>();
        for(String app:apps){
            mytrackers.put(app, new DialogueTrackerDemo(app));
        }

        System.err.println("Starting new dialogueTrackerDemos");

    }


    // ---------- BROWSER API: PUBLIC METHODS ---------

    @SuppressWarnings("unchecked")
    @POST
    @Path("taxibot")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map post1(
            Map data
    ) {
        return interpret(data,taxi);                                  //post revised tweet
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("paypalbot")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map post2(
            Map data
    ) {
        return interpret(data,paypal);                                  //post revised tweet
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("buybot")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map post3(
            Map data
    ) {
        return interpret(data,product);                                  //post revised tweet
    }



    // -------------- API: PRIVATE -------------

    private Map interpret(
            Map data, String app
    ) {
        Tweet tweet;

        //package Map contents into a Tweet
        {
            System.out.println(data);

            int userId = (int)data.get("userId");
            String text = (String)data.get("text");
            String screenName = (String)data.get("screenName");
            String name = (String)data.get("name");

            boolean geoEnabled = false;
            double lat = 0.0;
            double lon = 0.0;
            double radius = GPS_RADIUS;

            if (data.containsKey("geoLat") && data.containsKey("geoLong")) {
                Double geoLat = (Double)data.get("geoLat");
                Double geoLong = (Double)data.get("geoLong");
                if (!(geoLat == null) && !(geoLong == null)) {
                    geoEnabled = true;
                    lat = geoLat;
                    lon = geoLong;
                    radius = GPS_RADIUS;
                }
            }
            tweet = new Tweet(text, userId, screenName, name, geoEnabled, lat, lon, radius);
        }

        //Extract id, address, and text from tweet and create dialog
        String id = tweet.fetchUserIdAsString();
        String address = tweet.getScreenName();
        String text = tweet.fetchStrippedText();
        Map<String,String> attributes=new HashMap<>();
        attributes.put("name",address);
        User userData = new User(tweet.getLat(),tweet.getLon(),tweet.getRadius(),attributes);

        //convert the latest dialog system message into a tweet, with dialog fromAddr as the @recipient
        String response = tweet.createTweetText(address, mytrackers.get(app).getResponse(id,text,userData));

        //put response back into data and return data
        data.put("text", response);
        data.put("inReplyToStatusId", data.get("id"));
        return data;                                                       //post revised tweet
    }


}


