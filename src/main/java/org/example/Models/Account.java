package org.example.Models;

import java.io.Serializable;

public class Account implements Serializable {
    private String name;
    private String accountNumber;
    private String ownersDni;
    private double balance;

    public Account() {
    }

    public Account(String name, String accountNumber, String ownersDni, double balance) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.ownersDni = ownersDni;
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getOwnersDni() {
        return ownersDni;
    }

    public void setOwnersDni(String ownersDni) {
        this.ownersDni = ownersDni;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
