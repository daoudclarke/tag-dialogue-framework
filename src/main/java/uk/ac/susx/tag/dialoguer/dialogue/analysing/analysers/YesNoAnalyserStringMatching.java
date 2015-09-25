package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;

/**
 * Tries to determine whether the using is yes/no-ing or confirming if the Handler has indicated that it is expecting
 * such a response.
 *
 * Uses the default yes/no intents. See Intent documentation.
 *
 * Created by Andrew D. Robertson on 12/04/2015.
 */
public class YesNoAnalyserStringMatching extends Analyser {

    private static List<String> yesPhraseList = Arrays.asList(
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
            "cool",
            "awesome",
            "affirmative",
            "amen",
            "fine",
            "good",
            "all right",
            "aye",
            "certainly",
            "by all means",
            "definitely", "definately",
            "exactly",
            "good enough",
            "indubitably",
            "just so",
            "most assuredly",
            "precisely",
            "very well",
            "brilliant",
            "acceptable",
            "delightful",
            "kosher",
            "sufficient"
    );
    private static Set<String> yesPhrases = new HashSet<>(yesPhraseList);

    private static Pattern yesPattern =
            Pattern.compile(StringUtils.addWordBoundaries("(y+([eau]+[hps]*)?)+"));

    private static Pattern noPattern = StringUtils.buildDisjunctionWithWordBoundaries(Arrays.asList(
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
                return Intent.buildNoChoiceIntent(message).toList();
            }
        } else return new ArrayList<>();
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
