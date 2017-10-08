package SmartGridBillingSenario.starter;

import SmartGridBillingSenario.Senario;
import SmartGridBillingSenario.TRE;

/**
 * Created by yuandai on 28/9/17.
 */
public class TREStarter {

    public static void main(String[] args) {

        String type = "NormalSenario";
        Senario senario = Senario.get(type);

        TRE tre = new TRE(5000, senario);
    }
}
