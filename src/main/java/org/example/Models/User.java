package org.example.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private String dni;
    private String nombre;
    private String apellido;
    private String email;
    private int edad;
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

    public User(String dni, String nombre, String apellido, String email, int edad, byte[] password, ArrayList<String> accountNumbers) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.edad = edad;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
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
