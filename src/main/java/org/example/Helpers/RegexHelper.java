package org.example.Helpers;

import java.util.regex.*;

public class RegexHelper {
    /**
     * Dado un String y una expresión regular, devuelve true si matchean
     * @param input El String a comprobar
     * @param pattern El patrón que se comprueba
     * @return
     */
    public static boolean stringMatches(String input, String pattern) {
        Pattern pat=null;
        Matcher mat=null;
        pat=Pattern.compile(pattern);
        mat=pat.matcher(input);
        return mat.matches();
    }
}
