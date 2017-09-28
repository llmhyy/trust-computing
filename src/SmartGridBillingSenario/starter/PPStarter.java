package SmartGridBillingSenario.starter;

import SmartGridBillingSenario.PP;

/**
 * Created by yuandai on 28/9/17.
 */
public class PPStarter {
    
    public static void main(String[] args) {
        PP pp = new PP("localhost", 8091);
        pp.smartGridBillWorkFlow();
    }
}
