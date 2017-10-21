package SmartGridBillingSenario.utils;

import lombok.AllArgsConstructor;

/**
 * Created by ydai on 8/10/17.
 */

@AllArgsConstructor
public enum Senario {
    NormalSenario("NormalSenario"),
    WrongQuoteSenario("WrongQuoteSenario"),
    manInTheMiddleSenario("ManInTheMiddleSenario"),
    fakePPSenario("fakePPSenario"),
    ddosTreSenario("ddosTreSenario");
    private String senarioType;


    public static Senario get(String snenarioString) {
        for (Senario senario : Senario.values()) {
            if (senario.senarioType.equals(snenarioString)) {
                return senario;
            }
        }
        return null;
    }
}
