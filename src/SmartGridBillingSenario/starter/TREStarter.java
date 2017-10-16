package SmartGridBillingSenario.starter;

import SmartGridBillingSenario.utils.Senario;
import SmartGridBillingSenario.TRE;

/**
 * Created by yuandai on 28/9/17.
 */
public class TREStarter {

    public static void main(String[] args) {

        String type = "ManInTheMiddleSenario";
        Senario senario = Senario.get(type);

        TRE tre = new TRE(3000, senario);
    }
}
