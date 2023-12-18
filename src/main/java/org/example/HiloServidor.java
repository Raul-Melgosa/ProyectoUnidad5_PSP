package org.example;

import org.example.Helpers.EncryptionHelper;
import org.example.Helpers.InputHelper;
import org.example.Helpers.RegexHelper;
import org.example.Models.Account;
import org.example.Models.Movement;
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
import java.util.*;

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

    /**
     * Gestiona las posibles excepciones, crea y guarda los streams de entrada y salida, recoge información del cliente para saber si empezar con un inicio de sesión o un registro
     */
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

    /**
     * Se comunica con el cliente para pedir los datos de inicio de sesión
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws ClassNotFoundException
     */
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

    /**
     * Se comunica con el cliente para obtener todos los datos necesarios para crear un usuario, en caso de que el usuario se cree correctamente, se inicia sesión de forma automática
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SignatureException
     */
    private void register() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, ClassNotFoundException, SignatureException {
        // DNI
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

        // Nombre
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

        // Apellido
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

        // Email
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

        // Edad
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

        // Contraseña
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

        // Firma digital normas del banco
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

    /**
     * Pregunta al cliente qué tipo de operación quiere realizar.
     * @param usuario El usuario con el que se ha accedido a la aplicación
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void app(User usuario) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, ClassNotFoundException {
        int opcion;
        do {
            out.writeObject(EncryptionHelper.encryptMessage("menu#1#4", clientPublicKey));
            out.writeObject(EncryptionHelper.encryptMessage("Bienvenido " + usuario.getNombre() + " " + usuario.getApellido() + ", elige una opción\n1 - Ver mis cuentas\n2 - Abrir una nueva cuenta\n3 - Realizar transferencia\n4 - Salir", clientPublicKey));
            opcion = Integer.parseInt(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));
            switch (opcion) {
                case 1:
                    cuentas(usuario);
                    break;
                case 2:
                    crearCuenta(usuario);
                    break;
                case 3:
                    transferencia(usuario);
                    break;
                default:
                    out.writeObject(EncryptionHelper.encryptMessage("SALIR", clientPublicKey));
                    out.writeObject(EncryptionHelper.encryptMessage("Saliendo de la aplicación...", clientPublicKey));
                    break;
            }
        } while (opcion != 4);
        cliente.close();
    }

    /**
     * Busca todas las cuentas de un usuario y las envía al cliente para que este pueda elegir una para realizar operaciones, o si prefiere regresar al menú inicial
     * @param usuario El usuario con el que se ha accedido a la aplicación
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void cuentas(User usuario) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, ClassNotFoundException {
        List<Account> cuentas = accountProvider.getAllByUser(usuario);

        if (cuentas.isEmpty()) {
            out.writeObject(EncryptionHelper.encryptMessage("menu#1#2", clientPublicKey));
            out.writeObject(EncryptionHelper.encryptMessage("No tienes ninguna cuenta todavía\n1 - Crear una ahora\n2 - Volver", clientPublicKey));
            int opcion = Integer.parseInt(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));
            if (opcion == 1) {
                crearCuenta(usuario);
            }
        } else {
            String menu = "Elige una cuenta:";
            int x = 1;
            for (Account cuenta : cuentas) {
                menu += "\n" + x + " - " + cuenta.getAccountNumber();
                x++;
            }
            menu += "\n" + x + " - Volver";
            out.writeObject(EncryptionHelper.encryptMessage("menu#1#" + x, clientPublicKey));
            out.writeObject(EncryptionHelper.encryptMessage(menu, clientPublicKey));
            int opcion = Integer.parseInt(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));
            if (opcion == x) {
                return;
            }
            Account cuentaSeleccionada = cuentas.get(opcion - 1);
            cuenta(cuentaSeleccionada);
        }
    }

    /**
     * Dada una cuenta, ofrece al cliente las diferentes operaciones disponibles la misma
     * @param cuenta Cuenta que el usuario ha seleccionado para realizar operaciones
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void cuenta(Account cuenta) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, ClassNotFoundException {
        cuenta = accountProvider.getByAccountNumber(cuenta.getAccountNumber());
        String menu = "Nombre: " + cuenta.getName() + "\nNúmero de cuenta: " + cuenta.getAccountNumber() + "\nSaldo: " + cuenta.getBalance() + "€" + "\n\nElige una acción:" + "\n1 - Ver todos los movimientos" + "\n2 - Ver movimientos de ingreso" + "\n3 - Ver movimientos de retirada" + "\n4 - Realizar ingreso desde cajero" + "\n5 - Realizar retirada desde cajero" + "\n6 - Volver a inicio";
        out.writeObject(EncryptionHelper.encryptMessage("menu#1#6", clientPublicKey));
        out.writeObject(EncryptionHelper.encryptMessage(menu, clientPublicKey));
        int opcion = Integer.parseInt(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));
        double cantidad;

        List<Movement> movimientos;
        switch (opcion) {
            case 1:
                movimientos = movementsProvider.getAllByAccount(cuenta);
                verMovimientos(movimientos);
                break;
            case 2:
                movimientos = movementsProvider.getAllReceivedByAccount(cuenta);
                verMovimientos(movimientos);
                break;
            case 3:
                movimientos = movementsProvider.getAllSentByAccount(cuenta);
                verMovimientos(movimientos);
                break;
            case 4:
                out.writeObject(EncryptionHelper.encryptMessage("double#0#" + Double.MAX_VALUE, clientPublicKey));
                out.writeObject(EncryptionHelper.encryptMessage("Introduce la cantidad en euros a ingresar: ", clientPublicKey));
                cantidad = Double.parseDouble(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));
                cuenta.setBalance(cuenta.getBalance() + cantidad);
                accountProvider.editAccount(cuenta.getAccountNumber(), cuenta);
                Movement ingresoCajero = new Movement("Cajero", cuenta.getAccountNumber(), cantidad, "Ingreso desde cajero");
                movementsProvider.insertMovement(ingresoCajero);
                out.writeObject(EncryptionHelper.encryptMessage("info", clientPublicKey));
                out.writeObject(EncryptionHelper.encryptMessage("Operación realizada con éxito", clientPublicKey));
                break;
            case 5:
                out.writeObject(EncryptionHelper.encryptMessage("double#0#" + Double.MAX_VALUE, clientPublicKey));
                out.writeObject(EncryptionHelper.encryptMessage("Introduce la cantidad en euros a retirar: ", clientPublicKey));
                cantidad = Double.parseDouble(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));
                cuenta = accountProvider.getByAccountNumber(cuenta.getAccountNumber());
                cuenta.setBalance(cuenta.getBalance() - cantidad);
                if (cuenta.getBalance() < 0) {
                    out.writeObject(EncryptionHelper.encryptMessage("info", clientPublicKey));
                    out.writeObject(EncryptionHelper.encryptMessage("No puedes retirar más dinero del que tienes en la cuenta, abortando", clientPublicKey));
                }
                if (numeroSeguridadCorrecto()) {
                    accountProvider.editAccount(cuenta.getAccountNumber(), cuenta);
                    Movement retiradaCajero = new Movement(cuenta.getAccountNumber(), "Cajero", cantidad, "Retirada desde cajero");
                    movementsProvider.insertMovement(retiradaCajero);
                    out.writeObject(EncryptionHelper.encryptMessage("info", clientPublicKey));
                    out.writeObject(EncryptionHelper.encryptMessage("Operación realizada con éxito", clientPublicKey));
                } else {
                    out.writeObject(EncryptionHelper.encryptMessage("info", clientPublicKey));
                    out.writeObject(EncryptionHelper.encryptMessage("El número de seguridad es incorrecto", clientPublicKey));
                }
                break;
            default:
                break;
        }
    }

    /**
     * Envía un número aleatorio de 4 cifras y espera una respuesta del usuario, devuelve si el número enviado es igual al recibido
     * @return Booleano indicando si la validación fue correcta o no
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private boolean numeroSeguridadCorrecto() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, ClassNotFoundException {
        Random random = new Random();
        String numeroSeguridad = String.valueOf(random.nextInt(0, 9)) + String.valueOf(random.nextInt(0, 9)) + String.valueOf(random.nextInt(0, 9)) + String.valueOf(random.nextInt(0, 9));

        out.writeObject(EncryptionHelper.encryptMessage("info", clientPublicKey));
        out.writeObject(EncryptionHelper.encryptMessage("Tu número de confirmación es: " + numeroSeguridad, clientPublicKey));

        out.writeObject(EncryptionHelper.encryptMessage("integer#0#9999", clientPublicKey));
        out.writeObject(EncryptionHelper.encryptMessage("Introduce el número de seguridad enviado: ", clientPublicKey));
        String numeroRecibido = String.valueOf(Integer.parseInt(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey)));
        return numeroRecibido.equals(numeroSeguridad);
    }

    /**
     * Envía al usuario un menú con los movimientos de una cuenta, espera una respuesta del usuario indicando qué movimiento quiere ver en detalle o la opcion de salir
     * @param movimientos
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void verMovimientos(List<Movement> movimientos) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, ClassNotFoundException {
        String menu = "Selecciona un movimiento para ver sus detalles:";
        int x = 1;
        for (Movement movimiento : movimientos) {
            menu += "\n" + x + " - " + movimiento.getConcept();
            x++;
        }
        menu += "\n" + x + " - Volver";
        out.writeObject(EncryptionHelper.encryptMessage("menu#1#" + x, clientPublicKey));
        out.writeObject(EncryptionHelper.encryptMessage(menu, clientPublicKey));
        int opcion = Integer.parseInt(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));
        while (opcion != x) {
            out.writeObject(EncryptionHelper.encryptMessage("info", clientPublicKey));
            out.writeObject(EncryptionHelper.encryptMessage(movimientos.get(opcion - 1).toString(), clientPublicKey));

            out.writeObject(EncryptionHelper.encryptMessage("menu#1#" + x, clientPublicKey));
            out.writeObject(EncryptionHelper.encryptMessage(menu, clientPublicKey));
            opcion = Integer.parseInt(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));
        }

    }

    /**
     * Se comunica con el usuario para obtener los datos necesarios para crear una nueva cuenta bancaria asociada al mismo
     * @param usuario El usuario con el que se ha accedido a la aplicación
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void crearCuenta(User usuario) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, ClassNotFoundException {
        out.writeObject(EncryptionHelper.encryptMessage("string", clientPublicKey));
        out.writeObject(EncryptionHelper.encryptMessage("Introduce un nombre para la nueva cuenta", clientPublicKey));
        String nombre = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
        String accountNumber = accountProvider.getNewAccountNumber();
        Account cuenta = new Account(nombre, accountNumber, usuario.getDni(), 0.0);
        accountProvider.insertAccount(cuenta);
        out.writeObject(EncryptionHelper.encryptMessage("info", clientPublicKey));
        out.writeObject(EncryptionHelper.encryptMessage("Cuenta creada con éxito, su número de cuenta generado es: " + accountNumber, clientPublicKey));
        return;
    }

    /**
     * Dado un usuario, le ofrece al cliente una lista con sus cuentas para que elija una de ellas
     * para ser la cuenta que emite la transferencia, pide un número de cuenta de destino,
     * un concepto y una cantidad y si la validación de número aleatorio es correcta, realiza la transferencia
     * @param usuario
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void transferencia(User usuario) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, ClassNotFoundException {
        List<Account> cuentas = accountProvider.getAllByUser(usuario);

        if (cuentas.isEmpty()) {
            out.writeObject(EncryptionHelper.encryptMessage("menu#1#2", clientPublicKey));
            out.writeObject(EncryptionHelper.encryptMessage("No tienes ninguna cuenta todavía, sin una no puedes realizar transferencias\n1 - Crear una ahora\n2 - Volver", clientPublicKey));
            int opcion = Integer.parseInt(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));
            if (opcion == 1) {
                crearCuenta(usuario);
            }
        } else {
            String menu = "Elige una cuenta:";
            int x = 1;
            for (Account cuenta : cuentas) {
                menu += "\n" + x + " - " + cuenta.getAccountNumber();
                x++;
            }
            menu += "\n" + x + " - Volver";
            out.writeObject(EncryptionHelper.encryptMessage("menu#1#" + x, clientPublicKey));
            out.writeObject(EncryptionHelper.encryptMessage(menu, clientPublicKey));
            int opcion = Integer.parseInt(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));
            if (opcion == x) {
                return;
            }
            Account cuentaSeleccionada = cuentas.get(opcion - 1);
            out.writeObject(EncryptionHelper.encryptMessage("string", clientPublicKey));
            out.writeObject(EncryptionHelper.encryptMessage("Introduce el número de cuenta de la cuenta de destino", clientPublicKey));
            String destinationAccountNumber = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            Account cuentaDestino = accountProvider.getByAccountNumber(destinationAccountNumber);
            if (cuentaDestino == null) {
                out.writeObject(EncryptionHelper.encryptMessage("info", clientPublicKey));
                out.writeObject(EncryptionHelper.encryptMessage("No existe ninguna cuenta con ese número de cuenta", clientPublicKey));
                return;
            }
            out.writeObject(EncryptionHelper.encryptMessage("double#0#" + Double.MAX_VALUE, clientPublicKey));
            out.writeObject(EncryptionHelper.encryptMessage("Introduce la cantidad en euros a enviar: ", clientPublicKey));
            double cantidad = Double.parseDouble(EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey));
            cuentaSeleccionada = accountProvider.getByAccountNumber(cuentaSeleccionada.getAccountNumber());
            if (cuentaSeleccionada.getBalance() < cantidad) {
                out.writeObject(EncryptionHelper.encryptMessage("info", clientPublicKey));
                out.writeObject(EncryptionHelper.encryptMessage("No se puede realizar una transferencia de un importe mayor al saldo de la cuena de origen", clientPublicKey));
                return;
            }
            out.writeObject(EncryptionHelper.encryptMessage("string", clientPublicKey));
            out.writeObject(EncryptionHelper.encryptMessage("Introduce el concepto de la transferencia", clientPublicKey));
            String concepto = EncryptionHelper.decryptMessage((byte[]) in.readObject(), privateKey);
            if (numeroSeguridadCorrecto()) {
                cuentaSeleccionada = accountProvider.getByAccountNumber(cuentaSeleccionada.getAccountNumber());
                cuentaDestino = accountProvider.getByAccountNumber(cuentaDestino.getAccountNumber());
                cuentaSeleccionada.setBalance(cuentaSeleccionada.getBalance() - cantidad);
                cuentaDestino.setBalance(cuentaDestino.getBalance() + cantidad);
                accountProvider.editAccount(cuentaSeleccionada.getAccountNumber(), cuentaSeleccionada);
                accountProvider.editAccount(cuentaDestino.getAccountNumber(), cuentaDestino);
                Movement retiradaCajero = new Movement(cuentaSeleccionada.getAccountNumber(), cuentaDestino.getAccountNumber(), cantidad, concepto);
                movementsProvider.insertMovement(retiradaCajero);
                out.writeObject(EncryptionHelper.encryptMessage("info", clientPublicKey));
                out.writeObject(EncryptionHelper.encryptMessage("Operación realizada con éxito", clientPublicKey));
            }
        }
    }
}
