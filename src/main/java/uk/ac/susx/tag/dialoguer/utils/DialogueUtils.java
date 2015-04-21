package uk.ac.susx.tag.dialoguer.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.io.Resources;
import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.reader.AbstractReader;
import com.googlecode.clearnlp.segmentation.AbstractSegmenter;
import com.googlecode.clearnlp.tokenization.AbstractTokenizer;
import uk.ac.susx.tag.classificationframework.datastructures.Instance;
import uk.ac.susx.tag.classificationframework.featureextraction.inference.FeatureInferrer;
import uk.ac.susx.tag.classificationframework.featureextraction.pipelines.FeatureExtractionPipeline;
import uk.ac.susx.tag.classificationframework.featureextraction.pipelines.PipelineBuilder;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.SimplePatterns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

        public int size(){
            return cumulCountsToElements.size();
        }

        public E pick(){
            return cumulCountsToElements.higherEntry(r.nextInt(size)).getValue();
        }

        public String toString(){
            return cumulCountsToElements.values().toString();
        }
    }

    /**
     * Given a tokenised input corpus, build a Markov Chain model capable of generating from that model.
     *
     * The ngramOrder parameter allows you to specify how many tokens we pay attention to when learning the probability
     * of picking the next token.
     *
     * For example, with an order of 2, it will store counts for every bigram what the next token was.
     */
    public static class MarkovChainModel {

        private Map<String, WeightedRandomElementPicker<String>> model;
        private int ngramOrder;

        public MarkovChainModel(Iterable<List<String>> corpusSentences, int ngramOrder){
            Map<String, Multiset<String>> counts = new HashMap<>();

            LinkedList<String> ngram = new LinkedList<>();

            int count = 0;
            for (List<String> sentence : corpusSentences){

                if (count % 1000 == 0)
                    System.out.print("\rSentences complete: "+ count);

                ngram.clear();
                // Represent the beginning of a sentence with empty strings
                for (int i = 0; i < ngramOrder; i++)
                    ngram.addLast("");

                for (String token : sentence) {
                    addTo(counts, ngramToString(ngram), token);
                    ngram.removeFirst();
                    ngram.addLast(token);
                }
                for (int i = 0; i < ngramOrder; i++){
                    addTo(counts, ngramToString(ngram), "");
                    ngram.removeFirst();
                    ngram.addLast("");
                }

                count++;
            }
            model = createModel(counts);
            this.ngramOrder = ngramOrder;
        }

        public List<String> generateSentence(){
            return generateSentence(Integer.MAX_VALUE);
        }

        public List<String> generateSentence(int tokenLimit){
            List<String> sentence = new ArrayList<>();
            LinkedList<String> ngram = new LinkedList<>();

            for (int i = 0; i < ngramOrder; i++)
                ngram.addLast("");
            for (int i = 0; i < tokenLimit; i++){
                String token = getToken(ngramToString(ngram));
                if (token.equals("") && !sentence.isEmpty())
                    break;
                sentence.add(token);
                ngram.removeFirst();
                ngram.addLast(token);
            }
            return sentence;
        }

        public String generateSentence(int charLimit, int tries){
            String sentence;
            List<String> tokens;
            int timesTried = 0;
            do {
                tokens = generateSentence();
                sentence = StringUtils.detokenise(tokens);

            } while (timesTried < tries
                       && sentence.length() > charLimit
                       && SimplePatterns.puncFraction(tokens)>= 0.35
                       && tokens.size() < 4);
            return sentence;
        }

        public void interactiveTest(){
            try{
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String input;
                while((input=br.readLine())!=null){
                    try{
                        int limit = Integer.parseInt(input.trim());
                        System.out.print("\r"+generateSentence(limit));
                    } catch (NumberFormatException e){
                        System.out.print("\r"+generateSentence(144, 30));
                    }
                }
            }catch(IOException io){ io.printStackTrace();}
        }

        private String getToken(String previousNgram){
            if (model.containsKey(previousNgram)){
                WeightedRandomElementPicker<String> data = model.get(previousNgram);
                if (data.size() > 0)
                    return data.pick();
                else return "";
            } return "";
        }

        private Map<String, WeightedRandomElementPicker<String>> createModel(Map<String, Multiset<String>> counts){
            Map<String, WeightedRandomElementPicker<String>> model = new HashMap<>();
            for(Map.Entry<String, Multiset<String>> entry : counts.entrySet()){
                model.put(entry.getKey(), new WeightedRandomElementPicker<>(entry.getValue()));
            } return model;
        }

        private <K,V> void addTo(Map<K, Multiset<V>> map, K key, V value){
            if (!map.containsKey(key)){
                map.put(key, HashMultiset.create());
            } map.get(key).add(value);
        }

        private String ngramToString(List<String> ngram) { return Joiner.on(" ").join(ngram); }
    }

    public static Iterable<List<String>> simpleCorpusReader(File corpus) throws IOException {
        final String language = AbstractReader.LANG_EN;
        AbstractTokenizer tokenizer  = EngineGetter.getTokenizer(language, Resources.getResource("dictionary-1.4.0.zip").openStream());
        AbstractSegmenter segmenter = EngineGetter.getSegmenter(language, tokenizer);


        try (final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(corpus), "UTF-8"))) {
            Iterator<List<String>> sentences = segmenter.getSentences(r).iterator();
            return () -> new Iterator<List<String>>() {
                public boolean hasNext() {
                    return sentences.hasNext();
                }
                public List<String> next() {
                    while(hasNext()){
                        List<String> sentence = sentences.next();
                        if (!SimplePatterns.isJunkSentence(sentence)){
                            return sentence;
                        }
                    } throw new NoSuchElementException();
                }
            };
        }
    }

    public static Iterable<List<String>> tweetPerLineReader(File corpus) throws IOException {
        PipelineBuilder pb = new PipelineBuilder();
        List<PipelineBuilder.Option> options = Lists.newArrayList(
                new PipelineBuilder.Option("tokeniser", ImmutableMap.of("type", "cmuTokeniseOnly",
                        "filter_punctuation", "true",
                        "normalise_urls", "true",
                        "lower_case", "true")),
                new PipelineBuilder.Option("unigrams", true));
        FeatureExtractionPipeline p = pb.build(options);

        return new Iterable<List<String>>() {
            final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(corpus), "UTF-8"));
            String line = r.readLine();

            @Override
            public Iterator<List<String>> iterator() {
                return new Iterator<List<String>>() {
                    public boolean hasNext() { return line != null; }
                    public List<String> next() {
                        List<String> tokens = p.extractUnindexedFeatures(new Instance("",line,"")).stream()
                                            .map(FeatureInferrer.Feature::value)
                                            .filter((t) -> (!t.equals("HTTPLINK")))
                                            .filter((t) -> (!t.startsWith("@")))
                                            .collect(Collectors.toList());

                        try {
                            line = r.readLine();
                        } catch (IOException e) {
                            line = null;
                            try { r.close();
                            } catch (IOException e1) { e1.printStackTrace(); }
                        } return tokens;
                    }
                };
            }
        };
    }


    public static void main(String[] args) throws IOException {

//        new MarkovChainModel(
//                Iterables.concat(
//                    simpleCorpusReader(new File("J:\\Corpora\\art_history.txt")),
//                    simpleCorpusReader(new File("J:\\Corpora\\test.txt"))
//                ),
//        2).interactiveTest();

        new MarkovChainModel(tweetPerLineReader(new File("/Volumes/LocalDataHD/scpbox/xaa")), 3).interactiveTest();

    }
}
