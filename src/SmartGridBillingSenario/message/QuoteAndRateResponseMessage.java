package SmartGridBillingSenario.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;

/**
 * Created by ydai on 8/10/17.
 */
@Getter
public class QuoteAndRateResponseMessage extends Object implements Serializable {
    @JsonProperty("quote")
    private String quote;

    @JsonProperty("rateValue")
    private Integer rateValue;

    @JsonCreator
    public QuoteAndRateResponseMessage(@JsonProperty("quote") String quote, @JsonProperty("rateValue") Integer rateValue) {
        this.quote = quote;
        this.rateValue = rateValue;
    }
}
