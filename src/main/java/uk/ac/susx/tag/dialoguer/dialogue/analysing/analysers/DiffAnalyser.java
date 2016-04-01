package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

import com.google.plaintext.diff_match_patch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.utils.Logger;

/**
 * Created by dc on 28/10/15.
 */
public class DiffAnalyser extends Analyser {
    HashMap<String, Intent> queryTermIntents = new HashMap<>();
    diff_match_patch dmp = new diff_match_patch();
    Logger logger;

    public DiffAnalyser(Logger logger) {
        this.logger = logger;
    }

    private class MatchTemplateResult {
        public Intent intent;
        public String nlGloss;

        public MatchTemplateResult(String intentName, String nlGloss) {
            this.intent = new Intent(intentName);
            this.nlGloss = nlGloss;
        }
    }

    public void train(Map<String, Intent> queryParses) {
        for (Map.Entry<String, Intent> entry : queryParses.entrySet()) {
            String query = entry.getKey();
            Intent intent = entry.getValue();
            queryTermIntents.put(query, intent);
        }
    }

    private float diffSimilarity(String s, String t) {
        LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(s, t);
        int common = 0;
        for (diff_match_patch.Diff diff : diffs) {
            if (diff.operation == diff_match_patch.Operation.EQUAL) {
                common += diff.text.length();
            }
        }
        return common / (float) Math.max(t.length(),s.length());
    }

    // TODO: there is a bug here when the template term has a some characters in common with the
    // query term. We need to find a way around that. E.g:
    //  - "Search for John on Facebook" - "Search for shoes on Amazon"
    //  - "John" and "shoes" has a complex diff that messes up the matching.
    private MatchTemplateResult matchTemplate(String translation, Intent intent, String input) {
        MatchTemplateResult result = new MatchTemplateResult(intent.getName(), translation);
        for (Intent.Slot slot : intent.getSlotCollection()) {
            if (translation.contains(slot.value)) {
                String replaced = translation.replace(slot.value, "*");
                ArrayList<diff_match_patch.Diff> inputDiffs =
                        new ArrayList<>(dmp.diff_main(replaced, input));
                for (int i=0; i<inputDiffs.size(); ++i) {
                    logger.info("Diff: " + i + inputDiffs.get(i));
                    diff_match_patch.Diff diff = inputDiffs.get(i);
                    if (diff.text.equals("*") && i + 1 < inputDiffs.size()) {
                        String replacement = inputDiffs.get(i + 1).text;
                        result.intent.fillSlot(slot.name, replacement);
                        result.nlGloss = result.nlGloss.replace(slot.value, replacement);
                    }
                }
            } else {
                result.intent.fillSlot(slot);
            }
        }

        return result;
    }

    @Override
    public List<Intent> analyse(String message, Dialogue dialogue) {
        Intent best = null;
        float bestEnt = 0.0F;
        for (Map.Entry<String, Intent> entry : queryTermIntents.entrySet()) {
            MatchTemplateResult matchTemplateResult = matchTemplate(entry.getKey(), entry.getValue(), message);
            float ent = diffSimilarity(message, matchTemplateResult.nlGloss);
            logger.info("Match template result: " + ent +
                    " " + entry.getKey() + " " + matchTemplateResult.nlGloss);
            if (ent > 0.5 && ent > bestEnt) {
                bestEnt = ent;
                best = matchTemplateResult.intent;
            }
        }

        if (best != null) {
            return Collections.singletonList(best);
        }
        return Collections.emptyList();
    }

    @Override
    public void close() throws Exception {

    }
}
