package SmartGridBillingSenario;


import SmartGridBillingSenario.message.Message;
import SmartGridBillingSenario.message.MessageType;
import SmartGridBillingSenario.message.QuoteAndRateResponseMessage;
import SmartGridBillingSenario.socket.SocketClient;
import SmartGridBillingSenario.utils.Senario;
import SmartGridBillingSenario.utils.Utils;
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

    private String quote = "AHv/VENHgBgAIgALMpmLnQLsp8pN1U2PmypMQb9E";

    private String identity = "password";

    public PP(String host, int port, Senario senario) {
        super(host, port);
        this.senario = senario;
    }

    private void getPublicKey() throws JsonProcessingException {
        log.info("Get Public Key!!");
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(new Message(MessageType.ATTESTATION_REQUEST, identity));
        Message responseForPublicKey = sendToPort(jsonInString);
        publicInfo = Base64.decodeBase64(String.valueOf(responseForPublicKey.getObject()));
    }

    private String setEncryptQueryAndGetPrice(String query) throws NoSuchAlgorithmException, IOException, ClassNotFoundException {

        if (StringUtils.isNotEmpty(query)) {
            String encryptedQuery = Utils.encrypt(query, publicInfo);
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

    public String smartGridBillWorkFlow() {
        try {
            getPublicKey();
            //      Thread.sleep(100000);
            String query = "Mike";
            log.info("Start Encryption ");
            String value = setEncryptQueryAndGetPrice(query);
            Thread.sleep(100000);
            return value;
        } catch (NoSuchAlgorithmException | IOException | ClassNotFoundException e) {
            log.error("Error when Encryption", e);
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}

