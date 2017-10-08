package SmartGridBillingSenario;

import SmartGridBillingSenario.calculator.Calculator;
import SmartGridBillingSenario.message.Message;
import SmartGridBillingSenario.message.MessageType;
import SmartGridBillingSenario.message.QuoteAndRateResponseMessage;
import SmartGridBillingSenario.socket.SocketServer;
import SmartGridBillingSenario.utils.Senario;
import SmartGridBillingSenario.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import tss.Helpers;
import tss.Tpm;
import tss.TpmFactory;
import tss.Tss;
import tss.tpm.*;

import java.util.Arrays;

import static SmartGridBillingSenario.message.MessageType.RESPONSE_FROM_TRE;

/**
 * Created by ydai on 24/9/17.
 */
@Slf4j
public class TRE extends SocketServer {


    private Tpm tpm;

    private CreatePrimaryResponse ek;

    private Tss.ActivationCredential aik;

    private transient TPMT_PUBLIC rsaEKTemplate;

    //Quote Related
    private CreatePrimaryResponse quotingKey;
    private QuoteResponse quote;
    private TPMS_PCR_SELECTION[] pcrToQuote;

    //TSS.key
    public byte[] privatePart;
    public transient TPMT_PUBLIC publicPart;

    //Current Senario
    private Senario senario;

    //Current Calculator
    private Calculator calculator;


    public TRE(int serverPort, Senario senario) {
        super(serverPort);
        this.senario = senario;
        tpm = TpmFactory.localTpmSimulator();
        start();
        runServer();
    }

    private String decryptKey(String encrypteKey) {
        if (encrypteKey == null) {
            return null;
        } else {
            return Utils.decrypt(encrypteKey, privatePart);
        }
    }

    @Override
    public Message handleMessage(Message message) {

        MessageType messageType = message.getMessageType();

        switch (messageType) {
            case ATTESTATION_REQUEST:
                log.info("response with Encrypted Key");
                return new Message(RESPONSE_FROM_TRE, publicPart);
            case GET_PRICE:
                try {
                    String user = decryptKey(String.valueOf(message.getObject()));
                    Integer rateValue = calculator.getMemberRateMap().get(user);
                    log.info("Return with quote and value!");
                    if (senario.equals(Senario.NormalSenario)) {
                        return new Message(RESPONSE_FROM_TRE, new QuoteAndRateResponseMessage(Arrays.copyOf(quote.toTpm(), 30), rateValue));
                    } else if (senario.equals(Senario.WrongQuoteSenario)){
                        return new Message(RESPONSE_FROM_TRE, new QuoteAndRateResponseMessage(new byte[]{1}, rateValue));
                    }

                } catch (Exception e) {
                    log.error("Error when parse data, {}", message.getObject());
                }
        }
        return null;
    }

    /**
     * TRE create AIK by its EK (see tutorial)
     */
    public void start() {
        initTpm();
        ek = createEK();
        quote = initQuote();
        //create aik and also put the value into quote
        createAik();
        log.info("Sign New Quote");
        calculator = new Calculator();

        signNewData(Utils.getByteArrayOfClass(calculator.getClass()));
    }

    private QuoteResponse initQuote() {

        // Note that we create the quoting key in the endorsement hierarchy so
        // that the
        // CLOCK_INFO is not obfuscated


        // Create an RSA restricted signing key in the owner hierarchy
        TPMT_PUBLIC rsaTemplate = new TPMT_PUBLIC(TPM_ALG_ID.SHA256,
                new TPMA_OBJECT(TPMA_OBJECT.sign, TPMA_OBJECT.sensitiveDataOrigin, TPMA_OBJECT.userWithAuth,
                        TPMA_OBJECT.restricted),
                new byte[0], new TPMS_RSA_PARMS(TPMT_SYM_DEF_OBJECT.nullObject(),
                new TPMS_SIG_SCHEME_RSASSA(TPM_ALG_ID.SHA256), 2048, 65537),
                new TPM2B_PUBLIC_KEY_RSA());
        quotingKey = tpm.CreatePrimary(TPM_HANDLE.from(TPM_RH.ENDORSEMENT),
                new TPMS_SENSITIVE_CREATE(new byte[0], new byte[0]), rsaTemplate, new byte[0],
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
        //CryptoServices.riotTest();
        GetCapabilityResponse caps = tpm.GetCapability(TPM_CAP.HANDLES, TPM_HT.TRANSIENT.toInt() << 24, 8);
        TPML_HANDLE handles = (TPML_HANDLE) caps.capabilityData;

        if (handles.handle.length == 0)
            System.out.println("No dangling handles");
        else for (TPM_HANDLE h : handles.handle)
            System.out.printf("Dangling handle 0x%08X\n", h.handle);


    }


    private CreatePrimaryResponse createEK() {
        // Make an RSA primary storage key that can be the target of duplication
        // operations
        TPMT_PUBLIC srkTemplate = new TPMT_PUBLIC(TPM_ALG_ID.SHA1,
                new TPMA_OBJECT(TPMA_OBJECT.fixedTPM, TPMA_OBJECT.fixedParent, TPMA_OBJECT.sensitiveDataOrigin,
                        TPMA_OBJECT.userWithAuth, TPMA_OBJECT.noDA, TPMA_OBJECT.restricted, TPMA_OBJECT.decrypt),
                new byte[0], new TPMS_ECC_PARMS(new TPMT_SYM_DEF_OBJECT(TPM_ALG_ID.AES, 128, TPM_ALG_ID.CFB),
                new TPMS_NULL_ASYM_SCHEME(),
                TPM_ECC_CURVE.NIST_P256,
                new TPMS_NULL_KDF_SCHEME()),
                new TPMS_ECC_POINT());

        CreatePrimaryResponse rsaEk = tpm.CreatePrimary(TPM_HANDLE.from(TPM_RH.OWNER),
                new TPMS_SENSITIVE_CREATE(new byte[0], new byte[0]), srkTemplate, new byte[0],
                new TPMS_PCR_SELECTION[0]);
        System.out.println("RSA Primary Key: \n" + rsaEk.toString());


        // Use the helper routines in tss.Java to create a duplication
        // blob *without* using the TPM
        rsaEKTemplate = new TPMT_PUBLIC(
                TPM_ALG_ID.SHA1,
                new TPMA_OBJECT(TPMA_OBJECT.userWithAuth, TPMA_OBJECT.sign),
                new byte[0],
                new TPMS_ECC_PARMS(
                        TPMT_SYM_DEF_OBJECT.nullObject(),
                        new TPMS_SIG_SCHEME_ECDSA(TPM_ALG_ID.SHA1),
                        TPM_ECC_CURVE.NIST_P256,
                        new TPMS_NULL_KDF_SCHEME()),
                new TPMS_ECC_POINT());
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
        quote = tpm.Quote(quotingKey.handle, Arrays.copyOf(dataToSign, 10), new TPMS_NULL_SIG_SCHEME(), pcrToQuote);
    }
}
