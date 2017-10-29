package SmartGridBillingSenario.starter;

import SmartGridBillingSenario.attack.Dos;
import SmartGridBillingSenario.utils.PropertyReader;

/**
 * Created by ydai on 18/10/17.
 */
public class DosStarter {

    public static void main(String[] args) {

        String treIp = PropertyReader.getProperty("tre.ip");
        String trePort = PropertyReader.getProperty("tre.port");

        String threadSize = PropertyReader.getProperty("ddos.thread.number");
        Dos dos = new Dos(Integer.valueOf(threadSize), treIp, Integer.valueOf(trePort));
        dos.startCapture();
    }
}
