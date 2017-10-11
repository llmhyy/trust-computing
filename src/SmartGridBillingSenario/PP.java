package SmartGridBillingSenario;


import SmartGridBillingSenario.message.Message;
import SmartGridBillingSenario.message.MessageType;
import SmartGridBillingSenario.message.QuoteAndRateResponseMessage;
import SmartGridBillingSenario.socket.SocketClient;
import SmartGridBillingSenario.utils.Senario;
import SmartGridBillingSenario.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import tss.tpm.TPMT_PUBLIC;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ydai on 24/9/17.
 */
@Slf4j
public class PP extends SocketClient {

    private transient TPMT_PUBLIC publicInfo;

    private Senario senario;

    private String quote = "AHv/VENHgBgAIgALZhEyX2VzFWDlx62jA2VVx5Ri";
    public PP(String host, int port, Senario senario) {
        super(host, port);
        this.senario = senario;
    }

    private void getPublicKey() {
        log.info("Get Public Key!!");
        Message responseForPublicKey = sendToPort(new Message(MessageType.ATTESTATION_REQUEST, null));
        publicInfo = (TPMT_PUBLIC) responseForPublicKey.getObject();
    }

    private String setEncryptQueryAndGetPrice(String query) throws NoSuchAlgorithmException, IOException, ClassNotFoundException {

        if (StringUtils.isNotEmpty(query)) {
           // String encryptedQuery = Utils.encrypt(query, publicInfo.authPolicy);
            String encryptedQuery = query;
            log.info("Send Encrypted value to TRE");
            QuoteAndRateResponseMessage quoteAndRateResponseMessage = (QuoteAndRateResponseMessage) sendToPort(new Message(MessageType.GET_PRICE, encryptedQuery)).getObject();

            String receivedQuote = Base64.encodeBase64String(quoteAndRateResponseMessage.getQuote());

            if (receivedQuote.equals(quote)) {
                log.info("Great!! Rate Value for user: {} is {}", query, quoteAndRateResponseMessage.getRateValue());
                return String.valueOf(quoteAndRateResponseMessage.getRateValue());
            } else {
                log.error("Error!! Quote Not match!!!");
                return null;
            }
        } else {
            return null;
        }
    }

    //TODO: (JVM + Calculator) ByteCode;
    //TODO: Case 1: Execute but return wrong value to PP
    //TODO: Spoof attack and week2

    //Hash: Java /
    public String smartGridBillWorkFlow() {
        getPublicKey();
        String query = "Mike";
        try {
            log.info("Start Encryption ");
            return setEncryptQueryAndGetPrice(query);
        } catch (NoSuchAlgorithmException | IOException | ClassNotFoundException e) {
            log.error("Error when Encryption", e);
            return null;
        }
    }
}
