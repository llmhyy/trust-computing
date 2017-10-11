package SmartGridBillingSenario.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by ydai on 24/9/17.
 */
@Getter
@ToString
public class Message implements Serializable {

    private static final long serialVersionUID = -5399605122490343339L;
    @JsonProperty("messageType")
    private MessageType messageType;
    @JsonProperty("object")
    private Object object;

    @JsonCreator
    public Message(@JsonProperty("messageType")MessageType messageType,  @JsonProperty("object")Object object) {
        this.messageType = messageType;
        this.object = object;
    }
}
