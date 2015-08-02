package uk.ac.susx.tag.dialoguer.dialogue.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data about the user. Including geo data. This is present on the Dialogue object as current user data, and present
 * in the Dialogue's message history as historical user data.
 *
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 15:19
 */
public class User {

    private boolean locationData = false;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private double uncertaintyRadius = 0.0; // in Metres
    private Map<String, String> attributes;

    public User(){
        this(new HashMap<String, String>());
    }

    public User(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public User(double latitude, double longitude, double uncertaintyRadius) {
        this(latitude, longitude, uncertaintyRadius, new HashMap<String, String>());
    }

    public User(double latitude, double longitude, double uncertaintyRadius, Map<String, String> attributes) {
        this.locationData = true;
        this.latitude = latitude;
        this.longitude = longitude;
        this.uncertaintyRadius = uncertaintyRadius;
        this.attributes = attributes;
    }

    public User newCopy(){
        return new User(latitude, longitude, uncertaintyRadius, attributes);
    }

    public boolean isLocationDataPresent() {
        return locationData;
    }

    public void setLocationDataPresence(boolean locationData) {
        this.locationData = locationData;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<Double> getLocationData(){
        return Arrays.asList(latitude, longitude, uncertaintyRadius);
    }

    public double getUncertaintyRadius() {
        return uncertaintyRadius;
    }

    public void setUncertaintyRadius(double uncertaintyRadius) {
        this.uncertaintyRadius = uncertaintyRadius;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getAttribute(String name){
        return attributes.get(name);
    }

    public boolean hasAttribute(String name){
        return attributes.containsKey(name);
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public User setAttribute(String key, String value){
        attributes.put(key, value); return this;
    }
}
