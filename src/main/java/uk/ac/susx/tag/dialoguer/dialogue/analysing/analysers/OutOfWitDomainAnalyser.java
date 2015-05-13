package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

import com.google.common.collect.Lists;
import edu.berkeley.nlp.lm.ArrayEncodedNgramLanguageModel;
import edu.berkeley.nlp.lm.ConfigOptions;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.collections.BoundedList;
import edu.berkeley.nlp.lm.io.ArpaLmReader;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.berkeley.nlp.lm.io.MakeLmBinaryFromArpa;
import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.OutOfWitDomainAnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.SimplePatterns;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This analyser creates an ngram language model from the training data of a Wit.Ai instance, and fires an "out_of_domain"
 * intent if a given message does not seem to be drawn from the same domain as the Wit.Ai instance.
 *
 * Performance is greater:
 *
 *  1. The more expressions you have in the Wit.Ai instance : more examples means better understanding of domain
 *  2. The more constrained your language domain is : it's must easier to determine an out-of-domain sentence for a system
 *     that only deals with taxi ordering, than one that deals with many different problems.
 *
 * User: Andrew D. Robertson
 * Date: 12/05/2015
 * Time: 13:33
 */
public class OutOfWitDomainAnalyser extends Analyser {

    public static String outOfDomainIntentName = "out_of_domain";

    private static String witAiIntentsApi = "https://api.wit.ai/intents";

    private ArrayEncodedNgramLanguageModel<String> lm;
    private double threshold;

    public OutOfWitDomainAnalyser(int ngramOrder, String serverAccessToken, Set<String> excludedIntents) throws IOException {

        File trainingFile = File.createTempFile("oowd.training_intents", null);     trainingFile.deleteOnExit();
        File calibrationFile = File.createTempFile("oowd.calibration_intents", null);     calibrationFile.deleteOnExit();
        File arpaFile = File.createTempFile("oowd.arpa", null);     arpaFile.deleteOnExit();
        File binaryFile = File.createTempFile("oowd.arpa.binary", null);    binaryFile.deleteOnExit();

        writeIntents(serverAccessToken, trainingFile, calibrationFile, excludedIntents);

        List<String> inputFiles = Lists.newArrayList(trainingFile.getAbsolutePath());

        final StringWordIndexer wordIndexer = new StringWordIndexer();
        wordIndexer.setStartSymbol(ArpaLmReader.START_SYMBOL);
        wordIndexer.setEndSymbol(ArpaLmReader.END_SYMBOL);
        wordIndexer.setUnkSymbol(ArpaLmReader.UNK_SYMBOL);

        LmReaders.createKneserNeyLmFromTextFiles(inputFiles, wordIndexer, ngramOrder, arpaFile, new ConfigOptions());
        MakeLmBinaryFromArpa.main(new String[]{arpaFile.getAbsolutePath(), binaryFile.getAbsolutePath()});

        lm = (ArrayEncodedNgramLanguageModel)LmReaders.readLmBinary(binaryFile.getAbsolutePath());

        threshold = calibrateThreshold(calibrationFile, lm);

        if (!trainingFile.delete()) throw new Dialoguer.DialoguerException("Unable to delete temp file: " + trainingFile.getAbsolutePath());
        if (!calibrationFile.delete()) throw new Dialoguer.DialoguerException("Unable to delete temp file: " + calibrationFile.getAbsolutePath());
        if (!arpaFile.delete()) throw new Dialoguer.DialoguerException("Unable to delete temp file: " + arpaFile.getAbsolutePath());
        if (!binaryFile.delete()) throw new Dialoguer.DialoguerException("Unable to delete temp file: " + binaryFile.getAbsolutePath());
    }

    public static double calibrateThreshold(File calibrationFile, ArrayEncodedNgramLanguageModel<String> lm) throws IOException {
        double min = 0;
        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(calibrationFile), "UTF-8"))){
            while((line=br.readLine())!=null){
                List<String> tokens = Lists.newArrayList(SimplePatterns.splitByWhitespace(line.trim()));
                double score = scoreSentence(tokens, lm);
                if (score < min)
                    min = score;
            }
        }
        return min;
    }

    public static void writeIntents(String serverAccessToken, File training, File calibration, Set<String> excludedIntentNames) throws IOException {

        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(witAiIntentsApi);

        target = target
                .queryParam("v", 20150512);

        String response = target.request()
                        .header("Authorization", "Bearer " + serverAccessToken)
                        .header("Accept",  "application/json")
                        .buildGet().invoke(String.class);

        for (String intentId : Dialoguer.gson.fromJson(response, IntentList.class).getIntentIds(excludedIntentNames)){
            target = client.target(witAiIntentsApi + "/" + intentId);

            response = target.request()
                    .header("Authorization", "Bearer " + serverAccessToken)
                    .header("Accept",  "application/json")
                    .buildGet().invoke(String.class);

            IntentDefinition d = Dialoguer.gson.fromJson(response, IntentDefinition.class);

            try (BufferedWriter bwTraining = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(training), "UTF-8"));
                 BufferedWriter bwCalibration = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(calibration), "UTF-8"))){

                List<ExpressionDefinition> shuffledExpressions = new ArrayList<>(d.expressions);
                Collections.shuffle(shuffledExpressions);
                for (int i = 0; i < shuffledExpressions.size()/10; i++){
                    String expression = stripMessage(shuffledExpressions.get(i).body);
                    bwCalibration.write(expression); bwCalibration.write("\n");
                }
                for (int i = shuffledExpressions.size()/10; i < shuffledExpressions.size(); i++){
                    String expression = stripMessage(shuffledExpressions.get(i).body);
                    bwTraining.write(expression); bwTraining.write("\n");
                }
            }
        }
        client.close();
    }

    public static class IntentList extends ArrayList<HashMap<String, Object>>{
        public List<String> getIntentIds(Set<String> excludedIntentNames){
            return this.stream()
                    .filter(i -> !excludedIntentNames.contains((String)i.get("name")))
                    .map(i -> (String) i.get("id")).collect(Collectors.toList());
        }
    }

    public static class IntentDefinition {
        public List<ExpressionDefinition> expressions;
        public List<Map<String, Object>> entities;
        public String id;
        public String name;
        public String doc;
    }

    public static class ExpressionDefinition {
        public List<Map<String, String>> entities;
        public String body;
        public String id;
    }

    @Override
    public List<Intent> analyse(String message, Dialogue dialogue) {
        message = dialogue.getStrippedText();
        message = SimplePatterns.stripDigits(message);
        message = SimplePatterns.stripPunctuation(message);

        List<String> words = Lists.newArrayList(SimplePatterns.splitByWhitespace(message.trim()));

        return scoreSentence(words, lm) >= threshold? new ArrayList<>() : new Intent(outOfDomainIntentName).toList();
    }

    @Override
    public AnalyserFactory getFactory() {
        return new OutOfWitDomainAnalyserFactory();
    }

    @Override
    public void close() throws Exception {

    }

    /**
     * String pre-processing for training data.
     */
    private static String stripMessage(String message){
        message = SimplePatterns.stripAll(message);
        message = SimplePatterns.stripDigits(message);
        message = SimplePatterns.stripPunctuation(message);
        return message.toLowerCase();
    }

    /**
     * The score of a sentence is the average ngram probability (including sentence boundary ngrams).
     */
    private static <T> float scoreSentence(final List<T> sentence, final ArrayEncodedNgramLanguageModel<T> lm) {
        final List<T> sentenceWithBounds = new BoundedList<>(sentence, lm.getWordIndexer().getStartSymbol(), lm.getWordIndexer().getEndSymbol());

        final int lmOrder = lm.getLmOrder();
        float sentenceScore = 0.0f;
        for (int i = 1; i < lmOrder - 1 && i <= sentenceWithBounds.size() + 1; ++i) {
            final List<T> ngram = sentenceWithBounds.subList(-1, i);
            final float scoreNgram = lm.getLogProb(ngram);
            sentenceScore += scoreNgram;
        }
        for (int i = lmOrder - 1; i < sentenceWithBounds.size() + 2; ++i) {
            final List<T> ngram = sentenceWithBounds.subList(i - lmOrder, i);
            final float scoreNgram = lm.getLogProb(ngram);
            sentenceScore += scoreNgram;
        }
        return sentenceScore / sentenceWithBounds.size();
    }

    private static <T> float getLogProb(final int[] ngram, final ArrayEncodedNgramLanguageModel<T> lm) {
        return lm.getLogProb(ngram, 0, ngram.length);
    }

    private static <T> float getLogProb(final List<T> ngram, final ArrayEncodedNgramLanguageModel<T> lm) {
        final int[] ints = NgramLanguageModel.StaticMethods.toIntArray(ngram, lm);
        return lm.getLogProb(ints, 0, ints.length);

    }
}
