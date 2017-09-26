package SmartGridBillingSenario.socket;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public abstract class SocketServer {

    protected int port;
    private ServerSocket ss = null;

    public void runServer() {

        try {
            ss = new ServerSocket(port);
            log.info("Receive Connection established");
            while (true) {
                try {
                    Socket clientSocket = ss.accept();
                    ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream());
                    Message m = (Message) is.readObject();
                    Message returnMessage = handleMessage(m);
                    os.writeObject(returnMessage);
                    clientSocket.close();
                } catch (ClassNotFoundException ex) {
                    log.error("Message Not Found {}", ex);
                }
            }
        } catch (IOException ex) {
            log.error("IO Exception {}", ex);
        }

    }

    public abstract Message handleMessage(Message message);

    public SocketServer(int port) {
        this.port = port;
        runServer();
    }
}
