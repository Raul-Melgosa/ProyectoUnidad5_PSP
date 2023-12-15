package org.example.Models;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public class Movement implements Serializable {
    private String originAccountNumber;
    private String destinationAccountNumber;
    private double quantity;
    private String concept;
    private String dateTime;

    public Movement() {
    }

    public Movement(String originAccountNumber, String destinationAccountNumber, double quantity, String concept) {
        this.originAccountNumber = originAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.quantity = quantity;
        this.concept = concept;
        this.dateTime = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss").format(new java.util.Date());
    }

    public Movement(String originAccountNumber, String destinationAccountNumber, double quantity, String concept, String dateTime) {
        this.originAccountNumber = originAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.quantity = quantity;
        this.concept = concept;
        this.dateTime = dateTime;
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

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "Concepto: " + concept +
                "\nFecha: " + dateTime +
                "\nCuenta de origen: " + originAccountNumber +
                "\nCuenta de destino: " + destinationAccountNumber +
                "\nCantidad: " + quantity + "€";
    }
}
