package SmartGridBillingSenario.utils;


import SmartGridBillingSenario.message.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Created by ydai on 24/9/17.
 */
@Slf4j
public class Utils {


    public static final BigInteger pubExp = new BigInteger("010001", 16);


    private static final KeyPair keyPair = buildKeyPair();

    /**
     * Encrypt the plain text using public key.
     *
     * @param text : original plain text
     * @param key  :The public key
     * @return Encrypted text
     * @throws java.lang.Exception
     */
    public static String encrypt(String text, byte[] key) {
        byte[] cipherText = null;
        try {
            PrivateKey privateKey =
                    KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(key));
            // get an RSA cipher object and print the provider
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            // encrypt the plain text using the public key
            cipherText = cipher.doFinal(text.getBytes("UTF-8"));
        } catch (Exception e) {
            log.error("{}", e);
        }
        return Base64.encodeBase64String(cipherText);
    }

    /**
     * Decrypt text using private key.
     *
     * @param msg :encrypted text
     * @param key :The private key
     * @return plain text
     * @throws java.lang.Exception
     */
    public static String decrypt(String msg, byte[] key) throws BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            BigInteger modulus = new BigInteger(1,
                    key);
            //  RSAPrivateCrtKeySpec privateCrtKeySpec = new RSAPrivateCrtKeySpec()
            // get an RSA cipher object and print the provider
            // decrypt the text using the private key
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPublic());
            return new String(cipher.doFinal(Base64.decodeBase64(msg)), "UTF-8");
    }


    public static KeyPair buildKeyPair() {

        if (keyPair != null) {
            return keyPair;
        }
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

    public static String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }
        return sb.toString();
    }

    public static String getTcpValue(byte[] rawData) {

        String hexValue = Hex.encodeHexString(rawData);
        String value = Utils.convertHexToString(hexValue).substring(3);
        return value;
    }

    public static Message stringToMessage(String value) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(value, Message.class);
    }

    public static String messageToString(Message message) throws JsonProcessingException {
        if (message == null) {
            return "";
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(message);
    }

    public static byte[] shaHashing(byte[] input, byte[] currentQuote) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(input);
        byte byteData[] = md.digest();
        return ArrayUtils.addAll(currentQuote, byteData);
    }

    public static byte[] getMethodQuoteCode(Class clazz, String methodName) throws NoSuchMethodException {
        log.info("Get Code for clazz {}, method: {}", clazz.getName(), methodName);
        return clazz.getMethod(methodName, null).getAnnotation(MethodQuoteCode.class).code().getBytes();
    }
}
