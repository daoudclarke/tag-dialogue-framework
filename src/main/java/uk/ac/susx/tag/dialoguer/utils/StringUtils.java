package uk.ac.susx.tag.dialoguer.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import java.util.regex.Pattern;

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
}
