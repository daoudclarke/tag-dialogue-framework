package uk.ac.susx.tag.dialoguer.knowledge.database.product.collections.xml;


import uk.ac.susx.tag.dialoguer.knowledge.database.product.Product;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.collections.ProductCollection;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * See ProductCollection
 *
 * This class provides some of the functionality required to support an XML-based product collection. Mainly, it manages
 * the creation and destruction of the XML reader, and a fewer helper methods.
 *
 *
 * Created by Andrew D. Robertson on 16/09/2014.
 */
public abstract class XMLCollection extends ProductCollection {

    protected XMLStreamReader reader;

    public XMLCollection(File xmlFile) throws FileNotFoundException, UnsupportedEncodingException, XMLStreamException {
        super();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        reader = factory.createXMLStreamReader(
                    new BufferedReader( new InputStreamReader( new FileInputStream(xmlFile), "UTF8")));
    }

    /**
     * Get access to the underlying XML reader.
     */
    protected XMLStreamReader getReader() { return reader; }

    protected abstract boolean hasNextProduct(XMLStreamReader xmlStreamReader);

    protected abstract Product getNextProduct(XMLStreamReader xmlStreamReader) throws XMLStreamException;

    /**
     * Return true if the reader is at the start of an element called localName
     */
    protected boolean isStartElement(String localName){
        return reader.isStartElement() && reader.getLocalName().equals(localName);
    }

    /**
     * Return true if the reader is at the end of an element called localName
     */
    protected boolean isEndElement(String localName){
        return reader.isEndElement() && reader.getLocalName().equals(localName);
    }

    @Override
    public void close() throws XMLStreamException {
        reader.close();
    }

    @Override
    protected Product getNextProduct() {
        try {
            return getNextProduct(reader);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e); // TODO maybe better exception
        }
    }

    @Override
    protected boolean hasNextProduct() {
        return hasNextProduct(reader);
    }

}
