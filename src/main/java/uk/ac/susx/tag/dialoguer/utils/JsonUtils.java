package uk.ac.susx.tag.dialoguer.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utilities that make Gson able to better handle the datatypes in this project during JSON (de)serialisation.
 *
 * Generally most of this functionality is encoded within the public static gson instance on the Dialoguer class.
 *
 * So you can get the benefit of these by doing like:
 *
 *   Dialoguer.gson.toJson(thingToSerialise);
 *
 * etc.
 *
 * User: Andrew D. Robertson
 * Date: 13/04/2015
 * Time: 17:18
 */
public class JsonUtils {


/***************************************************************
 * Support for (de)serialisation compiled regex patterns
 *************************************************************/

    public static class PatternAdaptor extends TypeAdapter<Pattern> {
        @Override
        public void write(JsonWriter out, Pattern value) throws IOException {
            out.value(value.pattern());
        }
        @Override
        public Pattern read(JsonReader in) throws IOException {
            return Pattern.compile(in.nextString());
        }
    }


/***************************************************************
 * Support for deserialisation of guava's ImmutableSet
 *************************************************************/

    public static JsonDeserializer<ImmutableSet<?>> immutableSetJsonDeserializer(){
        // see: http://stackoverflow.com/questions/7706772/deserializing-immutablelist-using-gson
        return (json, typeOfT, context) -> {
            final TypeToken<ImmutableSet<?>> immutableSetToken = (TypeToken<ImmutableSet<?>>) TypeToken.of(typeOfT);
            final TypeToken<? super ImmutableSet<?>> setToken = immutableSetToken.getSupertype(Set.class);
            final Set<?> set = context.deserialize(json, setToken.getType());
            return ImmutableSet.copyOf(set);
        };
    }

/***************************************************************
 * Support for deserialisation and serialisation of guava's multimaps
 *************************************************************/
    public static JsonSerializer<Multimap<?, ?>> multimapJsonSerializer(){
        return (src, typeOfSrc, context) -> context.serialize(src.asMap());
    }

    public static JsonDeserializer<Multimap<?, ?>> multimapJsonDeserializer(){
        return (json, typeOfT, context) -> {
            final ArrayListMultimap<Object, Object> result = ArrayListMultimap.create();
            final Map<Object, Collection<?>> map = context.deserialize(json, multimapTypeToMapType(typeOfT));
            for (final Map.Entry<?, ?> e : map.entrySet()) {
                final Collection<?> value = (Collection<?>) e.getValue();
                result.putAll(e.getKey(), value);
            }
            return result;
        };
    }

    /**
     * Hmm... See: http://stackoverflow.com/questions/25673984/convert-guava-hashmultimap-to-json
     */
    private static <V> Type multimapTypeToMapType(Type type) {
        final Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
        final TypeToken<Map<String, Collection<V>>> mapTypeToken = new TypeToken<Map<String, Collection<V>>>() {}
                .where(new TypeParameter<V>() {}, (TypeToken<V>) TypeToken.of(typeArguments[1]));
        return mapTypeToken.getType();
    }

/***************************************************************
 * Support for deserialisation of Handlers
 *************************************************************/
    public static class HandlerAdaptor extends TypeAdapter<Handler> {
        public void write(JsonWriter out, Handler value) throws IOException {
            throw new UnsupportedOperationException("Cannot be written");
        }

        public Handler read(JsonReader in) throws IOException {
            String handlerName = null;
            String handlerPath = null;

            in.beginObject();
            while (in.hasNext()){
                String name = in.nextName();
                switch (name){
                    case "name": handlerName = in.nextString(); break;
                    case "path": handlerPath = in.nextString(); break;
                }
            } in.endObject();

            if (handlerName==null) throw new IOException("No handler name found");
            try {
                return Handler.getHandler(handlerName, handlerPath == null ? null : new File(handlerPath));
            } catch (IllegalAccessException | InstantiationException e) {
                throw new IOException(e);
            }
        }
    }

/***************************************************************
 * Support for deserialisation of Analysers
 *************************************************************/
    public static class AnalyserAdaptor extends TypeAdapter<Analyser> {
        public void write(JsonWriter out, Analyser value) throws IOException {
            throw new UnsupportedOperationException("Cannot be written.");
        }
        public Analyser read(JsonReader in) throws IOException {
            String analyserName = null;
            String analyserPath = null;

            in.beginObject();
            while (in.hasNext()){
                String name = in.nextName();
                switch (name){
                    case "name": analyserName = in.nextString(); break;
                    case "path": analyserPath = in.nextString(); break;
                }
            } in.endObject();

            if (analyserName==null) throw new IOException("No analyser name found");
            try {
                return Analyser.getAnalyser(analyserName, analyserPath==null? null : new File(analyserPath));
            } catch (IllegalAccessException | InstantiationException e){
                throw new IOException(e);
            }
        }
    }
}
