package uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers;

import com.google.plaintext.diff_match_patch;

import java.util.ArrayList;
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
        // TODO: match template (copy from js)
        return null;
    }

    @Override
    public List<Intent> analyse(String message, Dialogue dialogue) {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
