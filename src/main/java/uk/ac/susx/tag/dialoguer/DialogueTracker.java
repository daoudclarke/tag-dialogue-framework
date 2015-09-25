package uk.ac.susx.tag.dialoguer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.User;
import uk.ac.susx.tag.dialoguer.utils.Logger;

/**
 * Most high-level class. The main entry point into the dialogueing system is the getResponse() method
 * which takes a message, user info and an Id, maps it to the appropriate dialogue and uses the Dialoguer
 * to produce a response from the system.
 *
 * Tracks incomplete dialogues and handles logging.
 *
 * It is possible to register a CompletedDialogueHandler, which will be given a chance to play with Dialogue objects
 * when they are completed, just before they get untracked and forgotten (or logged).
 *
 *
 * User: Andrew D. Robertson
 * Date: 17/03/2015
 * Time: 14:13
 */
public class DialogueTracker implements AutoCloseable {

    private Dialoguer dialoguer;
    private Map<String, Dialogue> dialogues;
    private Map<String, GregorianCalendar> lastUpdated;
    private int trackingTimeLimitHours;
    private boolean logDialogues;
    private CompletedDialogueHandler cdHandler;
    private Logger logger;

//    /**
//     * Create a new Dialogue Tracker, passing in the path to the JSON file containing the definition of the
//     * Dialoguer. The JSON file can be a normal file or bundled in the jar's resources.
//     */
//    public DialogueTracker(String resourcePath) throws IOException {
//        this(Dialoguer.loadDialoguerFromJsonResourceOrFile(resourcePath));
//    }

    public DialogueTracker(Dialoguer dialoguer, Logger logger){
        this.dialoguer = dialoguer;
        this.dialogues = new HashMap<>();
        lastUpdated = new HashMap<>();
        trackingTimeLimitHours = 60*60*24;
        logDialogues = false;
        cdHandler = null;
        this.logger = logger;
    }

    /**
     * This allows you to register a function that will be called on all completed Dialogues before they are
     * untracked.
     */
    public void registerCompletedDialogueHandler(CompletedDialogueHandler h){
        cdHandler = h;
    }

    public void removeTrackingTimeLimit(){ trackingTimeLimitHours = 0; }
    public void setTrackingTimeLimitHours(int d){ trackingTimeLimitHours = d; }


    public String getResponse(String dialogueId, String userMessage, User userData){
        return getResponse(dialogueId, userMessage, userData, trackingTimeLimitHours);
    }

    public String getResponse(String dialogueId, String userMessage, User userData, int trackedTimeLimitHours){
        Dialogue d;
        if (isTracked(dialogueId, trackedTimeLimitHours)){
            d = dialoguer.interpret(userMessage, userData, dialogues.get(dialogueId));
        } else {
            d = dialoguer.interpret(userMessage, userData, dialoguer.startNewDialogue(dialogueId));
        }
        lastUpdated.put(dialogueId, new GregorianCalendar());
        dialogues.put(dialogueId, d);
        if (d.isComplete()) {
            if (logDialogues)
                log(dialogueId);
            if (cdHandler != null)
                cdHandler.handle(d);
            unTrackDialogue(dialogueId);
        }
        return d.isLastMessageByUser()? null : d.getLastMessage().getText();
    }

    public void unTrackDialogue(String id){
        dialogues.remove(id);
        lastUpdated.remove(id);
    }

    public boolean isTracked(String dialogueId) {
        return isTracked(dialogueId, trackingTimeLimitHours); // whatever the general time limit is set to
    }

    public boolean isTracked(String dialogueId, int timeLimitInHours){
        return dialogues.containsKey(dialogueId) && (isTimeSinceLastUpdateLessThan(timeLimitInHours, dialogueId));
    }

    public boolean isTrackedNoTimeLimit(String dialogueId){
        return isTracked(dialogueId, 0);
    }

    public boolean isTimeSinceLastUpdateLessThan(int timeInHours, String dialogueId){
        GregorianCalendar current = new GregorianCalendar();
        GregorianCalendar dialogueLastUpdated = lastUpdated.get(dialogueId);

        current.add(Calendar.HOUR, -timeInHours);
        return current.compareTo(dialogueLastUpdated) < 0;
    }

    public void log(String dialogueId){
        logger.info(dialogues.get(dialogueId).toString());
    }

    public void logAll(){
        for (Dialogue d : dialogues.values())
            logger.info(d.toString());
    }

    @Override
    public void close() throws Exception {
        dialoguer.close();
        if (logDialogues)
            logAll();
    }

    public static interface CompletedDialogueHandler {
        public void handle(Dialogue d);
    }


    //debugging
    public static Map<String,List<Double>> populateLocations(){
        Map<String,List<Double>> locations= new HashMap<>();

        List<Double> location = new ArrayList<>();
        location.add(50.823694);
        location.add(-0.143587);
        location.add(3.0);
        locations.put("clocktower",location);

        location=new ArrayList<>();
        location.add(50.823538);
        location.add(-0.143807);
        location.add(3.0);
        locations.put("waterstones",location);

        location=new ArrayList<>();
        location.add(50.823471);
        location.add(-0.143441);
        location.add(3.0);
        locations.put("bench",location);

        location=new ArrayList<>();
        location.add(50.823838);
        location.add(-0.144005);
        location.add(3.0);
        locations.put("indulge",location);

        location = new ArrayList<>();
        location.add(50.823732);
        location.add(-0.143193);
        location.add(3.0);
        locations.put("boots",location);
        //Inside boots: 50.823732, -0.143193

        location = new ArrayList<>();
        location.add(50.823589);
        location.add(-0.143933);
        location.add(3.0);
        locations.put("eat",location);

        //Inside eat: 50.823589, -0.143933

        location = new ArrayList<>();
        location.add(50.823609);
        location.add(-0.144029);
        location.add(3.0);
        locations.put("pret",location);
        //Inside pret: 50.823609, -0.144029

        location = new ArrayList<>();
        location.add(50.823843);
        location.add(-0.143801);
        location.add(3.0);
        locations.put("superdry",location);

        //Inside superdry: 50.823843, -0.143801

        location = new ArrayList<>();
        location.add(50.8240815);
        location.add(-0.1434446);
        location.add(3.0);
        locations.put("quadrant",location);
        //Inside the quadrant: 50.8240815, -0.1434446


        return locations;
    }

//    public static void main(String[] args){
//
//        //TEMP
//        ///new WikidataHandler().analyse("", null);
//        ///new NominatimAPIWrapper().queryAPI("HSBC,Manchester,United Kingdom", 200, 1, 1);
//
//        // get task name from command line arguments
//        String task="bot_driven";
//        if(args.length>0){
//            task=args[0];
//        }
//        String filename=task+"_dialoguer.json";
//
//        //set up test user ... maybe configure this from command line later
//        String userId="julie";
//        if(args.length>1){
//            userId=args[1];
//        }
//        String locationstring="clocktower";
//        if(args.length>2){
//            locationstring=args[2];
//        }
//        List<Double> location = populateLocations().get(locationstring);
//        Map<String,String> attributes=new HashMap<>();
//        attributes.put("name",userId);
//        User userData = new User(location.get(0),location.get(1),location.get(2),attributes);
//
//        //set up scanner to get user input
//        System.out.printf("Hello %s. I am the %s app.  What would you like to do?\n>", userData.getAttribute("name"),task);
//        Scanner userinput=new Scanner(System.in);
//        String userMessage;
//        boolean doContinue=true;
//
//        try (DialogueTracker myTracker = new DialogueTrackerDemo(filename)) {
//            //System.err.println("Using json file: "+filename);
//            while (doContinue){
//                userMessage=userinput.nextLine();
//                if(!userMessage.equals("")) {
//                    if (userMessage.startsWith("end")) {
//                        doContinue = false;
//                    } else {
//                        System.out.printf("%s\n", myTracker.getResponse(userId, userMessage, userData));
//                    }
//                    if (myTracker.isTracked(userId)) {
//                        //System.err.println("Still tracking");
//                        System.out.print(">");
//                    } else {
//                        //System.err.println("Finished tracking");
//                        if (doContinue) {
//                            System.out.printf("Hello %s. I am the %s app.  What would you like to do?\n>", userData.getAttribute("name"), task);
//                        }
//                    }
//                } else {
//                    System.out.print(">");
//                }
//            }
//
//        }
//        catch(Exception e){
//            System.err.println("Exception thrown: "+e.toString());
//            e.printStackTrace();
//            System.exit(1);
//        }
//    }
}
