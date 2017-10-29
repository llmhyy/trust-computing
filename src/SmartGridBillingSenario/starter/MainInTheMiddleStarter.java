package SmartGridBillingSenario.starter;

import SmartGridBillingSenario.attack.ManInTheMiddle;
import SmartGridBillingSenario.utils.PropertyReader;

/**
 * Created by ydai on 15/10/17.
 */
public class MainInTheMiddleStarter {

    public static void main(String[] args) {

        String treIp = PropertyReader.getProperty("tre.ip");
        String trePort = PropertyReader.getProperty("tre.port");
        ManInTheMiddle mainInTheMiddle = new ManInTheMiddle(treIp, Integer.valueOf(trePort));
        mainInTheMiddle.startCapture();
    }
}
