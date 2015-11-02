package uk.ac.susx.tag.dialoguer;

import uk.ac.susx.tag.dialoguer.dialogue.analysing.analysers.DiffAnalyser;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DiffAnalyserTest {
    @Test
    public void testCanRecogniseExistingQuery() {
        DiffAnalyser analyser = new DiffAnalyser(new Logger() {
            @Override
            public void info(String message) {
                System.out.println(message);
            }
        });

        Map<String, Intent> trainingSet = new HashMap<>();
        trainingSet.put("Search for potato on Amazon",
                newIntent("search_amazon", "amazon_search_item", "potato"));
        analyser.train(trainingSet);

        List<Intent> results = analyser.analyse("Search for orange on Amazon", new Dialogue("1"));

        assertEquals(1, results.size());
        Intent result = results.get(0);
        assertEquals("search_amazon", result.getName());

        ArrayList<Intent.Slot> slots = new ArrayList<>(result.getSlotCollection());
        assertEquals(1, slots.size());
        Intent.Slot slot = slots.get(0);
        assertEquals("amazon_search_item", slot.name);
        assertEquals("orange", slot.value);
    }

    @Test
    public void testMatchesLongestExactMatch() {
        DiffAnalyser analyser = new DiffAnalyser(new Logger() {
            @Override
            public void info(String message) {
                System.out.println(message);
            }
        });

        Map<String, Intent> trainingSet = new HashMap<>();
        trainingSet.put("Search for potato on Amazon",
                newIntent("search_amazon", "amazon_search_item", "potato"));
        trainingSet.put("Search on Amazon", new Intent("search_amazon"));
        analyser.train(trainingSet);

        List<Intent> results = analyser.analyse("Search for orange on Amazon", new Dialogue("1"));

        assertEquals(1, results.size());
        Intent result = results.get(0);
        assertEquals("search_amazon", result.getName());

        ArrayList<Intent.Slot> slots = new ArrayList<>(result.getSlotCollection());
        assertEquals(1, slots.size());
        Intent.Slot slot = slots.get(0);
        assertEquals("amazon_search_item", slot.name);
        assertEquals("orange", slot.value);
    }

    private Intent newIntent(String name, String slotName, String slotValue) {
        Intent result = new Intent(name);
        result.fillSlot(slotName, slotValue);
        return result;
    }
}
