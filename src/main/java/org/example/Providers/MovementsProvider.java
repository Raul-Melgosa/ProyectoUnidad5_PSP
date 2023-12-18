package org.example.Providers;

import org.example.Models.Account;
import org.example.Models.Movement;
import org.example.Models.User;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class MovementsProvider {
    /**
     * Recoge todos los objetos Movement que haya en el archivo de almacenamiento de Movimientos
     * @return Lista de objetos Movement
     */
    public synchronized List<Movement> getAll() {
        List<Movement> movements = new LinkedList<>();
        try {
            File fichero = new File("./Storage/movements.dat");
            if(!fichero.exists()) {
                fichero.createNewFile();
                return movements;
            }
            try {
                FileInputStream filein = new FileInputStream(fichero);
                try {
                    ObjectInputStream dataIS = new ObjectInputStream(filein);
                    while(true) {
                        movements.add((Movement) dataIS.readObject());
                    }
                } catch(EOFException e) {
                    filein.close();
                    return movements;
                } catch (ClassNotFoundException e) {
                    System.out.println("Se ha encontrado un objeto que no es del tipo movement en el fichero de movements");
                }
            } catch(FileNotFoundException e) {
                fichero.createNewFile();
                return getAll();
            }
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Se ha producido un error inesperado");
        }
        return movements;
    }

    /**
     * Escribe al archivo de almacenamiento de movimientos todos los movimientos de una lista dada, SOBREESCRIBE el archivo completo
     * @param movements Lista de movimientos a insertar
     * @return
     */
    private synchronized boolean writeToFile(List<Movement> movements) {
        boolean error = false;
        try {
            File fichero = new File("./Storage/movements.dat");
            if(!fichero.exists()) {
                fichero.createNewFile();
            }
            FileOutputStream fileout = new FileOutputStream(fichero);
            ObjectOutputStream dataOS = new ObjectOutputStream(fileout);
            for (Movement m:movements) {
                dataOS.writeObject(m);
            }
            dataOS.close();
        } catch (IOException e) {
            System.out.println("Se ha producido un error inesperado");
            error = true;
        }
        return error;
    }

    /**
     * Devuelve todos los movimientos asociados a una cuenta dada
     * @param account La cuenta de la que se quieren obtener los movimientos
     * @return Lista de movimientos
     */
    public List<Movement> getAllByAccount(Account account) {
        List<Movement> movements = this.getAll();
        List<Movement> accountMovements = new LinkedList<>();
        for (Movement movement:movements) {
            if(movement.getOriginAccountNumber().equals(account.getAccountNumber()) || movement.getDestinationAccountNumber().equals(account.getAccountNumber())) {
                accountMovements.add(movement);
            }
        }
        return accountMovements;
    }

    /**
     * Devuelve todos los movimientos en los que una cuenta dada es la cuenta de destino
     * @param account La cuenta de la que se quieren obtener los movimientos
     * @return Lista de movimientos
     */
    public List<Movement> getAllReceivedByAccount(Account account) {
        List<Movement> movements = this.getAll();
        List<Movement> accountMovements = new LinkedList<>();
        for (Movement movement:movements) {
            if(movement.getDestinationAccountNumber().equals(account.getAccountNumber())) {
                accountMovements.add(movement);
            }
        }
        return accountMovements;
    }

    /**
     * Devuelve todos los movimientos en los que uno cuenta dada es la cuenta de origen
     * @param account La cuenta de la que se quieren obtener los movimientos
     * @return Lista de movimientos
     */
    public List<Movement> getAllSentByAccount(Account account) {
        List<Movement> movements = this.getAll();
        List<Movement> accountMovements = new LinkedList<>();
        for (Movement movement:movements) {
            if(movement.getOriginAccountNumber().equals(account.getAccountNumber())) {
                accountMovements.add(movement);
            }
        }
        return accountMovements;
    }

    /**
     * Inserta un nuevo movimiento
     * @param movement El movimiento a insertar
     */
    public synchronized void insertMovement(Movement movement) {
        LinkedList<Movement> movements = (LinkedList<Movement>) this.getAll();
        movements.add(movement);
        writeToFile(movements);
    }
}
