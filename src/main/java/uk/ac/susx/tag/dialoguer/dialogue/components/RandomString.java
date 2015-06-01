package uk.ac.susx.tag.dialoguer.dialogue.components;

import java.util.Random;

/**
 * Code adapted from http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string/41156#41156
 *
 * Created by jpr27 on 02/09/2014.
 */
public class RandomString {

    // --------------- GLOBAL VARIABLES ---------------
    private static final char[] symbols;

    static {
        StringBuilder tmp = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ++ch) {
            tmp.append(ch);
        }
        for (char ch = 'a'; ch <= 'z'; ++ch) {
            tmp.append(ch);
        }
        for (char ch = 'A'; ch <= 'Z'; ++ch) {
            tmp.append(ch);
        }
        symbols = tmp.toString().toCharArray();
    }

    // -------------------- FIELDS --------------------
    private final Random random = new Random();
    private final char[] buf;

    // ----------------- CONSTRUCTORS -----------------
    public RandomString(int length) {
        if (length < 1)
            length = 1;
        buf = new char[length];
    }


    // -------------- API: PUBLIC METHODS -------------
    public String nextString() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }
}


// --------------- GLOBAL VARIABLES ---------------
// -------------------- FIELDS --------------------
// ----------------- CONSTRUCTORS -----------------
// -------------- API: PUBLIC METHODS -------------
// ---------- API: STD OVERRIDE METHODS -----------
// ---------------- PRIVATE METHODS ---------------
// ---------------- HELPER METHODS ----------------
// -------------- PROTECTED METHODS ---------------
// ---------------- STATIC METHODS ----------------
// ----------------- STATIC CLASS -----------------
// ------------- MAIN(): DEFAULT TEST -------------

