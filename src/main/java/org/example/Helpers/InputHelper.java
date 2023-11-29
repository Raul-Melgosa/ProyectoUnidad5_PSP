package org.example.Helpers;

import java.util.Scanner;

public class InputHelper {
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    public static void pressEnterToContinue(String message) {
        System.out.println("\n" + message);
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    public static int showMenu(int min, int max, String menu, String lastInput) {
        String selection;
        int numericSelection;
        Scanner scanner = new Scanner(System.in);

        System.out.println(menu);
        System.out.print("==> ");

        try {
            selection = scanner.nextLine();
            lastInput = selection;
            numericSelection = Integer.parseInt(selection);
            if(numericSelection < min || numericSelection > max) {
                throw new Exception("");
            }
        } catch (Exception e) {
            System.out.println("\"" + RED + lastInput + RESET + "\" no es una opción válida, prueba otra vez");
            return showMenu(min, max, menu, lastInput);
        }
        return numericSelection;
    }

    public static int getNumericUserInput(String instructions, int min, int max) {
        int inputInt;
        String input = getUserInput(instructions);
        try {
            inputInt = Integer.parseInt(input);
            if(inputInt > max || inputInt < min) {
                throw new NumberFormatException();
            }
        } catch(java.lang.NumberFormatException e) {
            System.out.println("Debes introducir un valor numérico entre " + min + " y " + max);
            return getNumericUserInput(instructions, min, max);
        }
        return inputInt;
    }

    public static double getNumericDecimalUserInput(String instructions, double min, double max) {
        double inputDouble;
        String input = getUserInput(instructions);
        try {
            inputDouble = Double.parseDouble(input);
            if(inputDouble > max || inputDouble < min) {
                throw new NumberFormatException();
            }
        } catch(java.lang.NumberFormatException e) {
            System.out.println("Debes introducir un valor numérico entre " + min + " y " + max);
            return getNumericDecimalUserInput(instructions, min, max);
        }
        return inputDouble;
    }

    public static String getUserInput(String instructions) {
        String input = "";
        Scanner scanner = new Scanner(System.in);
        boolean firstTime = true;
        do {
            if(firstTime) {
                firstTime = false;
            } else {
                System.out.println("No se permite introducir un valor vacío, por favor, introduce un valor: ");
            }
            System.out.println(instructions);
            System.out.print("==> ");
            input = scanner.nextLine();
        } while(input.isEmpty());
        return input;
    }
}
