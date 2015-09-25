package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

/**
 * An analyser that fires and intent when a pattern is found in the stripped text of the last user message. The intent
 * will contain a slot for each match, filled with the text of the match. The slot type is "match".
 *
 * User: Andrew D. Robertson
 * Date: 16/04/2015
 * Time: 12:00
 */
public class PatternFindingAnalyser extends Analyser {

    private Pattern regex;
    private String intentName;

    private PatternFindingAnalyser(){
        super();
        regex = null;
        intentName = null;
    }

    public PatternFindingAnalyser(Pattern regex, String intentName){
        super();
        this.regex = regex;
        this.intentName = intentName;
    }

    @Override
    public List<Intent> analyse(String message, Dialogue dialogue) {
        Matcher m = regex.matcher(dialogue.getStrippedText());

        Intent i = new Intent(intentName, dialogue.getStrippedText());

        while (m.find()){
            i.fillSlot("match", m.group(), m.start(), m.end()).toList();
        }

        return i.isAnySlotFilled()? i.toList() : new ArrayList<Intent>();
    }

//    @Override
//    public AnalyserFactory getFactory() {
//        return new PatternFindingAnalyserFactory();
//    }

    @Override
    public void close() throws Exception {
        //No resource to close
    }

}
