package SmartGridBillingSenario.utils;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

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

    public static Senario currentSenario = getCurrentSenario();

    private static Senario getCurrentSenario() {
        String senarioString = PropertyReader.getProperty("senario");
        return StringUtils.isEmpty(senarioString) ? NormalSenario : get(senarioString);
    }
}
