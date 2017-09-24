package SmartGridBillingSenario;


import SmartGridBillingSenario.Socket.Message;
import SmartGridBillingSenario.Socket.SocketClient;
import tss.tpm.TPM2B_PUBLIC_KEY_RSA;

/**
 * Created by ydai on 24/9/17.
 */
public class PP extends SocketClient{

    private TPM2B_PUBLIC_KEY_RSA publicKeyRsa;

    private void getPublicKey() {
        publicKeyRsa = (TPM2B_PUBLIC_KEY_RSA) sendToPort(new Message(MessageDescription.GETPUBLICKEY, null)).getObject();
    }

}
