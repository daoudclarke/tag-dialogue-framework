package uk.ac.susx.tag.dialoguer.knowledge.linguistic;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Andrew D. Robertson on 11/04/2015.
 */
public class Numbers {

    public static final Map<String, Integer> cardinal = new ImmutableMap.Builder<String,Integer>()
            .put("one", 1)
            .put("two", 2)
            .put("three", 3)
            .put("four", 4)
            .put("five", 5)
            .put("six", 6)
            .put("seven", 7)
            .put("eight", 8)
            .put("nine", 9)
            .put("ten", 10)
            .put("eleven", 11)
            .put("twelve", 12)
            .put("thirteen", 13)
            .put("fourteen", 14)
            .put("fifteen", 15)
            .put("sixteen", 16)
            .put("seventeen", 17)
            .put("eighteen", 18)
            .put("nineteen", 19)
            .put("twenty", 20)
            .build();

    public static final Map<String, Integer> wordOrdinal = new ImmutableMap.Builder<String,Integer>()
            .put("first", 1)
            .put("second", 2)
            .put("third", 3)
            .put("fourth", 4)
            .put("fifth", 5)
            .put("sixth", 6)
            .put("seventh", 7)
            .put("eighth", 8)
            .put("nineth", 9)
            .put("tenth", 10)
            .put("eleventh", 11)
            .put("twelfth", 12)
            .put("thirteenth", 13)
            .put("fourteenth", 14)
            .put("fifteenth", 15)
            .put("sixteenth", 16)
            .put("seventeenth", 17)
            .put("eighteenth", 18)
            .put("nineteenth", 19)
            .put("twentieth", 20)
            .build();

    public static final Pattern ordinalSuffixRegex = Pattern.compile("(th|st|rd|nd)\\b");

    public static int parseNumber(String number){
        try{
            return Integer.parseInt(number);
        } catch (NumberFormatException e){
            if (cardinal.containsKey(number))
                return cardinal.get(number);
            else if (wordOrdinal.containsKey(number))
                return wordOrdinal.get(number);
            else return Integer.parseInt(ordinalSuffixRegex.matcher(number).replaceAll(""));
        }
    }

    /**
     * Convert integer to number-based ordinal, e.g.:
     *
     * 1 --> 1st
     * 111 --> 111th
     *
     * From: http://stackoverflow.com/questions/6810336/is-there-a-library-or-utility-in-java-to-convert-an-integer-to-its-ordinal
     */
    public static String convertToNumberOrdinal(int i){
        String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + suffixes[i % 10];

        }
    }
}
