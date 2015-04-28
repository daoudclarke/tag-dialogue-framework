package uk.ac.susx.tag.dialoguer.knowledge.database.product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * Created by jpr27 on 04/08/2014.
 */
public class ProductSet {

    public enum Property {

        // TITLE = title of work
        // AUTHOR = author of product
        // PUB_YEAR = year of publication
        // PUBLISHER = publisher
        // FORMAT = product format e.g. cd, vinyl, hardback, paperback
        // SERIAL = serial number e.g. ISBN

        title, contributor, contributorArtist, contributorDirector, releaseDate, publisher, formatType, edition

        //  title, contributorArtist, formatType, edition

        // These appear in the toy dataset currently as:
        // TITLE = title
        // AUTHOR = author
        // PUB_YEAR = releaseDate
        // FORMAT = format
        // SERIAL = serial

    }

    // -------------------- FIELDS --------------------
    private boolean filterByGeolocation;                    //out:  true=yes, false=no
    private double lat;                                     //      lattitude
    private double lon;                                     //      longitude
    private double radius;                                  //      radius in metres
    private List<String> candidateNamesTags;                //      match strings for names, tags
    private Map<Property, List<String>> candidateProperties;//      descriptor->content properties
    private List<String> candidateOptions;                  //      match strings for options
    private List<Product> products;                         //back: products that match
    private Map<Merchant, List<Product>> merchantMap;       //      merchant->products map
    private Set<Merchant> inMerchantCandidates;

    // ----------------- CONSTRUCTORS -----------------
    public ProductSet() {
    }

    /**
     * Given the user info, candidate properties etc, and a list of products returned from a MongoDB query,
     * this constructor handles the creation of the merchant map and inMerchantCandidates
     */
    public ProductSet(boolean filterByGeolocation, double lat, double lon, double radius,
                      List<String> candidateNamesTags, Map<Property, List<String>> candidateProperties,
                      List<String> candidateOptions, List<Product> products) {
        this.filterByGeolocation = filterByGeolocation;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.candidateNamesTags = candidateNamesTags;
        this.candidateProperties = candidateProperties;
        this.candidateOptions = candidateOptions;

        updateProducts(products);
    }

    public ProductSet(List<Double> locationInfo, List<Product> products){
        this(true, locationInfo.get(0), locationInfo.get(1), locationInfo.get(2), new ArrayList<String>(), new HashMap<Property, List<String>>(), new ArrayList<String>(), products);
    }

    // -------------- API: PUBLIC METHODS -------------

    // Getters ---
    public boolean getFilterByGeolocation() {
        return filterByGeolocation;
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

    public List<String> getCandidateNamesTags() {
        return candidateNamesTags;
    }

    public Map<Property, List<String>> getCandidateProperties() {
        return candidateProperties;
    }

    public List<String> getCandidateOptions() {
        return candidateOptions;
    }

    public List<Product> getProducts() {
        return products;
    }

    public Map<Merchant, List<Product>> getMerchantMap() {
        if (merchantMap == null)
            updateProducts(products);
        return merchantMap;
    }

    public Set<Merchant> getInMerchantCandidates() {
        if (inMerchantCandidates == null)
            updateProducts(products);
        return inMerchantCandidates;
    }

    // Fetchers --- (as getters but no corresponding fields)
    public int fetchProductsSize() {
        return products.size();
    }

    public Set<Merchant> fetchMerchants() {
        return getMerchantMap().keySet();
    }

    // Setters ---
    public void setFilterByGeolocation(boolean filterByGeolocation) {
        this.filterByGeolocation = filterByGeolocation;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setCandidateNamesTags(List<String> candidateNamesTags) {
        this.candidateNamesTags = candidateNamesTags;
    }

    public void setCandidateProperties(Map<Property, List<String>> candidateProperties) {
        this.candidateProperties = candidateProperties;
    }

    public void setCandidateOptions(List<String> candidateOptions) {
        this.candidateOptions = candidateOptions;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public boolean setFilterByGeolocation() {
        return filterByGeolocation;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setMerchantMap(Map<Merchant, List<Product>> merchantMap) {
        this.merchantMap = merchantMap;
    }

    public void setInMerchantCandidates(Set<Merchant> inMerchantCandidates) {
        this.inMerchantCandidates = inMerchantCandidates;
    }

    public List<Product> filterByInsideMerchant(){
        return filterByInsideMerchant(lat, lon, radius);
    }

    public List<Product> filterByInsideMerchant(double lat, double lon, double allowableErrorMetres){
        List<Product> filtered = new ArrayList<>();
        for (Map.Entry<Merchant, List<Product>> entry : merchantMap.entrySet()){
            if (entry.getKey().isInside(lat, lon, allowableErrorMetres))
                filtered.addAll(entry.getValue());
        } return filtered;
    }



    /**
     * This method not only sets the products field, but then also uses those products to re-constitute the
     * merchantMap and inMerchantCandidates
     */
    public void updateProducts(List<Product> products){
        this.products = products;

        // Fill the merchant map and inMerchantCandidates
        merchantMap = new HashMap<>();
        inMerchantCandidates = new HashSet<>();
        
        for (Product p : products){
            Merchant m = p.getMerchant();

            if(m.isInside(lat, lon, radius))
                inMerchantCandidates.add(m);                                        //this is not defined

            if (!merchantMap.containsKey(m)){
                merchantMap.put(m, new ArrayList<Product>());
            }
            merchantMap.get(p.getMerchant()).add(p);
        }
    }

    /**
     * Establish whether merchant is distinguishing for the current ProductSet
     *
     */
    public boolean isMerchantDistinguishing(){
        return isMerchantDistinguishing(this.getProducts());
    }


    /**
     * Establish whether merchant is distinguishing for a list of products
     * @param products - a list of Products
     * @return boolean - whether the merchant is different for all of the list of products
     */
    public static boolean isMerchantDistinguishing(List<Product> products){

        Set<String> merchantNames=new HashSet<>();

        for (Product p:products){
            merchantNames.add(p.getMerchant().getMerchantId());
        }

        return (merchantNames.size()>1);
    }
}