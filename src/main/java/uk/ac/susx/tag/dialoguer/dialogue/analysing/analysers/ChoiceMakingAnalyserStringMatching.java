package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.ChoiceMakingAnalyserStringMatchingFactory;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.InteractiveHandler;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.EnglishStemmer;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.Numbers;
import uk.ac.susx.tag.dialoguer.knowledge.linguistic.SimplePatterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tries to determine what choices the user is making when the Handler has indicated that it has presented choices to
 * the user.
 *
 * Uses the default choices intents. See Intent documentation.
 *
 * User: Andrew D. Robertson
 * Date: 31/03/2015
 * Time: 17:03
 */
public class ChoiceMakingAnalyserStringMatching  extends Analyser {

    private double choiceFraction = 0.5;

    public ChoiceMakingAnalyserStringMatching() {

    }

    public ChoiceMakingAnalyserStringMatching(double choiceFraction){
        this.choiceFraction = choiceFraction;
    }

    public ChoiceMakingAnalyserStringMatching(ChoiceMakingAnalyserStringMatching other) {
        super();
        choiceFraction = other.choiceFraction;
    }

    public static final Set<String> nullChoicePhrases = Sets.newHashSet(
            "none",
            "no",
            "none of them",
            "no thanks",
            "neither"
    );

    /**
     * Check whether latest message is choice by seeing how well described the user comment is by the closest matching
     * choice presented to the user.
     */
    public static boolean isChoice(Dialogue d, List<String> choices, double threshold) {
        // Strip message to basics
        String userMessage = d.getStrippedNoStopwordsText();

        try {
            
            String firstWord = SimplePatterns.whitespaceRegex.split(SimplePatterns.stripPunctuation(userMessage), 2)[0];
            if(!(Numbers.parseNumber(firstWord)-1<choices.size())) {
                throw new NumberFormatException();
            }
            return true;

        } catch (NumberFormatException e) {

            // Set of unique words in remaining message, normalising any number strings
//            Set<String> uniqueWordsRemaining = Sets.newHashSet(
//                    Arrays.stream(SimplePatterns.whitespaceRegex.split(userMessage))
//                            .map(Numbers::convertIfNumber)
//                            .collect(Collectors.toList())
//            );
            EnglishStemmer stemmer = new EnglishStemmer();


            Set<String> uniqueWordsRemaining = Sets.newHashSet(SimplePatterns.whitespaceRegex.split(userMessage)).stream()
                                                .map(stemmer::stem)
                                                .collect(Collectors.toSet());

            // Given each possible choice, find the maximum fraction of words in the user message that appear in a choice
            double maxFractionDescribed = choices.stream()
                    .mapToDouble((c) -> {
                        Set<String> uniqueWordsInChoice = Sets.newHashSet(SimplePatterns.splitByWhitespace(SimplePatterns.stripPunctuation(c.toLowerCase()))).stream()
                                .map(stemmer::stem)
                                .collect(Collectors.toSet());
                        return 1 - (Sets.difference(uniqueWordsRemaining, uniqueWordsInChoice).size() / (double) uniqueWordsRemaining.size());
                    })
                    .max().getAsDouble();

            // Return true is the maximum fraction found is above the given threshold
            return maxFractionDescribed >= threshold;
        }
    }

    /**
     * Try to find the most likely choice being made, by first checking for a number, then if failed, use levenshtein
     * distance.
     */
    public static int whichChoice(Dialogue d, List<String> choices, double threshold) {
        if (choices.size() == 0) throw new RuntimeException("There must be at least one choice");

        String userMessage = d.getStrippedText();

        try {
            String noStopwords = d.getStrippedNoStopwordsText();
            String firstWord = SimplePatterns.whitespaceRegex.split(noStopwords, 2)[0];
            if(Numbers.parseNumber(firstWord)-1<choices.size()) {
                return Numbers.parseNumber(firstWord) - 1;
            } else {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e){

            int minLength = choices.stream().max(Comparator.comparing(String::length)).get().length();

            int closestChoice = 0;
            int closestDistance = Integer.MAX_VALUE;

            for (int i = 0; i < choices.size(); i++) {
                String choice = choices.get(i).toLowerCase();
                int distance = StringUtils.getLevenshteinDistance(userMessage, Strings.padEnd(choice, minLength, ' ')); //Pad the strings for fair comparison
                if (distance < closestDistance) {
                    closestChoice = i;
                    closestDistance = distance;
                }
            }
            return closestChoice;
        }
    }
    /**
     * If the stripped message is equal to any of the null choice messages, it is a null choice.
     */
    public static boolean isNullChoice(Dialogue d){
        return nullChoicePhrases.contains(d.getFromWorkingMemory("stripped"));
    }

    /**
     * If a choice has been presented to the user (the Dialogue's "choices" field is non-empty):
     *   If the user explicitly rejected the choices, return "null_choice" intent
     *   Otherwise, if the user selected a choice, return a "choice" intent, with a slot detailing the choice
     *   Otherwise, return a "no_choice" intent
     * Otherwise return no intents
     */
    @Override
    public List<Intent> analyse(String message, Dialogue d) {
        if (d.isChoicesPresented()){
            if (isNullChoice(d)){
                return Intent.buildNullChoiceIntent(message).toList();
            } else if (isChoice(d, d.getChoices(), choiceFraction)){
                int choice = whichChoice(d, d.getChoices(), choiceFraction);
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
