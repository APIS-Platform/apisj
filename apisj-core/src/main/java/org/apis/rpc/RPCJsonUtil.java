package org.apis.rpc;

import com.google.gson.*;
import org.apis.crypto.HashUtil;
import org.apis.util.AesUtil;
import org.apis.util.ByteUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.Charset;

public class RPCJsonUtil {

    /***********************
     * encrypt
     ***********************/
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

    public static String AESDecrypt(String phrase, String msg) throws IllegalStateException {
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


    /***********************
     * encode
     ***********************/
    // auth키 를 받아 토큰 생성
    public static byte[] createToken(String auth, String ip) {
        long current = System.currentTimeMillis();
        return HashUtil.sha3(
                ByteUtil.merge(auth.getBytes(), ip.getBytes(), ByteUtil.longToBytes(current))
        );
    }

    // payload 내부 method, params등을 이용하여 tokenHash를 생성
    public static String createTokenHash(String payload, String token) throws ParseException {
        String method = getDecodeMessageMethod(payload);
        String params = getDecodeMessageParam(payload);
        long id = getDecodeMessageId(payload);

        byte[] methodByte = method.getBytes(Charset.forName("UTF-8"));
        byte[] paramsByte = params.getBytes(Charset.forName("UTF-8"));
        byte[] idByte = ByteUtil.longToBytes(id);
        byte[] tokenByte = Hex.decode(token);

        byte[] tokenHashByte = ByteUtil.merge(methodByte, paramsByte, idByte, tokenByte);
        tokenHashByte = HashUtil.sha3(tokenHashByte);

        return ByteUtil.toHexString(tokenHashByte);
    }

    // return json
    public static String createJson(long id, String method, Object result) {
        RPCResultData rpcResultData = new RPCResultData(id, method, result);
        return new Gson().toJson(rpcResultData);
    }

    public static String createJson(long id, String method, Object result, String error) {
        RPCResultData rpcResultData = new RPCResultData(id, method, result, error);
        return new Gson().toJson(rpcResultData);
    }

    public static String createJson(long id, String method, Object result, Exception error) {
        RPCResultData rpcResultData = new RPCResultData(id, method, result, error);
        return new Gson().toJson(rpcResultData);
    }


    /***********************
     * decode
     ***********************/
    public static boolean isEncrypRequestMessage(String msg) throws JsonSyntaxException {
        boolean isEncrypt = true;
        JsonObject jsonObject = new Gson().fromJson(msg, JsonObject.class);

        if (jsonObject.has(RPCCommand.TAG_HASH)
                && jsonObject.has(RPCCommand.TAG_PAYLOAD) ) {
            isEncrypt = false;
        }

        return isEncrypt;
    }

    public static boolean hasJsonObject(String msg, String type) {
        boolean hasData = false;
        try {
            JsonObject jsonObject = new Gson().fromJson(msg, JsonObject.class);
            if (jsonObject.has(type)) {
                hasData = true;
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return hasData;
    }

    public static String getDecodeMessageTokenHash(String msg) throws ParseException {
        return getDecodeMessage(msg, RPCCommand.TAG_HASH);
    }

    public static String getDecodeMessagePayload(String msg) throws ParseException {
        return getDecodeMessage(msg, RPCCommand.TAG_PAYLOAD);
    }

    public static String getDecodeMessageMethod(String msg) throws ParseException {
        return getDecodeMessage(msg, RPCCommand.TAG_METHOD);
    }

    public static String getDecodeMessageParam(String msg) throws ParseException {
        return getDecodeMessage(msg, RPCCommand.TAG_PARAMS);
    }

    public static Object[] getDecodeMessageParams(String msg) {
        JsonObject jsonObject = new Gson().fromJson(msg, JsonObject.class);
        JsonArray jsonArray = jsonObject.getAsJsonArray(RPCCommand.TAG_PARAMS);
        String[] params = new Gson().fromJson(jsonArray, String[].class);

        return params;
    }

    public static long getDecodeMessageId(String msg) throws ParseException {
        return getDecodeMessageLong(msg, RPCCommand.TAG_ID);
    }




    public static String getDecodeMessage(String msg, String type) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(msg);

        Object object = jsonObject.get(type);
        return object.toString();
    }

    public static int getDecodeMessageInteger(String msg, String type) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(msg);

        Object object = jsonObject.get(type);
        int result = Integer.parseInt(object.toString());
        return result;
    }

    public static long getDecodeMessageLong(String msg, String type) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(msg);

        Object object = jsonObject.get(type);
        long result = Long.parseLong(object.toString());
        return result;
    }
}
