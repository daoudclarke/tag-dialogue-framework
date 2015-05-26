package uk.ac.susx.tag.dialoguer.knowledge.linguistic;

import com.google.common.collect.Lists;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 31/03/2015
 * Time: 11:35
 */
public class SimplePatterns {

    public static Pattern punctuationRegex = Pattern.compile("[!?\"#$%&'()*+,-./:;<=>@\\[\\]^_`{|}~]+");
    public static Pattern whitespaceRegex = Pattern.compile("\\s+");
    public static Pattern numberRegex = Pattern.compile("[0-9]");

    public static Pattern politenessRegex = StringUtils.buildDisjunctionWithWordBoundaries(Lists.newArrayList(
            "i('| w(oul|u))?d (like|lyk)( to)?",
            "i wa?nt( to)?",
            "ca?n i h(ave|v)",
            "let'?s( (h(ave|v))|(go with))?",
            "tha?n?(ks|x)",
            "tha?nk (u|you)",
            "pl(ease|s|z|izzle)",
            "no worries",
            "s(or)?ry"
    ));

    public static Pattern simplePolitenessRegex = StringUtils.buildDisjunctionWithWordBoundaries(Lists.newArrayList(
            "tha?n?(ks|x)",
            "tha?nk (u|you)",
            "pl(ease|s|z|izzle)",
            "no worries",
            "s(or)?ry"
    ));

    public static Pattern hesitationRegex = StringUtils.buildDisjunctionWithWordBoundaries(Lists.newArrayList(
            "a+h+",
            "e+r+m*",
            "o+h+",
            "u+h+",
            "h*mm+",
            "u+r*m+"
    ));

    public static Pattern botReferenceRegex = StringUtils.buildDisjunctionWithWordBoundaries(Lists.newArrayList(
            "(why|y) do?n'?t (u|you)",
            "ca?n (you|u)",
            "i'?d l(i|y)ke? (you|u) (to|2)",
            "i wa?nt (you|u) (to|2)",
            "w(oul|u)d (you|u)"
    ));

    public static Pattern emoticonRegex = Pattern.compile("([:;=x][-o^]?[)(/\\\\pd])|([/\\\\)(d][-o^]?[:;=x])");

    public static String stripAll(String userMessage){
        String strippedOfEmoticons = emoticonRegex.matcher(userMessage.toLowerCase()).replaceAll("");
        String strippedOfPunctuation = punctuationRegex.matcher(strippedOfEmoticons).replaceAll("");
        String strippedOfPoliteness = politenessRegex.matcher(strippedOfPunctuation).replaceAll("");
        String strippedOfHesitation = hesitationRegex.matcher(strippedOfPoliteness).replaceAll("");
        String strippedOfBotReferences = botReferenceRegex.matcher(strippedOfHesitation).replaceAll("");
        String whitespaceSquashedAndTrimmed = whitespaceRegex.matcher(strippedOfBotReferences).replaceAll(" ").trim();
        return whitespaceSquashedAndTrimmed;
    }

    public static String strip(String message, Pattern pattern){
        return pattern.matcher(message).replaceAll("");
    }

    public static String[] splitByWhitespace(String text){
        return whitespaceRegex.split(text);
    }

    public static String stripPunctuation(String text){
        return punctuationRegex.matcher(text).replaceAll("");
    }

    public static String stripDigits(String text) {
        return numberRegex.matcher(text).replaceAll("");
    }

    public static boolean isPunctuation(String text) {
        return punctuationRegex.matcher(text).matches();
    }

    public static double uppercaseFraction(List<String> tokens){
        int uppercaseCount = 0;
        for (String token : tokens){
            if (org.apache.commons.lang3.StringUtils.isAllUpperCase(token))
                uppercaseCount++;
        } return uppercaseCount / (double) tokens.size();
    }

    public static double puncFraction(List<String> tokens){
        int puncCount = 0;
        for (String token : tokens)
            if (isPunctuation(token))
                puncCount++;
        return puncCount / (double)tokens.size();
    }

    public static int alphaCount(List<String> tokens){
        return (int)tokens.stream().filter(org.apache.commons.lang3.StringUtils::isAlpha).count();
    }

    public static boolean isJunkSentence(List<String> tokens){
        if (tokens.isEmpty())
            return true;
        if (tokens.size() < 4)
            return true;
        if (SimplePatterns.isPunctuation(tokens.get(0)))
            return true;
        if (puncFraction(tokens) >= 3.5)
            return true;
        if (uppercaseFraction(tokens) >= 3.5)
            return true;
        if (alphaCount(tokens) < tokens.size()/2 )
            return true;
        return false;
    }
}
