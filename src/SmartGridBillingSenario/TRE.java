package SmartGridBillingSenario;

import SmartGridBillingSenario.socket.Message;
import SmartGridBillingSenario.socket.SocketServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import tss.Helpers;
import tss.Tpm;
import tss.TpmFactory;
import tss.Tss;
import tss.tpm.*;

import java.io.IOException;
import java.math.BigDecimal;

import static SmartGridBillingSenario.MessageType.RESPONSE_FROM_TRE;

/**
 * Created by ydai on 24/9/17.
 */
@Slf4j
public class TRE extends SocketServer {


    private Tpm tpm;

    private CreatePrimaryResponse ek;

    private Tss.ActivationCredential aik;

    private TPMT_PUBLIC rsaEKTemplate;

    //Quote Related
    private CreatePrimaryResponse quotingKey;
    private QuoteResponse quote;
    private TPMS_PCR_SELECTION[] pcrToQuote;

    //TSS.key
    public byte[] privatePart;
    public TPMT_PUBLIC publicPart;

    @Override
    public Message handleMessage(Message message) {

        MessageType messageType = message.getMessageType();

        switch (messageType) {
            case ATTESTATION_REQUEST:
                return new Message(RESPONSE_FROM_TRE, publicPart);
            case GET_PRICE:
                try {
                    byte[] totalByte = ArrayUtils.addAll(decryptKeyAndGetPrice(Utils.serialize(message.getObject())).toPlainString().getBytes(), quote.toTpm());
                    return new Message(RESPONSE_FROM_TRE,  totalByte);
                } catch (IOException e) {
                    log.error("Error when parse data, {}", message.getObject());
                }
        }
        return null;
    }

    private BigDecimal decryptKeyAndGetPrice(byte[] encrypteKey) {
        if (encrypteKey == null) {
            return null;
        } else {
            String finalValue = Utils.decrypt(encrypteKey, privatePart);
            return calculatePrice(finalValue);
        }
    }

    public TRE(int serverPort) {
        super(serverPort);
        tpm = TpmFactory.localTpmSimulator();
        start();
    }

    public TRE(String host, int trePort, int serverPort) {
        super(serverPort);
        tpm = TpmFactory.localTpmSimulator(host, port);
        this.port = port;
        start();
    }


    /**
     * TRE create AIK by its EK (see tutorial)
     */
    public void start() {
        initTpm();
        quote = initQuote();
        ek = createEK();
        //create aik and also put the value into quote
        signNewData(createAik().toTpm());
    }

    private QuoteResponse initQuote() {

        // Note that we create the quoting key in the endorsement hierarchy so
        // that the
        // CLOCK_INFO is not obfuscated
        quotingKey  = tpm.CreatePrimary(TPM_HANDLE.from(TPM_RH.ENDORSEMENT),
                new TPMS_SENSITIVE_CREATE(new byte[0], new byte[0]), rsaEKTemplate, new byte[0],
                new TPMS_PCR_SELECTION[0]);

        System.out.println("RSA Primary quoting Key: \n" + quotingKey.toString());

        // Set some PCR to non-zero values
        tpm.PCR_Event(TPM_HANDLE.pcr(10), new byte[]{0, 1, 2});
        tpm.PCR_Event(TPM_HANDLE.pcr(11), new byte[]{3, 4, 5});
        tpm.PCR_Event(TPM_HANDLE.pcr(12), new byte[]{6, 7, 8});

        pcrToQuote = new TPMS_PCR_SELECTION[]{
                new TPMS_PCR_SELECTION(TPM_ALG_ID.SHA256, new int[]{10, 11, 12})};

        // Get the PCR so that we can validate the quote
        PCR_ReadResponse pcrs = tpm.PCR_Read(pcrToQuote);


        byte[] dataToSign = Helpers.getRandom(10);
        return tpm.Quote(quotingKey.handle, dataToSign, new TPMS_NULL_SIG_SCHEME(), pcrToQuote);
    }

    private void initTpm() {
        GetCapabilityResponse caps = tpm.GetCapability(TPM_CAP.HANDLES, TPM_HT.TRANSIENT.toInt() << 24, 8);
        TPML_HANDLE handles = (TPML_HANDLE) caps.capabilityData;

        if (handles.handle.length == 0)
            log.info("No dangling handles");
        else for (TPM_HANDLE h : handles.handle)
            log.info("Dangling handle 0x%08X\n", h.handle);
    }


    private CreatePrimaryResponse createEK() {
        // This policy is a "standard" policy that is used with vendor-provided
        // EKs
        byte[] standardEKPolicy = new byte[]{(byte) 0x83, 0x71, (byte) 0x97, 0x67, 0x44, (byte) 0x84, (byte) 0xb3,
                (byte) 0xf8, 0x1a, (byte) 0x90, (byte) 0xcc, (byte) 0x8d, 0x46, (byte) 0xa5, (byte) 0xd7, 0x24,
                (byte) 0xfd, 0x52, (byte) 0xd7, 0x6e, 0x06, 0x52, 0x0b, 0x64, (byte) 0xf2, (byte) 0xa1, (byte) 0xda,
                0x1b, 0x33, 0x14, 0x69, (byte) 0xaa};

        // Note: this sample allows userWithAuth - a "standard" EK does not (see
        // the other EK sample)
        rsaEKTemplate = new TPMT_PUBLIC(TPM_ALG_ID.SHA256,
                new TPMA_OBJECT(TPMA_OBJECT.fixedTPM, TPMA_OBJECT.fixedParent, TPMA_OBJECT.sensitiveDataOrigin,
                        TPMA_OBJECT.userWithAuth,
                        /* TPMA_OBJECT.adminWithPolicy, */ TPMA_OBJECT.restricted, TPMA_OBJECT.decrypt),
                standardEKPolicy,
                new TPMS_RSA_PARMS(new TPMT_SYM_DEF_OBJECT(TPM_ALG_ID.AES, 128, TPM_ALG_ID.CFB),
                        new TPMS_NULL_ASYM_SCHEME(), 2048, 0),
                new TPM2B_PUBLIC_KEY_RSA());
        log.info("Create EK");
        CreatePrimaryResponse rsaEk = tpm.CreatePrimary(TPM_HANDLE.from(TPM_RH.OWNER),
                new TPMS_SENSITIVE_CREATE(), rsaEKTemplate, new byte[0], new TPMS_PCR_SELECTION[0]);

        return rsaEk;
    }


    private TPMT_PUBLIC createAik() {
        log.info("Create Key");
        Tss.Key key = Tss.createKey(rsaEKTemplate);
        privatePart = key.PrivatePart;
        publicPart = key.PublicPart;
        return publicPart;
    }

    private void signNewData(byte[] dataToSign) {
        byte[] totalData =  ArrayUtils.addAll(dataToSign, quote.toTpm());
        quote = tpm.Quote(quotingKey.handle, totalData, new TPMS_NULL_SIG_SCHEME(), pcrToQuote);
    }


    private static BigDecimal calculatePrice(String query) {

        if (StringUtils.isNotEmpty(query)) {
            return new BigDecimal(query);
        } else {
            return null;
        }
    }
}
