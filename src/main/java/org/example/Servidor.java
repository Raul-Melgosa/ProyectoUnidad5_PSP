package org.example;

import org.example.Providers.AccountProvider;
import org.example.Providers.MovementsProvider;
import org.example.Providers.UserProvider;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class Servidor {
    /**
     * Instancia un servidor seguro haciendo uso de un certificado SSL, va lanzando hilos según los clientes se vayan conectando
     * @param args
     */
    public static void main(String[] args) {
        try {
            System.setProperty("javax.net.ssl.keyStore", "Certificate/AlmacenSSL.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "12345Abcde");

            SSLServerSocketFactory sfact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket servidorSSL = (SSLServerSocket) sfact.createServerSocket(8182);

            System.out.println("Server up and running");
            UserProvider userProvider = new UserProvider();
            AccountProvider accountProvider = new AccountProvider();
            MovementsProvider movementsProvider = new MovementsProvider();
            while(true) {
                String threadName = "[Client-" + new SimpleDateFormat("yyyyMMddHHmmssSS").format(new java.util.Date()) + "] ";
                new HiloServidor(servidorSSL.accept(), threadName, userProvider, accountProvider, movementsProvider).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
