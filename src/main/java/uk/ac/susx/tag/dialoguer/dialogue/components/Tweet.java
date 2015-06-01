//package uk.ac.susx.tag.dialoguer.dialogue.components;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// *
// * Created by jpr27 on 21/08/2014.
// */
//public class Tweet {
//
//    // -------------------- FIELDS --------------------
//    private RandomString randomString;
//
//    private String text;
//    private int userId;
//    private String screenName;
//    private String name;
//    private boolean geoEnabled;
//    private double lat;
//    private double lon;
//    private double radius;
//
//        // ----------------- CONSTRUCTORS -----------------
//    public Tweet() {
//        //intentionally left empty
//    }
//
//    public Tweet(String text, int userId, String screenName, String name, boolean geoEnabled,
//                 double lat, double lon, double radius) {
//        this.text = text;
//        this.userId = userId;
//        this.screenName = screenName;
//        this.name = name;
//        this.geoEnabled = geoEnabled;
//        this.lat =lat;
//        this.lon = lon;
//        this.radius = radius;
//        randomString = new RandomString(4);
//    }
//
//    // -------------- API: PUBLIC METHODS -------------
//
//    // Getters
//    public boolean isGeoEnabled() {
//        return geoEnabled;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public int getUserId() {
//        return userId;
//    }
//
//    public String getScreenName() {
//        return screenName;
//    }
//
//    public String getText() {
//        return text;
//    }
//
//    public double getLat() {
//        return lat;
//    }
//
//    public double getLon() {
//        return lon;
//    }
//
//    public double getRadius() {
//        return radius;
//    }
//
//    //Setters
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public void setGeoEnabled(boolean geoEnabled) {
//        this.geoEnabled = geoEnabled;
//    }
//
//    public void setUserId(int userId) {
//        this.userId = userId;
//    }
//
//    public void setScreenName(String screenName) {
//        this.screenName = screenName;
//    }
//
//    public void setText(String text) {
//        this.text = text;
//    }
//
//    public void setLat(double lat) {
//        this.lat = lat;
//    }
//
//    public void setLon(double lon) {
//        this.lon = lon;
//    }
//
//    public void setRadius(double radius) {
//        this.radius = radius;
//    }
//
//    //Fetchers: not for serialisation / deserialisation
//    public String fetchStrippedText() {
//        return text.replaceAll("\\B@[\\w]+\\b", "").replaceAll("  ", " ");
//    }
//
//    public String fetchUserIdAsString() {
//        return String.valueOf(userId);
//    }
//
//    //Create a single text tweet from a String
//    public String createTweetText(String address, String text) {
//        String prefix = randomString.nextString();                              //create random prefix code to append
//        int offset = prefix.length() + address.length() + 3;                    //offset from 140 chars. +3 for @ + " "
//        text = text.substring(0, Math.min(140 - offset, text.length()));        //trim text and lose rest
//        text = prefix + " @" + address + " " + text;
//        return text;
//    }
//
//    //Create a list of text tweets of the right size from a String
//    public List<String> createTweetListFromText(String address, String text) {
//        List<String> texts = new ArrayList<>();
//        String remainder = text;
//        boolean truncated = true;
//        while (truncated) {
//            String prefix = randomString.nextString();                          //create random prefix code to append
//
//            //figure out how much of text to take and if we are truncating
//            int offset = prefix.length() + address.length() + 3;                //offset from 140 chars. +3 for @ + " "
//            int textLengthTaken = Math.min(140 - offset, remainder.length());
//            if ((140 - offset) < remainder.length()) {
//                textLengthTaken = textLengthTaken - 3;
//                truncated = true;
//            } else {
//                truncated = false;
//            }
//
//            //getValue the text and add prefix, address and ... if we are truncating
//            text = remainder.substring(0, textLengthTaken);
//            text = prefix + " @" + address + " " + text;
//            if (truncated) {
//                text = text + "...";
//            }
//            texts.add(text);
//
//            //scoop up remainder
//            if (truncated) {
//                remainder = remainder.substring(textLengthTaken).trim();
//                if (remainder.isEmpty()) {
//                    truncated = false;                                          //in case trim() emptied it
//                }
//                remainder = "..." + remainder;
//            }
//        }
//        return texts;
//    }
//
//    public static void main(String args[]) {
//        String text = "This is a very long text which will need to be truncated and worked into several tweets as " +
//                "there is too much text here to manage in a single tweet so there we go.";
//        String address = "hellofoo";
//
//        Tweet base = new Tweet("", 0, "", "", true, 0.0, 0.0, 0.0);
//        List<String> tweets =base.createTweetListFromText(address, text);
//        for (String tweet : tweets) {
//            System.out.println(tweet);
//        }
//    }
//}

// --------------- GLOBAL VARIABLES ---------------
// -------------------- FIELDS --------------------
// ----------------- CONSTRUCTORS -----------------
// -------------- API: PUBLIC METHODS -------------
// ---------- API: STD OVERRIDE METHODS -----------
// ---------------- PRIVATE METHODS ---------------
// -------------- PROTECTED METHODS ---------------
// ---------------- STATIC METHODS ----------------
// ----------------- STATIC CLASS -----------------
// ------------- MAIN(): DEFAULT TEST -------------


