package SmartGridBillingSenario.attack;

import SmartGridBillingSenario.message.Message;
import SmartGridBillingSenario.socket.SocketClient;
import SmartGridBillingSenario.socket.SocketServer;

/**
 * Created by yuandai on 28/9/17.
 */
public class ManInTheMiddle {

    private Message message;

    private String clientHost;
    private int clientPort;

    private MiddleManClient middleManClient;
    private MiddleManServer middleManServer;



    public ManInTheMiddle(String clientHost, int clientPort, int serverPort) {

        middleManClient = new MiddleManClient(clientHost, clientPort);
        middleManServer = new MiddleManServer(serverPort);
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
}
