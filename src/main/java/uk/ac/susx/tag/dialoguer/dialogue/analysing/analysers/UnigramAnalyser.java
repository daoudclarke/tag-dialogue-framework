package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.SimplePatterns;

/**
 * Created by dc on 24/08/15.
 *
 * Simple analyser that uses the nearest neighbour on the training set to determine the intent,
 * where the nearest neighbour is determined using cosine on unigrams in the query.
 *
 */
public class UnigramAnalyser extends Analyser {
    ArrayList<Map.Entry<HashSet<String>, Intent>> queryTermIntents;

    public void train(Map<String, Intent> queryParses) {
        for (Map.Entry<String, Intent> entry : queryParses.entrySet()) {
            String query = entry.getKey();
            Intent intent = entry.getValue();
            HashSet<String> tokenSet = getTokens(query);
            queryTermIntents.add(new HashMap.SimpleEntry<>(tokenSet, intent));
        }
    }

//    // A map showing which words occur in queries for each intent
//    HashMap<String, ArrayList<Intent>> intentWords = new HashMap<>();
//
//    public void train(Map<String, Intent> queryParses) {
//        for (Map.Entry<String, Intent> entry : queryParses.entrySet()) {
//            String query = entry.getKey();
//            Intent intent = entry.getValue();
//            String[] tokens = SimplePatterns.splitByWhitespace(query);
//            for (String token : tokens) {
//                if (intentWords.containsKey(token)) {
//                    intentWords.get(token).add(intent);
//                } else {
//                    intentWords.put(token, intent);
//                }
//            }
//        }
//    }

    @Override
    public List<Intent> analyse(String message, Dialogue dialogue) {
        HashSet<String> tokens = getTokens(message);
        double bestF1 = 0.0;
        Intent bestIntent = null;
        for (Map.Entry<HashSet<String>, Intent> entry : queryTermIntents) {
            HashSet<String> queryTerms = entry.getKey();
            int intersectionSize = 0;
            for (String token : tokens) {
                if (queryTerms.contains(token)) {
                    intersectionSize += 1;
                }
            }
            double precision = intersectionSize/(double)tokens.size();
            double recall = intersectionSize/(double)queryTerms.size();
            double f1 = 2*precision*recall/(precision + recall);
            if (f1 > bestF1) {
                bestIntent = entry.getValue();
                bestF1 = f1;
            }
        }
        ArrayList<Intent> results = new ArrayList<>();
        results.add(bestIntent);
        return results;
    }

    @Override
    public void close() throws Exception {

    }

    private HashSet<String> getTokens(String query) {
        String[] tokens = SimplePatterns.splitByWhitespace(query);
        HashSet<String> tokenSet = new HashSet<>();
        Collections.addAll(tokenSet, tokens);
        return tokenSet;
    }
}
