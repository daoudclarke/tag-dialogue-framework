package uk.ac.susx.tag.dialoguer;

import com.google.gson.Gson;
import uk.ac.susx.tag.dialoguer.dialogue.analisers.Analiser;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.components.User;
import uk.ac.susx.tag.dialoguer.dialogue.handlers.Handler;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The definition of a Dialoguer task.
 *
 * Definition includes:
 *
 * --------- Responses -----------
 *
 *    Available responses are defined in a JSON object:
 *
 *    {
 *        responseName1 : {
 *            templates : [string, alternative_1, alternative2, ...]
 *        },
 *        responseName2 : {
 *            templates : [string, alternative_1, alternative2, ...]
 *        },
 *        ...
 *    }
 *
 *    Each response name refers to a type of response that the system can make. Each response has a list of
 *    one or more templates, which are simply alternative ways of expressing that response (among which the system
 *    will choose randomly). One may define variables to be filled in the template using curly braces. For example:
 *
 *      "Are you sure you want to purchase {product name}?"
 *
 *    The system will require a response that contains the variable "product name", and will replace "{product name}"
 *    with its value when using the template.
 *
 * -------------------------------
 *
 *
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 15:28
 */
public class Dialoguer {

    private static final Random random = new Random();
    private static final Gson gson = new Gson();

    private Handler handler;
    private Analiser analiser;
    private Map<String, List<String>> responseTemplates;

    public Dialogue interpret(String message, Dialogue dialogue){
        dialogue.addNewUserMessage(message, analiser.analise(message, dialogue));

        Response r = handler.handle(dialogue);
        dialogue.addNewSystemMessage(fillTemplateWithResponse(r));
        dialogue.setStates(r.getNewStates());

        return dialogue;
    }

    private String fillTemplateWithResponse(Response r){
        List<String> alternatives = responseTemplates.get(r.getResponseName());
        if (alternatives != null){
            if (alternatives.size() == 1)
                return r.fillTemplate(alternatives.get(0));
            else
                return r.fillTemplate(alternatives.get(random.nextInt(alternatives.size())));
        }
        throw new DialoguerException("No response template found for this response name: " + r.getResponseName());
    }

    private static class DialoguerException extends RuntimeException{
        public DialoguerException(String msg) {
            super(msg);
        }
        public DialoguerException(Throwable cause){
            super(cause);
        }
        public DialoguerException(String msg, Throwable cause){
            super(msg, cause);
        }
    }
}
