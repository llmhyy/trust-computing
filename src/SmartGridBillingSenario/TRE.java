package SmartGridBillingSenario;

import SmartGridBillingSenario.calculator.Calculator;
import SmartGridBillingSenario.message.AuthenticationMessage;
import SmartGridBillingSenario.message.Message;
import SmartGridBillingSenario.message.MessageType;
import SmartGridBillingSenario.message.QuoteAndRateResponseMessage;
import SmartGridBillingSenario.socket.SocketServer;
import SmartGridBillingSenario.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import tss.Tpm;
import tss.TpmFactory;
import tss.Tss;
import tss.tpm.*;

import java.util.Arrays;

import static SmartGridBillingSenario.message.MessageType.*;

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

    //Current Scenario
    private Scenario scenario;

    //Current Calculator
    private Calculator calculator;

    private PpAuthentication ppAuthentication;

    private String realSealValue = "AAAAGAAAAAEABAMEAAAAAAABABQ=";

    public TRE(int serverPort, Scenario scenario) throws NoSuchMethodException {
        super(serverPort);
        this.scenario = scenario;
        String tpmHost = PropertyReader.getProperty("tpm.ip");
        String tpmPort = PropertyReader.getProperty("tpm.port");
        tpm = TpmFactory.localTpmSimulator(tpmHost, Integer.valueOf(tpmPort));
        ppAuthentication = new PpAuthentication();
        start();
        runServer(scenario);
    }

    private String decryptKey(String encrypteKey) {
        if (encrypteKey == null) {
            return null;
        } else {
            try {
                return Utils.decrypt(encrypteKey, privatePart);
            } catch (Exception ex) {
                //log.error("Error when decrypt Key {}", ex);
                return null;
            }

        }
    }

    @Override
    public Message handleMessage(Message message) {

        MessageType messageType = message.getMessageType();

        switch (messageType) {
            case GET_TOKEN:
                log.info("response with Token");
                String user = String.valueOf(message.getObject());
                String token = ppAuthentication.assignNewTokenForUser(user);

                String seal = Base64.encodeBase64String(Arrays.copyOf(sealForTre2(), 20));

                if (seal.equals(realSealValue)) {
                    return new Message(RESPONSE_FROM_GET_TOKEN, token);
                } else {
                    log.error("Wrong Seal, should stop whole process");
                    return new Message(RESPONSE_FROM_GET_TOKEN, "");
                }

            //Login with PassWord And Token
            case ATTESTATION_REQUEST:
                log.info("response with Encrypted Key");
                AuthenticationMessage identity = AuthenticationMessage.fromMessage(message);
                if (scenario.equals(Scenario.manInTheMiddleScenario)) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        log.error("Error in thread: {}", e);
                    }
                }
                if (ppAuthentication.checkUserPassword(identity.getUsername(), identity.getPassword()) && ppAuthentication.checkUserToken(identity.getUsername(), identity.getToken())) {
                    return new Message(RESPONSE_FROM_TRE_ATTESTATION_REQUEST, Base64.encodeBase64String(publicPart.authPolicy));
                } else {
                    log.error("Wrong Identity, you are fake PP!!! Input Identity: {}", identity);
                    return null;
                }
            case GET_PRICE:
                try {
                    String checkPpUser = decryptKey(String.valueOf(message.getObject()));
                    Integer rateValue = calculator.getMemberRateMap().get(checkPpUser);
                    log.info("Return with quote and value!");
                    if (scenario.equals(Scenario.WrongQuoteScenario)) {
                        return new Message(RESPONSE_FROM_TRE_GET_PRICE, new QuoteAndRateResponseMessage("1", rateValue));
                    } else {
                        return new Message(RESPONSE_FROM_TRE_GET_PRICE, new QuoteAndRateResponseMessage(Base64.encodeBase64String(quote.toQuote()), rateValue));
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
    public void start() throws NoSuchMethodException {
        initTpm();
        ek = createEK();
        initQuote();
        //create aik and also put the value into quote
        createAik();
        log.info("Sign New Quote");
        calculator = new Calculator();
        calculator.initMemberRateProcessor();
        byte[] codeArray1 = Utils.getMethodQuoteCode(calculator.getClass(), "initMemberRateProcessor");
        signNewData(codeArray1);
        calculator.initMemberRateMap();
        byte[] codeArray2 = Utils.getMethodQuoteCode(calculator.getClass(), "initMemberRateMap");
        signNewData(codeArray2);
    }

    private byte[] sealForTre2() {
        PCR_ReadResponse pcrAtStart = tpm.PCR_Read(TPMS_PCR_SELECTION.CreateSelectionArray(TPM_ALG_ID.SHA1, 2));
        return pcrAtStart.toTpm();
    }

    private void initQuote() {

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

        tpm.PCR_Event(TPM_HANDLE.pcr(0), new byte[]{0});

        pcrToQuote = new TPMS_PCR_SELECTION[]{
                new TPMS_PCR_SELECTION(TPM_ALG_ID.SHA256, new int[]{0})};
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
                TPM_ALG_ID.RSA,
                new TPMA_OBJECT(TPMA_OBJECT.userWithAuth, TPMA_OBJECT.sign),
                new byte[0],
                new TPMS_ECC_PARMS(
                        TPMT_SYM_DEF_OBJECT.nullObject(),
                        new TPMS_SIG_SCHEME_ECDSA(TPM_ALG_ID.RSA),
                        TPM_ECC_CURVE.NIST_P256,
                        new TPMS_NULL_KDF_SCHEME()),
                new TPM2B_PUBLIC_KEY_RSA());
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
