package org.apis.rpc;

import com.google.gson.*;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.http.conn.util.InetAddressUtils;
import org.apis.crypto.HashUtil;
import org.apis.facade.Ethereum;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.Base64;
import org.json.JSONException;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RPCWebSocketServer extends WebSocketServer {
    private Logger logger = LoggerFactory.getLogger("rpc");

    private String serverID;
    private char[] serverPW;

    private List<String> allowedAddressList = new ArrayList<>();
    private int maxConnections;
    private Map<String, Client> userMap = new HashMap<>();

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
//        mDisportThread.start();
    }


    public void setConnectionRule(String addresses, int max) {
        String[] addressArray = addresses.split(",");
        for(String address : addressArray) {
            address = address.trim();
            if(InetAddressValidator.getInstance().isValid(address)) {
                this.allowedAddressList.add(address);
            }
        }
        this.maxConnections = max;
    }

    private void deportClient(WebSocket conn) {
        if (conn.isOpen()) {
            // send error message
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(RPCCommand.TAG_CODE, errorCode);
            jsonObject.addProperty(RPCCommand.TAG_ERROR, errorMsg);
            conn.send(new Gson().toJson(jsonObject));

            // close
            conn.close();

            String hostAddress = conn.getRemoteSocketAddress().toString();
            logger.debug(ConsoleUtil.colorYellow("[onClose] client host:" + hostAddress));
            logger.debug(ConsoleUtil.colorYellow("[onClose] client error (code:" + errorCode + ") " + errorMsg));
        }
    }

    // header check
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String hostAddress = conn.getRemoteSocketAddress().toString();
        logger.debug(ConsoleUtil.colorCyan("Websocket client connected : " + hostAddress));

        // 서버 설정에 따른 생성 가능여부 판단
        if (!isValidConnection(conn)) {
            deportClient(conn);
            return;
        }

        // authkey 검증
        String authkey = handshake.getFieldValue(RPCCommand.TAG_AUTHKEY); // header authKey

        if(authkey == null || authkey.isEmpty()) {
            authkey = handshake.getResourceDescriptor().replace("/?authkey=", "");
            try {
                authkey = new String(Base64.decode(authkey), Charset.forName("UTF-8"));
            } catch (IOException | IllegalArgumentException e ) {
                authkey = "";
            }
        }

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
                deportClient(conn);
                return;
            }
        }


        // result
        // pass
        if (guestIDHash.equals(serverIDHash)) {
            // register client
            registerClient(conn, authkey, hostAddress);

            // send token (AES encrypt)
            byte[] token = userMap.get(hostAddress).getToken();
            logger.debug(ConsoleUtil.colorCyan("Token of client[{}] : {}"), hostAddress, ByteUtil.toHexString(token));

            String tokenEnc = RPCJsonUtil.AESEncrypt(new String(serverPW), ByteUtil.toHexString(token));
            String tokenJson = RPCJsonUtil.createJson(0, RPCCommand.TAG_TOKEN, tokenEnc);
            tokenJson = RPCJsonUtil.AESEncrypt(new String(serverPW), tokenJson);
            conn.send(tokenJson);
        }

        else {
            logger.debug(ConsoleUtil.colorBRed("Client authentication failed."));
            errorCode = RPCCommand.ERROR_CODE_WRONG_AUTHKEY;
            errorMsg = RPCCommand.ERROR_DEPORT_WRONG_AUTHKEY;
            deportClient(conn);
        }
    }



    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String hostAddress = conn.getRemoteSocketAddress().toString();
        logger.debug(ConsoleUtil.colorYellow("Client connection closed : {}"), hostAddress);

        if (userMap.get(hostAddress) == null) {
            logger.debug(ConsoleUtil.colorCyan("An unregistered user has been closed."));
        }
        else {
            userMap.remove(hostAddress);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String clientAddress = conn.getRemoteSocketAddress().toString();
        Client client = userMap.get(clientAddress);
        logger.debug(ConsoleUtil.colorCyan("[onMessage] Received from [{}] : {}"), clientAddress, message);

        // ID와 PW 인증된 사용자가 아니면 접속을 종료한다.
        if (!isApprovedClient(clientAddress)) {
            errorCode = RPCCommand.ERROR_CODE_WITHOUT_PERMISSION_CLIENT;
            errorMsg = RPCCommand.ERROR_DEPORT_WITHOUT_PERMISSION_CLIENT;
            deportClient(conn);
            return;
        }

        // 메시지가 암호화 되어있는지 여부를 검사한다.
        boolean isEncrypted = true;
        try {
            isEncrypted = RPCJsonUtil.isEncrypRequestMessage(message);
        } catch (JsonSyntaxException | JSONException e) {
            logger.trace(ConsoleUtil.colorCyan("[onMessage] Received messages are encrypted."));
        }


        byte[] tokenByte = client.getToken();
        String token = ByteUtil.toHexString(tokenByte);

        // 암호화 된 메시지는 복호화를 진행한다.
        if (isEncrypted) {
            message = RPCJsonUtil.AESDecrypt(token, message);
            logger.debug(ConsoleUtil.colorCyan("[onMessage] The decrypted message is: {}"), message);
        }

        // payload
        String payload;
        try {
            payload = RPCJsonUtil.getDecodeMessagePayload(message);
        } catch (ParseException | IllegalStateException e) {
            e.printStackTrace();
            conn.send(RPCCommand.ERROR_MESSAGE_UNKNOWN);
            return;
        }

        // check id
        if (!isValidId(client, payload)) {
            deportClient(conn);
            return;
        }

        // check token
        if (!checkTokenHash(client, message)) {
            deportClient(conn);
            return;
        }

        // check message
        try {
            String method = RPCJsonUtil.getDecodeMessageMethod(payload);

            if (method != null && !method.isEmpty()) {
                // 정상적 json 파일을 받은 경우 접속기간을 증가
                client.refreshLastDiscoveredTime();

                // conduct
                RPCCommand.conduct(mEthereum, conn, token, payload, isEncrypted);
                client.addID();
            }
        } catch (ParseException | IllegalStateException e) {
            e.printStackTrace();
            conn.send(RPCCommand.ERROR_MESSAGE_UNKNOWN);
            deportClient(conn);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if( conn != null ) {
            // some errors like port binding failed may not be assignable to a specific websocket
            deportClient(conn);
        }
    }

    @Override
    public void onStart() {
        ConsoleUtil.printBlue("RPC Server started!\n");
    }

    private boolean isValidConnection(WebSocket conn) {
        // 접속 허용 갯수
        int clientSize = userMap.size();
        if (maxConnections > 0 && maxConnections < clientSize) {
            logger.warn(ConsoleUtil.colorBRed("The allowed clients are full. (max: {})"), maxConnections);
            errorCode = RPCCommand.ERROR_CODE_OVERFLOW_MAXCONNECTION;
            errorMsg = RPCCommand.ERROR_DEPORT_OVERFLOW_MAXCONNECTION;
            return false;
        }

        // 특정아이피만 허용
        if(allowedAddressList.indexOf("0.0.0.0") < 0) {
            String guestAddress = conn.getRemoteSocketAddress().getAddress().toString();

            if(allowedAddressList.indexOf(guestAddress) < 0) {
                errorCode = RPCCommand.ERROR_CODE_WITHOUT_PERMISSION_IP;
                errorMsg = RPCCommand.ERROR_DEPORT_WITHOUT_PERMISSION_IP;
                logger.warn(ConsoleUtil.colorBRed("The IP address of the client[{}] can not be found in the whitelist."), guestAddress);
                return false;
            }
        }

        return true;
    }

    // client 등록
    private void registerClient(WebSocket conn, String authKey, String hostAddress) {
        // create client(token) & register
        byte[] token = RPCJsonUtil.createToken(authKey, hostAddress);

        Client clientInfo = new Client(conn, authKey.getBytes(), conn.getRemoteSocketAddress(), token);
        userMap.put(hostAddress, clientInfo); // register
    }

    // 등록된 client 인지 체크

    /**
     * 입력된 IP:PORT 주소가 승인된 사용자 목록에 존재하는지 확인한다.
     * @param clientAddress 승인되었는지 확인하려는 주소
     * @return TRUE : 승인된 주소, FALSE : 승인되지 않은 주소
     */
    private boolean isApprovedClient(String clientAddress) {
        for (String user : userMap.keySet()) {
            if (user.equals(clientAddress)) {
                return true;
            }
        }

        return false;
    }

    // check id
    private boolean isValidId(Client client, String msg) {
        boolean result = false;
        try {
            long currentId = client.getID();
            long messageId = RPCJsonUtil.getDecodeMessageId(msg);

            if (currentId >= messageId) {
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
    private boolean checkTokenHash(Client client, String msg) {
        boolean result = false;

        try {
            String messageTokenHash = RPCJsonUtil.getDecodeMessageTokenHash(msg);

            // check token dec
            String clientPayload = RPCJsonUtil.getDecodeMessagePayload(msg);
            String clientToken = ByteUtil.toHexString(client.getToken());
            String clientTokenHash = RPCJsonUtil.createTokenHash(clientPayload, clientToken);

            // pass
            if (clientTokenHash.equals(messageTokenHash)) {
                result = true;
            } else {
                logger.debug(ConsoleUtil.colorCyan("[onMessage] Message token does not match. Expected[{}]  Received[{}]"), clientTokenHash, messageTokenHash);
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
    /*private static final int REMAIN_CONNECTSTAY_PERIOD = 1000 * 60 * 10;
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
                            deportClient(userMap.get(user).getWebSocket());
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
    });*/
}
