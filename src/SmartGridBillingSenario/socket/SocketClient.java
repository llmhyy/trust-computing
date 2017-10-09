package SmartGridBillingSenario.socket;


import SmartGridBillingSenario.message.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;

/**
 * Created by ydai on 24/9/17.
 */
@Slf4j
@AllArgsConstructor
public class SocketClient {

    private String host;
    private int port;

    public Message sendToPort(Message message) {
        OutputStreamWriter osw;
        try {
            log.info("Connecting to " + host + " on port " + port);
            Socket client = new Socket(host, port);

            log.info("Just connected to " + client.getRemoteSocketAddress());
            client.getLocalPort();
            OutputStream outToServer = client.getOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(outToServer);

            out.writeObject(message);
            InputStream inFromServer = client.getInputStream();
            ObjectInputStream in = new ObjectInputStream(inFromServer);

            Message returnMessage = (Message) in.readObject();

            log.info("SocketServer return {}", returnMessage);
            client.close();
            return returnMessage;
        } catch (IOException e) {
            log.error("IO Exception: {}", e);
            return null;
        } catch (ClassNotFoundException e) {
            log.error("Parse Class Not Found Example: {}", e);
            return null;
        }
    }
}
