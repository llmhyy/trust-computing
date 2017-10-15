package SmartGridBillingSenario.socket;

import SmartGridBillingSenario.message.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ydai on 24/9/17.
 */
@Slf4j
public abstract class SocketServer {

    protected int port;
    private ServerSocket ss = null;

    public void runServer() {
        try {
            ss = new ServerSocket(port);
        } catch (IOException ex) {
            log.error("IO Exception {}", ex);
        }
        while (true) {
            try {
                log.info("Receive Connection established, with Port = " + port);
                Socket clientSocket = ss.accept();
                new SocketServerThread(clientSocket).start();
            } catch (IOException ex) {
                log.error("IO Exception {}", ex);
            }
        }
    }


    public abstract Message handleMessage(Message message);

    public SocketServer(int port) {
        this.port = port;
    }


    private class SocketServerThread extends Thread {
        protected Socket socket;

        private ObjectOutputStream os;
        private ObjectInputStream is;

        public SocketServerThread(Socket clientSocket) throws IOException {
            this.socket = clientSocket;
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());

        }

        public void run() {
            while (true) {
                try {
                    // SocketAddress socketAddress = ss.accept().getRemoteSocketAddress();
                    //  log.info("listening on port: {}" , ((InetSocketAddress) socketAddress).getPort());
                    String valueString = "";
                    try {
                        valueString = (String) is.readObject();
                    } catch (Exception ex) {
                        continue;
                    }


                    ObjectMapper mapper = new ObjectMapper();
                    Message m = mapper.readValue(valueString, Message.class);

                    Message returnMessage = handleMessage(m);

                    if (returnMessage != null) {
                        os.writeObject(mapper.writeValueAsString(returnMessage));
                    }

                } catch (Exception ex) {
                    log.error("Message Not Found {}", ex);
                }
            }

        }
    }
}
