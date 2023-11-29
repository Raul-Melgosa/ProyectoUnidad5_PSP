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
    public static void main(String[] args) {
        try {
            System.setProperty("javax.net.ssl.trustStore", "Certificate/SSLCertificate");
            System.setProperty("javax.net.ssl.trustStorePassword", "2971613");

            SSLSocketFactory sfact = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket servidor = (SSLSocket) sfact.createSocket("localhost", 8182);

            ObjectOutputStream out = new ObjectOutputStream(servidor.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(servidor.getInputStream());

            KeyPair keypair = EncryptionHelper.generateKeyPair();
            PrivateKey privateKey = keypair.getPrivate();
            PublicKey serverPublicKey = (PublicKey) in.readObject();
            out.writeObject(keypair.getPublic());

            String instrucciones = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);

            Integer option = InputHelper.showMenu(1,3,instrucciones,"");
            out.writeObject(option);

            if(option == 1) {
                //login();
            } else if (option == 2) {
                //register();
            } else if (option == 3) {
                servidor.close();
            }

        } catch (IOException | NoSuchAlgorithmException |
                 ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
