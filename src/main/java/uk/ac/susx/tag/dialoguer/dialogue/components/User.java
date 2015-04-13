package uk.ac.susx.tag.dialoguer.dialogue.components;

import java.util.HashMap;
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
    private Map<String, Object> attributes;

    public User(){
        this(new HashMap<>());
    }

    public User(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public User(double latitude, double longitude, double uncertaintyRadius) {
        this(latitude, longitude, uncertaintyRadius, new HashMap<>());
    }

    public User(double latitude, double longitude, double uncertaintyRadius, Map<String, Object> attributes) {
        this.locationData = true;
        this.latitude = latitude;
        this.longitude = longitude;
        this.uncertaintyRadius = uncertaintyRadius;
        this.attributes = attributes;
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

    public double getUncertaintyRadius() {
        return uncertaintyRadius;
    }

    public void setUncertaintyRadius(double uncertaintyRadius) {
        this.uncertaintyRadius = uncertaintyRadius;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public <T> T getAttribute(String name){
        return (T)attributes.get(name);
    }

    public boolean hasAttribute(String name){
        return attributes.containsKey(name);
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
