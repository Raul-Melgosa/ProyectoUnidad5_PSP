package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class HiloServidor extends Thread {
    private Socket cliente;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    public HiloServidor(Socket cliente) {
        this.cliente = cliente;
    }

    @Override
    public void run() {
        try {
            this.in = new ObjectInputStream(this.cliente.getInputStream());
            this.out = new ObjectOutputStream(this.cliente.getOutputStream());


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
