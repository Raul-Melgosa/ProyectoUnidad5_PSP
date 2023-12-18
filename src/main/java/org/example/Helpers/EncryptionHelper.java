package org.example.Helpers;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

public class EncryptionHelper {
    /**
     * Genera un par de claves (Pública + Privda) siguiendo el algoritmo RSA
     * @return Par de claves
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
        return keygen.generateKeyPair();
    }

    /**
     * Encripta un String utilizando una clave dada usando el algoritmo RSA
     * @param message String a encriptar
     * @param key La clave a utilizar
     * @return byte[] El String encriptado
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static byte[] encryptMessage(String message, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.ENCRYPT_MODE, key);
        return rsaCipher.doFinal(message.getBytes());
    }

    /**
     * Desencripta un String utilizando una clave dada usando el algoritmo RSA
     * @param encryptedMessage byte[] a encriptar
     * @param key La clave a utilizar
     * @return String El String desencriptado
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static String decryptMessage(byte[] encryptedMessage, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.DECRYPT_MODE, key);
        return new String(rsaCipher.doFinal(encryptedMessage));
    }

    /**
     * Hashea un String dado mediante el algoritmo SHA
     * @param message String el string a hashear
     * @return byte[] el mensaje hasheado
     * @throws NoSuchAlgorithmException
     */
    public static byte[] hash(String message) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA");
        byte dataBytes[] = message.getBytes();
        md.update(dataBytes);//TEXTO A RESUMIR
        return md.digest();
    }

    /**
     * Compara dos hashes y devuelve si son iguales o no
     * @param hash1 byte[] primer hash a comparar
     * @param hash2 byte[] segundo hash a comparar
     * @return boolean Indicando si son iguales
     */
    public static boolean compareHashes(byte[] hash1, byte[] hash2) {
        return MessageDigest.isEqual(hash1, hash2);
    }

    /**
     * Genera una firma digital a través de un String y una clave privada
     * @param message String a firmar digitalmente
     * @param privateKey PrivateKey a utilizar en la firma
     * @return byte[] firma digital
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static byte[] signString(String message, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature firmaDigital = Signature.getInstance("SHA1WITHRSA");
        firmaDigital.initSign(privateKey);
        firmaDigital.update(message.getBytes());
        return firmaDigital.sign();
    }

    /**
     * Comprueba la veracidad de una firma digital dada la firma, el String y la clave pública
     * @param message String mensaje en plano
     * @param signature byte[] Firma digital
     * @param publicKey PublicKey Par de la clave privada utilizada para firmar digitalmente el documento
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     */
    public static boolean checkSignatureValid(String message, byte[] signature, PublicKey publicKey) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        Signature tester = Signature.getInstance("SHA1WITHRSA");
        tester.initVerify(publicKey);
        tester.update(message.getBytes());
        return tester.verify(signature);
    }
}
