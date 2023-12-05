package org.example.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private String dni;
    private byte[] password;
    private ArrayList<String> accountNumbers;

    public User() {
        this.accountNumbers = new ArrayList<String>();
    }

    public User(String dni, byte[] password, ArrayList<String> accountNumbers) {
        this.dni = dni;
        this.password = password;
        this.accountNumbers = accountNumbers;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public ArrayList<String> getAccountNumbers() {
        return accountNumbers;
    }

    public void setAccountNumbers(ArrayList<String> accountNumbers) {
        this.accountNumbers = accountNumbers;
    }

    public void addAccount(Account account) {
        this.accountNumbers.add(account.getAccountNumber());
    }
}
