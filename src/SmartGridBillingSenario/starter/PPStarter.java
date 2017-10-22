package SmartGridBillingSenario.starter;

import SmartGridBillingSenario.PP;
import SmartGridBillingSenario.utils.Senario;

/**
 * Created by yuandai on 28/9/17.
 */
public class PPStarter {

    public static void main(String[] args) {

        Senario senario = Senario.currentSenario;

        PP pp = new PP("192.168.0.154", 3000, senario);
        String response = pp.smartGridBillWorkFlow();
        // finish workflow
        if (response != null) {
            pp.ppServerThread.interrupt();
        }
    }
}
