package SmartGridBillingSenario.socket;


import SmartGridBillingSenario.message.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;

/**
 * Created by ydai on 24/9/17.
 */
@Slf4j
@AllArgsConstructor
public class SocketClient {

    protected String serverHost;
    protected int serverPort;

    protected int clientPort;
    @Getter
    protected Socket client;
    protected ObjectOutputStream out;
    protected ObjectInputStream in;

    public SocketClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        try {
            client = new Socket(serverHost, serverPort);
            clientPort = client.getLocalPort();
            
            log.info("port for client: {}", clientPort);
            
            OutputStream outToServer = client.getOutputStream();

            out = new ObjectOutputStream(outToServer);

            InputStream inFromServer = client.getInputStream();
            in = new ObjectInputStream(inFromServer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Message sendToPort(String message) throws IOException{
        @SuppressWarnings("unused")
		OutputStreamWriter osw;
        try {
            log.info("Connecting to " + serverHost + " on port " + serverPort);
            out.writeObject(message);

            String returnMessageString = (String) in.readObject();
            ObjectMapper mapper = new ObjectMapper();
            Message returnMessage = mapper.readValue(returnMessageString, Message.class);
            return returnMessage;
        } catch (ClassNotFoundException e) {
            log.error("Parse Class Not Found Example: {}", e);
            return null;
        }
    }
}
