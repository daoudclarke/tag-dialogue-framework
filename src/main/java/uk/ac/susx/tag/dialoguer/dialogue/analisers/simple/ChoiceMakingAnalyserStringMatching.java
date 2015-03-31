package uk.ac.susx.tag.dialoguer.dialogue.analisers.simple;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.SimplePatterns;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.Stopwords;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 31/03/2015
 * Time: 17:03
 */
public class ChoiceMakingAnalyserStringMatching  implements ChoiceMakingAnalyser {

    public static final Set<String> nullChoicePhrases = Sets.newHashSet(
            "none",
            "no",
            "none of them",
            "no thanks"
    );

    @Override
    public boolean isChoice(String userMessage, List<String> choices) {
        userMessage = SimplePatterns.stripAll(userMessage);
        userMessage = Stopwords.removeStopwords(userMessage).trim();
        Set<String> uniqueWordsRemaining = Sets.newHashSet(SimplePatterns.whitespaceRegex.split(userMessage));


        //TODO these unique words in the choices. Too many that don't appear in the choices means bad
        return false;
    }

    @Override
    public int whichChoice(String userMessage, List<String> choices) {
        if (choices.size() == 0) throw new RuntimeException("There must be at least one choice");

        userMessage = SimplePatterns.stripAll(userMessage);

        int closestChoice = 0;
        int closestDistance = Integer.MAX_VALUE;

        for (int i = 0; i < choices.size(); i++) {
            String choice = choices.get(i);
            int distance = StringUtils.getLevenshteinDistance(userMessage, choice);
            if (distance < closestDistance) {
                closestChoice = i;
                closestDistance = distance;
            }
        }
        return closestChoice;
    }

    /**
     * Given a list of choices that were presented to the user, and the user response, determine whether or not
     * the user response was in fact making a choice.
     */
    public boolean isNullChoice(String userMessage, List<String> choices){
        userMessage = SimplePatterns.stripAll(userMessage);
        return nullChoicePhrases.contains(userMessage);
    }

    @Override
    public boolean isConfirmed(String userMessage) {
        return false;
    }
}
