package SmartGridBillingSenario.starter;

import SmartGridBillingSenario.attack.FakePP;

/**
 * Created by ydai on 15/10/17.
 */
public class FakePPStarter {

    public static void main(String[] args) {
        FakePP fakePP = new FakePP("192.168.0.154", 3000);
        fakePP.startCapture();
    }
}
