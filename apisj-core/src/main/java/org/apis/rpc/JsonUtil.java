package org.apis.rpc;

import com.google.gson.Gson;
import org.apis.crypto.HashUtil;
import org.apis.util.AesUtil;
import org.apis.util.ByteUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonUtil {
    private static final int KEY_SIZE = 128;
    private static final int ITERATION_COUNT = 10000;
    private static final int SALT_LENGTH = 64;
    private static final int IV_LENGTH = 32;

    public static String AESEncrypt(String phrase, String msg) {
        AesUtil aesUtil = new AesUtil(KEY_SIZE, ITERATION_COUNT);
        String salt = AesUtil.random(SALT_LENGTH / 2);
        String iv = AesUtil.random(IV_LENGTH / 2);

        return salt + iv + aesUtil.encrypt(salt, iv, phrase, msg);
    }

    // 내부 생성 비교용 //
    public static String AESEncrypt(String salt, String iv, String phrase, String msg) {
        AesUtil aesUtil = new AesUtil(KEY_SIZE, ITERATION_COUNT);

        return salt + iv + aesUtil.encrypt(salt, iv, phrase, msg);
    }

    public static String AESDecrypt(String phrase, String msg) {
        AesUtil aesUtil = new AesUtil(KEY_SIZE, ITERATION_COUNT);

        String salt = getSalt(msg);
        String iv = getIv(msg);
        msg = msg.replace(salt, "");
        msg = msg.replace(iv, "");
        return aesUtil.decrypt(salt, iv, phrase, msg);
    }

    public static String getSalt(String msg) {
        return msg.substring(0, SALT_LENGTH);
    }

    public static String getIv(String msg) {
        return msg.substring(SALT_LENGTH, SALT_LENGTH + IV_LENGTH);
    }

    /**
     * 내부 비교용
     */
    public static String createAuth(String salt, String iv, String id, char[] pw) {
        byte[] byteID = HashUtil.sha3( id.getBytes() );
        byte[] bytePW = HashUtil.sha3( new String(pw).getBytes() );

        return AESEncrypt(salt, iv, ByteUtil.toHexString(bytePW), ByteUtil.toHexString(byteID));
    }

    // auth키 를 받아 토큰 생성
    public static byte[] createToken(String auth, String ip) {
        long current = System.currentTimeMillis();
        return HashUtil.sha3(
                ByteUtil.merge(auth.getBytes(), ip.getBytes(), ByteUtil.longToBytes(current)));
    }

    // 생성
    public static String createJson(String type, Object data) {
        RPCCommandData rpcCommandData = new RPCCommandData(type, data);
        return new Gson().toJson(rpcCommandData);
    }

    public static String createJson(String type, Object data, String error) {
        RPCCommandData rpcCommandData = new RPCCommandData(type, data, error);
        return new Gson().toJson(rpcCommandData);
    }

    public static String createJson(String type, Object data, Exception error) {
        RPCCommandData rpcCommandData = new RPCCommandData(type, data, error);
        return new Gson().toJson(rpcCommandData);
    }

    // decode
    // json string 해석
    public static String getDecodeMessageNonce(String msg) throws ParseException {
        return getDecodeMessage(msg, Command.DATA_TAG_NONCE);
    }

    public static String getDecodeMessageAuth(String msg) throws ParseException {
        return getDecodeMessage(msg, Command.DATA_TAG_AUTH);
    }

    public static String getDecodeMessageType(String msg) throws ParseException {
        return getDecodeMessage(msg, Command.DATA_TAG_TYPE);
    }

    public static String getDecodeMessage(String msg, String kind) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(msg);

        String result = (String) object.get(kind);
        return result;
    }

    public static String getDecodeMessageDataContent(String msg, String kind) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(msg);
        JSONObject dataObject = (JSONObject) object.get(Command.DATA_TAG_DATA);

        String result = (String) dataObject.get(kind);
        return result;
    }
}
