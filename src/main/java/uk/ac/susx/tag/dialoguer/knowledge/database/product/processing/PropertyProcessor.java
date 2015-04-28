package uk.ac.susx.tag.dialoguer.knowledge.database.product.processing;

import com.google.common.collect.Multimap;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 20/10/2014
 * Time: 15:35
 */
public class PropertyProcessor {

    /**
     * Given a list of products, return a subset of all the mainProps specified by those products, which distinguishes
     * those products. Intended to work with very similar products (e.g. the output of the groupByName() method)
     *
     * For example:
     *
     *   - You have a list of products, which are all types of the "Batman Returns" film.
     *   - Each product has a field "mainProps" which specify the important properties for distinguishing between
     *     products of this kind. In this example, each product says that "contributorDirector", "edition" and "formatType"
     *     are the mainProps.
     *   - This function looks through the films, finds that they are all directed by the same guy, and are all DVDs.
     *   - Therefore, the only distinguishing mainProp is "edition". This is what the function returns.
     */
    public static Set<String> getDistinguishingMainProps(List<Product> productList){
        Map<String, Set<String>> mainProps = new HashMap<>();
        Set<String> distinguishingProps = new HashSet<>();

        for (Product p : productList){
            for (String mainProp : p.getMainProps()){
                mainProps.put(mainProp, p.fetchPropertyValues(mainProp));
            }
        }
        for (Map.Entry<String, Set<String>> entry : mainProps.entrySet()) {
            for (Product p : productList){
                String propertyType = entry.getKey();
                if (entry.getValue() == null){
                    if (p.fetchPropertyValues(propertyType) !=null){
                        distinguishingProps.add(propertyType); break;
                    }
                } else {
                    if (!entry.getValue().equals(p.fetchPropertyValues(propertyType))){
                        distinguishingProps.add(propertyType); break;
                    }
                }
            }
        }
        return distinguishingProps;
    }

   /**
    * Given a list of products, return the union of all the mainProps specified by those products
    */
    public static Set<String> getUnionMainProps(List<Product> productList){
        Set<String> unionProps = new HashSet<>();
        for(Product p: productList){
            for(String mainProp:p.getMainProps()){
                unionProps.add(mainProp);
            }

        }
        return unionProps;
    }


    /**
     * Group a list of products by their name field.
     *
     * The return type is  a mapping from product name, to the sublist of products which have that name, for
     * each unique name in the full list.
     *
     * IMPORTANT: The reason a linkedhashmap is used is: typically, the input list you give to this
     * function will be ordered by relevance, and you probably don't want to lose that information. A LinkedHashMap
     * remembers the order in which items are inserted. So when you iterate through the map, you will see the
     * product names in the order in which they first appeared. So if your product names are [D, B, A, B, D, C], then
     * when iterating over the map, you will see that the keys are: [D, B, A, C].
     *
     * You might do something like this:
     *
     *   Map<String, List<Product>> grouped = groupByName(products);
     *
     *   for (Map.Entry<String, List<Product>> entry : grouped.entrySet()) {
     *       String productName = entry.getKey();
     *       Set<String> distinguishingProps = getDistinguishingMainProps(entry.getValue());
     *
     *       // Do something with productName + distinguishingProps
     *   }
     */
    public static Map<String, List<Product>> groupByName(List<Product> productList) {
        Map<String, List<Product>> groupedProducts = new LinkedHashMap<>();
        for (Product product : productList) {
            if (!groupedProducts.containsKey(product.getName())) {
                groupedProducts.put(product.getName(), new ArrayList<Product>());
            }
            groupedProducts.get(product.getName()).add(product);
        }
        return groupedProducts;
    }

    /**
     * Get a list of all the products (in the original ordering) which have the specified property key and value.
     * E.g.
     *   "get me all products that have a "title" property "thriller".
     *
     * If you then want to update the product set to only contain info about these products and their merchants, then
     * pass the products through a call to the "updateProducts" method.
     */
    public static List<Product> getFilteredProducts(String propertyKey, String propertyValue, List<Product> products){
        switch(propertyKey) {
            case("mainContributor"):
                return getContributorFilteredProducts("contributor",propertyValue, products);

            default:
                List<Product> filtered = new ArrayList<>();
                for (Product p : products) {
                    if (p.hasMatchedProperty(propertyKey, propertyValue)) {
                        filtered.add(p);
                    }
                }
                return filtered;
        }
    }



    /**
     * Get a list of all the products (in the original ordering) which have the specified key-value properties.
     * E.g.
     *   "get me all products that have a "title" property "thriller" and an "author" property "michael jackson"
     *
     * If you then want to update the product set to only contain info about these products and their merchants, then
     * pass the products through a call to the "updateProducts" method.
     */
    public static List<Product> getFilteredProducts(Multimap<String, String> requiredProperties, List<Product> products){
        List<Product> filtered = new ArrayList<>();
        for (Product p : products){
            if (p.hasProperties(requiredProperties)){
                filtered.add(p);
            }
        } return filtered;
    }

    /**
     * Get a list of all the products (in the original ordering) which have the specified property types.
     * E.g.
     *   "get me all products that have a "title" property and an "author" property.
     *
     * If you then want to update the product set to only contain info about these products and their merchants, then
     * pass the products through a call to the "updateProducts" method.
     */
    public static List<Product> getFilteredProducts(Set<String> propertyKeys, List<Product> products){
        List<Product> filtered = new ArrayList<>();
        for (Product p : products){
            if (p.hasProperties(propertyKeys)){
                filtered.add(p);
            }
        } return filtered;
    }

    /*****
     * Establish whether a property is distinguishing for a list of products
     *
     * @param products - a list of Products
     * @param prop - a propertyType e.g., formatType
     * @return boolean - is this property distinguishing for all of the products in the list
     *
     * this method is probably not required - why not, getDistinguishingProps for the list and the check whether the property is in the set?
     * I think I thought this would be more efficient.
     * This was intended for use in the now commented getTextOptionsBetter in DialogElementTypes (for converting a list of items into text with options)
     */

    public static boolean isDistinguishing(List<Product> products, String prop){
        Set<Set<String>> values = new HashSet<>();
        for (Product p:products){
            values.add(p.getProperties().get(prop));
        }

        return(values.size()>1);
    }

    /*
    *get a list of products where the main contributor (in mainprops) matches the contributorValue
    *
    *this is a special case of getFilteredProducts (where the main contributor key is not known for the product in advance)
    */
    private static List<Product> getContributorFilteredProducts(String contributorPrefix, String contributorValue, List<Product> products){
        //System.err.println("Performing getContributorFilteredProducts with: "+contributorKey+" : "+contributorValue);
        List<Product> filtered = new ArrayList<>();
        for(Product p:products){

            List<String> mainprops = p.getMainProps();
            for(String mp:mainprops){
                if (mp.startsWith(contributorPrefix)){
                    //System.err.println("Attempting to match "+mp+" with "+contributorValue);
                    if (p.hasMatchedProperty(mp,contributorValue)){
                        filtered.add(p);
                    }
                }
            }

        }
        return filtered;
    }
}
