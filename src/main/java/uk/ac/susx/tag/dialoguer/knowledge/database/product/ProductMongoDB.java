package uk.ac.susx.tag.dialoguer.knowledge.database.product;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import org.bson.types.ObjectId;
import uk.ac.susx.tag.dialoguer.knowledge.location.OverpassAPIWrapper;
import uk.ac.susx.tag.dialoguer.knowledge.location.ResultsElement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Easiest way to use this is a try-with-resources:
 *
 * try (MongoDB db = new MongoDB(hostname, port)) {
 *     // Do stuff with "db"
 * }
 *
 * Then the DB's resources will automatically be released when control steps out of the try statement.
 *
 * "productQuery()" is where the main text index querying is done. There is also functionality for searching by product
 * tags and adding new merchants and products.
 *
 * Otherwise you'll have to manually call "close()" on the object.
 *
 * Created by Andrew D. Robertson on 11/08/2014.
 */
public class ProductMongoDB implements AutoCloseable {

    private String dbName = "parcel";
    private static final String productCollection = "products";
    private static final String merchantCollection = "merchants";

    private final MongoClient client;

    // ----- Constructors -------------
    public ProductMongoDB() throws UnknownHostException {
        this("localhost", 27017, "parcel");
    }

    public ProductMongoDB(MongoClient client, String dbName){
        this.dbName = dbName;
        this.client = client;
    }

    public ProductMongoDB(String hostname, int port, String dbName) throws UnknownHostException {
        this(new MongoClient(new ServerAddress(hostname, port)), dbName);
    }

    // ----- DB tables ----------------
    private DBCollection getMerchants(){
        return client.getDB(dbName).getCollection(merchantCollection);
    }

    private DBCollection getProducts(){
        return client.getDB(dbName).getCollection(productCollection);
    }
    // --------------------------------

    public List<Product> productQuery(String textQuery) {
        return productQuery(textQuery, new ArrayList<Long>(), new HashSet<String>());
    }

    public List<Product> productQuery(String textQuery, List<Long> osmIds) {
        return productQuery(textQuery, osmIds, new HashSet<String>());
    }

    public List<Product> productQuery(String textQuery, List<Long> osmIds, Set<String> necessaryTags){
        return productQuery(textQuery, osmIds, necessaryTags, 0);
    }

    /**
     * Same as productQuery but using List<Merchant> instead of List<long>
     * Use this with the result of merchantQueryByLocation()
     */
    public List<Product> productQueryWithMerchants(String textQuery, List<Merchant> merchants, Set<String> necessaryTags, int limit){
        //------- Build query ----------
        BasicDBObject query = new BasicDBObject();

        // The product must have one of the merc Ids provided (ignored if none provided)
        if (merchants!=null && merchants.size() > 0) {
            query.append("mercID", new BasicDBObject("$in", extractMerchantIDs(merchants, true))); // Only match products whose "mercID" field is equal to one IDs of the product with the relevant osmIDs
        }

        // The product must satisfy the text query (using its name, description, property values and tags fields)
        query.append("$text", new BasicDBObject("$search", textQuery));

        // The product must have ALL of the tags requested (ignored if none provided)
        if (necessaryTags!=null){
            for (String tag : necessaryTags) {
                query.append("tags", new BasicDBObject("$in", Lists.newArrayList(tag)));
            }
        }
        //--------------------------------

        // Sorting key
        DBObject sortField = new BasicDBObject("score", new BasicDBObject("$meta", "textScore")); // Ensures that the relevancy score meta data of the product is added to the results so we can sort by it

        // Run query
        List<Product> results = new ArrayList<>();
        try (DBCursor cursor = getProducts().find(query, sortField)) {
            cursor.sort(sortField); // Sort by the relevancy score

            if (limit > 0) cursor.limit(limit); // If the limit is greater than zero, then limit the results to that number.

            // Convert results
            while (cursor.hasNext()) {
                results.add(convertProduct(cursor.next()));
            }
        }
        return results;
    }

    /**
     * Send a query to the MongoDB product database. The query will be matched against the Text index. The text index
     * covers: product name, description, property values, and tags. The results will be returned in order of relevance,
     * where the product at index 0 is the most relevant.
     *
     * @param textQuery The text query. Each word separated by a space is an individual keyword to be searched. So a query
     *                  of "red hot chili peppers" will search for anything that has "red" or "hot" etc. in it. Though
     *                  the products will be sorted by relevance, so products that contain all of those terms will be
     *                  more relevant.
     *
     *                  However, if you know that you're specifically looking for that ordering of words, then you can
     *                  make them a "phrase", such that products will only be returned if they contain that entire phrase.
     *                  This is achieved by encasing the phrase in double-quotes. You can combine any number of words or
     *                  phrases.
     *                  (NB: remember in Java they need escaping:  productQuery("\"red hot chili peppers\"");)
     *
     *                  You can negate words by prefixing them with a hyphen. Documents which contain any of the negated
     *                  words will be removed from the results.
     *
     * @param osmIds The list of Open Street Map IDs of shops that you're interested in. Set as null or empty to include
     *               all shops in the results.
     * @param necessaryTags A set of strings, where each string must be present in the "tags" field of the product for it
     *                      to remain in the results set. Leave null or empty to allow any tags field.
     * @param limit Limit the returned results size to this number. To have unlimited results, specify 0.
     * @return query results.
     */
    public List<Product> productQuery(String textQuery, List<Long> osmIds, Set<String> necessaryTags, int limit) {
        //System.err.println("Text query is "+textQuery);

        //------- Build query ----------
        BasicDBObject query = new BasicDBObject();

        // The product must have one of the merc Ids provided (ignored if none provided)
        if (osmIds!=null && osmIds.size() > 0) {
            osmIds.add(0L); //include online stores
            query.append("mercID", new BasicDBObject("$in", getMerchantIDs(osmIds))); // Only match products whose "mercID" field is equal to one IDs of the product with the relevant osmIDs
        }

        // The product must satisfy the text query (using its name, description, property values and tags fields)
        query.append("$text", new BasicDBObject("$search", textQuery));

        // The product must have ALL of the tags requested (ignored if none provided)
        if (necessaryTags!=null){
            for (String tag : necessaryTags) {
                query.append("tags", new BasicDBObject("$in", Lists.newArrayList(tag)));
            }
        }
        //--------------------------------

        // Sorting key
        DBObject sortField = new BasicDBObject("score", new BasicDBObject("$meta", "textScore")); // Ensures that the relevancy score meta data of the product is added to the results so we can sort by it

        // Run query
        List<Product> results = new ArrayList<>();
        try (DBCursor cursor = getProducts().find(query, sortField)) {
            cursor.sort(sortField); // Sort by the relevancy score

            if (limit > 0) cursor.limit(limit); // If the limit is greater than zero, then limit the results to that number.

            // Convert results
            while (cursor.hasNext()) {
                results.add(convertProduct(cursor.next()));
            }
        }
        return results;
    }

    /**
     * Product query by tags alone.
     *
     * @param tags the products should contain one or more of these tags
     * @param necessaryTags the products should contain all of these tags
     */
    public List<Product> productQueryTagsOnly(List<Long> osmIds, Set<String> tags, Set<String> necessaryTags) {
        //------- Build query ----------
        BasicDBObject query = new BasicDBObject();

        // The product must have one of the merc Ids provided (ignored if none provided)
        if (osmIds.size() > 0) {
            osmIds.add(0L); // include online stores
            query.append("mercID", new BasicDBObject("$in", getMerchantIDs(osmIds)));
        }

        if (tags.size() > 0){
            query.append("tags", new BasicDBObject("$in", Lists.newArrayList(tags)));
        }

        // The product must have ALL of the tags requested (ignored if none provided)
        for (String tag : necessaryTags) {
            query.append("tags", new BasicDBObject("$in", Lists.newArrayList(tag)));
        }

        // Run query
        List<Product> results = new ArrayList<>();
        try (DBCursor cursor = getProducts().find(query)) {
            // Convert results
            while (cursor.hasNext()) {
                results.add(convertProduct(cursor.next()));
            }
        }
        return results;
    }


    public Iterator<Product> allProducts() {
        try (final DBCursor cursor = getProducts().find()) {

            return new Iterator<Product>() {
                @Override
                public boolean hasNext() {
                    return cursor.hasNext();
                }

                @Override
                public Product next() {
                    return convertProduct(cursor.next());
                }

                @Override
                public void remove() {}
            };
        }
    }

    /**
     * Get all products from a particular merchant.
     */
    public List<Product> productQueryMerchantOnly(List<Long> osmIds) {
        return productQueryTagsOnly(osmIds, Sets.<String>newHashSet(), Sets.<String>newHashSet());
    }

    public Merchant getMerchant(String id) {
        DBObject m = getMerchants().findOne(new BasicDBObject("_id", new ObjectId(id)));

        return m==null? null : convertMerchant(m);
    }


    /**
     * Get a product by its ID
     */
    public Product getProduct(String id) {
        DBObject p = getProducts().findOne(new BasicDBObject("_id", new ObjectId(id)));

        return p==null? null: convertProduct(p);
    }

    /**
     * Given a list of Product IDs, get a list of the corresponding products.
     */
    public List<Product> getProductList(List<String> ids){
        List<Product> products = new ArrayList<>();
        for(String id: ids){
            products.add(getProduct(id));

        }
        return products;
    }

    /**
     * Given a list of Merchant Ids, get a list of the corresponding merchants.
     */
    public List<Merchant> getMerchantList(List<String> ids){
        List<Merchant> merchants = new ArrayList<>();
        for(String id:ids){
            merchants.add(getMerchant(id));

        }
        return merchants;

    }

    public List<Merchant> merchantQuery(String textQuery) {
        return merchantQuery(textQuery, null, 0);
    }

    public List<Merchant> merchantQuery(String textQuery, Set<String> necessaryTags){
        return merchantQuery(textQuery, necessaryTags, 0);
    }

    public List<Merchant> merchantQuery(String textQuery, Set<String> necessaryTags, int limit) {
        //------- Build query ----------
        BasicDBObject query = new BasicDBObject();

        // The merchant must satisfy the text query (using its name, description, and tags fields)
        query.append("$text", new BasicDBObject("$search", textQuery));

        // The product must have ALL of the tags requested (ignored if none provided)
        if (necessaryTags!=null){
            for (String tag : necessaryTags) {
                query.append("tags", new BasicDBObject("$in", Lists.newArrayList(tag)));
            }
        }
        //--------------------------------

        // Sorting key
        DBObject sortField = new BasicDBObject("score", new BasicDBObject("$meta", "textScore")); // Ensures that the relevancy score meta data of the product is added to the results so we can sort by it

        // Run query
        List<Merchant> results = new ArrayList<>();
        try (DBCursor cursor = getMerchants().find(query, sortField)) {
            cursor.sort(sortField); // Sort by the relevancy score

            if (limit > 0) cursor.limit(limit); // If the limit is greater than zero, then limit the results to that number.

            // Convert results
            while (cursor.hasNext()) {
                results.add(convertMerchant(cursor.next()));
            }
        }
        return results;
    }

    public List<Merchant> merchantQueryNameOnly(List<String> names) {
        //------- Build query ----------
        BasicDBObject query = new BasicDBObject();

        if (names.size() > 0){
            query.append("name", new BasicDBObject("$in", names));
        }

        // Run query
        List<Merchant> results = new ArrayList<>();
        try (DBCursor cursor = getMerchants().find(query)) {
            // Convert results
            while (cursor.hasNext()) {
                results.add(convertMerchant(cursor.next()));
            }
        }
        return results;
    }

    public List<Merchant> merchantQueryByLocation(double lat, double lon){
        return merchantQueryByLocation(lat, lon, 0, 0);
    }

    /**
     * Use this to list all supported merchants in the order of how close they are.
     *
     * maxDistance: do not add to the list any merchants above this distance away
     * limit: limit the results to this number
     */
    public List<Merchant> merchantQueryByLocation(double lat, double lon, double maxDistance, int limit){
        //------- Build query ----------
        BasicDBObject query = new BasicDBObject();

        BasicDBObject point = new BasicDBObject("type", "Point")
                .append("coordinates", Lists.newArrayList(lon, lat));

        BasicDBObject nearnessDef = new BasicDBObject("$geometry", point);

        if (maxDistance > 0){
            nearnessDef.append("$maxDistance", maxDistance);
        }

        query.append("geojson", new BasicDBObject("$near", nearnessDef));

        List<Merchant> results = new ArrayList<>();
        try (DBCursor cursor = getMerchants().find(query)) {

            if (limit > 0) cursor.limit(limit); // If the limit is greater than zero, then limit the results to that number.

            // Convert results
            while (cursor.hasNext()) {
                results.add(convertMerchant(cursor.next()));
            }
        }
        return results;
    }

    /**
     * Update the fields of an already existing product.
     */
    public void updateProduct(Product p) {
        WriteResult r = getProducts()
                .update(new BasicDBObject("_id", new ObjectId(p.getProductId())),
                        convertProduct(p));
        if (!r.isUpdateOfExisting()) throw new DatabaseException("Product doesn't exist");
    }

    /**
     * Update the fields of an already existing merchant.
     * @param m the merchant
     */
    public void updateMerchant(Merchant m) {
        WriteResult r = getMerchants()
                .update(new BasicDBObject("_id", new ObjectId(m.getMerchantId())),
                        convertMerchant(m));
        if (!r.isUpdateOfExisting()) throw new DatabaseException("Merchant doesn't exist");
    }


    public void addNewProduct(Product p) {
        getProducts().insert(convertProduct(p));
    }

    /**
     * Add new Products to the database.
     */
    public void addNewProducts(Iterable<Product> products) {
        BulkWriteOperation bulk = getProducts().initializeUnorderedBulkOperation();
        for (Product p : products)
            bulk.insert(convertProduct(p));
        bulk.execute();
    }

    /**
     * Add a new merchant given is open street map ID, and other meta data.
     */
    public void addNewMerchant(long openStreetMapID, String locationDescription, String address) {
        ResultsElement osmElement;
        try {
            osmElement = new OverpassAPIWrapper().getResultsElement(openStreetMapID);
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
        BasicDBObject newMerc = new BasicDBObject()
                .append("osmID", openStreetMapID)
                .append("name",  osmElement.getTagValue("name"))
                .append("locDesc",locationDescription)
                .append("address", address)
                .append("lat",  osmElement.lat)
                .append("lon",  osmElement.lon)
                .append("radius",osmElement.radius);
        getMerchants().insert(newMerc);
    }


    /**
     * Currently only testing purposes. Provide raw JSON query to database.
     */
    public List<Product> jsonProductQuery(String jsonString) {
        List<Product> results = new ArrayList<>();
        try (DBCursor cursor = getProducts().find(new Gson().fromJson(jsonString, BasicDBObject.class))) {
            // Convert results
            while (cursor.hasNext()) {
                results.add(convertProduct(cursor.next()));
            }
        }
        return results;
    }



    public List<ObjectId> getMerchantIDs(List<Long> osmIds){
        BasicDBObject query = new BasicDBObject("osmID", new BasicDBObject("$in", osmIds));

        // Run query
        List<ObjectId> results = new ArrayList<>();
        try (DBCursor cursor = getMerchants().find(query)) {
            // Convert results
            while (cursor.hasNext()) {
                results.add((ObjectId)cursor.next().get("_id"));
            }
        }
        return results;
    }

    public List<ObjectId> extractMerchantIDs(List<Merchant> merchants, boolean includeOnlineShopIds){
        List<ObjectId> ids = new ArrayList<>();
        for (Merchant m: merchants){
            ids.add(new ObjectId(m.getMerchantId()));
        }

        if (includeOnlineShopIds)
            ids.addAll(getMerchantIDs(Lists.newArrayList(0L)));

        return ids;
    }

    /**
     * Given a DBObject representing a merchant obtained from the database, create a Merchant object.
     */
    private Merchant convertMerchant(DBObject m){
        Merchant converted = new Merchant(m.get("_id").toString(),
                (long)m.get("osmID"),
                (String)m.get("name"),
                (String)m.get("locDesc"),
                (String)m.get("address"),
                (double)m.get("lat"),
                (double)m.get("lon"),
                (double)m.get("radius"),
                (String)m.get("description"),
                (List<String>)m.get("tags"));

        if (m.containsField("geojson"))
            converted.setGeojson(JSON.serialize(m.get("geojson")));

        return converted;
    }

    /**
     * Given a Merchant object, create the appropriate equivalent DBObject.
     */
    private DBObject convertMerchant(Merchant m){
        BasicDBObject o =  new BasicDBObject("_id", new ObjectId(m.getMerchantId()))
                .append("osmID", m.getOsmId())
                .append("name", m.getName())
                .append("locDesc", m.getLocDesc())
                .append("address", m.getAddress())
                .append("lat", m.getLat())
                .append("lon", m.getLon());

        if (m.getGeojson() != null && !m.getGeojson().equals(""))
            o.append("geojson", JSON.parse(m.getGeojson()));

        o.append("radius", m.getRadius());

        if (m.getDescription() != null) o.append("description", m.getDescription());
        if (m.getTags() != null) o.append("tags", m.getTags());


        return o;
    }

    /**
     * Given a DBObject from the product table, convert it into a Product object.
     *
     * Note the complication that "properties" are stored quite differently between the two representations. So the
     * conversion must handle this. This is to allow the text index to index the value array (which wouldn't be possible
     * if the keys and values were in a map, rather than matched arrays). But the most intuitive structure is a mapping,
     * for specific lookup purposes, which are carried out on Product objects.
     *
     * In the MongoDB, the properties field looks like this:
     *
     *   {
     *       properties: {
     *           keys : ["key1", "key2", ... ],
     *           values : ["value1", "value2", ...]
     *       }
     *   }
     *
     * But our Product objects use a HashMap, creating a structure more like:
     *
     *   {
     *       properties: {
     *           key1 : "value1",
     *           key2 : "value2"
     *       }
     *   }
     */

    /**
     * Convert a DBObject into a Product
     * @param dbObject DBObject to be converted
     * @return Property representing the DBObject
     */
    @SuppressWarnings("unchecked")
    private Product convertProduct(DBObject dbObject){

        //Create parameters for Product object
        String id = dbObject.get("_id").toString();
        String name = (String)dbObject.get("name");
        String description = (String)dbObject.get("description");
        HashMultimap<String, String> properties = dbProperties2Map((DBObject)dbObject.get("properties"));
        Merchant merchant = getMerchant(dbObject.get("mercID").toString());
        int price = (int)dbObject.get("price");
        List <String> tags = (List<String>)dbObject.get("tags");

        // -- Options
        Map<String, List<String>> options = new HashMap<>();
        BasicDBObject dbOptions = (BasicDBObject)dbObject.get("options");
        if (dbOptions != null){
            for (Map.Entry<String, Object> entry : dbOptions.entrySet()){
                options.put(entry.getKey(), (List<String>)entry.getValue());
            }
        }

        // -- Option defaults
        Map<String, String> optDefaults = new HashMap<>();
        BasicDBObject dbOptDefaults = (BasicDBObject)dbObject.get("optDefaults");
        if (dbOptDefaults!=null) {
            for (Map.Entry<String, Object> entry : dbOptDefaults.entrySet()) {
                optDefaults.put(entry.getKey(), (String) entry.getValue());
            }
        }

        // -- Main props
        List<String> temp = (List<String>)dbObject.get("mainProps");
        List<String> mainProps = (temp == null ? new ArrayList<String>() : temp);

        return new Product(id, name, description, properties, merchant, price, options, optDefaults, tags, mainProps);
    }

    /**
     * Given a Product object, convert it to its database representation ready to be stored/looked up in the database.
     *
     * Note the complication that "properties" are stored quite differently between the two representations. So the
     * conversion must handle this. This is to allow the text index to index the value array (which wouldn't be possible
     * if the keys and values were in a map, rather than matched arrays). But the most intuitive structure is a mapping,
     * for specific lookup purposes, which are carried out on Product objects.
     *
     * In the MongoDB, the properties field looks like this:
     *
     *   {
     *       properties: {
     *           keys : ["key1", "key2", ... ],
     *           values : ["value1", "value2", ...]
     *       }
     *   }
     *
     * But our Product objects use a HashMap, creating a structure more like:
     *
     *   {
     *       properties: {
     *           key1 : "value1",
     *           key2 : "value2"
     *       }
     *   }
     */
    private BasicDBObject convertProduct(Product product){
        BasicDBObject converted = new BasicDBObject()
                .append("name", product.getName())
                .append("description", product.getDescription())
                .append("price", product.getPrice())
                .append("mercID", new ObjectId(product.getMerchant().getMerchantId()))
                .append("tags", product.getTags());

        if (product.getProductId() != null)
            converted.append("_id", new ObjectId(product.getProductId()));

        if (product.getProperties()!= null && !product.getProperties().isEmpty())
            converted.append("properties", map2DBProperties(product.getProperties()));
        if (product.getOptions() != null && !product.getOptions().isEmpty())
            converted.append("options", product.getOptions());
        if (product.getOptDefaults()!= null && !product.getOptDefaults().isEmpty())
            converted.append("optDefaults", product.getOptDefaults());
        if (product.getMainProps()!= null && !product.getMainProps().isEmpty())
            converted.append("mainProps", product.getMainProps());
        return converted;
    }

    private Map<String, List<String>> map2DBProperties(HashMultimap<String, String> productFormatProperties){
        Map<String, List<String>> properties = new HashMap<>();
        List<String> propertyKeys = new ArrayList<>();
        List<String> propertyValues = new ArrayList<>();
        for(Map.Entry<String, String> entry : productFormatProperties.entries()){
            propertyKeys.add(entry.getKey());
            propertyValues.add(entry.getValue());
        }
        properties.put("keys", propertyKeys);
        properties.put("values", propertyValues);
        return properties;
    }

    @SuppressWarnings("unchecked")
    private HashMultimap<String, String> dbProperties2Map(DBObject dbFormatProperties){
        HashMultimap<String, String> properties = HashMultimap.create();

        if (dbFormatProperties == null) return properties;

        List<String> propertyKeys = (List<String>)dbFormatProperties.get("keys");
        List<String> propertyValues = (List<String>)dbFormatProperties.get("values");

        for (int i = 0; i < propertyKeys.size(); i++){
            properties.put(propertyKeys.get(i), propertyValues.get(i).trim());
        }
        return properties;
    }

    @Override
    public void close() throws Exception {
        client.close();
    }

    public static class DatabaseException extends RuntimeException {

        public DatabaseException (String msg){
            super(msg);
        }

        public DatabaseException (String msg, Exception cause){
            super(msg, cause);
        }

        public DatabaseException (Exception e){
            super(e);
        }
    }

    public static class LengthOrdering extends Ordering<String> {

        @Override
        public int compare(String left, String right) {
            return left.length() - right.length();
        }
    }

    public static void main(String[] args) throws Exception {

//        Set<String> dictionary = new HashSet<>();
//
//        String line;
//        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("/Volumes/LocalDataHD/adr27/Downloads/british/brit-a-z.txt"), "UTF-8"))){
//            while ((line = br.readLine()) != null){
//                dictionary.add(line);
//            }
//        }
//
//        Set<String> authors = new HashSet<>();
//        Set<String> titles = new HashSet<>();
//
//        try (MongoDB d = new MongoDB()) {
//
//            List<Product> products = d.productQueryTagsOnly(Lists.<Long>newArrayList(), Sets.newHashSet("book"), Sets.<String>newHashSet());
//
//            for (Product p : products) {
//                if (p.hasProperty("bicSubject") && p.getProperties().get("bicSubject").iterator().next().contains("fiction")) {
//                    if (p.hasProperty("contributorPrimaryAuthor")) {
//                        Set<String> currentAuthors = p.getProperties().get("contributorPrimaryAuthor");
//                        for (String author : currentAuthors)
//                            if (!dictionary.contains(author.toLowerCase()))
//                                authors.add(author);
//                    }
//
//                    if (p.hasProperty("title")){
//                        Set<String> currentTitles = p.getProperties().get("title");
//                        for (String title : currentTitles)
//                            if (!dictionary.contains(title.toLowerCase()))
//                                titles.add(title);
//                    }
//                }
//            }
//
//        }

//        for (String title : Ordering.natural().sortedCopy(titles)){
//            System.out.println(title);
//        }

//        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Volumes/LocalDataHD/adr27/Desktop/authors.txt"), "UTF-8")) ){
//            for (String author : new LengthOrdering().sortedCopy(Sets.difference(authors, titles))) {
//                bufferedWriter.write(author+"\n");
//            }
//        }
//
//        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Volumes/LocalDataHD/adr27/Desktop/titles.txt"), "UTF-8")) ){
//            for (String title : new LengthOrdering().sortedCopy(Sets.difference(titles, authors))) {
//                bufferedWriter.write(title+"\n");
//            }
//        }

//        try (BufferedReader br = new BufferedReader(new InputStreamReader(Resources.getResource("ner-ext/KnownLists/gazetteers-list.txt").openStream(), "UTF-8"))){
//            String line;
//            while ((line=br.readLine()) != null) {
//                System.out.println(line);
//            }
//        }

//        Parameters.readConfigAndLoadExternalData("/Volumes/LocalDataHD/adr27/Downloads/illinois-ner/config/ontonotes.config");
//        NETagPlain.init();
//        String input = "I would like to buy an album by Michael Jackson.";
//        String result = NETagPlain.tagLine(input);
//        System.out.println(result);

        String text  = "WASHINGTON (AP) _ All else being equal, Duane Roelands would prefer to dash off short instant text messages to co-workers and friends with the service offered by Microsoft _ the one he finds easiest to use. But for Roelands, all else is not equal: His office, clients and nearly everyone else he knows use America Online's messaging system. Now, he does too. ``There are features that I want and I like,'' said Roelands, a Web developer, who likens it to the battle between VHS and Beta video recorders in the 1980s. ``But the reality is if I use the better product, I get less functionality.'' For this reason, instant messaging rivals like Microsoft, AT&AMP;T and ExciteAtHome maintain their users ought to be able to send messages to anyone else, regardless of what service they happen to have. That's not currently possible. The companies are lobbying the Federal Communications Commission to require AOL to make its product compatible with those offered by competitors as a condition of its merger with Time Warner. So far, the agency appears to favor a more tailored approach. The commission's staff has recommended that AOL be required to make its system work with at least one other provider, but the requirement would apply only to advanced instant messaging services offered over Time Warner's cable lines. How the agency defines advanced services is unclear. They could refer to features beyond text messaging, such as video teleconferencing, the sharing of files or messaging over interactive television. Today, consumers more commonly take advantage of the garden variety functions. They type short real-time phrases to others, allowing them to ``chat'' back-and-forth using text. Unlike e-mail, it's instantaneous and gets the recipient's attention right away. People can communicate with international friends without the hefty phone bills. And the service has taken hold with those who have hearing or speech disabilities. Unlike the telephone, people can discreetly interact with others _ or decide not to. ``It's communications that can be ignored,'' said Jonathan Sacks, a vice president at AOL, which runs the two leading messaging services _ ICQ and AIM _ with 140 million users. ``On the telephone, you can't see when somebody is near the phone. You can't see when it's convenient for them to communicate with you.'' AOL rivals say that if instant messaging is to be as ubiquitous as the phone network, it has to work the same way: People who use different providers must still be able to contact one another. They continue to lobby the FCC, hoping to see the conditions broadened before the agency issues its final decision. ``It's really important to get this right before innovation is squashed because one company has a monopoly,'' said Jon Englund, vice president of government affairs for ExciteAtHome. ``It's absolutely critical that Internet uses have real choice among competing platforms.'' AOL has said it wants to work toward interoperability, but first needs to protect consumer privacy and security to prevent the kinds of problems that have emerged in the e-mail world, like spamming _ unwanted junk messages. Company officials disagreed that AOL's market share was keeping out competitors. AOL executives cited a recent study by Media Metrix indicating that the messaging services offered by Yahoo! and Microsoft are the fastest growing in the United States. Why all the fuss over a free product that anyone, even those who don't subscribe to AOL, can use? Some pointed to the recent demise of two instant messaging competitors _ iCAST and Tribal Voice _ as evidence that AOL's dominance could prevent choices in the market. Another concern is that AOL could use its substantial customer base to tack on new advanced services and then charge for them. Rivals said the ability of various services to work together will become increasingly important in the future. For example, as instant messaging migrates to cell phones or hand-held computer organizers, consumers won't want to have to install multiple services on these devices, said Brian Park, senior product for Yahoo! Communications Services. ``You can have the best service and the coolest features, but nobody is going to use it if it doesn't communicate with other services,'' Park said. ___ On the Net: America Online corporate site: http://corp.aol.com IMUnified, coalition formed by AT&AMP;T, ExciteAtHome, Microsoft: http://www.imunified.org/ ";

        text = "I would like to buy a book by J R R Tolkien.";

//        IllinoisNerExtHandler handler = new IllinoisNerExtHandler("nerdata/config/mynewmodel.config");
////        IllinoisNerExtHandler handler = new IllinoisNerExtHandler("/Volumes/LocalDataHD/adr27/Downloads/illinois-ner/config/ontonotes.config");
//        Record input = RecordGenerator.generateTokenRecord(text, false);
//        Labeling labels = handler.performNer(input);
//        for(Iterator<Span> label= labels.getLabelsIterator(); label.hasNext() ; ) {
//            Span span = label.next();
//            System.out.println("["+span.start+"-"+span.ending+"]");
//            System.out.println(text.substring(span.start, span.ending)+"\t:\t"+span.getLabel());
//        }
////
//        try (MongoDB d = new MongoDB()) {

//            List<Product> books = d.productQueryTagsOnly(new ArrayList<Long>(), Sets.newHashSet("book"), new HashSet<String>());

//            File authors = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/neil_gaiman/neil_gaiman_author.txt");
//            File titles = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/neil_gaiman/neil_gaiman_titles.txt");
//            File trainingTemplate = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/neil_gaiman/neil_gaiman_text.txt");
//            File output = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/neil_gaiman/neil_gaiman_products.txt");

//            File authors = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/michael_connelly/michael_connelly_author.txt");
//            File titles = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/michael_connelly/michael_connelly_titles.txt");
//            File trainingTemplate = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/michael_connelly/michael_connelly_text.txt");
//            File output = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/michael_connelly/michael_connelly_products.txt");

//            File authors = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/to_kill_a_mocking_bird/to_kill_a_mockingbird_author.txt");
//            File titles = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/to_kill_a_mocking_bird/to_kill_a_mockingbird_title.txt");
//            File trainingTemplate = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/to_kill_a_mocking_bird/to_kill_a_mockingbird_text.txt");
//            File output = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/to_kill_a_mocking_bird/to_kill_a_mockingbird_products.txt");

//            File authors = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/black_echo/black_echo_author.txt");
//            File titles = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/black_echo/black_echo_title.txt");
//            File trainingTemplate = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/black_echo/black_echo_text.txt");
//            File output = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/black_echo/black_echo_products.txt");



//            makeTrainingData(ImmutableMap.of("contributorPrimaryAuthor", authors, "title", titles),
//                             ImmutableMap.of("contributorPrimaryAuthor", "PERSON", "title", "WORK_OF_ART"),
//                             books, trainingTemplate, output);

//            File author_lst = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/Authors.lst");
//            File title_lst = new File("/Volumes/LocalDataHD/git/method51-deploy/data/webapp/output/parcel/dataexport/Titles.lst");
//            Set<String> authors = new HashSet<>();
//            Set<String> titles = new HashSet<>();
//
//            for (Product book : books){
//                if (book.hasProperty("title")) {
//                    titles.add(book.fetchPropertyValues("title").iterator().next());
//                }
//                if (book.hasProperty("contributorPrimaryAuthor")){
//                    String author= book.fetchPropertyValues("contributorPrimaryAuthor").iterator().next();
//                    authors.add(author);
//                    String[] authorSplit = author.split(" ");
//                    if (authorSplit.length > 2){
//                        authors.add(authorSplit[0] + " " + authorSplit[authorSplit.length-1]);
//                    }
//                }
//            }
//
//            try (BufferedWriter authorBW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(author_lst), "UTF-8"));
//                 BufferedWriter titleBW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(title_lst), "UTF-8"))){
//
//                for (String author : Ordering.natural().sortedCopy(authors)){
//                    authorBW.write(author); authorBW.write("\n");
//                }
//
//                for (String title : Ordering.natural().sortedCopy(titles)){
//                    titleBW.write(title); titleBW.write("\n");
//                }
//
//            }

//            List<Merchant> m = d.merchantQueryByLocation(50.82362064, -0.14392826, 0,0);

//            d.addNewMerchant(566635627, "Can be seen from the clock tower.", "12-13 North Street, Brighton, East Sussex BN1 3GJ");

//            System.out.println("Done");


//            List<Product> products = d.productQuery("pale ale", Lists.newArrayList(566635627L));

//            List<Merchant> merchants = d.merchantQueryByLocation(50.823843, -0.143801);
//
//            Merchant indulge = d.getMerchant("54044e4c846a9317e78a36d0");
//
//            List<String> bookCatalogues = Lists.newArrayList(
//                    "/Volumes/LocalDataHD/adr27/Desktop/West10Data/West10 Book Data/WEST10_BDS_DBBOOK_H_FULL_20141110_1.xml",
//                    "/Volumes/LocalDataHD/adr27/Desktop/West10Data/West10 Book Data/WEST10_BDS_DBBOOK_H_FULL_20141110_2.xml",
//                    "/Volumes/LocalDataHD/adr27/Desktop/West10Data/West10 Book Data/WEST10_BDS_DBBOOK_H_FULL_20141110_3.xml",
//                    "/Volumes/LocalDataHD/adr27/Desktop/West10Data/West10 Book Data/WEST10_BDS_DBBOOK_H_FULL_20141110_4.xml",
//                    "/Volumes/LocalDataHD/adr27/Desktop/West10Data/West10 Book Data/WEST10_BDS_DBBOOK_H_FULL_20141110_5.xml",
//                    "/Volumes/LocalDataHD/adr27/Desktop/West10Data/West10 Book Data/WEST10_BDS_DBBOOK_H_FULL_20141110_6.xml",
//                    "/Volumes/LocalDataHD/adr27/Desktop/West10Data/West10 Book Data/WEST10_BDS_DBBOOK_H_FULL_20141110_7.xml",
//                    "/Volumes/LocalDataHD/adr27/Desktop/West10Data/West10 Book Data/WEST10_BDS_DBBOOK_H_FULL_20141110_8.xml",
//                    "/Volumes/LocalDataHD/adr27/Desktop/West10Data/West10 Book Data/WEST10_BDS_DBBOOK_H_FULL_20141111_9.xml",
//                    "/Volumes/LocalDataHD/adr27/Desktop/West10Data/West10 Book Data/WEST10_BDS_DBBOOK_H_FULL_20141111_10.xml",
//                    "/Volumes/LocalDataHD/adr27/Desktop/West10Data/West10 Book Data/WEST10_BDS_DBBOOK_H_FULL_20141111_11.xml",
//                    "/Volumes/LocalDataHD/adr27/Desktop/West10Data/West10 Book Data/WEST10_BDS_DBBOOK_H_FULL_20141111_12.xml",
//                    "/Volumes/LocalDataHD/adr27/Desktop/West10Data/West10 Book Data/WEST10_BDS_DBBOOK_H_FULL_20141111_13.xml",
//                    "/Volumes/LocalDataHD/adr27/Desktop/West10Data/West10 Book Data/WEST10_BDS_DBBOOK_H_FULL_20141111_14.xml",
//                    "/Volumes/LocalDataHD/adr27/Desktop/West10Data/West10 Book Data/WEST10_BDS_DBBOOK_H_FULL_20141111_15.xml"
//            );
//
//            for (String book : bookCatalogues){
//                try (BookCollection f = new BookCollection(new File(book), indulge)){
//                    d.addNewProducts(f);
//                }
//            }
//
//
//            try (GameCollection f = new GameCollection(new File("/Volumes/LocalDataHD/adr27/Desktop/West10Data/WEST10_DBGAME_A_FULL_20141107_1.xml"), indulge)){
//                d.addNewProducts(f);
//            }
//
//
//            System.out.println("Done.");
//
//
//
//        }
///
//
//            for (Product p : d.productQuery("bon jovi", null, null, 3)) {
//                System.out.println("=============");
//                System.out.println("Title: " + p.fetchPropertyValues("title"));
//                System.out.println("Artist:" + p.fetchPropertyValues("contributorDirector"));
//                System.out.println("Editon: " + p.fetchPropertyValues("edition"));
//                System.out.println("Format: " + p.fetchPropertyValues("formatType"));
//            }
//
//            System.out.println(new ProductSet().getDistinguishingMainProps(d.productQuery("bon jovi", null, null, 3)));

//            Merchant indulge = d.getMerchant("54044e4c846a9317e78a36d0");

//            try  (FilmCollection f = new FilmCollection(new File("/Volumes/LocalDataHD/adr27/Downloads/West10Data/DVD_A_Feed_30_06_14/WEST10_DBDVD_A_20140627_1.xml"), indulge)){
//                d.addNewProducts(f);
//            }
//
//            try  (FilmCollection f = new FilmCollection(new File("/Volumes/LocalDataHD/adr27/Downloads/West10Data/DVD_A_Feed_30_06_14/WEST10_DBDVD_A_20140627_2.xml"), indulge)){
//                d.addNewProducts(f);
//            }
//
//            try (MusicCollection m = new MusicCollection(new File("/Volumes/LocalDataHD/adr27/Downloads/West10Data/Music Data/MUSIC_A_Feed_30_06_14/WEST10_DBMUSIC_A_20140627_1.xml"), indulge)){
//                d.addNewProducts(m);
//            }
//            try (MusicCollection m = new MusicCollection(new File("/Volumes/LocalDataHD/adr27/Downloads/West10Data/Music Data/MUSIC_A_Feed_30_06_14/WEST10_DBMUSIC_A_20140627_2.xml"), indulge)){
//                d.addNewProducts(m);
//            }
//            try (MusicCollection m = new MusicCollection(new File("/Volumes/LocalDataHD/adr27/Downloads/West10Data/Music Data/MUSIC_A_Feed_30_06_14/WEST10_DBMUSIC_A_20140628_3.xml"), indulge)){
//                d.addNewProducts(m);
//            }

//            List<Product> products = d.productQuery("Shrek", null, null, 20);
//
//            List<Product> foreverAfter = new ArrayList<>();
//
//            for (Product p : products){
//                if(p.getName().equals("Shrek: Forever After - The Final Chapter"))
//                    foreverAfter.add(p);
//            }
//
//            System.out.println(new ProductSet(false, 0,0,0,null,null,null,foreverAfter).getDistinguishingMainProps());
//
//            System.out.println("Done");

    }

    public static void makeTrainingData(Map<String, File> propertyValueFiles, Map<String, String> outputTagsPerProperty, List<Product> products, File trainingTemplates, File output) throws IOException {
        Map<String, List<String>> propertyValues = new HashMap<>();

        // For ordering by string length
        Ordering<String> byLengthOrdering = new Ordering<String>() {
            public int compare(String left, String right) {
                return Ints.compare(left.length(), right.length());
            }
        };

        // Read in the values that properties can take
        for (Map.Entry<String, File> entry : propertyValueFiles.entrySet()){
            List<String> values = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(entry.getValue()), "UTF-8"))){
                String line;
                while ((line=br.readLine()) != null) {
                    values.add(line.toLowerCase());
                }
            }
            propertyValues.put(entry.getKey(), byLengthOrdering.reverse().sortedCopy(values));
        }

        // Read in the templates and write out the training data
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
             BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(trainingTemplates), "UTF-8"))){

            String trainingTemplate;
            while ((trainingTemplate=br.readLine()) != null) {
                Product p = randomProductWithProperties(products, propertyValues.keySet(), 100);
                for (Map.Entry<String, List<String>> entry : propertyValues.entrySet()){
                    String productProperty = p.fetchPropertyValues(entry.getKey()).iterator().next();
                    String outputLabel = "[" + outputTagsPerProperty.get(entry.getKey()) + " " + productProperty + "]";
                    for (String toReplace : entry.getValue()){
                        String old = trainingTemplate;
                        try {
                            trainingTemplate = trainingTemplate.replaceAll("(?i)" + toReplace, outputLabel);
                        } catch  (IndexOutOfBoundsException e){
                            System.out.println("To replace:"+toReplace); throw e;
                        }
                    }
                }
                bw.write(trainingTemplate);
                bw.write("\n");
            }
        }
    }


    public static Product randomProductWithProperties(List<Product> products, Set<String> properties, int maxTries){
        Random r = new Random();
        Product finalProduct = null;
        int tries = 0;
        while (finalProduct == null){
            if (tries >= maxTries) throw new RuntimeException("After " + maxTries + " random selections, no product was found with all the necessary properties.");
            Product p = products.get(r.nextInt(products.size()));
            if (p.hasProperties(properties)){
                finalProduct = p;
            }
            tries++;
        }
        return finalProduct;
    }

}
