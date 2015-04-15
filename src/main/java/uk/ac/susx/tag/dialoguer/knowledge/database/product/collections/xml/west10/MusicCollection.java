package uk.ac.susx.tag.dialoguer.knowledge.database.product.collections.xml.west10;

import com.google.common.base.CaseFormat;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Merchant;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Product;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.collections.xml.RecordBasedXMLCollection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 17/09/2014
 * Time: 15:26
 */
public class MusicCollection extends RecordBasedXMLCollection {

    private Merchant merchant;

    public MusicCollection(File xmlFile, Merchant merchant) throws FileNotFoundException, UnsupportedEncodingException, XMLStreamException {
        super(xmlFile, "record");
        this.merchant = merchant;
    }

    @Override
    protected Product readNextProductRecord(XMLStreamReader reader) throws XMLStreamException {
        Record r = new Record(reader);
        return r.toProduct();
    }

    private class Record {

        private Map<String, String> atts;
        private List<BasicRecord> contributorList;
        private List<BasicRecord> trackList;

        public Record(XMLStreamReader reader) throws XMLStreamException {
            atts = new HashMap<>();
            contributorList = new ArrayList<>();
            trackList = new ArrayList<>();
            readRecord(reader);
        }

        private void readRecord(XMLStreamReader reader) throws XMLStreamException {
            String currentKey = null;
            if (isStartElement("record")){
                while (reader.hasNext() && !isEndElement("record")){
                    reader.next();
                    if (reader.isStartElement()){
                        if (reader.getLocalName().equals("contributor_list")){
                            contributorList = readBasicRecords(reader, "person", "contributor_list");
                            currentKey = null;
                        } else if(reader.getLocalName().equals("tracklist")) {
                            trackList = readBasicRecords(reader, "track_details", "tracklist");
                            currentKey = null;
                        } else {
                            currentKey = reader.getLocalName();
                        }
                    } else if (reader.isCharacters()) {
                        if (currentKey != null && !reader.getText().trim().equals("")){
                            atts.put(currentKey, reader.getText());
                            currentKey = null;
                        }
                    }
                }
            } else throw new IllegalStateException("Reader is not at the start of a record");
        }

        public Product toProduct(){

            //Create empty Product
            HashMultimap<String, String> properties = HashMultimap.create();
            Product p = new Product(null, "", "", properties, null, 0, new HashMap<String, List<String>>(),
                    new HashMap<String, String>(), new ArrayList<String>(), new ArrayList<String>());

            // Fields that start with "sort" are also excluded
            Set<String> propertyExcludes = Sets.newHashSet("authTitle", "facet", "imprintExact",
                    "jacketL","jacketM","jacketS", "kwordIndex","nameExact", "seriesExact", "titleExact",
                    "extras2", "extras1", "psc", "psg", "tracks");

            for (Map.Entry<String, String> entry : atts.entrySet()){
                String propertyType = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, entry.getKey().trim());
                String propertyValue = entry.getValue().trim();

                if (propertyType.equals("ctitle")){
                    p.addPropertyValue("title", propertyValue);
                    p.setName(propertyValue);
                } else if (propertyType.equals("description")) {
                    p.setDescription(propertyValue);
                } else if (!propertyType.startsWith("sort")  && !propertyExcludes.contains(propertyType)){
                    p.addPropertyValue(propertyType, propertyValue);
                }
            }

            if (contributorList!=null){
                for (BasicRecord r : contributorList){
                    p.addPropertyValue("contributor"+r.getAtts().get("role"), r.getAtts().get("person_display_name"));
                }
            }

            if (trackList!=null){
                for (BasicRecord r : trackList){
                    p.addPropertyValue("track", r.getAtts().get("track_name"));
                }
            }


            p.setTags(Lists.newArrayList("music"));
            if(atts.containsKey("uk_vat_price") ){
                String price = atts.get("uk_vat_price").replaceAll("[^\\d.]", "");
                p.setPrice(Integer.parseInt(price.replaceFirst("\\.", "")));
            }
            p.setMerchant(merchant);

            if (contributorList.isEmpty()) {
                p.setMainProps(Lists.newArrayList("title", "formatType", "edition"));
            } else {
                p.setMainProps(Lists.newArrayList("title", "contributor"+contributorList.get(0).getAtts().get("role") ,"formatType", "edition"));
            }
            return p;
        }
    }
}
