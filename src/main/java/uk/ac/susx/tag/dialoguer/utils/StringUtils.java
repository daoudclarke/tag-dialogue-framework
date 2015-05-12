package uk.ac.susx.tag.dialoguer.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 31/03/2015
 * Time: 17:24
 */
public class StringUtils {



    public static Pattern buildDisjunctionWithWordBoundaries(Iterable<String> items){
        return Pattern.compile("("+Joiner.on("|").join(
                Iterables.transform(items,
                        input-> "\\b"+input+"\\b"))
        +")");
    }

    public static String addWordBoundaries(String input){
        return "\\b" + input + "\\b";
    }

    /**
     * Given a list of choices, return a string formatting those choices as a numbered list, with a custom separator.
     */
    public static String numberList(List<String> choices, String separator){
        return IntStream.range(0, choices.size())
                    .mapToObj(i -> i+1+". "+choices.get(i))
                    .collect(Collectors.joining(separator));
    }

    public static String numberList(List<String> choices){
        return numberList(choices, "\n");
    }


    private static Detokeniser d = new Detokeniser();
    public static String detokenise(List<String> tokens){
        return d.dektokenise(tokens);
    }
    public static class Detokeniser {

        private static Set<String> noSpaceBefore = Sets.newHashSet(",", ".", ";", ":", ")", "}", "]", "-", "n't", "'m", "'d", "'s");
        private static Set<String> noSpaceAfter = Sets.newHashSet("(", "[", "{", "-");

        public String dektokenise(List<String> tokens){
            if (tokens.isEmpty()) return "";

            StringBuilder sb = new StringBuilder();
            sb.append(tokens.get(0));
            for (int i = 1; i < tokens.size(); i++){
                if (!noSpaceAfter.contains(tokens.get(i-1)) && !noSpaceBefore.contains(tokens.get(i))){
                    sb.append(" ");
                }
                sb.append(tokens.get(i));
            } return sb.toString();
        }
    }


   public static String phrasejoin(List<String> items){
       return detokenise(items.stream().map(item->"\""+item+"\"").collect(Collectors.toList()));
   }
}
