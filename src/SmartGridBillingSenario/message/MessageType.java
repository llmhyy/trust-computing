package SmartGridBillingSenario.message;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ydai on 24/9/17.
 */
public enum MessageType {

    @JsonProperty("GET_TOKEN")
    GET_TOKEN,
    @JsonProperty("RESPONSE_FROM_GET_TOKEN")
    RESPONSE_FROM_GET_TOKEN,
    @JsonProperty("ATTESTATION_REQUEST")
    ATTESTATION_REQUEST,
    @JsonProperty("GET_PRICE")
    GET_PRICE,
    @JsonProperty("RESPONSE_FROM_TRE_ATTESTATION_REQUEST")
    RESPONSE_FROM_TRE_ATTESTATION_REQUEST,
    @JsonProperty("DDOS")
    DDOS,
    @JsonProperty("RESPONSE_FROM_TRE_GET_PRICE")
    RESPONSE_FROM_TRE_GET_PRICE
}
