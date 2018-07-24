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
    private static final String IV = "9927726BCEFE7510B1BDD3D137F27D5C";

    public static String AESEncrypt(String phrase, String msg) {
        AesUtil aesUtil = new AesUtil(KEY_SIZE, ITERATION_COUNT);
        String salt = AesUtil.random(SALT_LENGTH/2);

        return salt + aesUtil.encrypt(salt, IV, phrase, msg);
    }

    public static String AESEncrypt(String salt, String phrase, String msg) {
        AesUtil aesUtil = new AesUtil(KEY_SIZE, ITERATION_COUNT);

        return salt + aesUtil.encrypt(salt, IV, phrase, msg);
    }

    public static String AESDecrypt(String phrase, String msg) {
        AesUtil aesUtil = new AesUtil(KEY_SIZE, ITERATION_COUNT);

        String salt = getSalt(msg);
        msg = msg.substring(SALT_LENGTH, msg.length());
        return aesUtil.decrypt(salt, IV, phrase, msg);
    }

    public static String getSalt(String msg) {
        return msg.substring(0, SALT_LENGTH);
    }

    /**
     * 내부 비교용
     */
    public static String createAuth(String salt, String id, char[] pw) {
        byte[] byteID = HashUtil.sha3( id.getBytes() );
        byte[] bytePW = HashUtil.sha3( new String(pw).getBytes() );

        return AESEncrypt(salt, ByteUtil.toHexString(bytePW), ByteUtil.toHexString(byteID));
    }

    // auth키 를 받아 토큰 생성
    public static byte[] createToken(String auth, String ip) {
        long current = System.currentTimeMillis();
        return HashUtil.sha3(
                ByteUtil.merge(auth.getBytes(), ip.getBytes(), ByteUtil.longToBytes(current)));
    }

    // 생성
    public static String createJson(String type, Object data, boolean error) {
        RPCCommandData rpcCommandData = new RPCCommandData(type, data, error);
        return new Gson().toJson(rpcCommandData);
    }

    public static String createJson(String type, Object data, String error) {
        RPCCommandData rpcCommandData = new RPCCommandData(type, data, error);
        return new Gson().toJson(rpcCommandData);
    }

    // decode
    // json string 해석
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
