package org.example;

import org.example.Helpers.EncryptionHelper;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;

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

            Cipher rsaCipher = Cipher.getInstance("RSA");
            KeyPair keypair = EncryptionHelper.generateKeyPair();
            PrivateKey privateKey = keypair.getPrivate();
            out.writeObject(keypair.getPublic());
            PublicKey clientPublicKey = (PublicKey) in.readObject();

            System.out.println("Client correctly connected and keys exchanged");

            byte[] encryptedMessage = EncryptionHelper.encryptMessage("Bienvenido, estimado cliente, elija una opción:\n1 - Iniciar sesión\n2 - Registrarse\n3 - Salir", clientPublicKey);
            System.out.println(encryptedMessage.toString());
            out.writeObject(encryptedMessage);

            Integer option = (Integer) in.readObject();
            if(option == 3) {
                cliente.close();
            }

        } catch (IOException | InvalidKeyException | ClassNotFoundException | BadPaddingException |
                 NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }
}
