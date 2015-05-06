package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.HandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.RuleBasedHandlerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 30/04/2015
 * Time: 16:44
 */
public class RuleBasedHandler extends Handler{

    public static List<String> variables = Lists.newArrayList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");

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

        public Expression rule;
        public Map<String, String> intentToVarMap;
        public String responseName;
        public List<ResponseVariable> responseVariables;

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

    public static Map<String, String> parseIntentToVarMap(String expressionDefinition){
        Pattern p = Pattern.compile("\\{(.+?)\\}");
        Map<String, String> intentToVarMap = new HashMap<>();
        Matcher m = p.matcher(expressionDefinition);

        int variable = 0;
        while (m.find()){
            String intentName = m.group(1);
            if (!intentToVarMap.containsKey(intentName)){
                if (variable >= variables.size()) throw new Dialoguer.DialoguerException("Rule based handler doesn't support rules using more than 26 intents");
                intentToVarMap.put(intentName, variables.get(variable));
                variable++;
            }
        }

        return intentToVarMap;
    }

    public static String convertIntentNamesToVariables(String expressionDefinition, Map<String, String> intentToVarMap){
        String expression = expressionDefinition;
        for (Map.Entry<String, String> entry : intentToVarMap.entrySet()){
            expression = expression.replaceAll("\\{" + entry.getKey() + "\\}", entry.getValue());
        }
        return expression;
    }

}
