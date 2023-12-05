package org.example.Providers;

import org.example.Models.Movement;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class MovementsProvider {
    public static List<Movement> getAll() {
        List<Movement> movements = new LinkedList<>();
        try {
            File fichero = new File("./Storage/movements.dat");
            if(!fichero.exists()) {
                fichero.createNewFile();
                return movements;
            }
            try {
                FileInputStream filein = new FileInputStream(fichero);
                try {
                    ObjectInputStream dataIS = new ObjectInputStream(filein);
                    while(true) {
                        movements.add((Movement) dataIS.readObject());
                    }
                } catch(EOFException e) {
                    filein.close();
                    return movements;
                } catch (ClassNotFoundException e) {
                    System.out.println("Se ha encontrado un objeto que no es del tipo movement en el fichero de movements");
                }
            } catch(FileNotFoundException e) {
                fichero.createNewFile();
                return getAll();
            }
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Se ha producido un error inesperado");
        }
        return movements;
    }
    
    private static boolean writeToFile(List<Movement> movements) {
        boolean error = false;
        try {
            File fichero = new File("./Storage/movements.dat");
            if(!fichero.exists()) {
                fichero.createNewFile();
            }
            FileOutputStream fileout = new FileOutputStream(fichero);
            ObjectOutputStream dataOS = new ObjectOutputStream(fileout);
            for (Movement m:movements) {
                dataOS.writeObject(m);
            }
            dataOS.close();
        } catch (IOException e) {
            System.out.println("Se ha producido un error inesperado");
            error = true;
        }
        return error;
    }
}
