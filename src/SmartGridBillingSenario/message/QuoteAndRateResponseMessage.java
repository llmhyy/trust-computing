package SmartGridBillingSenario.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * Created by ydai on 8/10/17.
 */
@AllArgsConstructor
@Getter
public class QuoteAndRateResponseMessage extends Object implements Serializable{

    private byte[] quote;
    private Integer rateValue;
}
