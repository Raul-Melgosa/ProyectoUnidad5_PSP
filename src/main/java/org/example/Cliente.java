package org.example;

import org.example.Helpers.EncryptionHelper;
import org.example.Helpers.InputHelper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.security.*;

public class Cliente {
    private static SSLSocket servidor;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static PrivateKey privateKey;
    private static PublicKey serverPublicKey;

    public static void main(String[] args) {
        try {
            System.setProperty("javax.net.ssl.trustStore", "Certificate/SSLCertificate");
            System.setProperty("javax.net.ssl.trustStorePassword", "2971613");

            SSLSocketFactory sfact = (SSLSocketFactory) SSLSocketFactory.getDefault();
            servidor = (SSLSocket) sfact.createSocket("localhost", 8182);

            out = new ObjectOutputStream(servidor.getOutputStream());
            in = new ObjectInputStream(servidor.getInputStream());

            KeyPair keypair = EncryptionHelper.generateKeyPair();
            privateKey = keypair.getPrivate();
            serverPublicKey = (PublicKey) in.readObject();
            out.writeObject(keypair.getPublic());

            String instrucciones = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);

            Integer option = InputHelper.showMenu(1, 3, instrucciones, "");
            out.writeObject(option);

            if (option == 1) {
                login();
            } else if (option == 2) {
                register();
            } else if (option == 3) {
                servidor.close();
            }
            servidor.close();
        } catch (SocketException e) {
            System.out.println("Se ha perdido la conexión con el servidor, cerrando programa.");
            try {
                servidor.close();
            } catch (IOException ex) {
                // Ignore as the execution is intended to end here
            }
        } catch (IOException | NoSuchAlgorithmException | ClassNotFoundException | InvalidKeyException |
                 BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void login() throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        boolean loginCorrect = false;
        boolean isRetry = false;
        int tries = 1;
        while (!loginCorrect && tries < 3) {
            if(isRetry) {
                tries++;
                String message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
                System.out.println(message);
            } else {
                isRetry = true;
            }
            String message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            String response = InputHelper.getUserInput(message);
            out.writeObject(EncryptionHelper.encryptMessage(response, serverPublicKey));
            boolean valid = (boolean) in.readObject();
            while (!valid) {
                message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
                response = InputHelper.getUserInput(message);
                out.writeObject(EncryptionHelper.encryptMessage(response, serverPublicKey));
                valid = (boolean) in.readObject();
            }

            message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            out.writeObject(EncryptionHelper.encryptMessage(InputHelper.getUserInput(message), serverPublicKey));
            loginCorrect = (boolean) in.readObject();
        }
        if(!loginCorrect) {
            System.out.println("Couldn't login in 3 attempts, closing app");
            return;
        }
        app();
    }

    private static void register() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, ClassNotFoundException {
        // Dni
        String message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
        String response = InputHelper.getUserInput(message);
        out.writeObject(EncryptionHelper.encryptMessage(response, serverPublicKey));
        boolean valid = (boolean) in.readObject();
        while (!valid) {
            message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            response = InputHelper.getUserInput(message);
            out.writeObject(EncryptionHelper.encryptMessage(response, serverPublicKey));
            valid = (boolean) in.readObject();
        }

        // Nombre
        message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
        response = InputHelper.getUserInput(message);
        out.writeObject(EncryptionHelper.encryptMessage(response, serverPublicKey));
        valid = (boolean) in.readObject();
        while (!valid) {
            message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            response = InputHelper.getUserInput(message);
            out.writeObject(EncryptionHelper.encryptMessage(response, serverPublicKey));
            valid = (boolean) in.readObject();
        }

        // Apellido
        message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
        response = InputHelper.getUserInput(message);
        out.writeObject(EncryptionHelper.encryptMessage(response, serverPublicKey));
        valid = (boolean) in.readObject();
        while (!valid) {
            message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            response = InputHelper.getUserInput(message);
            out.writeObject(EncryptionHelper.encryptMessage(response, serverPublicKey));
            valid = (boolean) in.readObject();
        }

        // Email
        message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
        response = InputHelper.getUserInput(message);
        out.writeObject(EncryptionHelper.encryptMessage(response, serverPublicKey));
        valid = (boolean) in.readObject();
        while (!valid) {
            message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            response = InputHelper.getUserInput(message);
            out.writeObject(EncryptionHelper.encryptMessage(response, serverPublicKey));
            valid = (boolean) in.readObject();
        }

        // Edad
        message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
        response = InputHelper.getUserInput(message);
        out.writeObject(EncryptionHelper.encryptMessage(response, serverPublicKey));
        valid = (boolean) in.readObject();
        while (!valid) {
            message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            response = InputHelper.getUserInput(message);
            out.writeObject(EncryptionHelper.encryptMessage(response, serverPublicKey));
            valid = (boolean) in.readObject();
        }

        boolean first = true;
        do {
            if(first) {
                first = false;
            } else {
                System.out.println(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));
            }
            // Password
            message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            response = InputHelper.getUserInput(message);
            out.writeObject(EncryptionHelper.encryptMessage(response, serverPublicKey));

            // Repeat password
            message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            response = InputHelper.getUserInput(message);
            out.writeObject(EncryptionHelper.encryptMessage(response, serverPublicKey));
            valid = (boolean) in.readObject();
        } while(!valid);

        // Firma digital aceptar normas banco
        String mensajeNormas = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
        byte[] firma = (byte[]) in.readObject();
        boolean firmaValida = false;
        try {
            firmaValida = EncryptionHelper.checkSignatureValid(mensajeNormas, firma, serverPublicKey);
        } catch (SignatureException e) {
            System.out.println("La firma digital del banco no era correcta, cerrando aplicación por seguridad");
            servidor.close();
            return;
        }
        if(!firmaValida || InputHelper.showMenu(1,2,mensajeNormas,"") != 1) {
            out.writeObject(false);
        } else {
            out.writeObject(true);
            app();
        }
    }

    private static void app() throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String menu = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
        InputHelper.showMenu(1,1,menu,"");
    }
}
