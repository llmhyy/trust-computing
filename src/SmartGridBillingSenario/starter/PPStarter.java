package SmartGridBillingSenario.starter;

import SmartGridBillingSenario.PP;
import SmartGridBillingSenario.utils.Senario;

/**
 * Created by yuandai on 28/9/17.
 */
public class PPStarter {

    public static void main(String[] args) {
        String type = "NormalSenario";

        Senario senario = Senario.get(type);

        PP pp = new PP("192.168.0.154", 5000, senario);
        pp.smartGridBillWorkFlow();
    }
}
