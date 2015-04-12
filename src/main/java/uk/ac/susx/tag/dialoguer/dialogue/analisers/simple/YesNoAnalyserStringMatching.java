package uk.ac.susx.tag.dialoguer.dialogue.analisers.simple;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import uk.ac.susx.tag.dialoguer.dialogue.analisers.Analyser;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by Andrew D. Robertson on 12/04/2015.
 */
public class YesNoAnalyserStringMatching implements Analyser {

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
    public List<Intent> analise(String message, Dialogue dialogue) {
        if (dialogue.isRequestingYesNo()){
            String text = dialogue.getStrippedText();
            if (isNo(text)){
                dialogue.setRequestingYesNo(false);
                return Intent.buildNoIntent(message).toList();
            } else if (isYes(text)){
                dialogue.setRequestingYesNo(false);
                return Intent.buildYesIntent(message).toList();
            } else {
                return new ArrayList<>();
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
    public String getName() {
        return "yes_no_simple";
    }

    @Override
    public Analyser readJson(InputStream json) throws IOException {
        return new YesNoAnalyserStringMatching();
    }

    @Override
    public void close() throws Exception {
        // No resources to close
    }
}
