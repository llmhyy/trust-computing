package SmartGridBillingSenario.attack;

import SmartGridBillingSenario.socket.Message;
import SmartGridBillingSenario.socket.SocketClient;
import SmartGridBillingSenario.socket.SocketServer;

/**
 * Created by yuandai on 4/10/17.
 */
public class FakePP {

    private Message message;

    private String clientHost;
    private int clientPort;

    private FakePPClient FakePPClient;
    private FakePPServer FakePPServer;



    public FakePP(String clientHost, int clientPort, int serverPort) {

        FakePPClient = new FakePPClient(clientHost, clientPort);
        FakePPServer = new FakePPServer(serverPort);
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
