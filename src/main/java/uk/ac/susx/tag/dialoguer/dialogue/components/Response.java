package uk.ac.susx.tag.dialoguer.dialogue.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 14:58
 */
public class Response {

    private static final Pattern templateVariableRegex = Pattern.compile("\\{(.+?)\\}");

    private String responseName;
    private Map<String, String> responseVariables;
    private List<String> newStates;

    public Response(String responseName) {
        this(responseName, new HashMap<>(), new ArrayList<>());
    }

    public Response(String responseName, Map<String, String> responseVariables, List<String> newStates) {
        this.newStates = newStates;
        this.responseName = responseName;
        this.responseVariables = responseVariables;
    }

    public String getResponseName() {
        return responseName;
    }

    public Map<String, String> getResponseVariables() {
        return responseVariables;
    }

    public List<String> getNewStates() {
        return newStates;
    }

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

    public static class ResponseException extends RuntimeException {
        public ResponseException(String msg) {
            super(msg);
        }
        public ResponseException(Throwable cause){
            super(cause);
        }
        public ResponseException(String msg, Throwable cause){
            super(msg, cause);
        }
    }
}
