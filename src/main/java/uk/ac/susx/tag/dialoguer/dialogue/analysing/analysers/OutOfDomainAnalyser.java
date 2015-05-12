package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

import com.google.common.collect.Lists;
import edu.berkeley.nlp.lm.ArrayEncodedNgramLanguageModel;
import edu.berkeley.nlp.lm.ConfigOptions;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.collections.BoundedList;
import edu.berkeley.nlp.lm.io.ArpaLmReader;
import edu.berkeley.nlp.lm.io.ComputeLogProbabilityOfTextStream;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.berkeley.nlp.lm.io.MakeLmBinaryFromArpa;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.OutOfDomainAnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.SimplePatterns;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 12/05/2015
 * Time: 13:33
 */
public class OutOfDomainAnalyser extends Analyser {

    ArrayEncodedNgramLanguageModel<String> lm;

    @Override
    public List<Intent> analyse(String message, Dialogue dialogue) {
        message = dialogue.getStrippedText();
        message = SimplePatterns.stripDigits(message);
        message = SimplePatterns.stripPunctuation(message);

        return null;
    }

    @Override
    public AnalyserFactory getFactory() {
        return new OutOfDomainAnalyserFactory();
    }

    @Override
    public void close() throws Exception {

    }

    public static void main(String[] args){

        String infile = "doc.txt";
        List<String> inputFiles = Lists.newArrayList(infile);
        int ngramSize = 3;
        String outputFile = "doc.arpa";

        String sentence = "this is an example sentence.";

        final StringWordIndexer wordIndexer = new StringWordIndexer();
        wordIndexer.setStartSymbol(ArpaLmReader.START_SYMBOL);
        wordIndexer.setEndSymbol(ArpaLmReader.END_SYMBOL);
        wordIndexer.setUnkSymbol(ArpaLmReader.UNK_SYMBOL);

        LmReaders.createKneserNeyLmFromTextFiles(inputFiles, wordIndexer, ngramSize, new File(outputFile), new ConfigOptions());

        MakeLmBinaryFromArpa.main(new String[]{outputFile, outputFile + ".binary"});

        ArrayEncodedNgramLanguageModel<String> lm = (ArrayEncodedNgramLanguageModel)LmReaders.readLmBinary(outputFile + ".binary");

        List<String> words = Arrays.asList(sentence.trim().split("\\s+"));

        System.out.println(scoreSentence(words, lm));
    }

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
