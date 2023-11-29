package org.example;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;

public class Servidor {
    public static void main(String[] args) {
        try {
            System.setProperty("javax.net.ssl.keyStore", "Certificate/AlmacenSSL.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "12345Abcde");

            SSLServerSocketFactory sfact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket servidorSSL = (SSLServerSocket) sfact.createServerSocket(8182);

            System.out.println("Server up and running");
            while(true) {
                new HiloServidor(servidorSSL.accept()).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
