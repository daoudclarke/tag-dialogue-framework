package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.AnalyserFactory;
import uk.ac.susx.tag.dialoguer.dialogue.analysing.factories.YesNoAnalyserStringMatchingFactory;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Tries to determine whether the using is yes/no-ing or confirming if the Handler has indicated that it is expecting
 * such a response.
 *
 * Uses the default yes/no intents. See Intent documentation.
 *
 * Created by Andrew D. Robertson on 12/04/2015.
 */
public class YesNoAnalyserStringMatching extends Analyser {

    private static Set<String> yesPhrases = Sets.newHashSet(
            "thats right",
            "thats fine",
            "ok",
            "okay",
            "alright",
            "fab",
            "confirm",
            "why not",
            "brilliant",
            "definitely",
            "great",
            "of course",
            "cool"
    );

    private static Pattern yesPattern =
            Pattern.compile(StringUtils.addWordBoundaries("(y+([eau]+[hps]*)?)+"));

    private static Pattern noPattern = StringUtils.buildDisjunctionWithWordBoundaries(Lists.newArrayList(
            "no+(pe)?",
            "na+h+",
            "nevermind",
            "dont think so"
    ));


    public YesNoAnalyserStringMatching(){

    }

    @Override
    public List<Intent> analyse(String message, Dialogue dialogue) {
        if (dialogue.isRequestingYesNo()){
            String text = dialogue.getStrippedText();
            if (isNo(text)){
                return Intent.buildNoIntent(message).toList();
            } else if (isYes(text)){
                return Intent.buildYesIntent(message).toList();
            } else {
                return Intent.buildNoIntent(message).toList();
            }
        } else return new ArrayList<>();
    }

    @Override
    public AnalyserFactory getFactory() {
        return new YesNoAnalyserStringMatchingFactory();
    }

    public boolean isNo(String text){
        return noPattern.matcher(text).find();
    }

    public boolean isYes(String text){
        return yesPattern.matcher(text).find() || yesPhrases.contains(text);
    }

    @Override
    public void close() throws Exception {
        // No resources to close
    }
}
