package org.apis.rpc;

import com.google.gson.*;
import org.apis.crypto.HashUtil;
import org.apis.facade.Ethereum;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.bouncycastle.util.encoders.Hex;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class RPCWebSocketServer extends WebSocketServer {
    private String serverID;
    private char[] serverPW;

    private char[] allowIP;
    private int maxConnections;
    private Map<String, Client> userMap = new HashMap<String, Client>();

    private int errorCode = RPCCommand.ERROR_CODE_UNNKOWN;
    private String errorMsg = RPCCommand.ERROR_DEPORT_UNKNOWN;

//    Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    protected static Ethereum mEthereum;


    public RPCWebSocketServer(int port, String id, char[] pw, Ethereum ethereum) {
        super(new InetSocketAddress(port));
        serverID = id;
        serverPW = pw;
        mEthereum = ethereum;
        mDisportThread.start();
    }

    public void setIPConnections(char[] ip, int max) {
        this.allowIP = ip;
        this.maxConnections = max;
    }

    private void onDeportClient(WebSocket conn) {
        if (conn.isOpen()) {
            // send error message
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(RPCCommand.TAG_CODE, errorCode);
            jsonObject.addProperty(RPCCommand.TAG_ERROR, errorMsg);
            conn.send(new Gson().toJson(jsonObject));

            // close
            conn.close();

            String hostAddress = conn.getRemoteSocketAddress().toString();
            ConsoleUtil.printlnYellow("[onClose] client host:" + hostAddress);
            ConsoleUtil.printlnYellow("[onClose] client error (code:" + errorCode + ") " + errorMsg);
        }
    }

    // header check
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String hostAddress = conn.getRemoteSocketAddress().toString();
        ConsoleUtil.printlnBlue("=========== access client : " + hostAddress + " ===========\n");

        // 서버 설정에 따른 생성 가능여부 판단
        if (!permissionClientServerSetting(conn)) {
            onDeportClient(conn);
            return;
        }

        // authkey 검증
        String authkey = handshake.getFieldValue(RPCCommand.TAG_AUTHKEY); // header authKey

        byte[] serverIDHashByte = HashUtil.sha3( serverID.getBytes() );
        byte[] serverPWHashByte = HashUtil.sha3( new String(serverPW).getBytes() );
        String serverIDHash = ByteUtil.toHexString(serverIDHashByte);
        String serverPWHash = ByteUtil.toHexString(serverPWHashByte);


        String guestIDHash = null;
        try {
            guestIDHash = RPCJsonUtil.AESDecrypt(serverPWHash, authkey);
        } catch (IllegalStateException | StringIndexOutOfBoundsException e) {
            e.printStackTrace();
        } finally {
            if (guestIDHash==null) {
                errorCode = RPCCommand.ERROR_CODE_WRONG_ID_PASSWORD;
                errorMsg = RPCCommand.ERROR_DEPORT_WRONG_ID_PASSWORD;
                onDeportClient(conn);
                return;
            }
        }


        // result
        // pass
        if (guestIDHash.equals(serverIDHash)) {
            ConsoleUtil.printBlue("============ auth key pass ====================\n");
            // register client
            registerClient(conn, authkey, hostAddress);

            // send token (AES encrypt)
            byte[] token = userMap.get(hostAddress).getToken();
            String tokenEnc = RPCJsonUtil.AESEncrypt(new String(serverPW), ByteUtil.toHexString(token));
//            JsonObject tokenData = new JsonObject();
//            tokenData.addProperty(RPCCommand.TAG_TOKEN, tokenEnc);
//            String tokenJson = RPCJsonUtil.createJson(0, Command_useless.TYPE_TOKEN, tokenData);
            String tokenJson = RPCJsonUtil.createJson(0, RPCCommand.TAG_TOKEN, tokenEnc);
            tokenJson = RPCJsonUtil.AESEncrypt(new String(serverPW), tokenJson);
            conn.send(tokenJson);
        }

        else {
            ConsoleUtil.printBlue("============ non pass ====================\n");
            errorCode = RPCCommand.ERROR_CODE_WRONG_AUTHKEY;
            errorMsg = RPCCommand.ERROR_DEPORT_WRONG_AUTHKEY;
            onDeportClient(conn);
        }

        // temp
       /* RPCRequestData rpcResultData = new RPCRequestData("eth_getbalance", new String[]{}, 3);
        JsonElement a =new GsonBuilder().create().toJsonTree(rpcResultData);
        ConsoleUtil.printlnYellow(a.toString());


        JsonObject test = new JsonObject();
        test.addProperty("hash", "0x234234234");
        test.add("payload", a);
        conn.send(test.toString());
        ConsoleUtil.printlnYellow(test.toString());*/
    }



    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String hostAddress = conn.getRemoteSocketAddress().toString();
        ConsoleUtil.printlnBlue("close client (host):" + hostAddress);

        if (userMap.get(hostAddress) == null) {
            ConsoleUtil.printlnBlue("Unregistered client");
        }
        else {
            // 서버 등록 삭제
            userMap.remove(hostAddress);
            ConsoleUtil.printlnBlue("remove Client");
        }

        ConsoleUtil.printlnBlue( conn + " has left the room" );
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        ConsoleUtil.printlnBlue("[onMessage] message:" + message);
        String hostAddress = conn.getRemoteSocketAddress().toString();

        // 허가된 사용자 여부 체크
        if (!checkRegistClient(conn)) {
            errorCode = RPCCommand.ERROR_CODE_WITHOUT_PERMISSION_CLIENT;
            errorMsg = RPCCommand.ERROR_DEPORT_WITHOUT_PERMISSION_CLIENT;
            onDeportClient(conn);
            return;
        }

        // 메시지 암호화 여부 검사
        boolean isEncryt = true;
        try {
            isEncryt = RPCJsonUtil.isEncrypRequestMessage(message);
        } catch (JsonSyntaxException | JSONException e) {
//            e.printStackTrace();
            ConsoleUtil.printlnRed("[onMessage] is encrypt");
        }

        // decryt
        byte[] tokenByte = userMap.get(hostAddress).getToken();
        String token = ByteUtil.toHexString(tokenByte);
        if (isEncryt) {
            message = RPCJsonUtil.AESDecrypt(token, message);
            ConsoleUtil.printlnBlue("[onMessage] message(decrypt):" + message);
        }

        // payload
        String payload;
        try {
            payload = RPCJsonUtil.getDecodeMessagePayload(message);
            ConsoleUtil.printlnBlue("[onMessage] payload:" + payload);
        } catch (ParseException | IllegalStateException e) {
            e.printStackTrace();
            conn.send(RPCCommand.ERROR_MESSAGE_UNKNOWN);

            return;
        }

        // check id
        if (!checkID(hostAddress, payload)) {
            onDeportClient(conn);
            return;
        }

        // check token
        if (!checkTokenHash(hostAddress, message)) {
            onDeportClient(conn);
            return;
        }

        // check message
        try {
            String method = RPCJsonUtil.getDecodeMessageMethod(payload);
            ConsoleUtil.printlnBlue("[onMessage] method:" + method);

            if (method!=null && !method.equals("")) {
                // 정상적 json 파일을 받은 경우 접속기간을 증가
                userMap.get(hostAddress).initConnectTime();

                // conduct
                RPCCommand.conduct(mEthereum, conn, token, payload, isEncryt);
            }
        } catch (ParseException | IllegalStateException e) {
            e.printStackTrace();
            conn.send(RPCCommand.ERROR_MESSAGE_UNKNOWN);
        }

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if( conn != null ) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart() {
        ConsoleUtil.printBlue("RPC Server started!\n");
    }

    private boolean permissionClientServerSetting(WebSocket conn) {
        // 접속 허용 갯수
        int currentIPCount = userMap.size();
        if (maxConnections < currentIPCount) {
            ConsoleUtil.printlnRed("err. overflow allow ip (max:" + maxConnections + ")");
            errorCode = RPCCommand.ERROR_CODE_OVERFLOW_MAXCONNECTION;
            errorMsg = RPCCommand.ERROR_DEPORT_OVERFLOW_MAXCONNECTION;
            return false;
        }

        // 특정아이피만 허용
        String serverAllowIP = "/" + new String(allowIP);
        String guestIP = conn.getRemoteSocketAddress().getAddress().toString();
        ConsoleUtil.printlnRed("allow ip:" + serverAllowIP + "\nguest ip:" + guestIP + "\nch:" + conn.getRemoteSocketAddress().toString());
        if (guestIP.equals("/0.0.0.0")) {
            ConsoleUtil.printlnRed("accept allow ip");
            return true;
        }

        if (!guestIP.equals(serverAllowIP)) {
            ConsoleUtil.printlnRed("err. not allow ip");
            errorCode = RPCCommand.ERROR_CODE_WITHOUT_PERMISSION_IP;
            errorMsg = RPCCommand.ERROR_DEPORT_WITHOUT_PERMISSION_IP;
            return false;
        }

        // 중복 불가
        ConsoleUtil.printlnBlue("guest:"+conn.getRemoteSocketAddress().toString());
        for (String user : userMap.keySet()) {
            ConsoleUtil.printlnRed("list:"+userMap.get(user).getISocketAddress().toString());
            if ( userMap.get(user).getISocketAddress().getAddress().toString().equals(conn.getRemoteSocketAddress().getAddress().toString()) ) {
                ConsoleUtil.printlnRed("err. duplicate");
                errorCode = RPCCommand.ERROR_CODE_DUPLICATE_IP;
                errorMsg = RPCCommand.ERROR_DEPORT_DUPLICATE_IP;
                return false;
            }
        }

        return true;
    }

    // client 등록
    private void registerClient(WebSocket conn, String authkey, String hostAddress) {
        // create client(token) & register
        byte[] token = RPCJsonUtil.createToken(authkey, hostAddress);

        Client clientInfo = new Client(conn, authkey.getBytes(), conn.getRemoteSocketAddress(), token);
        System.out.println("register: " + hostAddress);
        userMap.put(hostAddress, clientInfo); // register
    }

    // 등록된 client 인지 체크
    private boolean checkRegistClient(WebSocket conn) {
        boolean isRegister = false;

        for (String user : userMap.keySet()) {
            if (user.equals(conn.getRemoteSocketAddress().toString())) {
                isRegister = true;
                break;
            }
        }

        return isRegister;
    }

    // check id
    private boolean checkID(String host, String msg) {
        boolean result = false;
        try {
            long registID = userMap.get(host).getID();
            long clientID = RPCJsonUtil.getDecodeMessageId(msg);

            if (registID >= clientID) {
                errorCode = RPCCommand.ERROR_CODE_WRONG_ID;
                errorMsg = RPCCommand.ERROR_DEPORT_WRONG_ID;
            }
            else {
                result = true;
            }
        } catch (ParseException e) {
            errorCode = RPCCommand.ERROR_CODE_WRONG_ID;
            errorMsg = RPCCommand.ERROR_DEPORT_WRONG_ID;
            e.printStackTrace();
        } catch (NumberFormatException e) {
            errorCode = RPCCommand.ERROR_CODE_NULL_ID;
            errorMsg = RPCCommand.ERROR_DEPORT_NULL_ID;
            e.printStackTrace();
        }

        return result;
    }

    // check token key
    private boolean checkTokenHash(String host, String msg) {
        boolean result = false;

        try {
            // check castoff token
            String clientTokenHash = RPCJsonUtil.getDecodeMessageTokenHash(msg);

            for(String castOffToken :userMap.get(host).getCastOffTokenHashList()) {
                if (clientTokenHash.equals(castOffToken)) {
                    throw new Exception();
                }
            }

            // check token dec
            String clientPayload = RPCJsonUtil.getDecodeMessagePayload(msg);
            byte[] registTokenByte = userMap.get(host).getToken();
            String registToken = ByteUtil.toHexString(registTokenByte);
            String registTokenHash = RPCJsonUtil.createTokenHash(clientPayload, registToken);

            // pass
            if (registTokenHash.equals(clientTokenHash)) {
                result = true;
                userMap.get(host).addCastOffTokenHash(clientTokenHash);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!result) {
            errorCode = RPCCommand.ERROR_CODE_WRONG_TOKENKEY;
            errorMsg = RPCCommand.ERROR_DEPORT_WRONG_TOKENKEY;
        }

        return result;
    }


    // check client connect time
    private static final int REMAIN_CONNECTSTAY_PERIOD = 1000 * 60 * 10;
    private Thread mDisportThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (userMap.size() > 0) {
                    long currentTime = System.currentTimeMillis();

                    for (String user : userMap.keySet()) {
                        if (userMap.get(user).getConnectTime() + REMAIN_CONNECTSTAY_PERIOD < currentTime) {
                            errorCode = RPCCommand.ERROR_CODE_TIMEOUT;
                            errorMsg = RPCCommand.ERROR_DEPORT_TIMEOUT;
                            onDeportClient(userMap.get(user).getWebSocket());
                        }
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });
}
