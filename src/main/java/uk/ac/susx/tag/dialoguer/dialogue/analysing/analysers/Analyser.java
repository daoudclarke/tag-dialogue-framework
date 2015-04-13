package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 18:02
 */
public abstract class Analyser implements AutoCloseable {

    // Perform analysis of latest message, forming a list of intents the user is trying to express
    public abstract List<Intent> analise(String message, Dialogue dialogue);

    // Return a new instance of the factory which can create this analyser
    public abstract AnalyserFactory getFactory();

    public String getName(){
        return getFactory().getName();
    }
}
