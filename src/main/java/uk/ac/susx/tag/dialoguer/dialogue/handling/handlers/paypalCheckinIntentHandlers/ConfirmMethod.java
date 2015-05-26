package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.paypalCheckinIntentHandlers;

import uk.ac.susx.tag.dialoguer.Dialoguer;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.PaypalCheckinHandler;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.Merchant;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.ProductMongoDB;

import java.util.*;

/**
 * Created by juliewe on 21/04/2015.
 */
public class ConfirmMethod implements Handler.ProblemHandler {

    public boolean isInHandleableState(List<Intent> intents, Dialogue d){
        boolean intentMatch= intents.stream().anyMatch(i -> i.isName(Intent.yes)||(i.isName(PaypalCheckinHandler.confirm)&&i.isSlotTypeFilledWith(PaypalCheckinHandler.yes_no_slot,"yes")));
        boolean stateMatch=(d.getFromWorkingMemory("merchantId")==null||d.getFromWorkingMemory("merchantId").equals("")?false:true);
        return intentMatch&&stateMatch;
    }

    public void handle(List<Intent> intents, Dialogue d, Object r){
        handleAccept(d);
    }

    public static void handleAccept(Dialogue d){
        //perform side effects
        System.out.println("Contacting paypal for user "+d.getId()+" to checkin at "+d.getFromWorkingMemory("merchantId")+" ("+d.getFromWorkingMemory("merchantName")+")....");
        //return Response
        d.setComplete(true);
        d.pushFocus("confirm_completion");
    }
}

