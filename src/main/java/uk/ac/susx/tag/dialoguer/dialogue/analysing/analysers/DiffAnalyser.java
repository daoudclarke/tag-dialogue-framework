package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

import com.google.plaintext.diff_match_patch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

/**
 * Created by dc on 28/10/15.
 */
public class DiffAnalyser extends Analyser {
    HashMap<String, Intent> queryTermIntents = new HashMap<>();
    diff_match_patch dmp = new diff_match_patch();

    public void train(Map<String, Intent> queryParses) {
        for (Map.Entry<String, Intent> entry : queryParses.entrySet()) {
            String query = entry.getKey();
            Intent intent = entry.getValue();
            queryTermIntents.put(query, intent);
        }
    }

    private float diffEntails(String s, String t) {
        LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(s, t);
        int common = 0;
        for (diff_match_patch.Diff diff : diffs) {
            common += diff.text.length();
        }
        return common / (float) Math.max(t.length(),s.length());
    }

    private Map.Entry<String, Intent> findBest(String trans) {
        Map.Entry<String, Intent> best = null;
        float bestEnt = 0.0F;
        for (Map.Entry<String, Intent> entry : queryTermIntents.entrySet()) {
            float ent = diffEntails(trans, entry.getKey());
            if (ent > 0.5 && ent > bestEnt) {
                bestEnt = ent;
                best = entry;
            }
        }
        return best;
    }

    private Intent matchTemplate(String translation, Intent intent, String input) {
        Intent result = new Intent(intent.getName());
        for (Intent.Slot slot : intent.getSlotCollection()) {
            if (translation.contains(slot.value)) {
                String replaced = translation.replace(slot.value, "*");
                ArrayList<diff_match_patch.Diff> inputDiffs =
                        new ArrayList<>(dmp.diff_main(replaced, input));
                for (int i=0; i<inputDiffs.size(); ++i) {
                    diff_match_patch.Diff diff = inputDiffs.get(i);
                    if (diff.text.equals("*")) {
                        String replacement = inputDiffs.get(i + 1).text;
                        result.fillSlot(slot.name, replacement);
                    }
                }
            } else {
                result.fillSlot(slot);
            }
        }

        return result;
    }

    @Override
    public List<Intent> analyse(String message, Dialogue dialogue) {
        Map.Entry<String, Intent> best = findBest(message);
        if (best != null) {
            return Collections.singletonList(
                    matchTemplate(best.getKey(), best.getValue(), message));
        }
        return Collections.emptyList();
    }

    @Override
    public void close() throws Exception {

    }
}
