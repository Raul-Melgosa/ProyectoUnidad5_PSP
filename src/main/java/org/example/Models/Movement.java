package org.example.Models;

import java.io.Serializable;

public class Movement implements Serializable {
    private String originAccountNumber;
    private String destinationAccountNumber;
    private Float quantity;

    public Movement() {
    }

    public Movement(String originAccountNumber, String destinationAccountNumber, Float quantity) {
        this.originAccountNumber = originAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.quantity = quantity;
    }

    public String getOriginAccountNumber() {
        return originAccountNumber;
    }

    public void setOriginAccountNumber(String originAccountNumber) {
        this.originAccountNumber = originAccountNumber;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber;
    }

    public Float getQuantity() {
        return quantity;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }
}
