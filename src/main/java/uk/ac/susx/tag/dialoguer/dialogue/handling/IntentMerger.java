package uk.ac.susx.tag.dialoguer.dialogue.handling;

import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Given a list of intents, merge a subset of them into a single intent and return the new list.
 *
 * There are two approaches to using the merge functionality. The choice is one of style only.
 *
 * Static method approach:
 *
 *   intents = IntentMerger.merge(intents, Sets.newHashSet("intent2", "intent4"), "intent24")    // Merge intents 2 and 4 into new intent 24
 *   intents = IntentMerger.merge(intents, Sets.newHashSet("intent3", "intent6"), "intent36")    // There merge intents 3 and 6 into new intent 36
 *
 * Non-static method approach:
 *
 *   intents = new IntentMerger(intents)
 *                  .merge(Sets.newHashSet("intent2", "intent4"), "intent24")
 *                  .merge(Sets.newHashSet("intent3", "intent6"), "intent36")
 *
 * In both approaches, the new intent name could be replaced with the MergeFunction, which performs a custom merging
 * of the user's own coding.
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


    /**
     * Merge into a single intent, those intents in *intents* whose names are present in *intentNamesToBeMerged*.
     *
     * The new intent produced will have the name *newIntentName*. The source Id of the new intent will be "merged".
     */
    public static List<Intent> merge(List<Intent> intents, Set<String> intentNamesToBeMerged, String newIntentName){
        return merge(intents, intentNamesToBeMerged, toBeMerged -> {
            Intent merged = new Intent(newIntentName);
            for (Intent i : toBeMerged) {
                i.getSlotCollection().forEach(merged::fillSlot);
            }
            return merged;
        });
    }

    /**
     * Merge into a single intent, those intents in *intents* whose names are present in *intentNamesToBeMerged*, using
     * *f* as the merge function.
     *
     * The source Id of the new intent will be "merged".
     */
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
