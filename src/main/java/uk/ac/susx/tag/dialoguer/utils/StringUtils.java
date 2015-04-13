package uk.ac.susx.tag.dialoguer.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
                    .mapToObj(i -> i+". "+choices.get(i))
                    .collect(Collectors.joining(separator));
    }

    public static String numberList(List<String> choices){
        return numberList(choices, "\n");
    }
}
