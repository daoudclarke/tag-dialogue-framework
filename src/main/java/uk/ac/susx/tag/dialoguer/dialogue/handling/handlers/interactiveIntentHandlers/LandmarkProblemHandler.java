package uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.interactiveIntentHandlers;

import com.jcabi.immutable.Array;
import uk.ac.susx.tag.dialoguer.dialogue.components.Dialogue;
import uk.ac.susx.tag.dialoguer.dialogue.components.Intent;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.Handler;
import uk.ac.susx.tag.dialoguer.dialogue.handling.handlers.InteractiveHandler;
import uk.ac.susx.tag.dialoguer.knowledge.location.NominatimAPIWrapper;
import uk.ac.susx.tag.dialoguer.knowledge.location.RadiusAssigner;

import java.util.*;

/**
 * Created by Daniel Saska on 6/27/2015.
 */
public class LandmarkProblemHandler implements Handler.ProblemHandler {
    NominatimAPIWrapper nom = new NominatimAPIWrapper();
    List<String> landmarks = new ArrayList<>();

    private static final int maxDiamter = 500;

    @Override
    public boolean isInHandleableState(List<Intent> intents, Dialogue dialogue) {
        if (dialogue.isEmptyFocusStack()) { return false; }
        boolean intentmatch = intents.stream().filter(i->i.getName().equals(InteractiveHandler.landmarkIntent)).count()>0;
        boolean intentmatch2 = intents.stream().filter(i->i.getName().equals(InteractiveHandler.choiceIntent)).count()>0;
        boolean statematch = dialogue.peekTopFocus().equals(InteractiveHandler.aLandmarks);
        return (intentmatch || intentmatch2) && statematch;
    }

    @Override
    public void handle(List<Intent> intents, Dialogue dialogue, Object resource) {
        System.err.println("landmark intent handler fired");
        //TODO: Handle no choice
        //TOOD: Handle all choice
        //TODO: Handle null choice
        Intent intent = intents.stream().filter(i->i.isName(InteractiveHandler.choiceIntent)).findFirst().orElse(null);
        if (intent != null) {
            Iterator<Intent.Slot> it = intent.getSlotByType("choice").iterator();
            while (it.hasNext()) {
                int idx = Integer.parseInt(it.next().value);
                landmarks.set(idx, null);
            }
            {
                int i = landmarks.size();
                while (i --> 0) {
                    if (landmarks.get(i) == null) {
                        landmarks.remove(i);
                    }
                }
            }
            List<NominatimAPIWrapper.NomResult> instances[] = new List[landmarks.size()];
            for (int i = 0; i < landmarks.size(); ++i) {
                NominatimAPIWrapper.NomResult results[] = nom.queryAPI(landmarks.get(i) + ", " + dialogue.getFromWorkingMemory("location_given"), 200, 0, 1);
                instances[i] = Arrays.asList(results);
            }
            List<List<NominatimAPIWrapper.NomResult>> areas = new ArrayList<>();
            buildAreas(0, instances, areas);
            dialogue.putToWorkingMemory("n_loc", Integer.toString(areas.size()));
            dialogue.pushFocus(InteractiveHandler.qAddLandmarks);
            return;
        }
        intent = intents.stream().filter(i->i.isName(InteractiveHandler.landmarkIntent)).findFirst().orElse(null);
        if(intent.getSlotByType("place") != null && !intent.getSlotByType("place").iterator().next().value.equals("")) {

            NominatimAPIWrapper.NomResult quick_results[] = nom.queryAPI(intent.getSlotByType("place").iterator().next().value.replace("¥","")+ ", " + dialogue.getFromWorkingMemory("location_given"), 1, 0, 0);
            if (quick_results.length == 0) {
                dialogue.pushFocus(InteractiveHandler.landmarkNotFound);
                return;
            }
            landmarks.add(intent.getSlotByType("place").iterator().next().value);


            List<NominatimAPIWrapper.NomResult> instances[] = new List[landmarks.size()];
            for (int i = 0; i < landmarks.size(); ++i) {
                NominatimAPIWrapper.NomResult results[] = nom.queryAPI(landmarks.get(i) + ", " + dialogue.getFromWorkingMemory("location_given"), 200, 0, 1);
                instances[i] = Arrays.asList(results);
            }
            List<List<NominatimAPIWrapper.NomResult>> areas = new ArrayList<>();
            buildAreas(0, instances, areas);
            dialogue.putToWorkingMemory("n_loc", Integer.toString(areas.size()));
            if (areas.size() == 1) { //Found precise position
                dialogue.putToWorkingMemory("location_processed", areas.get(0).get(0).display_name);
                dialogue.pushFocus(InteractiveHandler.aMedicalHelp);
                dialogue.pushFocus(InteractiveHandler.qMedicalHelp);
                return;
            }
            if (areas.size() == 0) {
                //TODO: Give multiple-choce and select the most likely one
                dialogue.setChoices(landmarks);
                String landmarkList = "";
                for (String l : landmarks) {
                    landmarkList += l + ", ";
                }
                landmarkList = landmarkList.substring(0,landmarkList.length() - 2);
                dialogue.putToWorkingMemory("landmarks", landmarkList);
                dialogue.pushFocus(InteractiveHandler.qLandmarksRemove);
                return;
            }
        }
        dialogue.pushFocus(InteractiveHandler.qAddLandmarks);
    }

    private void buildAreas(int lmark, List<NominatimAPIWrapper.NomResult> instances[], List<List<NominatimAPIWrapper.NomResult>> areas) {
        if (lmark == 0) {
            for (int i = 0; i < instances[lmark].size(); ++i) {
                List<NominatimAPIWrapper.NomResult> area = new ArrayList<>();
                area.add(instances[lmark].get(i));
                areas.add(area);
            }
        } else {
            List<List<NominatimAPIWrapper.NomResult>> areasNew = new ArrayList<>();
            for (int k = 0; k < instances[lmark].size(); ++k) {
                for (int i = 0; i < areas.size(); ++i) {
                    List<NominatimAPIWrapper.NomResult> area = new ArrayList<>();
                    area.add(instances[lmark].get(k));
                    for (int j = 0; j < areas.get(i).size(); ++j) {
                        double dist = RadiusAssigner.haversineM(Double.parseDouble(instances[lmark].get(k).lat)
                                , Double.parseDouble(instances[lmark].get(k).lon)
                                , Double.parseDouble(areas.get(i).get(j).lat)
                                , Double.parseDouble(areas.get(i).get(j).lon));
                        if (dist > maxDiamter) {
                            break;
                        }
                        area.add(areas.get(i).get(j));
                    }
                    if (area.size() == lmark + 1) {
                        areasNew.add(area);
                    }
                }
            }
            areas.clear();
            areas.addAll(areasNew);
        }
        if (lmark < instances.length - 1) {
            buildAreas(lmark + 1, instances, areas);
        }
    }
}

