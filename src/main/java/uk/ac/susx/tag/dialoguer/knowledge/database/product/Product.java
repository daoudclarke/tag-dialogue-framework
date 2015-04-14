package uk.ac.susx.tag.dialoguer.knowledge.database.product;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.processing.StringProcessor;

import java.text.NumberFormat;
import java.util.*;

/**
 *
 * Created by jpr27 on 04/08/2014.
 */
public class Product {

    //---------STATIC VARIABLES---
    private static int minLength = 5; //for non-exact string matching

    // -------------------- FIELDS --------------------
    private String productId;
    private String name;
    private String description;
    private HashMultimap<String, String> properties;
    private Merchant merchant;
    private int price;
    private Map<String, List<String>> options;
    private Map<String, String> optDefaults;
    private List<String> tags;
    private List<String> mainProps;

    // ----------------- CONSTRUCTORS -----------------
    public Product() {
        //deliberately left empty
    }

    public Product(String productId, String name, String description, HashMultimap<String, String> properties,
                   Merchant merchant, int price, Map<String, List<String>> options,
                   Map<String, String> optDefaults, List <String> tags, List<String> mainProps) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.properties = properties;
        this.merchant = merchant;
        this.price = price;
        this.options = options;
        this.optDefaults = optDefaults;
        this.tags = tags;
        this.mainProps = mainProps;
    }

    // -------------- API: PUBLIC METHODS -------------

    // Getters ---
    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public HashMultimap<String, String> getProperties() {
        return properties;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public int getPrice() {
        return price;
    }

    public Map<String, List<String>> getOptions() {
        return options;
    }

    public Map<String, String> getOptDefaults() {
        return optDefaults;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getMainProps() { return mainProps;}

    // Setters
    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProperties(HashMultimap<String, String> properties) {
        this.properties = properties;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setOptions(Map<String, List<String>> options) {
        this.options = options;
    }

    public void setOptDefaults(Map<String, String> optDefaults) {
        this.optDefaults = optDefaults;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setMainProps(List<String> mainProps) {
        this.mainProps = mainProps;
    }


    // ... API: PUBLIC METHODS - Manipulating properties

    /**
     * Associate a property key with another value. This doesn't overwrite. So if you call with arguments
     * ["actor", "Kate Beckinsale"], then an additional "actor" property will be added with value "Kate Beckinsale".
     * If you wish to overwrite all "actor" properties, then see assignPropertyValues().
     * @param propertyType property key
     * @param value property value
     */
    public void addPropertyValue(String propertyType, String value) {
        properties.put(propertyType, value);
    }

    /**
     * Overwrite the current values of a property with zero or more property values.
     * @param propertyType property key
     * @param values property values
     */
    public void assignPropertyValues(String propertyType, Set<String> values) {
        properties.replaceValues(propertyType, values);
    }

    /**
     * Get all the values associated with a particular property type.
     * E.g. get all values of properties whose keys are "actor". Will return null if property don't exist.
     * @param propertyType property key
     * @return values of properties with this key (or these keys for contributors)
     */
    public Set<String> fetchPropertyValues(String propertyType) {
        switch (propertyType) {
            case ("contributor"):
                Set<String> propTypes = fetchMainPropsContributorTypes();
                Set<String> answers = new HashSet<>();
                for (String propType : propTypes) {
                    if (properties.containsKey(propType)) {
                        answers.addAll(properties.get(propType));
                    }
                }
                return (answers.isEmpty() ? null : answers);
            default:
                return (properties.containsKey(propertyType) ? properties.get(propertyType) : null);
        }
    }

    /**
     * Get the values for all of the main properties in order of main properties
     * @return List of a set of strings
     */
    public List<Set<String>> fetchOrderedMainPropertyValues() {
        List<Set<String>> values = new ArrayList<>();
        for (String propertyType : mainProps){
            values.add(properties.get(propertyType));
        }
        return values;
    }

    /**
     * Look through the set of mainProps, and return a set of all those properties types that start with "contributor".
     * So if "contributorDirector" is one of the main props, then "contributorDirector" will be
     * an element in the returned set.
     * @return Set of property types
     */
    public Set<String> fetchMainPropsContributorTypes() {
        Set<String> cTypes = new HashSet<>();
        for (String s : mainProps){
            if (s.startsWith("contributor"))
                cTypes.add(s);
        } return cTypes;
    }

    /**
     * Get the number of values with a given property.
     * E.g. with the input string "actor", the result will be the number of properties with the "actor" key.
     * @param propertyType property key
     * @return number of values with a given property
     */
    public int fetchPropertyValueCount(String propertyType) {
        return properties.get(propertyType).size();
    }

    /**
     * Check if product has a property key
     * @param propertyType property key
     * @return true if product has the property, false otherwise
     */
    public boolean hasProperty(String propertyType) {
        return properties.containsKey(propertyType);
    }

    /**
     * Check if product has a set of property keys
     * @param propertyKeys set of property keys
     * @return true if product has all the properties, false otherwise
     */
    public boolean hasProperties(Set<String> propertyKeys){
        return properties.keySet().containsAll(propertyKeys);
    }

    /**
     * Check if product has a (key, value) property pair
     * @param propertyType property key
     * @param propertyValue property value
     * @return true if product has the property (key, value) pair, false otherwise
     */
    public boolean hasProperty(String propertyType, String propertyValue){
        return properties.containsKey(propertyType) && properties.get(propertyType).contains(propertyValue);
    }

    /**
     * Check if product has a set of (key, value) property pairs
     * @param properties Map of (key, value) property pairs
     * @return true if product has all the property (key, value) pairs, false otherwise
     */
    public boolean hasProperties(Multimap<String, String> properties){
        for (Map.Entry<String, String> property : properties.entries()){
            if (!this.properties.containsKey(property.getKey()) ||
                    !this.properties.get(property.getKey()).contains(property.getValue())){
                return false;
            }
        } return true;
    }

    /**
     * Check if textValue matches a value for a particular property type.
     * Allow for an exact match, a contains match, and an edit distance match
     * @param propertyType property type being examined
     * @param textValue text being examined
     * @return true if a match is found, false otherwise.
     */
    public boolean hasMatchedProperty(String propertyType, String textValue){

        if(this.hasProperty(propertyType)){
            textValue=textValue.toLowerCase().trim();
            Set<String> propertyValues=this.fetchPropertyValues(propertyType);
            for(String propertyValue:propertyValues){
                propertyValue=propertyValue.toLowerCase().trim();
                //System.err.println(propertyValue+" : "+textValue);
                if (StringProcessor.stringMatch(propertyValue, textValue)){
                    return true;

                }
            }
        }
        return false;
    }


    // ... API: PUBLIC METHODS - Manipulating options

    /**
     * Return true if the Product has a particular option (e.g. milk, or size), regardless of its value.
     */
    public boolean hasOption(String optionType){
        return options.containsKey(optionType);
    }

    /**
     * Return true if the product has a default for an option
     *
     * Usage example : does this product have a default value for its size option?
     */
    public boolean hasOptDefault(String optionType){
        return optDefaults.containsKey(optionType);
    }

    /**
     * Get the default for a particular option type (e.g. what's the default option for milk?)
     * Will return null if no default is found.
     */
    public String fetchOptDefault(String optionType){
        return optDefaults.get(optionType);
    }

    /**
     * Get the allowable values for a given option (e.g. get "soya", "skimmed" and "full fat" for option "milk")
     * Will return null if the option ain't found.
     */
    public List<String> fetchPermittedOptValues(String optionType){
        return options.get(optionType);
    }

    /**
     * Return true if the optionValue specified is an allowable option value for the optionType specified.
     */
    public boolean isPermittedOptValue(String optionType, String optionValue){
        return hasOption(optionType) && fetchPermittedOptValues(optionType).contains(optionValue);
    }


    // ... API: PUBLIC METHODS - Description strings

    public String toShortString(){
        try {
            return this.propertyDescription() + ", @ " + this.fetchRetailerInfo(new ArrayList<Double>());
        } catch (NullPointerException e){
            return this.getName();
        }
    }

    public String fetchInfo(List<Double> myloc){
        return this.propertyDescription()+", "+this.fetchRetailerInfo(myloc);
    }

    public String fetchRetailerInfo(List<Double> myloc){
        return fetchRetailerNameInfo(myloc)+ fetchRetailerPriceInfo();
    }

    public String fetchRetailerPriceInfo(){
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
        return " @ "+currencyFormatter.format(this.getPrice()/100.0);
    }

    public String fetchRetailerNameInfo(List<Double> myloc){
        return "@ "+this.getMerchant().getInfo(myloc);
    }

    private String propertyDescription(){
        List<String> mainprops = new ArrayList<>(this.getMainProps()); // this needs to be a copy so local version can be updated

        //Set<String> props = this.getProperties().keySet(); //alternative for displaying all properties rather than main props
        //List<String> mainprops = new ArrayList<>(props);

        String res="";
        if(mainprops.size()==0){
            res=this.getName();
        }
        else {
            Set<String> values;
            String titleprop = "title";
            if (!mainprops.contains(titleprop)) {

                titleprop = mainprops.get(0);
            }

            values = this.fetchPropertyValues(titleprop);
            for (String value : values) {
                res += value + " ";
            }
            mainprops.remove(titleprop);


            boolean addcomma=false;
            boolean addendbrace=false;
            for (int j = 0; j < mainprops.size(); j++) {
                if (j == 0) {
                    res += "(";
                    addcomma=false;
                    addendbrace=true;
                }
                try {
                    values = this.fetchPropertyValues(mainprops.get(j));

                    int todo = values.size();
                    if (addcomma){res+=", ";}
                    addcomma=true;
                    for (String value : values) {
                        //res+=mainprops.get(j)+" : ";
                        res += value;
                        todo--;
                        if (todo > 0) {
                            res += ", ";
                        }
                    }

                } catch(NullPointerException e){
                    //System.err.println("No values for "+mainprops.get(j));
                    //could look for alternatives.  E.g., if no "contributorDirector" for DVD, look for other contributors.

                }

            }
            if(addendbrace){res+=")";}
        }

        return res;
    }
    // ---------- API: STD OVERRIDE METHODS -----------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;

        return !(productId != null ? !productId.equals(product.productId) : product.productId != null);
    }

    @Override
    public int hashCode() {
        return productId != null ? productId.hashCode() : 0;
    }

    @Override
    public String toString(){

        String mystring=(productId==null? "ID-NULL":productId)+"; "+name+"; "+description+"; "+price+"\n";
        mystring+=merchant==null? "MERCHANT-NULL\n" : this.getMerchant().toString();
        Multimap<String,String> mymap = this.getProperties();
        if (mymap != null && mymap.keySet().size()>0) {

            for (String key : mymap.keySet()) {
                mystring += key + ":" + mymap.get(key) + "; ";
            }
            mystring += "\n";
        }

        HashMap<String,List<String>> mymap2 =(HashMap<String,List<String>>) this.getOptions();

        if (mymap2!=null && mymap2.keySet().size()>0) {
            for (String key : mymap2.keySet()) {
                mystring += key + ":";
                for (String option:mymap2.get(key)){
                    mystring+=option+", ";
                }
            }
            mystring += "\n";
        }
        HashMap<String, String> mymap3=(HashMap<String,String>) this.getOptDefaults();
        if (mymap3!=null&&mymap3.keySet().size()>0) {
            for (String key : mymap3.keySet()) {
                mystring += key + ":" + mymap3.get(key) + "; ";
            }
            mystring += "\n";
        }
        for (String tag:this.getTags()){
            mystring+=tag+"; ";
        }
        mystring+="\n";
        mystring+="*****\n";
        return mystring;
    }

}