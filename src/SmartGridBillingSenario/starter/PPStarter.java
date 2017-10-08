package SmartGridBillingSenario.starter;

import SmartGridBillingSenario.PP;
import SmartGridBillingSenario.Senario;

/**
 * Created by yuandai on 28/9/17.
 */
public class PPStarter {

    public static void main(String[] args) {
        String type = "NormalSenario";

        Senario senario = Senario.get(type);

        PP pp = new PP("localhost", 5000, senario);
        pp.smartGridBillWorkFlow();
    }
}
