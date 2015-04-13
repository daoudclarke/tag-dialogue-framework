package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.simple;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.simple.ChoiceMakingAnalyserStringMatchingFactory;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.Numbers;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.SimplePatterns;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 31/03/2015
 * Time: 17:03
 */
public class ChoiceMakingAnalyserStringMatching  extends Analyser {

    private double choiceFraction;

    public ChoiceMakingAnalyserStringMatching(double choiceFraction){
        this.choiceFraction = choiceFraction;
    }

    public static final Set<String> nullChoicePhrases = Sets.newHashSet(
            "none",
            "no",
            "none of them",
            "no thanks",
            "neither"
    );

    public boolean isChoice(Dialogue d, List<String> choices, double threshold) {
        // Strip message to basics and find the remaining unique words
        String userMessage = d.getStrippedNoStopwordsText();
        Set<String> uniqueWordsRemaining = Sets.newHashSet(SimplePatterns.whitespaceRegex.split(userMessage));

        // Given each possible choice, find the maximum fraction of words in the user message that appear in a choice
        double maxFractionDescribed = choices.stream()
                .mapToDouble((c) -> {
                    Set<String> uniqueWordsInChoice = Sets.newHashSet(SimplePatterns.whitespaceRegex.split(c));
                    return 1 - (Sets.difference(uniqueWordsRemaining, uniqueWordsInChoice).size() / (double) uniqueWordsRemaining.size());
                })
                .max().getAsDouble();

        // Return true is the maximum fraction found is above the given threshold
        return maxFractionDescribed >= threshold;
    }

    public int whichChoice(Dialogue d, List<String> choices) {
        if (choices.size() == 0) throw new RuntimeException("There must be at least one choice");

        String userMessage = d.getStrippedText();

        try {
            String noStopwords = d.getStrippedNoStopwordsText();
            String firstWord = SimplePatterns.whitespaceRegex.split(noStopwords, 2)[0];
            return Numbers.parseNumber(firstWord) - 1;

        } catch (NumberFormatException e){

            int minLength = choices.stream().max(Comparator.comparing(String::length)).get().length();

            int closestChoice = 0;
            int closestDistance = Integer.MAX_VALUE;

            for (int i = 0; i < choices.size(); i++) {
                String choice = choices.get(i);
                int distance = StringUtils.getLevenshteinDistance(userMessage, Strings.padEnd(choice, minLength, ' '));
                if (distance < closestDistance) {
                    closestChoice = i;
                    closestDistance = distance;
                }
            }
            return closestChoice;
        }
    }

    /**
     * Given a list of choices that were presented to the user, and the user response, determine whether or not
     * the user response was in fact making a choice.
     */
    public boolean isNullChoice(Dialogue d){
        return nullChoicePhrases.contains(d.getFromWorkingMemory("stripped"));
    }

    @Override
    public List<Intent> analise(String message, Dialogue d) {
        if (d.isChoicesPresented()){
            if (isNullChoice(d)){
                return Intent.buildNullChoiceIntent(message).toList();
            } else if (isChoice(d, d.getChoices(), choiceFraction)){
                int choice = whichChoice(d, d.getChoices());
                return Intent.buildChoiceIntent(message, choice).toList();
            } else {
                return Intent.buildNoChoiceIntent(message).toList();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public AnalyserFactory getFactory() {
        return new ChoiceMakingAnalyserStringMatchingFactory();
    }

    @Override
    public void close() throws Exception {
        // No resources to close
    }
}
