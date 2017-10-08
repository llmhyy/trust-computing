package SmartGridBillingSenario;


import SmartGridBillingSenario.socket.Message;
import SmartGridBillingSenario.socket.SocketClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import tss.tpm.TPMT_PUBLIC;
import tss.tpm.TPMU_PUBLIC_ID;

import java.security.NoSuchAlgorithmException;

/**
 * Created by ydai on 24/9/17.
 */
@Slf4j
public class PP extends SocketClient {

    private transient TPMU_PUBLIC_ID publicKey;


    public PP(String host, int port) {
        super(host, port);
    }

    private void getPublicKey() {
        log.info("Get Public Key!!");
        Message responseForPublicKey = sendToPort(new Message(MessageType.ATTESTATION_REQUEST, null));
        TPMT_PUBLIC publicInfo = (TPMT_PUBLIC) responseForPublicKey.getObject();
        publicKey = (TPMU_PUBLIC_ID) publicInfo.unique;
    }

    private String setEncryptQueryAndGetPrice(String query) throws NoSuchAlgorithmException {

        if (StringUtils.isNotEmpty(query)) {
            byte[] encryptedQuery = Utils.encrypt(query, publicKey.toTpm());
            log.info("Send Encrypted value to TRE");
            return String.valueOf(sendToPort(new Message(MessageType.GET_PRICE, encryptedQuery)).getObject());
        } else {
            return null;
        }

    }

    //TODO: Calculator for one;
    //TODO: (JVM + Calculator) ByteCode;
    //TODO: Case 1: Execute but return wrong value to PP
    //TODO: Spoof attack and week2

    //Hash: Java /
    public String smartGridBillWorkFlow() {
        getPublicKey();
        String query = "1000";
        try {
            log.info("Start Encryption ");
            return setEncryptQueryAndGetPrice(query);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error when Encryption", e);
            return null;
        }
    }
}
