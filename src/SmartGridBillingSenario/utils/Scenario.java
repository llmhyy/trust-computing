package SmartGridBillingSenario.utils;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by ydai on 8/10/17.
 */

@AllArgsConstructor
public enum Scenario {
    NormalScenario("NormalSenario"),
    WrongQuoteScenario("WrongQuoteSenario"),
    manInTheMiddleScenario("ManInTheMiddleSenario"),
    fakePPScenario("fakePPSenario"),
    ddosTreScenario("ddosTreSenario");
    private String scenarioType;


    public static Scenario get(String snenarioString) {
        for (Scenario scenario : Scenario.values()) {
            if (scenario.scenarioType.equals(snenarioString)) {
                return scenario;
            }
        }
        return null;
    }

    public static Scenario currentSenario = getCurrentSenario();

    private static Scenario getCurrentSenario() {
        String scenarioString = PropertyReader.getProperty("scenario");
        return StringUtils.isEmpty(scenarioString) ? NormalScenario : get(scenarioString);
    }
}
