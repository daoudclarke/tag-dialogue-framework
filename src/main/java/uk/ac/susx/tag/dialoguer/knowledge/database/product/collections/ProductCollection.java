package uk.ac.susx.tag.dialoguer.knowledge.database.product.collections;


import uk.ac.susx.tag.dialoguer.knowledge.database.product.Product;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A ProductCollection is an iterable over Products, potentially based on some external resource (e.g. xml file).
 *
 * To implement your own, you need to override the two abstract methods which perform the following function:
 *
 * 1. "hasNextProduct" : are there any more products in the collection?
 * 2. "getNextProduct" : if there are more products, return the next one, otherwise throw a NoSuchElementException
 *
 * Alternatively, exploit the functionality of one of its subclasses. For example, if your data is XML, you may find that
 * some of the functionality is covered for you in XMLCollection or even more in RecordBasedXMLCollection.
 *
 * Created by Andrew D. Robertson on 16/09/2014.
 */
public abstract class ProductCollection implements Iterable<Product>, AutoCloseable {

    public ProductCollection() {}

    protected abstract boolean hasNextProduct();

    protected abstract Product getNextProduct();

    @Override
    public Iterator<Product> iterator() {

        return new Iterator<Product>() {
            @Override
            public boolean hasNext() {
                return hasNextProduct();
            }

            @Override
            public Product next() {
                if (hasNext()) {
                    return getNextProduct();
                } else throw new NoSuchElementException();
            }

            @Override
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }
}
