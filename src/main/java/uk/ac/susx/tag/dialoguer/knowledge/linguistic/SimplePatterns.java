package uk.ac.susx.tag.dialoguer.knowledge.linguistic;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;

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

    public static Pattern hesitationRegex = StringUtils.buildDisjunctionWithWordBoundaries(Lists.newArrayList(
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
}
