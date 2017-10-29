package SmartGridBillingSenario.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by ydai on 22/10/17.
 */
@Getter
@EqualsAndHashCode
@ToString
public class AuthenticationMessage implements Serializable {

    @JsonProperty("token")
    private String token;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonCreator
    public AuthenticationMessage(@JsonProperty("token") String token, @JsonProperty("username") String username, @JsonProperty("password") String password) {
        this.token = token;
        this.username = username;
        this.password = password;
    }


    public static AuthenticationMessage fromMessage(Message message) {
        Map<String, Object> result = (Map<String, Object>) message.getObject();
        return new AuthenticationMessage(String.valueOf(result.get("token")), String.valueOf(result.get("username")), String.valueOf(result.get("password")));

    }
}
