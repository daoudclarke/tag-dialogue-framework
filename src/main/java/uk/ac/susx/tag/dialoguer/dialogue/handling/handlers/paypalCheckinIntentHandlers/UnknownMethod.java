package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.PaypalCheckinHandler;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by juliewe on 30/04/2015.
 */
public class UnknownMethod implements Handler.ProblemHandler {


    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        return intents.stream().anyMatch(i->i.isName(PaypalCheckinHandler.otherIntent)||i.isName(PaypalCheckinHandler.nochoice));
    }

    @Override
    public void handle(List<Intent> intents, Dialogue d, Object res){

        if(d.getStates().contains("initial")) {
            d.pushFocus("unknown_hello");
        } else {
            d.pushFocus("unknown_request_location");
        }

    }



}
