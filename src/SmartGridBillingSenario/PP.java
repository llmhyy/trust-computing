package SmartGridBillingSenario;


import SmartGridBillingSenario.message.Message;
import SmartGridBillingSenario.message.MessageType;
import SmartGridBillingSenario.message.QuoteAndRateResponseMessage;
import SmartGridBillingSenario.socket.SocketClient;
import SmartGridBillingSenario.utils.Senario;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created by ydai on 24/9/17.
 */
@Slf4j
public class PP extends SocketClient {

    private transient byte[] publicInfo;

    private Senario senario;

    private String quote = "AHv/VENHgBgAIgALZhEyX2VzFWDlx62jA2VVx5Ri";

    public PP(String host, int port, Senario senario) {
        super(host, port);
        this.senario = senario;
    }

    private void getPublicKey() throws JsonProcessingException {
        log.info("Get Public Key!!");
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(new Message(MessageType.ATTESTATION_REQUEST, ""));
        Message responseForPublicKey = sendToPort(jsonInString);
        publicInfo = Base64.decodeBase64(String.valueOf(responseForPublicKey.getObject()));
    }

    private String setEncryptQueryAndGetPrice(String query) throws NoSuchAlgorithmException, IOException, ClassNotFoundException {

        if (StringUtils.isNotEmpty(query)) {
            // String encryptedQuery = Utils.encrypt(query, publicInfo.authPolicy);
            String encryptedQuery = query;
            log.info("Send Encrypted value to TRE");
            ObjectMapper mapper = new ObjectMapper();
            String jsonInString = mapper.writeValueAsString(new Message(MessageType.GET_PRICE, encryptedQuery));
            Map<String, Object> result = (Map<String, Object>) sendToPort(jsonInString).getObject();
            QuoteAndRateResponseMessage quoteAndRateResponseMessage = new QuoteAndRateResponseMessage(String.valueOf(result.get("quote")), Integer.valueOf(String.valueOf(result.get("rateValue"))));
            String receivedQuote = quoteAndRateResponseMessage.getQuote();

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
        try {
            getPublicKey();
            String query = "Mike";
            log.info("Start Encryption ");
            return setEncryptQueryAndGetPrice(query);
        } catch (NoSuchAlgorithmException | IOException | ClassNotFoundException e) {
            log.error("Error when Encryption", e);
            return null;
        }
    }
}

