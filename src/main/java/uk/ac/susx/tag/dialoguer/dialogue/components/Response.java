package uk.ac.susx.tag.dialoguer.dialogue.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A handler should use this object to specify the name of an appropriate response template.
 *
 * If the specified template has variables to be filled, then the values for those variables should be given also.
 *
 * If you wish to fill a variable with a numbered list, then you should make use of StringUtils.numberList(). Since
 * this will introduce consistency, and improve the performance of the choice analyser.
 *
 * If you wish to put the dialogue in different states after a response, then these states should also be specified as
 * new states. Otherwise don't specify the states (or set as null) and the current states will be left as they are.
 *
 * The methods prefixed with "build" are helper methods which generate the default responses for certain common conditions.
 *
 * Example usage:
 *
 *   Response r = new Response("user_choice", Lists.newArrayList("state1", "state2")
 *                  .addResponseVariable("user", "Andy")
 *                  .addResponseVariable("choices", StringUtils.numberList(Lists.newArrayList("choice1","choice2"), "\n"));
 *
 * This creates a response for the template called "user_choice", and tells the Dialoguer to take the new states as
 * "state1" and "state2". It specifies that the "user" variable in the template should be filled with "Andy" and that
 * the "choices" variable should be filled with 2 choices formatted in a numbered list separated by newlines.
 *
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 14:58
 */
public class Response {

    private static final Pattern templateVariableRegex = Pattern.compile("\\{(.+?)\\}");

    private static final String defaultConfirmCancelResponseId = "confirm_cancellation";
    private static final String defaultAutoQueryResponseId = "auto_query";

    private String responseName;
    private Map<String, String> responseVariables;
    private List<String> newStates;

    public Response(String responseName) {
        this(responseName, new HashMap<>(), null);
    }

    public Response(String responseName, Map<String, String> responseVariables){
        this(responseName, responseVariables, null);
    }

    public Response(String responseName, List<String> newStates){
        this(responseName, new HashMap<>(), newStates);
    }

    public Response(String responseName, Map<String, String> responseVariables, List<String> newStates) {
        this.newStates = newStates;
        this.responseName = responseName;
        this.responseVariables = responseVariables;
    }

    public Response addResponseVariable(String variableName, String variableValue){
        responseVariables.put(variableName, variableValue);
        return this;
    }

    public String getResponseName() {
        return responseName;
    }
    public Map<String, String> getResponseVariables() {
        return responseVariables;
    }

    public void setResponseName(String responseName) { this.responseName = responseName; }
    public void setResponseVariables(Map<String, String> responseVariables) { this.responseVariables = responseVariables;}

    public boolean areNewStates() { return newStates != null; }
    public List<String> getNewStates() {
        return newStates;
    }
    public void setNewStates(List<String> newStates) { this.newStates = newStates; }

    /**
     * Used by the Dialoguer to fill the appropriate template with this response.
     */
    public String fillTemplate(String template){
        return fillTemplate(template, responseVariables);
    }
    public static String fillTemplate(String template, Map<String, String> variables){
        Matcher m = templateVariableRegex.matcher(template);
        StringBuffer filledTemplate = new StringBuffer();
        while(m.find()){
            String variable = m.group(1);
            if (variables.containsKey(variable)) {
                m.appendReplacement(filledTemplate, variables.get(variable));
            } else {
                throw new ResponseException("Template requires a variable not passed.\n" +
                                            "Template: " + template + "\n" +
                                            "Variable: " + variable);
            }
        }
        m.appendTail(filledTemplate);
        return filledTemplate.toString();
    }

    public static Response buildCancellationResponse(){
        return new Response(defaultConfirmCancelResponseId);
    }
    public static Response buildCancellationResponse(Map<String, String> responseVariables, List<String> newStates){
        return new Response(defaultConfirmCancelResponseId, responseVariables, newStates);
    }

    public static Response buildAutoQueryResponse(String query){
        return new Response(defaultAutoQueryResponseId)
                .addResponseVariable("query", query);
    }

    public static class ResponseException extends RuntimeException {
        public ResponseException(String msg) { super(msg);}
        public ResponseException(Throwable cause){ super(cause); }
        public ResponseException(String msg, Throwable cause){ super(msg, cause);}
    }
}
