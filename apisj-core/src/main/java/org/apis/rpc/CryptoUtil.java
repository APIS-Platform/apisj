package org.apis.rpc;

import com.google.gson.Gson;
import org.apis.crypto.HashUtil;
import org.apis.keystore.*;
import org.apis.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.generators.SCrypt;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

/**
 * Daniel
 *
 * Doc : https://github.com/ethereum/wiki/wiki/Web3-Secret-Storage-Definition
 */
public class CryptoUtil {



    private static final int DEFAULT_VERSION = 3;
    private static final String DEFAULT_CIPHER = "aes-128-ctr";
    private static final String DEFAULT_KDF = "scrypt";

    private final static Logger logger = LoggerFactory.getLogger("keyStoreUtil");

    public static byte[] decryptPrivateKey(String keyStore, String pwd) throws KeystoreVersionException, NotSupportKdfException, NotSupportCipherException, InvalidPasswordException {
        CryptoData crypto = new Gson().fromJson(keyStore.toLowerCase(), CryptoData.class);

        byte[] pwdBytes = pwd.getBytes(StandardCharsets.UTF_8);

        // scrypt generation
        byte[] generatedScrypt = SCrypt.generate(
                pwdBytes,
                Hex.decode(crypto.kdfparams.salt),
                crypto.kdfparams.n,
                crypto.kdfparams.r,
                crypto.kdfparams.p,
                crypto.kdfparams.dklen
        );

        String generatedScryptStr = Hex.toHexString(generatedScrypt);
        String generatedScryptRightStr = generatedScryptStr.substring(generatedScryptStr.length()/2);

        byte[] cipherBytes = Hex.decode(crypto.ciphertext);

        byte[] inputMac = new byte[generatedScrypt.length/2 + cipherBytes.length];
        System.arraycopy(Hex.decode(generatedScryptRightStr), 0, inputMac, 0, generatedScrypt.length/2);
        System.arraycopy(cipherBytes, 0, inputMac, generatedScrypt.length/2, cipherBytes.length);

        byte[] macGenerated = HashUtil.sha3(inputMac);
        byte[] macKeystore = Hex.decode(crypto.mac);

        // 비밀번호가 일치하지 않는다
        if(!FastByteComparisons.equal(macGenerated, macKeystore)) {
            throw new InvalidPasswordException();
        }

        byte[] ivBytes = Hex.decode(crypto.cipherparams.iv);

        String keyString = generatedScryptStr.substring(0, generatedScryptStr.length()/2);
        return decryptAES(cipherBytes, ivBytes, Hex.decode(keyString));
    }


    private static byte[] decryptAES(byte[] cipherBytes, byte[] ivBytes, byte[] keyBytes) {
        KeyParameter key = new KeyParameter(keyBytes);
        ParametersWithIV params = new ParametersWithIV(key, ivBytes);

        AESFastEngine engine = new AESFastEngine();
        SICBlockCipher ctrEngine = new SICBlockCipher(engine);

        ctrEngine.init(false, params);

        byte[] output = new byte[cipherBytes.length];
        ctrEngine.processBytes(cipherBytes, 0, 32, output, 0);

        return output;
    }

    private static byte[] encryptAES(byte[] privateKey, byte[] ivBytes, byte[] keyBytes) {
        KeyParameter key = new KeyParameter(keyBytes);
        ParametersWithIV params = new ParametersWithIV(key, ivBytes);

        AESFastEngine engine = new AESFastEngine();
        SICBlockCipher ctrEngine = new SICBlockCipher(engine);

        ctrEngine.init(true, params);

        byte[] output = new byte[privateKey.length];
        ctrEngine.processBytes(privateKey, 0, 32, output, 0);

        return output;
    }

    public static String getEncryptKeyStore(byte[] privateKey, String alias, String pwd, int n, int r, int p) {
        CryptoData data = new CryptoData();

        data.kdf = DEFAULT_KDF;
        data.cipher = DEFAULT_CIPHER;
        data.kdfparams = new CryptoData.kdfparams();
        data.kdfparams.dklen = privateKey.length;
        data.kdfparams.salt = Hex.toHexString(randomBytes(privateKey.length));
        data.kdfparams.n = n;
        data.kdfparams.r = r;
        data.kdfparams.p = p;
        data.cipherparams = new CryptoData.cipherparams();
        data.cipherparams.iv = Hex.toHexString(randomBytes(16));

        byte[] generatedScrypt = SCrypt.generate(
                pwd.getBytes(StandardCharsets.UTF_8),
                Hex.decode(data.kdfparams.salt),
                data.kdfparams.n,
                data.kdfparams.r,
                data.kdfparams.p,
                data.kdfparams.dklen
        );

        String scrypt = Hex.toHexString(generatedScrypt);
        String scryptLeft = scrypt.substring(0, scrypt.length()/2);
        String scryptRight = scrypt.substring(scryptLeft.length(), scrypt.length());

        byte[] cipherBytes = encryptAES(privateKey, Hex.decode(data.cipherparams.iv), Hex.decode(scryptLeft));

        data.ciphertext = Hex.toHexString(cipherBytes);

        data.mac = Hex.toHexString(HashUtil.sha3(Hex.decode(scryptRight + data.ciphertext)));


        return new Gson().toJson(data);
    }

    public static String getFileName(KeyStoreData data) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss.SSS");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return "UTC--" + format.format(new Date()) + "Z--" + data.address;
    }

    public static String getEncryptKeyStore(byte[] privateKey, String alias, String pwd) {
        return getEncryptKeyStore(privateKey, alias, pwd, 65536, 8, 1);
    }

    private static byte[] randomBytes(int len) {
        byte[] bytes = new byte[len];
        new Random().nextBytes(bytes);
        return bytes;
    }
}
