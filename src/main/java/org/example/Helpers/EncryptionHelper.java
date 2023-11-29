package org.example.Helpers;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

public class EncryptionHelper {
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
        return keygen.generateKeyPair();
    }

    public static byte[] encryptMessage(String message, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.ENCRYPT_MODE, key);
        return rsaCipher.doFinal(message.getBytes());
    }

    public static String decryptMessage(byte[] encryptedMessage, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.DECRYPT_MODE, key);
        return new String(rsaCipher.doFinal(encryptedMessage));
    }

    public static byte[] hash(String message) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA");
        byte dataBytes[] = message.getBytes();
        md.update(dataBytes);//TEXTO A RESUMIR
        return md.digest();
    }

    public static boolean compareHashes(byte[] hash1, byte[] hash2) {
        return MessageDigest.isEqual(hash1, hash2);
    }

    public static byte[] signString(String message, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature firmaDigital = Signature.getInstance("SHA1WITHRSA");
        firmaDigital.initSign(privateKey);
        firmaDigital.update(message.getBytes());
        return firmaDigital.sign();
    }

    public static boolean checkSignatureValid(String message, byte[] signature, PublicKey publicKey) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        Signature tester = Signature.getInstance("SHA1WITHRSA");
        tester.initVerify(publicKey);
        tester.update(message.getBytes());
        return tester.verify(signature);
    }
}
