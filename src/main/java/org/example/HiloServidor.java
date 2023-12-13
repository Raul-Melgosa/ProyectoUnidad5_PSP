package org.example;

import org.example.Helpers.EncryptionHelper;
import org.example.Helpers.RegexHelper;
import org.example.Models.User;
import org.example.Providers.AccountProvider;
import org.example.Providers.MovementsProvider;
import org.example.Providers.UserProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

public class HiloServidor extends Thread {
    private String threadName;
    private Socket cliente;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private PublicKey clientPublicKey;
    private PrivateKey privateKey;
    private UserProvider userProvider;
    private AccountProvider accountProvider;
    private MovementsProvider movementsProvider;

    public HiloServidor(Socket cliente, String threadName, UserProvider userProvider, AccountProvider accountProvider, MovementsProvider movementsProvider) {
        this.cliente = cliente;
        this.threadName = threadName;
        this.userProvider = userProvider;
        this.accountProvider = accountProvider;
        this.movementsProvider = movementsProvider;
    }

    @Override
    public void run() {
        try {
            Thread.currentThread().setName(this.threadName);
            System.out.println(Thread.currentThread().getName() + "Connected");

            this.in = new ObjectInputStream(this.cliente.getInputStream());
            this.out = new ObjectOutputStream(this.cliente.getOutputStream());

            KeyPair keypair = EncryptionHelper.generateKeyPair();
            privateKey = keypair.getPrivate();
            out.writeObject(keypair.getPublic());
            clientPublicKey = (PublicKey) in.readObject();

            System.out.println(Thread.currentThread().getName() + "Keys exchanged");

            byte[] encryptedMessage = EncryptionHelper.encryptMessage("Bienvenido, elija una opción:\n1 - Iniciar sesión\n2 - Registrarse\n3 - Salir", clientPublicKey);
            out.writeObject(encryptedMessage);

            Integer option = (Integer) in.readObject();
            if (option == 1) {
                login();
            }
            if (option == 2) {
                register();
            }
            if (option == 3) {
                cliente.close();
            }
            cliente.close();
            System.out.println(Thread.currentThread().getName() + "Disconnected");
        } catch (SocketException e) {
            System.out.println(Thread.currentThread().getName() + "Disconnected");
            try {
                this.cliente.close();
            } catch (IOException ex) {
                // Ignore this error, as the thread is going to end here
            }
        } catch (IOException | InvalidKeyException | ClassNotFoundException | BadPaddingException |
                 NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException | SignatureException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void login() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, ClassNotFoundException {
        User usuario = null;
        boolean userMatches = false;
        int retriesLeft = 3;
        do {
            byte[] encryptedMessage = EncryptionHelper.encryptMessage("Introduzca su DNI con la letra en mayúscula", clientPublicKey);
            out.writeObject(encryptedMessage);
            String dni = "";
            boolean valid = false;
            while (!valid) {
                dni = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
                valid = RegexHelper.stringMatches(dni, "^[0-9]{8}[A-Z]{1}$");
                out.writeObject(valid);
                if (!valid) {
                    encryptedMessage = EncryptionHelper.encryptMessage("El DNI introducido no es válido, debe seguir el patrón de 8 números y una letra en MAYÚSCULAS, introduzca otro válido, por favor", clientPublicKey);
                    out.writeObject(encryptedMessage);
                }
            }
            encryptedMessage = EncryptionHelper.encryptMessage("Introduzca su contraseña", clientPublicKey);
            out.writeObject(encryptedMessage);
            byte[] password = EncryptionHelper.hash(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));

            for (User u : userProvider.getAll()) {
                if (Objects.equals(u.getDni(), dni) && EncryptionHelper.compareHashes(u.getPassword(), password)) {
                    userMatches = true;
                    System.out.println(Thread.currentThread().getName() + "User successfully logged in with DNI: " + u.getDni());
                    usuario = u;
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
        app(usuario);
    }

    private void register() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, ClassNotFoundException, SignatureException {
        byte[] encryptedMessage = EncryptionHelper.encryptMessage("Introduzca su DNI con la letra en mayúscula", clientPublicKey);
        out.writeObject(encryptedMessage);
        String dni = "";
        boolean valid = false;
        String errorMessage = "";
        while (!valid) {
            dni = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            valid = RegexHelper.stringMatches(dni, "^[0-9]{8}[A-Z]{1}$");
            if (valid) {
                for (User u : userProvider.getAll()) {
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

        encryptedMessage = EncryptionHelper.encryptMessage("Introduzca su nombre", clientPublicKey);
        out.writeObject(encryptedMessage);
        String nombre = "";
        valid = false;
        while (!valid) {
            nombre = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            valid = RegexHelper.stringMatches(nombre, "[a-z,A-Z,á,é,í,ó,ú,â,ê,ô,ã,õ,ç,Á,É,Í,Ó,Ú,Â,Ê,Ô,Ã,Õ,Ç,ü,ñ,Ü,Ñ]+");
            out.writeObject(valid);

            if (!valid) {
                errorMessage = "El nombre introducido no es válido, introduce uno formado únicamente por letras (se permiten algunos caracteres especiales como tildes y la ñ)";
                encryptedMessage = EncryptionHelper.encryptMessage(errorMessage, clientPublicKey);
                out.writeObject(encryptedMessage);
            }
        }

        encryptedMessage = EncryptionHelper.encryptMessage("Introduzca su apellido", clientPublicKey);
        out.writeObject(encryptedMessage);
        String apellido = "";
        valid = false;
        while (!valid) {
            apellido = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            valid = RegexHelper.stringMatches(apellido, "[a-z,A-Z,á,é,í,ó,ú,â,ê,ô,ã,õ,ç,Á,É,Í,Ó,Ú,Â,Ê,Ô,Ã,Õ,Ç,ü,ñ,Ü,Ñ]+");
            out.writeObject(valid);

            if (!valid) {
                errorMessage = "El apellido introducido no es válido, introduce uno formado únicamente por letras (se permiten algunos caracteres especiales como tildes y la ñ)";
                encryptedMessage = EncryptionHelper.encryptMessage(errorMessage, clientPublicKey);
                out.writeObject(encryptedMessage);
            }
        }

        encryptedMessage = EncryptionHelper.encryptMessage("Introduzca su email", clientPublicKey);
        out.writeObject(encryptedMessage);
        String email = "";
        valid = false;
        while (!valid) {
            email = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            valid = RegexHelper.stringMatches(email, "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]+$");
            out.writeObject(valid);

            if (!valid) {
                errorMessage = "El email introducido no es válido, introduce uno válido";
                encryptedMessage = EncryptionHelper.encryptMessage(errorMessage, clientPublicKey);
                out.writeObject(encryptedMessage);
            }
        }

        encryptedMessage = EncryptionHelper.encryptMessage("Introduzca su edad", clientPublicKey);
        out.writeObject(encryptedMessage);
        int edad = 1;
        valid = false;
        while (!valid) {
            String edadString = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            valid = RegexHelper.stringMatches(edadString, "[1-9]{1}[0-9]{1,2}");
            if (valid) {
                edad = Integer.parseInt(edadString);
                valid = (edad >= 18 && edad <= 150);
            }
            out.writeObject(valid);

            if (!valid) {
                errorMessage = "La edad introducida no es válida, introduce en dígitos una edad entre 18 y 150 años";
                encryptedMessage = EncryptionHelper.encryptMessage(errorMessage, clientPublicKey);
                out.writeObject(encryptedMessage);
            }
        }

        boolean passwordsMatch = false;
        boolean first = true;
        byte[] password;
        do {
            if (first) {
                first = false;
            } else {
                encryptedMessage = EncryptionHelper.encryptMessage("Las contraseñas no coinciden", clientPublicKey);
                out.writeObject(encryptedMessage);
            }
            encryptedMessage = EncryptionHelper.encryptMessage("Introduzca su contraseña", clientPublicKey);
            out.writeObject(encryptedMessage);
            password = EncryptionHelper.hash(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));

            encryptedMessage = EncryptionHelper.encryptMessage("Repita su contraseña", clientPublicKey);
            out.writeObject(encryptedMessage);
            byte[] passwordRepeat = EncryptionHelper.hash(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));

            passwordsMatch = EncryptionHelper.compareHashes(passwordRepeat, password);
            out.writeObject(passwordsMatch);
        } while (!passwordsMatch);

        String normasbanco = "¿Aceptas las normas del banco?\n1 - Sí\n2 - No";
        out.writeObject(EncryptionHelper.encryptMessage(normasbanco, clientPublicKey));
        out.writeObject(EncryptionHelper.signString(normasbanco, privateKey));

        boolean normasAceptadas = (boolean) in.readObject();
        if (normasAceptadas) {
            LinkedList<User> users = (LinkedList<User>) userProvider.getAll();
            User usuario = new User(dni, nombre, apellido, email, edad, password, new ArrayList<>());
            users.add(usuario);
            userProvider.writeToFile(users);
            app(usuario);
        }

    }

    private void app(User usuario) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        out.writeObject(EncryptionHelper.encryptMessage("Bienvenido, " + usuario.getNombre() + " " + usuario.getApellido() + " elige una opción\n", clientPublicKey));
    }
}
