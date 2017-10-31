package SmartGridBillingSenario.starter;

import SmartGridBillingSenario.utils.PropertyReader;
import SmartGridBillingSenario.utils.Scenario;
import SmartGridBillingSenario.TRE;

/**
 * Created by yuandai on 28/9/17.
 */
public class TREStarter {

    public static void main(String[] args) throws NoSuchMethodException {

        Scenario scenario = Scenario.currentScenario;

        String trePort = PropertyReader.getProperty("tre.port");

        new TRE(Integer.valueOf(trePort), scenario);
    }
}
