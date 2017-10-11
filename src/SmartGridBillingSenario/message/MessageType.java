package SmartGridBillingSenario.message;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ydai on 24/9/17.
 */
public enum MessageType {
    @JsonProperty("ATTESTATION_REQUEST")
    ATTESTATION_REQUEST,
    @JsonProperty("GET_PRICE")
    GET_PRICE,
    @JsonProperty("RESPONSE_FROM_TRE")
    RESPONSE_FROM_TRE
}
