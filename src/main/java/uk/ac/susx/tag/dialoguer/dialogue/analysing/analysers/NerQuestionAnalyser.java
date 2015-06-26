package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.knowledge.questionanswering.QuestionPropertyModel;
import uk.ac.susx.tag.dialoguer.knowledge.questionanswering.WikidataAPIWrapper;

import java.io.IOException;
import java.util.*;

/**
 * Created by Daniel Saska on 6/18/2015.
 */

/**
 * NerQuestionAnalyser handles user-inputed factual questions such that it outputs intent containing entity and
 * property that the user is interested in.
 */
public class NerQuestionAnalyser extends Analyser {
    private String serializedClassifier;
    private String naiveBayesFolder;
    private AbstractSequenceClassifier<CoreLabel> classifier;
    private QuestionPropertyModel qpm;
    private WikidataAPIWrapper wi;

    private static final String interrogatives[] =
            { "who", "which", "what", "whose", "whom", "where", "whence", "whither", "when", "how", "why"};


    public NerQuestionAnalyser(NerQuestionAnalyser nerQuestionAnalyser) {
        super();
        serializedClassifier = nerQuestionAnalyser.serializedClassifier;
        naiveBayesFolder = nerQuestionAnalyser.naiveBayesFolder;
        try {
            classifier = CRFClassifier.getClassifier(serializedClassifier);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        qpm = new QuestionPropertyModel(classifier);
        //qpm.train("wikibase/annotated_fb_data_train_wikidata.txt");
        qpm.save(naiveBayesFolder);
        //qpm.load(naiveBayesFolder);

        wi = new WikidataAPIWrapper();
    }

    @Override
    public List<Intent> analyse(String message, Dialogue dialogue) {
        //Determine whether message is question
        String[] words = message.split("\\s+");
        if(words.length == 0) {
            List<Intent> r = new ArrayList<>();
            Intent i = new Intent("unknown", dialogue.getStrippedText());
            r.add(i);
            return r;
        }
        if(!Arrays.asList(interrogatives).contains(words[0].toLowerCase())
                && !message.substring(message.length()-1).equals("?")) {
            List<Intent> r = new ArrayList<>();
            Intent i = new Intent("unknown", dialogue.getStrippedText());
            r.add(i);
            return r;
        }

        //Identify entities
        System.out.println(classifier.classifyToString(message));
        List<List<CoreLabel>> labels = classifier.classify(message);

        String lastAnntation = null;
        String entity = "";
        List<String> classifTokens = new ArrayList<String>();
        for (CoreLabel l : labels.get(0)) { //Only use one sentence for now
            if (!l.get(CoreAnnotations.AnswerAnnotation.class).equals("O")
                    && (lastAnntation == null || l.get(CoreAnnotations.AnswerAnnotation.class) == lastAnntation)){
                lastAnntation = l.get(CoreAnnotations.AnswerAnnotation.class);
                entity += l.value() + " ";
            } else if (lastAnntation != null) {
                entity = entity.substring(0, entity.length() - 1);
                break;
            }
        }
        if (entity.equals("")) {
            List<Intent> r = new ArrayList<>();
            Intent i = new Intent("unknown", dialogue.getStrippedText());
            r.add(i);
            return r;
        }

        int entityId = wi.getEntityId(entity);
        Set<String> propertyIds = wi.getPropertyIds(entityId);

        Map<String, Double> probabilities = qpm.labelProbabilities(message, propertyIds);
        Map.Entry<String, Double> maxEntry = null;
        for (Map.Entry<String, Double> entry : probabilities.entrySet())
        {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = entry;
            }
        }

        List<Intent> r = new ArrayList<>();
        Intent i = new Intent("factual_qa", dialogue.getStrippedText());

        i.fillSlot("entity", Integer.toString(entityId));
        i.fillSlot("property", maxEntry.getKey().substring(1));

        r.add(i);
        return r;
    }

    @Override
    public AnalyserFactory getFactory() {
        return null;
    }

    @Override
    public void close() throws Exception {
        wi.close();
    }
}
