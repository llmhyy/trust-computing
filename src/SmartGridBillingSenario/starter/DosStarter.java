package SmartGridBillingSenario.starter;

import SmartGridBillingSenario.attack.Dos;

/**
 * Created by ydai on 18/10/17.
 */
public class DosStarter {

    public static void main(String[] args) {
        Dos dos = new Dos(3, "192.168.0.154", 3000);
        dos.startCapture();
    }
}
