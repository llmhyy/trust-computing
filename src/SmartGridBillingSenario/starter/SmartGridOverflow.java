package SmartGridBillingSenario.starter;

import SmartGridBillingSenario.PP;
import SmartGridBillingSenario.TRE;

/**
 * Created by ydai on 26/9/17.
 */
public class SmartGridOverflow {

    public static void main(String[] args) {
        PP pp = new PP("localhost", 8091);
        TRE tre = new TRE(8092);
        pp.smartGridBillWorkFlow();
    }
}
