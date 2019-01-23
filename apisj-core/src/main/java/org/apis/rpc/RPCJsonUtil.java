package org.apis.rpc;

import com.google.gson.*;
import org.apis.crypto.HashUtil;
import org.apis.rpc.template.MessageApp3;
import org.apis.util.AesUtil;
import org.apis.util.ByteUtil;
import org.apis.util.TimeUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.spongycastle.util.encoders.Hex;

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
        long current = TimeUtils.getRealTimestamp();
        return HashUtil.sha3(
                ByteUtil.merge(auth.getBytes(), ip.getBytes(), ByteUtil.longToBytes(current))
        );
    }


    /**
     * Web3에서 전달받은 payload 내용으로부터 고유 해시값을 추출해낸다.
     * 전달받은 메시지의 해시값과 여기서 생성한 해시값이 일치해야만 인증된 사용자로 판단한다.
     *
     * @param payload web3 연결로부터 전달받은 JSON 메시지
     * @param token 사용자와 공유하는 토큰
     * @return 고유 해시 값
     */
    static String createTokenHash(String payload, String token) {
        MessageApp3 message = new GsonBuilder().create().fromJson(payload, MessageApp3.class);

        byte[] methodByte = message.getMethodBytes();
        byte[] paramsByte = message.getMergedParamsBytes();
        byte[] idByte = message.getIdBytes();
        byte[] tokenByte = Hex.decode(token);

        byte[] tokenHashByte = ByteUtil.merge(methodByte, paramsByte, idByte, tokenByte);
        tokenHashByte = HashUtil.sha3(tokenHashByte);

        return ByteUtil.toHexString(tokenHashByte);
    }

    // return json
    public static String createSubscriptJson(String subscription, String method, Object result, String error) {
        RPCSubscriptData data = new RPCSubscriptData(subscription, method, result, error);
        return new Gson().toJson(data);
    }

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
