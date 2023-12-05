package org.example.Providers;

import org.example.Models.Account;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class AccountProvider {
    public static List<Account> getAll() {
        List<Account> accounts = new LinkedList<>();
        try {
            File fichero = new File("./Storage/accounts.dat");
            if(!fichero.exists()) {
                fichero.createNewFile();
                return accounts;
            }
            try {
                FileInputStream filein = new FileInputStream(fichero);
                try {
                    ObjectInputStream dataIS = new ObjectInputStream(filein);
                    while(true) {
                        accounts.add((Account) dataIS.readObject());
                    }
                } catch(EOFException e) {
                    filein.close();
                    return accounts;
                } catch (ClassNotFoundException e) {
                    System.out.println("Se ha encontrado un objeto que no es del tipo account en el fichero de accounts");
                }
            } catch(FileNotFoundException e) {
                fichero.createNewFile();
                return getAll();
            }
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Se ha producido un error inesperado");
        }
        return accounts;
    }
    
    private static boolean writeToFile(List<Account> accounts) {
        boolean error = false;
        try {
            File fichero = new File("./Storage/accounts.dat");
            if(!fichero.exists()) {
                fichero.createNewFile();
            }
            FileOutputStream fileout = new FileOutputStream(fichero);
            ObjectOutputStream dataOS = new ObjectOutputStream(fileout);
            for (Account a:accounts) {
                dataOS.writeObject(a);
            }
            dataOS.close();
        } catch (IOException e) {
            System.out.println("Se ha producido un error inesperado");
            error = true;
        }
        return error;
    }
}
