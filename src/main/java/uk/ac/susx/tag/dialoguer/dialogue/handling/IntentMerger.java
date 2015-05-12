package uk.ac.susx.tag.dialoguer.dialogue.handling;

import com.google.common.collect.Sets;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Given a list of intents, merge a subset of them into a single intent and return the new list.
 *
 *
 *
 * User: Andrew D. Robertson
 * Date: 11/05/2015
 * Time: 13:23
 */
public class IntentMerger {

    private List<Intent> intents;

    public IntentMerger(List<Intent> intents){
        this.intents = intents;
    }

    public IntentMerger merge(Set<String> intentNamesToBeMerged, String newIntentName){
        intents = merge(intents, intentNamesToBeMerged, newIntentName); return this;
    }

    public IntentMerger merge(Set<String> intentNamesToBeMerged, MergeFunction f){
        intents = merge(intents, intentNamesToBeMerged, f); return this;
    }

    public List<Intent> getIntents() { return intents; }


    public static List<Intent> merge(List<Intent> intents, Set<String> intentNamesToBeMerged, String newIntentName){
        return merge(intents, intentNamesToBeMerged, toBeMerged -> {
            Intent merged = new Intent(newIntentName);
            for (Intent i : toBeMerged) {
                i.getSlotCollection().forEach(merged::fillSlot);
            }
            return merged;
        });
    }

    public static List<Intent> merge(List<Intent> intents, Set<String> intentNamesToBeMerged, MergeFunction f){
        // Split intents into those that match the names to be merged and those that don't.
        Map<Boolean, List<Intent>> requiresMapping = intents.stream().collect(Collectors.partitioningBy(i -> intentNamesToBeMerged.contains(i.getName())));

        // If there was any intents that need merging
        if (!requiresMapping.get(true).isEmpty()){
            // Leave these ones unmerged
            List<Intent> output = requiresMapping.get(false);
            // Merge the matches into a single intent by calling the merge function
            Intent merged = f.merge(requiresMapping.get(true));
            // Set the new source
            merged.setSource("merged");
            // Add to the list of remaining unmerged intents
            output.add(merged);
            // Return list
            return output;
        } else return intents;
    }

    public static interface MergeFunction {

        Intent merge(List<Intent> toBeMerged);
    }
}
