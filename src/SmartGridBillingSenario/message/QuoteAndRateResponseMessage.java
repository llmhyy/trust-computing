package SmartGridBillingSenario.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by ydai on 8/10/17.
 */
@AllArgsConstructor
@Getter
public class QuoteAndRateResponseMessage extends Object{

    private byte[] quote;
    private Integer rateValue;
}
