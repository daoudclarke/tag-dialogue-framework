package uk.ac.susx.tag.dialoguer.knowledge.questionanswering;

import com.google.common.collect.ImmutableMap;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import uk.ac.susx.tag.classificationframework.classifiers.NaiveBayesClassifier;
import uk.ac.susx.tag.classificationframework.datastructures.Instance;
import uk.ac.susx.tag.classificationframework.datastructures.ModelState;
import uk.ac.susx.tag.classificationframework.datastructures.ProcessedInstance;
import uk.ac.susx.tag.classificationframework.featureextraction.pipelines.FeatureExtractionPipeline;
import uk.ac.susx.tag.classificationframework.featureextraction.pipelines.PipelineBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel Saska on 6/19/2015.
 */

/**
 * Handles identification of Entity property using simple instance of NB classifier trained on hand-labeled dataset of
 * questions and associated answers.
 */
public class QuestionPropertyModel {

    private NaiveBayesClassifier nbc;
    private FeatureExtractionPipeline fep;
    private List<Instance> instances;
    private AbstractSequenceClassifier<CoreLabel> classifier;

    /**
     * Constructs the Question Property Model and associated Standford NER
     * @param classifier Stanford NER instance
     */
    public QuestionPropertyModel(AbstractSequenceClassifier<CoreLabel> classifier) {
        PipelineBuilder.OptionList ol = new PipelineBuilder.OptionList();
        ol.add("tokeniser", ImmutableMap.of("type", "basic", "normalise_urls", "true", "lower_case", "true"));
        ol.add("unigrams", true);
        fep = new PipelineBuilder().build(ol);
        nbc = new NaiveBayesClassifier();
        this.instances = new ArrayList<>();
        nbc.empiricalLabelPriors(false);
        this.classifier = classifier;
    }

    /**
     * Trains the model with dataset file provided
     * @param filename File path for the dataset to be used for training
     */
    public void train(String filename) {

        List<ProcessedInstance> instances = new ArrayList<>();

        File file = new File(filename);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            String text = null;
            while ((text = reader.readLine()) != null) {
                String t[] = text.split("\t");


                List<List<CoreLabel>> labels = classifier.classify(t[1]);

                String classifTokens = "";
                for (CoreLabel l : labels.get(0)) { //Only use one sentence for now
                    if (l.get(CoreAnnotations.AnswerAnnotation.class).equals("O")){ //Use non-entity words
                        classifTokens += l.value() + " ";
                    }
                }
                this.instances.add(new Instance(t[0], classifTokens, "n/a"));
                instances.add(fep.extractFeatures(new Instance(t[0], classifTokens, "n/a")));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

        nbc.train(instances, 1);

    }

    public void save(String directory) {
        ModelState ms = new ModelState(nbc, instances, fep);
        File dir = new File(directory);
        if (dir.exists()) {
            try {
                ms.save(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void load(String directory) {
        File dir = new File(directory);
        try {
            ModelState ms = ModelState.load(dir);
            nbc = ms.classifier;
            fep = ms.pipeline;
            instances = ms.trainingDocuments;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * Returns the set of properly labels and their associated probabilities
     * @param sentence Sentence to be classified
     * @param allowedLabels Labels allowed for the entity
     * @return Map of Property labels to probabilities
     */
    public Map<String, Double> labelProbabilities(String sentence, Iterable<String> allowedLabels) {
        List<List<CoreLabel>> labels = classifier.classify(sentence);

        String classifTokens = "";
        for (CoreLabel l : labels.get(0)) { //Only use one sentence for now
            if (l.get(CoreAnnotations.AnswerAnnotation.class).equals("O")) { //Use non-entity words
                classifTokens += l.value() + " ";
            }
        }
        ProcessedInstance pi = fep.extractFeatures(new Instance("", classifTokens, "n/a"));
        Int2DoubleOpenHashMap outcomes = nbc.predict(pi.features);


        Map<String, Double> ret = new HashMap<>();

        try {
            for (String label : allowedLabels) {
                if (!fep.getLabelIndexer().contains(label)) {
                    continue;
                }

                int i = fep.getLabelIndexer().getIndex(label);

                if (i != -1) {
                    ret.put(label, outcomes.get(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }
}
