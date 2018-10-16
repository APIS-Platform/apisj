package org.apis.rpc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumFactory;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.BadPaddingException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

public class RPCServer extends WebSocketServer {
    // temp //////
    String tempID;
    String tempPassword;
    /////////////
    @Autowired
    protected static Ethereum mEthereum;

    public Timer connectionTimeoutTimer;
    public TimerTask connectionTimeoutTimerTask;

    private Map<String, Client> userMap = new HashMap<String, Client>();
    private static final int TIMEOUT_PERIOD = 5 * 1000;
    private char[] allow_ip;
    private int max_connections = Integer.MAX_VALUE; // default

    private int errorCode = Command.ERROR_CODE_UNNKOWN;
    private String errorMessage = Command.ERROR_DEPORT_UNKNOWN;

    public RPCServer(int port, String id, char[] pw, Ethereum ethereum) {
        super(new InetSocketAddress(port));
        tempID = id;
        tempPassword = new String(pw);

        mEthereum = ethereum;
        mDisportThread.start();
    }

    public RPCServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));

        mDisportThread.start();
        mEthereum = EthereumFactory.createEthereum();
    }

    public RPCServer( InetSocketAddress address ) {
        super( address );
    }

    public void setIPConnections(char[] ip, int max) {
        this.allow_ip = ip;
        this.max_connections = max;
    }

    private void cancelTimeout() {
        if (connectionTimeoutTimer!= null) {
            connectionTimeoutTimer.cancel();
            connectionTimeoutTimer = null;
        }
        if (connectionTimeoutTimerTask != null) {
            connectionTimeoutTimerTask.cancel();
            connectionTimeoutTimerTask = null;
        }
    }

    private void onDeportClient(WebSocket conn) {
        if (conn.isOpen()) {
            // send error message
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(Command.TYPE_CODE, errorCode);
            jsonObject.addProperty(Command.TYPE_ERROR, errorMessage);
            conn.send(new Gson().toJson(jsonObject));

            // close
            conn.close();
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        ConsoleUtil.printBlue("=========== connected client : " + conn.getLocalSocketAddress() + " ===========\n");

        connectionTimeoutTimer = new Timer();
        connectionTimeoutTimerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("time out :" + conn.getRemoteSocketAddress());
                if (conn.isOpen()) {
                    conn.close();
                }
            }
        };
        connectionTimeoutTimer.schedule(connectionTimeoutTimerTask, TIMEOUT_PERIOD);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String hostAddress = conn.getRemoteSocketAddress().toString();
        System.out.println("close host:" + hostAddress);

        if (userMap.get(hostAddress) == null) {
            ConsoleUtil.printBlue("Unregistered client\n");
        }
        else {
            // 서버 등록 삭제
            ConsoleUtil.printBlue("remove Client\n");
            userMap.remove(hostAddress);
        }


//        broadcast( conn.getRemoteSocketAddress().toString() + " has left the room!" );
        ConsoleUtil.printBlue( conn + " has left the room!\n" );
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

        boolean isPermission = checkPermissionClient(conn);
        String hostAddress = conn.getRemoteSocketAddress().toString();




        // 접속 허가 전
        if (!isPermission) {

            // 생성 가능여부 판단
            if (!permissionCreateClient(conn)) {
                onDeportClient(conn);
                return;
            }

            String requestId = "";
            String type = "";
            String sAuth = "";
            String tAuth = "";

            try {
                message = JsonUtil.AESDecrypt(tempPassword, message);
                requestId = JsonUtil.getDecodeMessageRequestId(message);
                type = JsonUtil.getDecodeMessageType(message);

                // 허가전 type은 LOGIN 만 허용
                if ( !type.equals(Command.TYPE_LOGIN)) {
                    errorCode = Command.ERROR_CODE_WITHOUT_PERMISSION_TYPE;
                    errorMessage = Command.ERROR_DEPORT_WITHOUT_PERMISSION_TYPE;
                    onDeportClient(conn);
                    return;
                }



                // compare
                tAuth = JsonUtil.getDecodeMessageAuth(message);
                String salt = JsonUtil.getSalt(tAuth);
                String iv = JsonUtil.getIv(tAuth);
                sAuth = JsonUtil.createAuth(salt, iv, tempID, tempPassword.toCharArray());

                if (tAuth.equals(sAuth)) {
                    ConsoleUtil.printBlue("============ auth key pass ====================\n");
                    cancelTimeout();

                    // create client(token) & register
                    byte[] token = JsonUtil.createToken(tAuth, hostAddress);

                    Client clientInfo = new Client(conn, tAuth.getBytes(), conn.getRemoteSocketAddress(), token);
                    System.out.println("register: "  + hostAddress);
                    userMap.put(hostAddress, clientInfo); // register

                    // success - send token (AES - encrypt)
                    // address data
                    String tokenEnc = JsonUtil.AESEncrypt(tempPassword, ByteUtil.toHexString(token));
                    JsonObject tokenData = new JsonObject();
                    tokenData.addProperty(Command.TYPE_TOKEN, tokenEnc);
                    String tokenJson = JsonUtil.createJson(false, requestId, Command.TYPE_TOKEN, tokenData);

                    tokenJson = JsonUtil.AESEncrypt(tempPassword, tokenJson); // 해당부분만 다른 phrase로 encrypt
                    conn.send(tokenJson);
                }

                else {
                    ConsoleUtil.printBlue("============ non pass ====================\n");
                    errorCode = Command.ERROR_CODE_WRONG_AUTHKEY;
                    errorMessage = Command.ERROR_DEPORT_WRONG_AUTHKEY;
                    onDeportClient(conn);
                    return;
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();

                errorCode = Command.ERROR_CODE_WRONG_ID_PASSWORD;
                errorMessage = Command.ERROR_DEPORT_WRONG_ID_PASSWORD;
                onDeportClient(conn);
                return;

            } catch (ParseException e) {
                e.printStackTrace();
            }


            // 접속 허용되지 않으면 disconnect
            if (!checkPermissionClient(conn)) {
                errorCode = Command.ERROR_CODE_WITHOUT_PERMISSION_CLIENT;
                errorMessage = Command.ERROR_DEPORT_WITHOUT_PERMISSION_CLIENT;
                onDeportClient(conn);
                return;
            }
        }

        // 접속 허가 후
        // 1. 데이터는 AES decrypt 하여 분석
        // 2. token을 검사
        else {
            try {
                // data decrypt
                byte[] token = userMap.get(hostAddress).getToken();
                message = JsonUtil.AESDecrypt(ByteUtil.toHexString(token), message);
                System.out.println("dec:" + message);

                // 접속 허가 후 requestId 검사
                if (!checkRequestId(hostAddress, message)) {
                    onDeportClient(conn);
                    return;
                }

                // 접속 허가 후 token 검사
                if (!checkPermissionToken(hostAddress, message)) {
                    onDeportClient(conn);
                    return;
                }


                String requestId = null;
                String request = null;

                requestId = JsonUtil.getDecodeMessageRequestId(message);
                request = JsonUtil.getDecodeMessageType(message);

                if (request != null) {
                    // 정상적 json 파일을 받은 경우 접속기간을 증가
                    userMap.get(hostAddress).initConnectTime();

                    byte[] sToken = userMap.get(hostAddress).getToken();
                    Command.conduct(mEthereum, conn, sToken,
                            requestId, request, message);

                }
            } catch (IllegalStateException e) {
                e.printStackTrace();

                errorCode = Command.ERROR_CODE_WRONG_TOKENKEY;
                errorMessage = Command.ERROR_DEPORT_WRONG_TOKENKEY;
                onDeportClient(conn);
                return;
            } catch (ParseException e) {
                e.printStackTrace();
            }


            System.out.println("[onMessage] " + message);
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
        ConsoleUtil.printBlue("Server started!\n");
    }


    // 허용된 client 인지 체크
    private boolean checkPermissionClient(WebSocket conn) {
        boolean isPermission = false;

        for (String user : userMap.keySet()) {
            if (user.equals(conn.getRemoteSocketAddress().toString())) {
                isPermission = true;
                break;
            }
        }

        return isPermission;
    }


    private boolean permissionCreateClient(WebSocket conn) {
        // 접속 허용 갯수
        int currentIPCount = userMap.size();
        if (max_connections < currentIPCount) {
            ConsoleUtil.printlnRed("err. overflow allow ip (max:" + max_connections + ")");
            errorCode = Command.ERROR_CODE_OVERFLOW_MAXCONNECTION;
            errorMessage = Command.ERROR_DEPORT_OVERFLOW_MAXCONNECTION;
            return false;
        }

        // 특정아이피만 허용
        String allowIP = "/" + new String(allow_ip);
        String targetIP = conn.getRemoteSocketAddress().getAddress().toString();
        ConsoleUtil.printlnRed("allowip:" + allowIP + "\ntargetip:" + targetIP + "\nch:" + conn.getRemoteSocketAddress().toString());
        if (targetIP.equals("/0.0.0.0")) {
            ConsoleUtil.printlnRed("accept allow ip");
            return true;
        }

        if (!targetIP.equals(allowIP)) {
            ConsoleUtil.printlnRed("err. not allow ip");
            errorCode = Command.ERROR_CODE_WITHOUT_PERMISSION_IP;
            errorMessage = Command.ERROR_DEPORT_WITHOUT_PERMISSION_IP;
            return false;
        }

        // 중복 불가
        for (String user : userMap.keySet()) {

            System.out.println("userMap:"+userMap.get(user).getISocketAddress().toString());
            System.out.println("target:"+conn.getRemoteSocketAddress().toString());
            if ( userMap.get(user).getISocketAddress().getAddress().toString().equals(conn.getRemoteSocketAddress().getAddress().toString()) ) {
                ConsoleUtil.printlnRed("err. duplicate");
                errorCode = Command.ERROR_CODE_DUPLICATE_IP;
                errorMessage = Command.ERROR_DEPORT_DUPLICATE_IP;
                return false;
            }
        }

        return true;

        // 동일한 ip내에서 추가 클라이언트 허가여부
        /*if (max_connections == 0) { return true; }

        int duplicateAddressCount = 0;

        for (String user : userMap.keySet()) {

            System.out.println("userMap:"+userMap.get(user).getISocketAddress().toString());
            System.out.println("target:"+conn.getRemoteSocketAddress().toString());
            if ( userMap.get(user).getISocketAddress().getAddress().toString().equals(conn.getRemoteSocketAddress().getAddress().toString()) ) {
                System.out.println("duplicate");
                duplicateAddressCount++;
            }
        }

        if (max_connections > duplicateAddressCount) {
            return true;
        }
        else {
            return false;
        }*/
    }

    // check requestId
    private boolean checkRequestId(String host, String msg) {

        try {
            int registRequestId = userMap.get(host).getRequestId();
            int targetRequestId = Integer.parseInt(JsonUtil.getDecodeMessageRequestId(msg));

            if (registRequestId >= targetRequestId) {
                errorCode = Command.ERROR_CODE_WRONG_REQUESTID;
                errorMessage = Command.ERROR_DEPORT_WRONG_REQUESTID;
                return false;
            }
        } catch (ParseException e) {
            errorCode = Command.ERROR_CODE_WRONG_REQUESTID;
            errorMessage = Command.ERROR_DEPORT_WRONG_REQUESTID;
            e.printStackTrace();
            return false;
        } catch (NumberFormatException e) {
            errorCode = Command.ERROR_CODE_NULL_REQUESTID;
            errorMessage = Command.ERROR_DEPORT_NULL_REQUESTID;
            e.printStackTrace();
            return false;
        }

        return true;
    }
    // 허용된 메세지 (auth key 확인)
    private boolean checkPermissionToken(String host, String msg) {
        boolean isPermission = false;

        try {
            // check castoff token
            String targetTokenEnc = JsonUtil.getDecodeMessageAuth(msg);

            for(String castOffToken :userMap.get(host).getCastOffTokenList()) {
                if (targetTokenEnc.equals(castOffToken)) {
                    errorCode = Command.ERROR_CODE_WRONG_TOKENKEY;
                    errorMessage = Command.ERROR_DEPORT_WRONG_TOKENKEY;
                    return false;
                }
            }

            // check token dec
            String salt = JsonUtil.getSalt(targetTokenEnc);
            String iv = JsonUtil.getIv(targetTokenEnc);

            byte[] registToken = userMap.get(host).getToken();
            String registTokenEnc = JsonUtil.AESEncrypt(salt, iv, tempPassword, ByteUtil.toHexString(registToken));


            if (registTokenEnc.equals(targetTokenEnc)) {
                isPermission = true;
                userMap.get(host).addCastOffToken(targetTokenEnc);
            }

        } catch (ParseException e) {
            e.printStackTrace();

            errorCode = Command.ERROR_CODE_WRONG_TOKENKEY;
            errorMessage = Command.ERROR_DEPORT_WRONG_TOKENKEY;
        }

        return isPermission;
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
                            userMap.get(user).getWebSocket().close();
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
