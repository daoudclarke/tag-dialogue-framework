package uk.ac.susx.tag.dialoguer.knowledge.linguistic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import uk.ac.susx.tag.dialoguer.utils.StringUtils;

/**
 * Collection of English stopwords. Obtained from NLTK.
 *
 * There is a convenience stripping function that also takes care of normalising the remaining whitespace.
 *
 * User: Andrew D. Robertson
 * Date: 31/03/2015
 * Time: 16:29
 */
public class Stopwords {


    public static final List<String> stopwordArray = Arrays.asList(
            "a",
            "about",
            "above",
            "after",
            "again",
            "against",
            "all",
            "am",
            "an",
            "and",
            "any",
            "are",
            "as",
            "at",
            "be",
            "because",
            "been",
            "before",
            "being",
            "below",
            "between",
            "both",
            "but",
            "by",
            "can",
            "did",
            "do",
            "does",
            "doing",
            "don",
            "down",
            "during",
            "each",
            "few",
            "for",
            "from",
            "further",
            "had",
            "has",
            "have",
            "having",
            "he",
            "her",
            "here",
            "hers",
            "herself",
            "him",
            "himself",
            "his",
            "how",
            "i",
            "if",
            "in",
            "into",
            "is",
            "it",
            "its",
            "itself",
            "just",
            "me",
            "more",
            "most",
            "my",
            "myself",
            "no",
            "nor",
            "not",
            "now",
            "of",
            "off",
            "on",
            "once",
            "only",
            "or",
            "other",
            "our",
            "ours",
            "ourselves",
            "out",
            "over",
            "own",
            "s",
            "same",
            "she",
            "should",
            "so",
            "some",
            "such",
            "t",
            "than",
            "that",
            "the",
            "their",
            "theirs",
            "them",
            "themselves",
            "then",
            "there",
            "these",
            "they",
            "this",
            "those",
            "through",
            "to",
            "too",
            "under",
            "until",
            "up",
            "very",
            "was",
            "we",
            "were",
            "what",
            "when",
            "where",
            "which",
            "while",
            "who",
            "whom",
            "why",
            "will",
            "with",
            "you",
            "your",
            "yours",
            "yourself",
            "yourselves"
    );
    public static final Set<String> stopwords = new HashSet<>(stopwordArray);

    public static final Pattern stopwordsRegex = StringUtils.buildDisjunctionWithWordBoundaries(stopwords);

    public static String removeStopwords(String message){
        String noStopwords = stopwordsRegex.matcher(message).replaceAll("");
        return SimplePatterns.whitespaceRegex.matcher(noStopwords).replaceAll(" ").trim();
    }
}
