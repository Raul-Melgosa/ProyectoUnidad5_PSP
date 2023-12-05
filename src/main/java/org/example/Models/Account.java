package org.example.Models;

import java.io.Serializable;

public class Account implements Serializable {
    private String accountNumber;
    private String ownersDni;

    public Account() {
    }

    public Account(String accountNumber, String ownersDni) {
        this.accountNumber = accountNumber;
        this.ownersDni = ownersDni;
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
}
