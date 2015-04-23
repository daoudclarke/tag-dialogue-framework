package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

import org.reflections.Reflections;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * It is the responsibility of an Analyser to look at the latest message received from the user (and potentially the
 * dialogue thusfar), and determine what intent or intents the user is trying to express.
 *
 * See Intent documentation.
 *
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 18:02
 */
public abstract class Analyser implements AutoCloseable {


    /**
     * Perform analysis of latest message, forming a list of intents the user is trying to express
     */
    public abstract List<Intent> analyse(String message, Dialogue dialogue);

    /**
     * Return a new instance of the factory which can create this analyser
     */
    public abstract AnalyserFactory getFactory();


    public String getName(){ return getFactory().getName();}

    /**
     * Find and build analyser with a given name, and json setup file.
     */
    public static Analyser getAnalyser(String analyserName, String analyserSetupJson) throws IllegalAccessException, InstantiationException, IOException {
        Reflections reflections = new Reflections("uk.ac.susx.tag.dialoguer.dialogue.analysing.factories");

        Set<Class<? extends AnalyserFactory>> foundAnalyserFactories = reflections.getSubTypesOf(AnalyserFactory.class);

        for (Class<? extends AnalyserFactory> klass : foundAnalyserFactories){
            AnalyserFactory analyserFactory = klass.newInstance();
            if (analyserFactory.getName().equals(analyserName)) {
                return analyserFactory.readJson(analyserSetupJson);
            }
        } throw new IOException("Unable to load analyser; analyser name not found.");
    }
}
