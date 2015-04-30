package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.google.common.collect.Sets;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.HandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.RuleBasedHandlerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 30/04/2015
 * Time: 16:44
 */
public class RuleBasedHandler extends Handler{

    private List<ResponseRule> rules;

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue) {
        return rules.stream()
                .map(r -> r.getResponse(intents))
                .filter(r -> r != null)
                .findFirst().orElse(Response.buildUnableToProcessResponse());
    }

    @Override
    public Dialogue getNewDialogue(String dialogueId) {
        return new Dialogue(dialogueId);
    }

    @Override
    public HandlerFactory getFactory() {
        return new RuleBasedHandlerFactory();
    }

    @Override
    public void close() throws Exception {

    }

    public static class ResponseRule {

        private Expression rule;
        private Map<String, String> intentToVarMap;
        private String responseName;
        private List<ResponseVariable> responseVariables;

        public Response getResponse(List<Intent> intents){
            Set<String> requiresFilling = intentToVarMap.keySet();
            Map<String, Intent> intentsPresent = intents.stream().collect(Collectors.toMap(Intent::getName, i -> i));
            Map<String, Boolean> expressionValues = new HashMap<>();
            for (String absentIntent : Sets.difference(requiresFilling, intentsPresent.keySet()))
                expressionValues.put(intentToVarMap.get(absentIntent), false);
            for (String presentIntent : Sets.intersection(requiresFilling, intentsPresent.keySet()))
                expressionValues.put(intentToVarMap.get(presentIntent), true);
            boolean evaluation =  RuleSet.assign(rule, expressionValues).toString().equals("true");

            if (evaluation) {
                Response r = new Response(responseName);
                for (ResponseVariable v : responseVariables){
                    r.addResponseVariable(v.variable, intentsPresent.get(v.intent).getSlotByType(v.slot).iterator().next().value);
                }
                return r;
            } else return null;
        }
    }

    public static class ResponseVariable {
        public String variable;
        public String intent;
        public String slot;
    }
}
