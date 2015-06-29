package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.components.Response;
import uk.ac.susx.tag.dialoguer.dialogue.handling.factories.HandlerFactory;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.interactiveIntentHandlers.*;
import uk.ac.susx.tag.dialoguer.knowledge.database.product.processing.StringProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Daniel Saska on 6/25/2015.
 */
public class InteractiveHandler extends Handler {
    private Map<String,String> knowledge = new HashMap<>();


    private Map<String, String> humanReadableSlotNames; //read from config file

    //Intent names
    public static final String unknownIntent="unknown";
    public static final String locationIntent="location";
    public static final String locationUnknownIntent="unknown_location";
    public static final String yesNoIntent="yes_no";
    public static final String landmarkIntent = "landmark";
    public static final String helpIntent = "help";
    public static final String choiceIntent = "choice";

    //Response/focus/state names
    public static final String initial = "initial";
    public static final String qLocation = "q_location";
    public static final String aLocation = "a_location";
    public static final String unknownResponse="unknown"; //For handeling aux. errors
    public static final String confirmResponse="confirm"; //Asks user to confirm his choice
    public static final String qEnableGps="q_enable_gps"; //Asks user whether he can enable gps
    public static final String aEnableGps="a_enable_gps";
    public static final String aWaitGps ="a_wait_gps";
    public static final String qWaitGps ="q_wait_gps";
    public static final String helpGps="help_gps";
    public static final String qGpsHelp="q_need_help_gps"; //Asks user whether he wants help with tunring on the gps
    public static final String aGpsHelp="a_need_help_gps";
    public static final String qGpsLocConfirm="q_confirm_gps_location"; //Asks user whether he wants help with tunring on the gps
    public static final String aGpsLocConfirm="a_confirm_gps_location";
    public static final String qMedicalHelp="q_medical_help"; //Asks user whether he needs abulance called
    public static final String aMedicalHelp="q_medical_help";
    public static final String qLandmarks="q_landmarks";//Asks user whether he can see any landmarks such as KFC or other points of interest
    public static final String aLandmarks="q_landmarks";
    public static final String qLandmarksRemove="q_remove_landmark";
    public static final String landmarkNotFound="no_landmark_found";//NO instances of such landmark were found
    public static final String qAddLandmarks="q_add_landmarks";//Asks user for more landmarks
    public static final String qLocationConfirm="q_location_confirm";//Asks user to confirm location for very last time
    public static final String aLocationConfirm="a_location_confirm";
    public static final String medicalHelp="medical_help"; //Inform user about help being dispatched.

    //Slot names
    public static final String locationSlot="location";
    public static final String landmarkSlot="landmark";
    public static final String landmarksSlot="landmarks";
    public static final String yesNoSlot="yes_no";
    public static final String addressSlot="address";
    public static final String nLocationsSlot = "n_loc";

    public InteractiveHandler() {

        super.registerProblemHandler(new UnknownProblemHandler());
        super.registerProblemHandler(new YesNoProblemHandler());
        super.registerProblemHandler(new LocationProblemHandler());
        super.registerProblemHandler(new HelpProblemHelper());
        super.registerProblemHandler(new LandmarkProblemHandler());
        super.registerProblemHandler(new GpsProblemHandler());
    }

    @Override
    public Response handle(List<Intent> intents, Dialogue dialogue) {
        boolean complete=useFirstProblemHandler(intents, dialogue, null); //is there a problem handler?
        if(!complete){ //no problem handler or intent handler
            dialogue.pushFocus(unknownResponse);
        }
        return processStack(dialogue);
    }

    @Override
    public Dialogue getNewDialogue(String dialogueId) {
        Dialogue d = new Dialogue(dialogueId);
        d.setState("initial");
        return d;
    }

    @Override
    public HandlerFactory getFactory() {
        return null;
    }

    @Override
    public void close() throws Exception {

    }


    /****
     *
     * @param d
     * @return
     * Generate a response based on the current state of the dialogue (most specifically the FocusStack)
     * Pop the focus stack, add responseVariables which are required by this focus, generate the Response associated with this focus and responseVariables
     */
    public Response processStack(Dialogue d){
        String focus=unknownResponse;
        if (!d.isEmptyFocusStack()) {
            focus = d.popTopFocus();
        }
        Map<String, String> responseVariables = new HashMap<>();
        switch(focus) {
            case qGpsLocConfirm:
            case qLocationConfirm:
                responseVariables.put(addressSlot, d.getFromWorkingMemory("location_processed"));
                break;
            case qAddLandmarks:
                responseVariables.put(nLocationsSlot, d.getFromWorkingMemory("n_loc"));
                break;
            case qLandmarksRemove:
                responseVariables.put(landmarksSlot, d.getFromWorkingMemory("landmarks"));
                break;

        }
        return new Response(focus,responseVariables);

    }
}
