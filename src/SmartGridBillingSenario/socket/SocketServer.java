package SmartGridBillingSenario.socket;

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

    @java.beans.ConstructorProperties({"port", "ss"})
    public SocketServer(int port, ServerSocket ss) {
        this.port = port;
        this.ss = ss;
    }

    public void runServer() {

        try {
            ss = new ServerSocket(port);
            log.info("Receive Connection established, with Port = " + port);
            try {
                Socket clientSocket = ss.accept();
                ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream());
                Message m = (Message) is.readObject();
                Message returnMessage = handleMessage(m);
                os.writeObject(returnMessage);
                clientSocket.close();

            } catch (Exception ex) {
                log.error("Message Not Found {}", ex);
            }

        } catch (IOException ex) {
            log.error("IO Exception {}", ex);
        }

    }

    public abstract Message handleMessage(Message message);

    public SocketServer(int port) {
        this.port = port;
    }
}
