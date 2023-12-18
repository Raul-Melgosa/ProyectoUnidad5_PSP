package org.example.Helpers;

import java.util.Scanner;

/**
 * Funciones de ayuda para la interacción con el usuario
 */
public class InputHelper {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";

    /**
     * Imprime por pantalla un mensaje y espera a que el usuario presione la tecla enter para continuar con la ejecución
     * @param message
     */
    public static void pressEnterToContinue(String message) {
        System.out.println("\n" + message);
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    /**
     * Imprime un menú y devuelve la opción del mismo que elija el usuario
     * @param min int mímimo que debe introducir el usuario
     * @param max int máximo que debe introducir el usuario
     * @param menu String que contiene el texto del menú
     * @param lastInput String que contiene el último valor introducido por el usuario (Al hacer la primera llamada introducir siempre "")
     * @return int correspondiente a la opción escogida por el usuario
     */
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

    /**
     * Muestra al usuario unas instruciones y devuelve su resouesta, obligando a que la misma sea un valor numérico entero
     * @param instructions String instrucciones que se muestran al usuario
     * @param min int Valor mínimo introducible por el usuario
     * @param max int Valor máximo introducible por el usuario
     * @return int itroducido por el usuario
     */
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

    /**
     * Muestra al usuario unas instruciones y devuelve su resouesta, obligando a que la misma sea un valor numérico decimal
     * @param instructions String instrucciones que se muestran al usuario
     * @param min double Valor mínimo introducible por el usuario
     * @param max double Valor máximo introducible por el usuario
     * @return double introducido por el usuario
     */
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

    /**
     * Muestra al usuario unas instruciones y devuelve su resouesta, obligando a que la misma sea una cadena de texto no vacía
     * @param instructions String instrucciones que se muestran al usuario
     * @return String introducido por el usuario
     */
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
