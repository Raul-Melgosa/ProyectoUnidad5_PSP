package org.example.Providers;

import org.example.Models.Account;
import org.example.Models.User;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AccountProvider {
    public synchronized List<Account> getAll() {
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
    
    private synchronized boolean writeToFile(List<Account> accounts) {
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

    public List<Account> getAllByUser(User user) {
        List<Account> accounts = this.getAll();
        List<Account> userAccounts = new LinkedList<>();
        for (Account account:accounts) {
            if(account.getOwnersDni().equals(user.getDni())) {
                userAccounts.add(account);
            }
        }
        return userAccounts;
    }

    public Account getByAccountNumber(String accountNumber) {
        List<Account> accounts = this.getAll();
        Account account = null;
        for (Account a:accounts) {
            if(a.getAccountNumber().equals(accountNumber)) {
                account = a;
            }
        }
        return account;
    }

    public synchronized void insertAccount(Account account) {
        LinkedList<Account> accounts = (LinkedList<Account>) this.getAll();
        accounts.add(account);
        writeToFile(accounts);
    }

    public synchronized void editAccount(String accountNumber, Account account) {
        LinkedList<Account> accounts = new LinkedList<>();

        for (Account cuenta:this.getAll()) {
            if(Objects.equals(accountNumber, cuenta.getAccountNumber())) {
                accounts.add(account);
            } else {
                accounts.add(cuenta);
            }
        }
        writeToFile(accounts);
    }

    public synchronized String getNewAccountNumber() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
