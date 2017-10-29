package SmartGridBillingSenario.starter;

import SmartGridBillingSenario.PP;
import SmartGridBillingSenario.utils.PropertyReader;
import SmartGridBillingSenario.utils.Scenario;

/**
 * Created by yuandai on 28/9/17.
 */
public class PPStarter {

    public static void main(String[] args) {

        Scenario senario = Scenario.currentSenario;

        String treIp = PropertyReader.getProperty("tre.ip");
        String trePort = PropertyReader.getProperty("tre.port");

        PP pp = new PP(treIp, Integer.valueOf(trePort), senario);
        String response = pp.smartGridBillWorkFlow();
        // finish workflow
        if (response != null) {
            pp.ppServerThread.interrupt();
        }
    }
}
