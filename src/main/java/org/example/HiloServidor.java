package org.example;

import org.example.Helpers.EncryptionHelper;
import org.example.Helpers.RegexHelper;
import org.example.Models.User;
import org.example.Providers.UserProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import java.util.Objects;

public class HiloServidor extends Thread {
    private Socket cliente;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private PublicKey clientPublicKey;
    private PrivateKey privateKey;

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
            privateKey = keypair.getPrivate();
            out.writeObject(keypair.getPublic());
            clientPublicKey = (PublicKey) in.readObject();

            System.out.println("Client correctly connected and keys exchanged");

            byte[] encryptedMessage = EncryptionHelper.encryptMessage("Bienvenido, elija una opción:\n1 - Iniciar sesión\n2 - Registrarse\n3 - Salir", clientPublicKey);
            out.writeObject(encryptedMessage);

            Integer option = (Integer) in.readObject();
            if (option == 1) {
                login();
            }
            if (option == 2) {
                login();
            }
            if (option == 3) {
                cliente.close();
            }

        } catch (IOException | InvalidKeyException | ClassNotFoundException | BadPaddingException |
                 NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void login() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, ClassNotFoundException {
        byte[] encryptedMessage = EncryptionHelper.encryptMessage("Introduzca su DNI con la letra en mayúscula", clientPublicKey);
        out.writeObject(encryptedMessage);
        String dni = "";
        boolean valid = false;
        while (!valid) {
            dni = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            valid = RegexHelper.stringMatches(dni, "^[0-9]{8}[A-Z]{1}$");
            out.writeObject(valid);
            if (!valid) {
                encryptedMessage = EncryptionHelper.encryptMessage("El DNI introducido no es válido, debe seguir el patrón de 8 números y una letra en MAYÚSCULAS, introduxca otro válido, por favor", clientPublicKey);
                out.writeObject(encryptedMessage);
            }
        }
        encryptedMessage = EncryptionHelper.encryptMessage("Introduzca su contraseña", clientPublicKey);
        out.writeObject(encryptedMessage);
        boolean userMatches = false;
        int retriesLeft = 3;
        do {
            byte[] password = EncryptionHelper.hash(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));

            for (User u : UserProvider.getAll()) {
                if (Objects.equals(u.getDni(), dni) && EncryptionHelper.compareHashes(u.getPassword(), password)) {
                    userMatches = true;
                    break;
                }
            }

            retriesLeft--;

            out.writeObject(userMatches);

            if (retriesLeft == 0) {
                return;
            }

            if (!userMatches) {
                encryptedMessage = EncryptionHelper.encryptMessage("Login incorrecto, quedan " + retriesLeft + " intentos", clientPublicKey);
                out.writeObject(encryptedMessage);
            }
        } while (!userMatches);
    }

    private void register() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, ClassNotFoundException {
        byte[] encryptedMessage = EncryptionHelper.encryptMessage("Introduzca su DNI con la letra en mayúscula", clientPublicKey);
        out.writeObject(encryptedMessage);
        String dni = "";
        boolean valid = false;
        String errorMessage = "";
        while (!valid) {
            dni = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            valid = RegexHelper.stringMatches(dni, "^[0-9]{8}[A-Z]{1}$");
            if (valid) {
                for (User u : UserProvider.getAll()) {
                    if (Objects.equals(u.getDni(), dni)) {
                        valid = false;
                        errorMessage = "El dni introducido ya existe en nuestra base de datos";
                        break;
                    }
                }
            } else {
                errorMessage = "El DNI introducido no es válido, debe seguir el patrón de 8 números y una letra en MAYÚSCULAS";
            }
            out.writeObject(valid);
            if (!valid) {
                encryptedMessage = EncryptionHelper.encryptMessage(errorMessage + ", introduzca otro válido, por favor", clientPublicKey);
                out.writeObject(encryptedMessage);
            }
        }
        encryptedMessage = EncryptionHelper.encryptMessage("Introduzca su contraseña", clientPublicKey);
        out.writeObject(encryptedMessage);

        byte[] password = EncryptionHelper.hash(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));

        ArrayList<User> users = (ArrayList<User>) UserProvider.getAll();
        users.add(new User(dni, password, new ArrayList<>()));
        UserProvider.writeToFile(users);
    }
}
