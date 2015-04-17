package uk.ac.susx.tag.dialoguer.utils;

import com.google.common.collect.Multiset;

import java.util.Random;
import java.util.TreeMap;

/**
 * Utilities for aspects of dialogue management.
 *
 * User: Andrew D. Robertson
 * Date: 17/04/2015
 * Time: 15:32
 */
public class DialogueUtils {


    /**
     * Given a multiset of elements (basically a set that allows multiples and tracks their frequency),
     * create a datastructure that is able to probabilistically pick an element according to its count
     * in the set.
     *
     * E.g. in the set {1, 1, 1, 2, 3, 4}, the number 1 would be picked 50% of the time.
     *
     * Where V is the number of distinct elements in the set.
     *   Running time: O(Log(V))
     *   Memory footprint: θ(V)
     */
    public static class WeightedRandomElementPicker<E> {

        private static Random r = new Random();
        private TreeMap<Integer, E> cumulCountsToElements;
        private int size;

        public WeightedRandomElementPicker(Multiset<E> elementsWithCounts){
            cumulCountsToElements = new TreeMap<>();
            size = elementsWithCounts.size();
            int cumul = 0;
            for (Multiset.Entry<E> elementWithCount : elementsWithCounts.entrySet()){
                cumul += elementWithCount.getCount();
                cumulCountsToElements.put(cumul, elementWithCount.getElement());
            }
        }

        public E pick(){
            return cumulCountsToElements.higherEntry(r.nextInt(size)).getValue();
        }
    }
}
