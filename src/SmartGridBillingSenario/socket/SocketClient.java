package SmartGridBillingSenario.socket;


import SmartGridBillingSenario.message.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private String serverHost;
    private int serverPort;

    protected int clientPort;
    private Socket client;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public SocketClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        try {
            client = new Socket(serverHost, serverPort);
            OutputStream outToServer = client.getOutputStream();

            out = new ObjectOutputStream(outToServer);

            InputStream inFromServer = client.getInputStream();
            in = new ObjectInputStream(inFromServer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Message sendToPort(String message) {
        OutputStreamWriter osw;
        try {
            log.info("Connecting to " + serverHost + " on port " + serverPort);
            out.writeObject(message);

            String returnMessageString = (String) in.readObject();
            ObjectMapper mapper = new ObjectMapper();
            Message returnMessage = mapper.readValue(returnMessageString, Message.class);
            log.info("SocketServer return {}", returnMessage);
            //client.close();
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
