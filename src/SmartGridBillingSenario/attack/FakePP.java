package SmartGridBillingSenario.attack;

import SmartGridBillingSenario.message.Message;
import SmartGridBillingSenario.message.MessageType;
import SmartGridBillingSenario.socket.SocketClient;
import SmartGridBillingSenario.socket.SocketServer;
import SmartGridBillingSenario.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.pcap4j.packet.Packet;

import java.io.IOException;

import static SmartGridBillingSenario.utils.Utils.getTcpValue;

/**
 * Created by yuandai on 4/10/17.
 */
@Slf4j
public class FakePP extends Pcap4j {

    private String clientHost;
    private int serverPort;

    private FakePPClient FakePPClient;
    private FakePPServer FakePPServer;

    private String password;

    public FakePP(String clientHost, int serverPort) {
        super(clientHost);

        this.clientHost = clientHost;
        this.serverPort = serverPort;

        super.startCapture();
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
                } catch (IOException e) {
                    return;
                }

            }
        }
    }

    private void startAttack() {

    }


    private class FakePPClient extends SocketClient {

        public FakePPClient(String host, int port) {
            super(host, port);
        }
    }

    private class FakePPServer extends SocketServer {

        @Override
        public Message handleMessage(Message message) {
            return null;
        }

        public FakePPServer(int serverPort){
            super(serverPort);
        }
    }
}
