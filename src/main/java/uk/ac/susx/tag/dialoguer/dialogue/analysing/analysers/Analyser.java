package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

//import org.reflections.Reflections;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * It is the responsibility of an Analyser to look at the latest message received from the user (and potentially the
 * dialogue thusfar), and determine what intent or intents the user is trying to express.
 *
 * See Intent documentation.
 *
 * Documentation for how to configure the Analyser should reside in the factory that can produce it, and documentation
 * pertaining to the function and purpose of the Analyser itself should reside in the actual analyser.
 *
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 18:02
 */
public abstract class Analyser implements AutoCloseable {

    private String sourceId = null;

    /**
     * Perform analysis of latest message, forming a list of intents the user is trying to express
     */
    public abstract List<Intent> analyse(String message, Dialogue dialogue);

//    /**
//     * Return a new instance of the factory which can create this analyser
//     */
//    public abstract AnalyserFactory getFactory();
//
//
//    public String getName(){ return getFactory().getName();}

    public String getSourceId() { return sourceId; }

    public boolean isSourceId(String sourceId){
        return isSourceIdPresent() && sourceId.equals(this.sourceId);
    }

    public boolean isSourceIdPresent() { return sourceId != null; }

//    /**
//     * Find and build analyser with a given name, and json setup file.
//     */
//    public static Analyser getAnalyser(String analyserName, String analyserSetupJson, String sourceId) throws IllegalAccessException, InstantiationException, IOException {
//        Reflections reflections = new Reflections("uk.ac.susx.tag.dialoguer.dialogue.analysing.factories");
//
//        Set<Class<? extends AnalyserFactory>> foundAnalyserFactories = reflections.getSubTypesOf(AnalyserFactory.class);
//
//        for (Class<? extends AnalyserFactory> klass : foundAnalyserFactories){
//            AnalyserFactory analyserFactory = klass.newInstance();
//            if (analyserFactory.getName().equals(analyserName)) {
//                Analyser a = analyserFactory.readJson(analyserSetupJson);
//                a.sourceId = sourceId;
//                return a;
//            }
//        } throw new IOException("Unable to load analyser; analyser name not found.");
//    }
}
