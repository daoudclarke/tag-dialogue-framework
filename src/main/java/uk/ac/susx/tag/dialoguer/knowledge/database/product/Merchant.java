package uk.ac.susx.tag.dialoguer.knowledge.database.product;

import uk.ac.susx.tag.dialoguer.knowledge.location.RadiusAssigner;

import java.text.DecimalFormat;
import java.util.List;

/**
* Created by jpr27 on 04/08/2014.
*/
public class Merchant {

    // -------------------- FIELDS --------------------
    private String merchantId;
    private long osmId;
    private String name;
    private String locDesc;
    private String address;
    private double lat;
    private double lon;
    private double radius;
    private String geojson;

    private String description;
    private List<String> tags;

    // ----------------- CONSTRUCTORS -----------------
    public Merchant() {
    }

    public Merchant(String merchantId, long osmId, String name, String locDesc, String address,
                    double lat, double lon, double radius, String description, List<String> tags) {
        this.merchantId = merchantId;
        this.osmId = osmId;
        this.name = name;
        this.locDesc = locDesc;
        this.address = address;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.description = description;
        this.tags = tags;
        this.geojson = null;
    }

    // -------------- API: PUBLIC METHODS -------------

    public boolean isInside(double currentLat, double currentLon) {
        return RadiusAssigner.haversineM(lat, lon, currentLat, currentLon) < radius;
    }

    public boolean isInside(double currentLat, double currentLon, double allowableError) {
        return RadiusAssigner.haversineM(lat, lon, currentLat, currentLon) < (radius + allowableError);
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public boolean isOnlineStore(){
        return osmId == 0L;
    }

    // Getters ---
    public String getMerchantId() {
        return merchantId;
    }

    public long getOsmId() {
        return osmId;
    }

    public String getName() {
        return name;
    }

    public String getLocDesc() {
        return locDesc;
    }

    public String getAddress() {
        return address;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getRadius() {
        return radius;
    }

    public String getGeojson() {
        return geojson;
    }

    // Setters ---
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public void setOsmId(long osmId) {
        this.osmId = osmId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocDesc(String locDesc) {
        this.locDesc = locDesc;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setGeojson(String geojson) {
        this.geojson = geojson;
    }

    @Override
    public String toString(){


        return String.valueOf(this.getMerchantId())+";"+this.getOsmId()+";"+this.getName()+"; "+this.getLocDesc()+"; "+
                this.getAddress()+"; "+this.getLat()+"; "+this.getLon()+"; "+this.getRadius()+"\n";
    }

    public String toShortString(){
        return this.getName();
    }

    public String getInfo(List<Double> locationInfo) {
        //info about merchant relative to given location
        String res = toShortString();
        //res+="<"+this.getLat()+","+this.getLon()+">";
        if (isOnlineStore()){
            res+= "("+this.getAddress()+")";
        } else {
            if (locationInfo.size() > 1) {
                if(isInside(locationInfo.get(0),locationInfo.get(1),locationInfo.get(2))){
                    res="here ("+toShortString()+")";
                }else {
                    DecimalFormat df = new DecimalFormat("#");
                    res += " (" + df.format(distance(locationInfo.get(0), locationInfo.get(1))) + "m " + direction(locationInfo.get(0), locationInfo.get(1)) + ")";
                }
            }
        }

        return res;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Merchant merchant = (Merchant) o;

        return !(merchantId != null ? !merchantId.equals(merchant.merchantId) : merchant.merchantId != null);

    }

    @Override
    public int hashCode() {
        return merchantId != null ? merchantId.hashCode() : 0;
    }



    private double distance(double lat, double lon) {
        return RadiusAssigner.haversineM(lat,lon,getLat(),getLon());

    }
    private String direction(double lat, double lon){
        return RadiusAssigner.getDirection(lat,lon,getLat(),getLon());

    }

}