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
    private static final String IV = "9927726BCEFE7510B1BDD3D137F27D5C";
    private static final String SALT = "88C0A59A55627B945225DEBAD71A01B6985FE84C95A70EB132882F3FF2EC019C";

    public static String AESEncrypt(String phrase, String msg) {
        AesUtil aesUtil = new AesUtil(KEY_SIZE, ITERATION_COUNT);
        return aesUtil.encrypt(SALT, IV, phrase, msg);
    }

    public static String AESDecrypt(String phrase, String msg) {
        AesUtil aesUtil = new AesUtil(KEY_SIZE, ITERATION_COUNT);
        return aesUtil.decrypt(SALT, IV, phrase, msg);
    }

    /**
     * 내부 비교용
     */
    public static String createAuth(String id, char[] pw) {
        byte[] byteID = HashUtil.sha3( id.getBytes() );
        byte[] bytePW = HashUtil.sha3( new String(pw).getBytes() );

        return AESEncrypt(ByteUtil.toHexString(bytePW), ByteUtil.toHexString(byteID));
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
