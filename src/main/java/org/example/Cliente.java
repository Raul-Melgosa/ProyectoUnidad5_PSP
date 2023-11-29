package org.example;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;

public class Cliente {
    public static void main(String[] args) {
        try {
            System.setProperty("javax.net.ssl.trustStore", "Certificate/SSLCertificate");
            System.setProperty("javax.net.ssl.trustStorePassword", "2971613");

            SSLSocketFactory sfact = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket servidor = (SSLSocket) sfact.createSocket("localhost", 8182);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
