package SmartGridBillingSenario;


import SmartGridBillingSenario.message.AuthenticationMessage;
import SmartGridBillingSenario.message.Message;
import SmartGridBillingSenario.message.MessageType;
import SmartGridBillingSenario.message.QuoteAndRateResponseMessage;
import SmartGridBillingSenario.socket.SocketClient;
import SmartGridBillingSenario.socket.SocketServer;
import SmartGridBillingSenario.utils.Senario;
import SmartGridBillingSenario.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created by ydai on 24/9/17.
 */
@Slf4j
public class PP extends SocketClient {

    private transient byte[] publicInfo;

    private Senario senario;

    //Quote for TRE TPM
    private String quote = "AHv/VENHgBgAIgALMpmLnQLsp8pN1U2PmypMQb9E";

    //authentication for PP
    private String userName = "pp";
    private String identity = "password";

    public Thread ppServerThread;

    public PP(String host, int port, Senario senario) {
        super(host, port);
        this.senario = senario;


        ppServerThread = new Thread() {
            public void run() {
                SocketServer ppSocketServer = new PpSocketServer(clientPort);
                ppSocketServer.runServer();
            }
        };

        ppServerThread.start();
    }

    private boolean getPublicKey(String token) throws IOException {
        log.info("Get Public Key!!");
        ObjectMapper mapper = new ObjectMapper();
        AuthenticationMessage authenticationMessage = new AuthenticationMessage(token, userName, identity);
        String jsonInString = mapper.writeValueAsString(new Message(MessageType.ATTESTATION_REQUEST, authenticationMessage));

        //Assign connection Timeout for 5s for response
        long startTime = System.currentTimeMillis();
        Message responseForPublicKey = sendToPort(jsonInString);
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= 1000) {
            log.info("Face DDOS attack. connection timeout, close connection");
            return false;
        }

        if (publicInfo == null) {
            setPublicInfo(Base64.decodeBase64(String.valueOf(responseForPublicKey.getObject())));
            return true;
        } else {
            return false;
        }
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
            String token = "";
            if (!senario.equals(Senario.fakePPSenario)) {
                token = getToken(userName);
            }

            if (senario.equals(Senario.ddosTreSenario)) {
                client.close();
                try {
                    Thread.sleep(5000);
                } catch (Exception ex) {

                }
                client = new Socket(serverHost, serverPort);
                clientPort = client.getLocalPort();
                OutputStream outToServer = client.getOutputStream();

                out = new ObjectOutputStream(outToServer);

                InputStream inFromServer = client.getInputStream();
                in = new ObjectInputStream(inFromServer);
            }
            boolean stepResult = getPublicKey(token);
            if (stepResult) {
                String query = "Mike";
                log.info("Start Encryption ");
                String value = setEncryptQueryAndGetPrice(query);
                return value;
            } else {
                return null;
            }

        } catch (NoSuchAlgorithmException | IOException | ClassNotFoundException e) {
            log.error("Error when Encryption", e);
            return null;
        }
    }

    private String getToken(String userName) throws IOException {
        log.info("Get Token!!");
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(new Message(MessageType.GET_TOKEN, userName));
        Message responseForToken = sendToPort(jsonInString);
        return String.valueOf(responseForToken.getObject());
    }


    private class PpSocketServer extends SocketServer {

        @Override
        public Message handleMessage(Message message) {
            MessageType messageType = message.getMessageType();
            switch (messageType) {
                case RESPONSE_FROM_TRE_ATTESTATION_REQUEST:
                    if (publicInfo == null) {
                        setPublicInfo(Base64.decodeBase64(String.valueOf(message.getObject())));
                        String query = "Mike";
                        log.info("Start Encryption ");
                        try {
                            String encryptedQuery = Utils.encrypt(query, publicInfo);
                            log.info("Send Encrypted value to TRE");
                            ObjectMapper mapper = new ObjectMapper();
                            String jsonInString = mapper.writeValueAsString(new Message(MessageType.GET_PRICE, encryptedQuery));
                            return Utils.stringToMessage(jsonInString);
                        } catch (IOException e) {
                            log.error("{}", e);
                        }
                    } else {
                        return null;
                    }
                    break;
                case RESPONSE_FROM_TRE_GET_PRICE:
                    Map<String, Object> result = (Map<String, Object>) message.getObject();
                    QuoteAndRateResponseMessage quoteAndRateResponseMessage = new QuoteAndRateResponseMessage(String.valueOf(result.get("quote")), Integer.valueOf(String.valueOf(result.get("rateValue"))));
                    log.info("Great!! Rate Value for user: {}", quoteAndRateResponseMessage.getRateValue());
                    return null;
            }

            return null;
        }

        public PpSocketServer(int serverPort) {
            super(serverPort);
        }
    }

    public synchronized void setPublicInfo(byte[] publicInfo) {
        this.publicInfo = publicInfo;
    }

}

