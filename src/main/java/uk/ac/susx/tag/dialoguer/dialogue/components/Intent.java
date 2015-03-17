package uk.ac.susx.tag.dialoguer.dialogue.components;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew D. Robertson
 * Date: 16/03/2015
 * Time: 14:58
 */
public class Intent {

    private String name;
    private String text;

    public static class Slot {
        public String name;
        public String type;
        public int start;
        public int end;
    }
}
