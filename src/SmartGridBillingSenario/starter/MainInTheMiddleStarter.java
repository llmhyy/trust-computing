package SmartGridBillingSenario.starter;

import SmartGridBillingSenario.attack.ManInTheMiddle;

/**
 * Created by ydai on 15/10/17.
 */
public class MainInTheMiddleStarter {

    public static void main(String[] args) {
        ManInTheMiddle mainInTheMiddle = new ManInTheMiddle("192.168.0.154", 3000);
        mainInTheMiddle.startCapture();
    }
}
