package SmartGridBillingSenario.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by ydai on 22/10/17.
 */
@Getter
@Slf4j
public class PpAuthentication {

    private Map<String, String> userTokenMap;
    private Map<String, String> userPasswordMap;

    public PpAuthentication() {
        userPasswordMap = generateUserPasswordMap();
        userTokenMap = new HashMap<>();
    }

    /**
     * Init the UserName/Password for new user
     *
     * @return
     */
    private Map<String, String> generateUserPasswordMap() {
        Map<String, String> userPasswordMap = new HashMap<>();

        //Username/Password for PP
        userPasswordMap.put("pp", "password");
        return userPasswordMap;
    }

    /**
     * Check input user/password match system or not
     *
     * @param user
     * @param inputPassword
     * @return
     */
    public boolean checkUserPassword(String user, String inputPassword) {
        return inputPassword != null && inputPassword.equals(userPasswordMap.get(user));

    }

    /**
     * Assign new Token for new User
     *
     * @param user
     * @return
     */
    public String assignNewTokenForUser(String user) {
        String token = generateRandomToken();
        userTokenMap.put(user, token);
        return token;
    }

    public boolean checkUserToken(String user, String token) {
        //if token == null means this system does not use token, for fake PP Attack, return true
        if (StringUtils.isEmpty(token)) {
            return true;
        }

        if (token.equals(userTokenMap.get(user))) {
            // for matched token, assign new token
            userTokenMap.put(user, generateRandomToken());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Random Token for six digit
     *
     * @return
     */
    private String generateRandomToken() {
        Random rnd = new Random();
        int n = 100000 + rnd.nextInt(900000);
        return String.valueOf(n);
    }


}
