package SmartGridBillingSenario.starter;

import SmartGridBillingSenario.attack.FakePP;
import SmartGridBillingSenario.utils.PropertyReader;

/**
 * Created by ydai on 15/10/17.
 */
public class FakePPStarter {

    public static void main(String[] args) {
        String treIp = PropertyReader.getProperty("tre.ip");
        String trePort = PropertyReader.getProperty("tre.port");
        FakePP fakePP = new FakePP(treIp, Integer.valueOf(trePort));
        fakePP.startCapture();
    }
}
