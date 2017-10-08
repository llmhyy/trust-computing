package SmartGridBillingSenario;


import org.apache.commons.io.IOUtils;

import javax.crypto.Cipher;
import java.io.*;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

/**
 * Created by ydai on 24/9/17.
 */
public class Utils {


    public static final BigInteger pubExp = new BigInteger("010001", 16);


    public static final KeyPair keyPair = buildKeyPair();
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
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
            // encrypt the plain text using the public key
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
        byte[] dectyptedText = text;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            BigInteger modulus = new BigInteger(1,
                    key);
          //  RSAPrivateCrtKeySpec privateCrtKeySpec = new RSAPrivateCrtKeySpec()
            // get an RSA cipher object and print the provider
            // decrypt the text using the private key
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPublic());
            dectyptedText = text;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new String(dectyptedText);
    }


    public static KeyPair buildKeyPair(){
        try {
            final int keySize = 2048;
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keySize);
            return keyPairGenerator.genKeyPair();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

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

    public static byte[] getByteArrayOfClass(Class<?> c) {
        String className = c.getName();
        String classAsPath = className.replace('.', '/') + ".class";
        InputStream stream = c.getClassLoader().getResourceAsStream(classAsPath);
        try {
            return IOUtils.toByteArray(stream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
