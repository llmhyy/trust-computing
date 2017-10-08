package SmartGridBillingSenario.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by ydai on 24/9/17.
 */
@AllArgsConstructor
@Getter
@ToString
public class Message implements Serializable {

    private static final long serialVersionUID = -5399605122490343339L;

    private MessageType messageType;
    private Object object;
}
