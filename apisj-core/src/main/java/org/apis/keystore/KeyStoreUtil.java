package org.apis.keystore;

import com.google.gson.Gson;
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

    public static byte[] decryptPrivateKey(String keyStore, String pwd) throws Exception {
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


    public static void main(String args[]) {
        try {
            decryptPrivateKey("{\"version\":3,\"id\":\"78a41934-1d7c-4bbb-9c33-bc4f93cab769\",\"address\":\"24fbc269f7d2c1c8df55ecb84fce9cee0a1d4e5a\",\"Crypto\":{\"ciphertext\":\"e0c4ebd57a7f9d443fedba11ff7aac12d4db2a14c3c3c9faf38f13de3fe664c3\",\"cipherparams\":{\"iv\":\"8e5aebe59d83c76ff053164d0f1681a2\"},\"cipher\":\"aes-128-ctr\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"salt\":\"83ab86693fcd53ed5f0f5455eefc50aa4e9285f83f737eb065c4fd5c2ef206f0\",\"n\":8192,\"r\":8,\"p\":1},\"mac\":\"d08a75a5424580a8eaa197aa506563b8c71b8b645fea91fce5aa8598913dc579\"}}",
                    "Smardi0292");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
