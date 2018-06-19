package org.apis.keystore;

import com.google.gson.Gson;
import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;
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
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Daniel
 *
 * Doc : https://github.com/ethereum/wiki/wiki/Web3-Secret-Storage-Definition
 */
public class KeyStoreUtil {



    private static final int DEFAULT_VERSION = 3;
    private static final String DEFAULT_CIPHER = "aes-128-ctr";
    private static final String DEFAULT_KDF = "scrypt";

    private final static Logger logger = LoggerFactory.getLogger("keyStoreUtil");

    public static byte[] decryptPrivateKey(String keyStore, String pwd) throws KeystoreVersionException, NotSupportKdfException, NotSupportCipherException, InvalidPasswordException {
        KeyStoreData keyStoreData = new Gson().fromJson(keyStore.toLowerCase(), KeyStoreData.class);

        if(keyStoreData.version != DEFAULT_VERSION) {
            logger.error("Support on V3. This is V" + keyStoreData.version, new KeystoreVersionException());
            throw new KeystoreVersionException();
        }

        else if(!keyStoreData.crypto.kdf.equals(DEFAULT_KDF)) {
            logger.error("kdf is " + keyStoreData.crypto.kdf, new NotSupportKdfException(keyStoreData.crypto.kdf));
            throw new NotSupportKdfException(keyStoreData.crypto.kdf);
        }

        else if(!keyStoreData.crypto.cipher.equals(DEFAULT_CIPHER)) {
            logger.error("cipher is " + keyStoreData.crypto.cipher, new NotSupportCipherException(keyStoreData.crypto.kdf));
            throw new NotSupportCipherException(keyStoreData.crypto.cipher);
        }

        byte[] pwdBytes = pwd.getBytes(StandardCharsets.UTF_8);

        // scrypt generation
        byte[] generatedScrypt = SCrypt.generate(
                pwdBytes,
                Hex.decode(keyStoreData.crypto.kdfparams.salt),
                keyStoreData.crypto.kdfparams.n,
                keyStoreData.crypto.kdfparams.r,
                keyStoreData.crypto.kdfparams.p,
                keyStoreData.crypto.kdfparams.dklen
        );

        String generatedScryptStr = Hex.toHexString(generatedScrypt);
        String generatedScryptRightStr = generatedScryptStr.substring(generatedScryptStr.length()/2);

        byte[] cipherBytes = Hex.decode(keyStoreData.crypto.ciphertext);

        byte[] inputMac = new byte[generatedScrypt.length/2 + cipherBytes.length];
        System.arraycopy(Hex.decode(generatedScryptRightStr), 0, inputMac, 0, generatedScrypt.length/2);
        System.arraycopy(cipherBytes, 0, inputMac, generatedScrypt.length/2, cipherBytes.length);

        byte[] macGenerated = HashUtil.sha3(inputMac);
        byte[] macKeystore = Hex.decode(keyStoreData.crypto.mac);

        // 비밀번호가 일치하지 않는다
        if(!FastByteComparisons.equal(macGenerated, macKeystore)) {
            throw new InvalidPasswordException();
        }

        byte[] ivBytes = Hex.decode(keyStoreData.crypto.cipherparams.iv);

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
        KeyStoreData data = new KeyStoreData();

        data.version = DEFAULT_VERSION;
        data.id = UUID.randomUUID().toString();
        data.alias = alias;
        data.address = Hex.toHexString(ECKey.fromPrivate(privateKey).getAddress());
        data.crypto = new KeyStoreData.Crypto();
        data.crypto.kdf = DEFAULT_KDF;
        data.crypto.cipher = DEFAULT_CIPHER;
        data.crypto.kdfparams = new KeyStoreData.kdfparams();
        data.crypto.kdfparams.dklen = privateKey.length;
        data.crypto.kdfparams.salt = Hex.toHexString(randomBytes(privateKey.length));
        data.crypto.kdfparams.n = n;
        data.crypto.kdfparams.r = r;
        data.crypto.kdfparams.p = p;
        data.crypto.cipherparams = new KeyStoreData.cipherparams();
        data.crypto.cipherparams.iv = Hex.toHexString(randomBytes(16));

        byte[] generatedScrypt = SCrypt.generate(
                pwd.getBytes(StandardCharsets.UTF_8),
                Hex.decode(data.crypto.kdfparams.salt),
                data.crypto.kdfparams.n,
                data.crypto.kdfparams.r,
                data.crypto.kdfparams.p,
                data.crypto.kdfparams.dklen
        );

        String scrypt = Hex.toHexString(generatedScrypt);
        String scryptLeft = scrypt.substring(0, scrypt.length()/2);
        String scryptRight = scrypt.substring(scryptLeft.length(), scrypt.length());

        byte[] cipherBytes = encryptAES(privateKey, Hex.decode(data.crypto.cipherparams.iv), Hex.decode(scryptLeft));

        data.crypto.ciphertext = Hex.toHexString(cipherBytes);

        data.crypto.mac = Hex.toHexString(HashUtil.sha3(Hex.decode(scryptRight + data.crypto.ciphertext)));


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
