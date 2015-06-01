//package uk.ac.susx.tag.dialoguer;
//
//import com.google.common.collect.ImmutableMap;
//import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
//import uk.ac.susx.tag.parcel.api.datastructures.Dialog;
//import uk.ac.susx.tag.parcel.api.datastructures.DialogElementTypes;
//import uk.ac.susx.tag.parcel.api.datastructures.DialogMessage;
//import uk.ac.susx.tag.parcel.api.datastructures.Tweet;
//import uk.ac.susx.tag.parcel.api.processing.wit.ProcessingStrategyWit;
//import uk.ac.susx.tag.parcel.api.processing.ProcessingStrategy;
//import uk.ac.susx.tag.parcel.api.processing.dep.ProcessingStrategyDep;
//import uk.ac.susx.tag.parcel.api.validation.ValidDialogue;
//
//import javax.inject.Singleton;
//import javax.ws.rs.Consumes;
//import javax.ws.rs.POST;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.MediaType;
//import java.util.*;
//
///**
// * This object offers 3 browser APIs:
// *
// * 1. "interpret" looks for a Dialog object, processes it, and returns it. The object is not cached in this program
// * as it is presumably being managed at the other end of the API.
// *
// * 2. "interpretTweet" looks for a Map of a Method51 Tweet object. It matches an incoming Tweet's userId against the stored cache
// * of ongoing dialogs. If it finds a match, it retrieves the cached dialog, updates it with the user's message,
// * and then processes it. Finally, it returns a tweet via the Map, addressed to the original sender via an @tag in the body of
// * the tweet, with the system's response.
// *
// * 3."interpretDialogMessage" looks for a DialogMessage. It matches an incoming DialogMessage's dialogId against the stored cache
// * of ongoing dialogs. If it finds a match, it retrieves the cached dialog, updates it with the user's message,
// * and then processes it. Finally, it returns a DialogMessage with the system's response.
// *
// * Cache Management.
// * Note that the cache is not currently flushed periodically so will fill up over time. The plan is that the cache will
// * be flushed of uncompleted old dialogs periodically (every 10 minutes or so). A completed dialog is currently
// * transferred to a completedCache. The object's API is designed to allow another object to inspect the completed cache
// * and extract dialogs for processing. This functionality will be replaced with a Method51-compatible approach at a later date.
// *
// * Note that no action is taken in this software (the dialog is not executed) when a dialog is completed.
// *
// * File created by jpr27 on 29/05/2014.
// * Doctored by JW on 1/6/2014
// */
//@SuppressWarnings("FieldCanBeLocal")
//@Path("/")
//@Singleton
//public class JSONInterpret {
//
//    // --------------- GLOBAL VARIABLES ---------------
//    private final double GPS_RADIUS = 3.0;
//
//    // -------------------- FIELDS --------------------
//    private final ProcessingStrategy processingStrategy;                //the strategy for processing a Dialogue object
//    private final ProcessingStrategy processingStrategyWit;
//    private final Map<String, Dialogue> cache;                            //cache for Dialogue objects
//    private final Map<String, Dialogue> completedCache;
//
//    // ----------------- CONSTRUCTORS -----------------
//    public JSONInterpret(){
//        this("multi-service");
//    }
//
//    public JSONInterpret(String strategy) {
//        List<String> s = new ArrayList<>();
//        s.add("nodebug");
//        switch(strategy) {
//            case "wit":
//                System.err.println("Starting new ProcessingStrategyWit");
//                processingStrategy = new ProcessingStrategyWit();
//                processingStrategyWit=processingStrategy;
//                break;
//            case "both":
//                System.err.println("Starting new ProcessingStrategyWit and new ProcessingStrategyDep");
//                processingStrategy=new ProcessingStrategyDep();
//                processingStrategyWit=new ProcessingStrategyWit();
//                break;
//            case "checkin":
//                System.err.println("Starting new ProcessingStrategyWit for Paypal Checkin");
//                s.add("checkin");
//                processingStrategy = new ProcessingStrategyWit(s);
//                processingStrategyWit = processingStrategy;
//                break;
//            case "multi-service":
//                System.err.println("Starting new ProcessingStrategyWit for multi-service system");
//                s.add("multi-service");
//                processingStrategy = new ProcessingStrategyWit(s);
//                processingStrategyWit = processingStrategy;
//                break;
//
//            default:
//                System.err.println("Starting new ProcessingStrategyDep");
//                processingStrategy = new ProcessingStrategyDep();
//                processingStrategyWit=processingStrategy;
//        }
//        cache = new HashMap<>();
//        completedCache = new HashMap<>();
//    }
//
//    // -------------- API: PUBLIC METHODS -------------
//    public Map<String, Dialogue> getCompletedCache() {
//        return completedCache;
//    }
//
//    public List<Dialogue> removeDialoguesFromCompletedCache() {
//        List<Dialogue> completedDialogues = (List<Dialogue>)completedCache.values();
//        completedCache.clear();
//        return completedDialogues;
//    }
//
//    // ---------- BROWSER API: PUBLIC METHODS ---------
//    @POST
//    @Path("interpret")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Dialogue post(
//            @ValidDialogue Dialogue dialogue                                        //data comes in as a Dialogue
//    ) {
//        cache.put(Dialogue.getDialogueId(), dialogue);                            //store Dialogue in cache
//        return processDialogue(dialogue);                                       //post and return Dialogue
//    }
//
//    @SuppressWarnings("unchecked")
//    @POST
//    @Path("interpretTweet")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Map post1(                                                     //data comes in as Tweet from Method51
//                                                                          Map data
//    ) {
//        Tweet tweet;
//
//        //package Map contents into a Tweet
//        {
//            System.out.println(data);
//
//            int userId = (int)data.get("userId");
//            String text = (String)data.get("text");
//            String screenName = (String)data.get("screenName");
//            String name = (String)data.get("name");
//
//            boolean geoEnabled = false;
//            double lat = 0.0;
//            double lon = 0.0;
//            double radius = GPS_RADIUS;
//
//            if (data.containsKey("geoLat") && data.containsKey("geoLong")) {
//                Double geoLat = (Double)data.get("geoLat");
//                Double geoLong = (Double)data.get("geoLong");
//                if (!(geoLat == null) && !(geoLong == null)) {
//                    geoEnabled = true;
//                    lat = geoLat;
//                    lon = geoLong;
//                    radius = GPS_RADIUS;
//                }
//            }
//            tweet = new Tweet(text, userId, screenName, name, geoEnabled, lat, lon, radius);
//        }
//
//        //Extract id, address, and text from tweet and create Dialogue
//        String id = tweet.fetchUserIdAsString();
//        String address = tweet.getScreenName();
//        String text = tweet.fetchStrippedText();
//        Dialogue dialogue = createOrFetchDialogueue(id, text, address, tweet.isGeoEnabled(), tweet.getLat(), tweet.getLon(), tweet.getRadius());             //if in cache already then fetch else create
//
//        //process the Dialogue
//        dialogue = processDialog(dialogue);
//
//        //convert the latest dialog system message into a tweet, with dialog fromAddr as the @recipient
//        String response = tweet.createTweetText(dialogue.getFromAddr(), dialog.fetchLatestSystemMessageText());
//
//        //put response back into data and return data
//        data.put("text", response);
//        data.put("inReplyToStatusId", data.get("id"));
//
//        return data;                                                       //post revised tweet
//    }
//
//    @SuppressWarnings("unchecked")
//    @POST
//    @Path("interpretText")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Map<String,String> post3(
//            Map<String, String> data
//    ) {
//
//        String id = data.get("id");
//        if(id == null) {
//            id = Long.toString(new Date().getTime());
//        }
//
//        String message = data.get("message");
//
//        Dialogue dialogue = createOrFetchDialog(id, message, id);
//
//        //process the dialog
//        dialogue = processDialog(dialogue);
//
//        return ImmutableMap.of("id", dialogue.getDialogueId(), "message", dialogue.fetchLatestSystemMessageText());                                                       //post revised tweet
//    }
//
//    @SuppressWarnings("unchecked")
//    @POST
//    @Path("interpretTweet2")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Map post4(
//            Map data
//    ) {
//        Tweet tweet;
//
//        //package Map contents into a Tweet
//        {
//            System.out.println(data);
//
//            int userId = (int)data.get("userId");
//            String text = (String)data.get("text");
//            String screenName = (String)data.get("screenName");
//            String name = (String)data.get("name");
//
//            boolean geoEnabled = false;
//            double lat = 0.0;
//            double lon = 0.0;
//            double radius = GPS_RADIUS;
//
//            if (data.containsKey("geoLat") && data.containsKey("geoLong")) {
//                Double geoLat = (Double)data.get("geoLat");
//                Double geoLong = (Double)data.get("geoLong");
//                if (!(geoLat == null) && !(geoLong == null)) {
//                    geoEnabled = true;
//                    lat = geoLat;
//                    lon = geoLong;
//                    radius = GPS_RADIUS;
//                }
//            }
//            tweet = new Tweet(text, userId, screenName, name, geoEnabled, lat, lon, radius);
//        }
//
//        //Extract id, address, and text from tweet and create dialog
//        String id = tweet.fetchUserIdAsString();
//        String address = tweet.getScreenName();
//        String text = tweet.fetchStrippedText();
//        Dialog dialog = createOrFetchDialog(id, text, address, tweet.isGeoEnabled(), tweet.getLat(), tweet.getLon(), tweet.getRadius());             //if in cache already then fetch else create
//
//        //process the dialog
//        dialog = processDialogWit(dialog);
//
//        //convert the latest dialog system message into a tweet, with dialog fromAddr as the @recipient
//        String response = tweet.createTweetText(dialog.getFromAddr(), dialog.fetchLatestSystemMessageText());
//
//        //put response back into data and return data
//        data.put("text", response);
//        data.put("inReplyToStatusId", data.get("id"));
//
//        return data;                                                       //post revised tweet
//    }
//
//    // -------------- API: PUBLIC METHODS -------------
//    @SuppressWarnings("unchecked")
//    @POST
//    @Path("interpretDialogMessage")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public DialogMessage post2(
//            DialogMessage dialogMessage                                     //object comes in as a DialogMessage
//    ) {
//        Dialog dialog = createOrFetchDialog(dialogMessage);
//
//        //process the dialog
//        dialog = processDialog(dialog);
//
//        //insert in the tweet the system message
//        dialogMessage.setMessageText(dialog.fetchLatestSystemMessageText());
//
//        return dialogMessage;                                               //post response as a DialogMessage
//    }
//
//    // ---------------- PRIVATE METHODS ---------------
//    /**
//     * Process the dialog and return it.
//     *
//     *   1. Create interpretation candidates and pick the best one
//     *   2. Create a dialog manager and use it to interpret the user's message and respond, if requested to do so
//     *
//     * @param dialog to be processed
//     * @return processed dialog
//     */
//    private Dialog processDialog(Dialog dialog) {
//        dialog = processingStrategy.processDialog(dialog);
//        cleanUpCache(dialog);
//        return dialog;
//    }
//
//    private Dialog processDialogWit(Dialog dialog){
//        dialog = processingStrategyWit.processDialog(dialog);
//        //update cache
//        String id = dialog.getDialogId();
//        cache.put(id,dialog);  // new dialog object created so cannot rely on pointers so need to explicitly reput dialog in the cache each time
//        cleanUpCache(dialog);
//        return dialog;
//    }
//
//    /**
//     * Create a dialog unless it is in cache, in which case fetch it and add the message to it
//     *
//     * @param id userId from a message
//     * @param text text of a message
//     * @param address user's address
//     * @return dialog object
//     */
//    private Dialog createOrFetchDialog(String id, String text, String address) {
//        Dialog dialog;
//
//        DialogMessage dialogMessage = new DialogMessage(id, address, text, id, id, false, 0.0, 0.0, GPS_RADIUS);
//
//        if (!cache.containsKey(id)) {
//            dialog = new Dialog(dialogMessage);
//            cache.put(id, dialog);
//        } else {
//            dialog = cache.get(id);
//            dialog.addNewUserDialogMessage(dialogMessage);
//        }
//
//        return dialog;
//    }
//
//    private Dialog createOrFetchDialog(DialogMessage dialogMessage) {
//        Dialog dialog;
//        String dialogId = dialogMessage.fetchDialogIdAsString();
//
//        if (!cache.containsKey(dialogId)) {
//            dialog = new Dialog(dialogMessage);
//            cache.put(dialogId, dialog);
//        } else {
//            dialog = cache.get(dialogId);
//            dialog.addNewUserDialogMessage(dialogMessage);
//        }
//
//        return dialog;
//    }
//
//    private Dialog createOrFetchDialog(String id, String text, String address, boolean geoEnabled, double lat, double lon, double radius) {
//        Dialog dialog;
//
//        DialogMessage dialogMessage = new DialogMessage(id, address, text, id, id, geoEnabled, lat, lon, radius);
//
//        if (!cache.containsKey(id)) {
//            dialog = new Dialog(dialogMessage);
//            cache.put(id, dialog);
//            System.err.println("Creating new dialog");
//        } else {
//            dialog = cache.get(id);
//            dialog.addNewUserDialogMessage(dialogMessage);
//            System.err.println("Fetched dialog from cache");
//        }
//
//        return dialog;
//    }
//
//    /**
//     * Clean up the dialog in the cache: 1. Remove those where transaction failed (cancelled). 2. Transfer completed
//     * dialogs to the completed cache.
//     *
//     * @param dialog dialog object
//     */
//    private void cleanUpCache(Dialog dialog) {
//        if (dialog.isCancelled()) {
//            cache.remove(dialog.getDialogId());
//        } else if (dialog.isCompleted(DialogElementTypes.Confirmation)) {
//            completedCache.put(dialog.getDialogId(), dialog);
//            cache.remove(dialog.getDialogId());
//        }
//
//
//    }
//
//}
//

// --------------- GLOBAL VARIABLES ---------------
// -------------------- FIELDS --------------------
// ----------------- CONSTRUCTORS -----------------
// -------------- API: PUBLIC METHODS -------------
// ---------- API: STD OVERRIDE METHODS -----------
// ---------------- PRIVATE METHODS ---------------
// ---------------- HELPER METHODS ----------------
// -------------- PROTECTED METHODS ---------------
// ---------------- STATIC METHODS ----------------
// ----------------- STATIC CLASS -----------------
// ------------- MAIN(): DEFAULT TEST -------------

