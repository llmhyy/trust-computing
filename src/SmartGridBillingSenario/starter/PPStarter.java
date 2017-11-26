package SmartGridBillingSenario.starter;

import SmartGridBillingSenario.PP;
import SmartGridBillingSenario.utils.PropertyReader;
import SmartGridBillingSenario.utils.Scenario;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by yuandai on 28/9/17.
 */
@Slf4j
public class PPStarter {

    public static void main(String[] args) throws NoSuchMethodException {

        Scenario scenario = Scenario.currentScenario;

        String treIp = PropertyReader.getProperty("tre.ip");
        String trePort = PropertyReader.getProperty("tre.port");
        
        PP pp = new PP(treIp, Integer.valueOf(trePort), scenario);
        String response = pp.smartGridBillWorkFlow();
        // finish workflow
//        if (response != null) {
//            pp.ppServerThread.interrupt();
//        }
    }
}
