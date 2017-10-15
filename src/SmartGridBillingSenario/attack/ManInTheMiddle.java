package SmartGridBillingSenario.attack;

import SmartGridBillingSenario.message.Message;
import SmartGridBillingSenario.socket.SocketClient;
import SmartGridBillingSenario.socket.SocketServer;
import lombok.extern.slf4j.Slf4j;
import org.pcap4j.packet.Packet;

/**
 * Created by yuandai on 28/9/17.
 */
@Slf4j
public class ManInTheMiddle extends Pcap4j {


    private String host;
    private int serverPort;

    private MiddleManClient middleManClient;
    private MiddleManServer middleManServer;



    public ManInTheMiddle(String host, int serverPort) {
        super(host);
        this.host = host;
        this.serverPort = serverPort;
        super.startCapture();
    }

    private class MiddleManClient extends SocketClient {

        public MiddleManClient(String host, int port) {
            super(host, port);
        }
    }

    private class MiddleManServer extends SocketServer {

        @Override
        public Message handleMessage(Message message) {
            return null;
        }

        public MiddleManServer(int serverPort){
            super(serverPort);
        }
    }

    @Override
    public void handleTcpData(String srcAddr, String dstAddr, String srcPort, String dstPort, Packet payload) {
    }

}

