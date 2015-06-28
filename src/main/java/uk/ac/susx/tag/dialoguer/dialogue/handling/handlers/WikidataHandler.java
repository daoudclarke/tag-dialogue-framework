package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.HandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.WikidataHandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.knowledge.questionanswering.WikidataAPIWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Process the factual question
 *
 * User: Daniel Saska
 * Date: 17/03/2015
 * Time: 14:17
 */

public class WikidataHandler extends Handler {

    WikidataAPIWrapper wi = new WikidataAPIWrapper();


    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue) {
        Response r = null;
        if (intents.get(0).getName().equals("factual_qa")) {
            String str = wi.getPropertyValue
                    ( Integer.parseInt(intents.get(0).getSlotByType("entity").iterator().next().value)
                    , Integer.parseInt(intents.get(0).getSlotByType("property").iterator().next().value) );
            Map<String, String> m = new HashMap<>();
            m.put("wd_answer", str);
            r = new Response("factual_qa", m);
        }
        if (intents.get(0).getName().equals("unknown")) {
            r = new Response("unknown", new HashMap<String, String>());
        }
        return r;
    }

    @Override
    public Dialogue getNewDialogue(String dialogueId) {
        Dialogue d = new Dialogue(dialogueId);
        d.setState("initial_query");
        return d;
    }

    @Override
    public HandlerFactory getFactory() {
        return new WikidataHandlerFactory();
    }

    @Override
    public void close() {
        wi.close();
    }

}
