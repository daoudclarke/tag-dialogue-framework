package uk.ac.susx.tag.dialoguer.knowledge.database.product.collections.xml;


import uk.ac.susx.tag.dialoguer.knowledge.database.product.Product;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This class caters to a particular kind of XML collection. In its most basic form, it looks something like below:
 *
 * <collection>
 *     <record>
 *         <att1>value</att1>
 *         <att2>value</att2>
 *     </record>
 *     <record>
 *         etc.
 *     </record>
 * </collection>
 *
 * Where you have a containing element ("collection") and a bunch of record elements ("record"), each of which are filled
 * with elements which specify properties of those product records.
 *
 *
 * User: Andrew D. Robertson
 * Date: 17/09/2014
 * Time: 15:03
 */
public abstract class RecordBasedXMLCollection extends XMLCollection{

    String recordElementType;
    private boolean hasNext;

    public RecordBasedXMLCollection(File xmlFile) throws FileNotFoundException, UnsupportedEncodingException, XMLStreamException {
        this(xmlFile, "record");
    }

    public RecordBasedXMLCollection(File xmlFile, String recordElementType) throws FileNotFoundException, UnsupportedEncodingException, XMLStreamException {
        super(xmlFile);
        this.recordElementType = recordElementType;
        hasNext = isMoreRecords(super.getReader());
    }

    private boolean isMoreRecords(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        while (xmlStreamReader.hasNext()) {
            xmlStreamReader.next();
            if (isStartElement(recordElementType))
                return true;
        } return false;
    }

    @Override
    protected boolean hasNextProduct(XMLStreamReader xmlStreamReader) {
        return hasNext;
    }

    @Override
    protected Product getNextProduct(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        if (hasNext) {
            Product p = readNextProductRecord(xmlStreamReader);
            hasNext = isMoreRecords(xmlStreamReader);
            return p;
        } else throw new NoSuchElementException();
    }

    protected abstract Product readNextProductRecord(XMLStreamReader reader) throws XMLStreamException;


    protected class BasicRecord {

        private Map<String, String> atts;

        public BasicRecord(Map<String, String> atts){
            this.atts = atts;
        }

        public Map<String, String> getAtts() { return atts; }
    }

    protected List<BasicRecord> readBasicRecords(XMLStreamReader reader, String recordElementName, String containingElementName) throws XMLStreamException {
        List<BasicRecord> basicRecords = new ArrayList<>();

        while (reader.hasNext() && !isEndElement(containingElementName)){
            reader.next();
            String currentKey = null;
            if (isStartElement(recordElementName)){
                Map<String, String> currentAtts = new HashMap<>();

                while (reader.hasNext() && !isEndElement(recordElementName)){
                    reader.next();
                    if (reader.isStartElement()){
                        currentKey = reader.getLocalName();
                    } else if (reader.isCharacters()){
                        if (currentKey != null){
                            currentAtts.put(currentKey, reader.getText());
                            currentKey = null;
                        }
                    }
                }
                basicRecords.add(new BasicRecord(currentAtts));
            }
        }
        return basicRecords;
    }
}
