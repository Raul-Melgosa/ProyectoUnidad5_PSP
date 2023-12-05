package org.example.Providers;

import org.example.Models.User;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class UserProvider {
    public static List<User> getAll() {
        List<User> users = new LinkedList<>();
        try {
            File fichero = new File("./Storage/users.dat");
            if(!fichero.exists()) {
                fichero.createNewFile();
                return users;
            }
            try {
                FileInputStream filein = new FileInputStream(fichero);
                try {
                    ObjectInputStream dataIS = new ObjectInputStream(filein);
                    while(true) {
                        users.add((User) dataIS.readObject());
                    }
                } catch(java.io.EOFException e) {
                    filein.close();
                    return users;
                } catch (ClassNotFoundException e) {
                    System.out.println("Se ha encontrado un objeto que no es del tipo user en el fichero de users");
                }
            } catch(java.io.FileNotFoundException e) {
                fichero.createNewFile();
                return getAll();
            }
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Se ha producido un error inesperado");
        }
        return users;
    }

    public static boolean writeToFile(List<User> users) {
        boolean error = false;
        try {
            File fichero = new File("./Storage/users.dat");
            if(!fichero.exists()) {
                fichero.createNewFile();
            }
            FileOutputStream fileout = new FileOutputStream(fichero);
            ObjectOutputStream dataOS = new ObjectOutputStream(fileout);
            for (User u:users) {
                dataOS.writeObject(u);
            }
            dataOS.close();
        } catch (IOException e) {
            System.out.println("Se ha producido un error inesperado");
            error = true;
        }
        return error;
    }
}
