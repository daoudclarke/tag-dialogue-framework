package uk.ac.susx.tag.dialoguer.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 31/03/2015
 * Time: 17:24
 */
public class StringUtils {
    public static Pattern buildDisjunctionWithWordBoundaries(Iterable<String> items){
        List<String> transformed = new ArrayList<>();
        for (String item : items) {
            transformed.add(addWordBoundaries(item));
        }
        return Pattern.compile(join(transformed, "|"));
    }

    public static String addWordBoundaries(String input){
        return "\\b" + input + "\\b";
    }


    public static String join(Iterable<String> toJoin, String joinOn) {
        Iterator<String> iterator = toJoin.iterator();
        StringBuilder builder = new StringBuilder();
        while(iterator.hasNext()) {
            builder.append(iterator.next());
            if (iterator.hasNext()) {
                builder.append(joinOn);
            }
        }
        return builder.toString();
    }
}
