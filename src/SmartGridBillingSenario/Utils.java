package SmartGridBillingSenario;


import javax.crypto.Cipher;
import java.io.*;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

import static sun.security.x509.CertificateAlgorithmId.ALGORITHM;

/**
 * Created by ydai on 24/9/17.
 */
public class Utils {


    public static final BigInteger pubExp = new BigInteger("010001", 16);

    /**
     * Encrypt the plain text using public key.
     *
     * @param text : original plain text
     * @param key  :The public key
     * @return Encrypted text
     * @throws java.lang.Exception
     */
    public static byte[] encrypt(String text, byte[] key) {
        byte[] cipherText = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            BigInteger modulus = new BigInteger(1,
                    key);
            RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(modulus, pubExp);
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(pubKeySpec);
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            cipherText = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    /**
     * Decrypt text using private key.
     *
     * @param text :encrypted text
     * @param key  :The private key
     * @return plain text
     * @throws java.lang.Exception
     */
    public static String decrypt(byte[] text, byte[] key) {
        byte[] dectyptedText = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            BigInteger modulus = new BigInteger(1,
                    key);
          //  RSAPrivateCrtKeySpec privateCrtKeySpec = new RSAPrivateCrtKeySpec()
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance(ALGORITHM);

            // decrypt the text using the private key
            // how to use given decrypt key??????
           // cipher.init(Cipher.DECRYPT_MODE, key);
            dectyptedText = cipher.doFinal(text);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new String(dectyptedText);
    }


    public static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
            try (ObjectOutputStream o = new ObjectOutputStream(b)) {
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream o = new ObjectInputStream(b)) {
                return o.readObject();
            }
        }
    }
}
