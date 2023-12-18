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
import java.util.Objects;

public class Cliente {
    private static SSLSocket servidor;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static PrivateKey privateKey;
    private static PublicKey serverPublicKey;

    /**
     * Crea y guarda como variable global una conexion con el servidor seguro y gestiona las posibles excepciones
     * @param args
     */
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

    /**
     * Se comunica con el servidor para iniciar sesión con un usuario (DNI) y una contraseña
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     */
    private static void login() throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        // USUARIOS DE PRUEBA:
        // DNI: 12345678A, PASSWORD: 123
        // DNI: 12345678B, PASSWORD 123
        boolean loginCorrect = false;
        boolean isRetry = false;
        int tries = 1;
        while (!loginCorrect && tries < 3) {
            if (isRetry) {
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
        if (!loginCorrect) {
            System.out.println("No se pudo iniciar sesión en 3 intentos o menos, cerrando aplicación");
            return;
        }
        app();
    }

    /**
     * Recibe instrucciones del servidor sobre los datos a mostrar al cliente y envía al servidor lo que el usuario introduce para acabar creando una cuenta de usuario
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws ClassNotFoundException
     */
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
            if (first) {
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
        } while (!valid);

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
        if (!firmaValida || InputHelper.showMenu(1, 2, mensajeNormas, "") != 1) {
            out.writeObject(false);
        } else {
            out.writeObject(true);
            app();
        }
    }

    /**
     * Hace el papel de aplicación cliente, constantemente recibe grupos de 2 parámetros,
     * siendo el primero un String con el tipo de dato que el servidor requiere como respuesta (o la palabra SALIR para acabar)
     * y el segundo el texto a mostrar al usuario
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     */
    private static void app() throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String dataType;
        String serverMessage;
        String[] dataTypeSplitted;
        do {
            dataType = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            serverMessage = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);

            dataTypeSplitted = dataType.split("#");
            switch(dataTypeSplitted[0]) {
                case "info":
                    System.out.println(serverMessage);
                    InputHelper.pressEnterToContinue("Presiona ENTER para continuar");
                    break;
                case "menu":
                    int min = Integer.parseInt(dataTypeSplitted[1]);
                    int max = Integer.parseInt(dataTypeSplitted[2]);
                    int opcion = InputHelper.showMenu(min, max, serverMessage, "");
                    out.writeObject(EncryptionHelper.encryptMessage(String.valueOf(opcion), serverPublicKey));
                    break;
                case "string":
                    String string = InputHelper.getUserInput(serverMessage);
                    out.writeObject(EncryptionHelper.encryptMessage(string, serverPublicKey));
                    break;
                case "integer":
                    int minInt = Integer.parseInt(dataTypeSplitted[1]);
                    int maxInt = Integer.parseInt(dataTypeSplitted[2]);
                    int integerRespuesta = InputHelper.getNumericUserInput(serverMessage, minInt, maxInt);
                    out.writeObject(EncryptionHelper.encryptMessage(String.valueOf(integerRespuesta), serverPublicKey));
                    break;
                case "double":
                    double minDouble = Double.parseDouble(dataTypeSplitted[1]);
                    double maxDouble = Double.parseDouble(dataTypeSplitted[2]);
                    double doubleRespuesta = InputHelper.getNumericDecimalUserInput(serverMessage, minDouble, maxDouble);
                    out.writeObject(EncryptionHelper.encryptMessage(String.valueOf(doubleRespuesta), serverPublicKey));
                    break;
                case "SALIR":
                    System.out.println(serverMessage);
                    servidor.close();
                    break;
                default:
                    break;
            }

        } while(!Objects.equals(dataTypeSplitted[0], "SALIR"));
    }
}
