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
import java.security.*;

public class Cliente {
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static PrivateKey privateKey;
    private static PublicKey serverPublicKey;

    public static void main(String[] args) {
        try {
            System.setProperty("javax.net.ssl.trustStore", "Certificate/SSLCertificate");
            System.setProperty("javax.net.ssl.trustStorePassword", "2971613");

            SSLSocketFactory sfact = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket servidor = (SSLSocket) sfact.createSocket("localhost", 8182);

            out = new ObjectOutputStream(servidor.getOutputStream());
            in = new ObjectInputStream(servidor.getInputStream());

            KeyPair keypair = EncryptionHelper.generateKeyPair();
            privateKey = keypair.getPrivate();
            serverPublicKey = (PublicKey) in.readObject();
            out.writeObject(keypair.getPublic());

            String instrucciones = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);

            Integer option = InputHelper.showMenu(1, 3, instrucciones, "");
            out.writeObject(option);

            if (option == 1 || option == 2) {
                login();
            } else if (option == 3) {
                servidor.close();
            }

        } catch (IOException | NoSuchAlgorithmException | ClassNotFoundException | InvalidKeyException |
                 BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void login() throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
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

        valid = false;
        while (!valid) {
            message = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            out.writeObject(EncryptionHelper.encryptMessage(InputHelper.getUserInput(message), serverPublicKey));
            valid = (boolean) in.readObject();
        }
    }
}
