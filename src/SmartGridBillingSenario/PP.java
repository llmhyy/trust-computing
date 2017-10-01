package SmartGridBillingSenario;


import SmartGridBillingSenario.socket.Message;
import SmartGridBillingSenario.socket.SocketClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import tss.tpm.TPM2B_PUBLIC_KEY_RSA;
import tss.tpm.TPMT_PUBLIC;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ydai on 24/9/17.
 */
@Slf4j
public class PP extends SocketClient {

    private TPM2B_PUBLIC_KEY_RSA publicKey;


    public PP(String host, int port) {
        super(host, port);
    }

    private void getPublicKey() {

        Message responseForPublicKey  = sendToPort(new Message(MessageType.ATTESTATION_REQUEST, null));
        TPMT_PUBLIC publicInfo = (TPMT_PUBLIC) responseForPublicKey.getObject();
        publicKey = (TPM2B_PUBLIC_KEY_RSA) publicInfo.unique;
    }

    private BigDecimal setEncryptQueryAndGetPrice(String query) throws NoSuchAlgorithmException {

        if (StringUtils.isNotEmpty(query)) {
            byte[] encryptedQuery = Utils.encrypt(query, publicKey.buffer);
            return (BigDecimal) sendToPort(new Message(MessageType.GET_PRICE, encryptedQuery)).getObject();
        } else {
            return null;
        }

    }

    public BigDecimal smartGridBillWorkFlow() {
        getPublicKey();
        String query = "1000";
        try {
           return setEncryptQueryAndGetPrice(query);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error when Encryption", e);
            return null;
        }
    }
}
