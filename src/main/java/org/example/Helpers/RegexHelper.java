package org.example.Helpers;

import java.util.regex.*;

public class RegexHelper {
    public static boolean stringMatches(String input, String pattern) {
        Pattern pat=null;
        Matcher mat=null;
        pat=Pattern.compile(pattern);
        mat=pat.matcher(input);
        return mat.matches();
    }
}
