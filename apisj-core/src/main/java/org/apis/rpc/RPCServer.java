package org.apis.rpc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apis.core.*;
import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumFactory;
import org.apis.keystore.*;
import org.apis.rpc.template.TransactionData;
import org.apis.rpc.template.TransactionReceiptData;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.blockchain.ApisUtil;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

public class RPCServer extends WebSocketServer {
    // temp //////
    String tempID = "jk";
    String tempPassword = "test";
    /////////////
    @Autowired
    protected static Ethereum mEthereum;

    public Timer connectionTimeoutTimer;
    public TimerTask connectionTimeoutTimerTask;

    private Map<String, Client> userMap = new HashMap<String, Client>();
    private static final int TIMEOUT_PERIOD = 5 * 1000;

    public RPCServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));

        mDisportThread.start();
        mEthereum = EthereumFactory.createEthereum();
    }

    public RPCServer( InetSocketAddress address ) {
        super( address );
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
            conn.close();
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("=========== connected client : " + conn.getLocalSocketAddress() + " ===========");

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
        String host = conn.getRemoteSocketAddress().getHostName();
        if (userMap.get(host).getWebSocket() == conn) {
            // 서버 등록 삭제
            System.out.println("unregist Client");
            userMap.remove(host);
        }


        broadcast( conn + " has left the room!" );
        System.out.println( conn + " has left the room!" );
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

        boolean isPermission = checkPermissionClient(conn);
        String host = conn.getRemoteSocketAddress().getHostName();




        // 접속 허가 전
        if (!isPermission) {



            String type = "";
            String sAuth = "";
            String tAuth = "";

            try {
                message = JsonUtil.AESDecrypt(tempPassword, message);

                type = JsonUtil.getDecodeMessageType(message);

                // 허가전 type은 LOGIN 만 허용
                if ( !type.equals(Command.TYPE_LOGIN)) {
                    onDeportClient(conn);
                    return;
                }



                // compare
                tAuth = JsonUtil.getDecodeMessageAuth(message);
                String salt = JsonUtil.getSalt(tAuth);
                sAuth = JsonUtil.createAuth(salt, tempID, tempPassword.toCharArray());

                if (tAuth.equals(sAuth)) {
                    System.out.println("============ pass ====================");
                    cancelTimeout();

                    // create client(token) & register
                    byte[] token = JsonUtil.createToken(tAuth, host);

                    Client clientInfo = new Client(conn, tAuth.getBytes(), conn.getRemoteSocketAddress(), token);
                    userMap.put(host, clientInfo); // register

                    // success - send token (AES - encrypt)
                    // address data
                    String tokenEnc = JsonUtil.AESEncrypt(tempPassword, ByteUtil.toHexString(token));
                    JsonObject tokenData = new JsonObject();
                    tokenData.addProperty(Command.TYPE_TOKEN, tokenEnc);
                    String tokenJson = JsonUtil.createJson(Command.TYPE_TOKEN, tokenData, false);

                    tokenJson = JsonUtil.AESEncrypt(tempPassword, tokenJson); // 해당부분만 다른 phrase로 encrypt
                    conn.send(tokenJson);
                }

                else {
                    System.out.println("============ non pass ====================");
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }


            // 접속 허용되지 않으면 disconnect
            if (!checkPermissionClient(conn)) {
                onDeportClient(conn);
                return;
            }
        }

        // 접속 허가 후
        // 1. 데이터는 AES decrypt 하여 분석
        // 2. token을 검사
        else {
            // data decrypt
            byte[] token = userMap.get(host).getToken();
            message = JsonUtil.AESDecrypt(ByteUtil.toHexString(token), message);
            System.out.println("dec:"+message);

            // 접속 허가 후 token 검사
            if (!checkAuthkey(host, message)) { // authkey가 맞지 않으면 접속해지
                onDeportClient(conn);
                return;
            }


            String request = null;

            try {
                request = JsonUtil.getDecodeMessageType(message);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (request!=null) {
                // 정상적 json 파일을 받은 경우 접속기간을 증가
                userMap.get(host).initLastTime();

                try {
                    byte[] sToken = userMap.get(host).getToken();
                    Command.conduct(mEthereum, conn, sToken, request, message);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

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
        System.out.println("Server started!");
    }


    // 허용된 client 인지 체크
    private boolean checkPermissionClient(WebSocket conn) {
        boolean isPermission = false;

        for (String user : userMap.keySet()) {
            if (user.equals(conn.getRemoteSocketAddress().getHostName())) {
                isPermission = true;
                break;
            }
        }

        return isPermission;
    }

    // 허용된 메세지 (auth key 확인)
    private boolean checkAuthkey(String host, String msg) {
        boolean isPermission = false;

        try {

            String tToken = JsonUtil.getDecodeMessageAuth(msg);
            String salt = JsonUtil.getSalt(tToken);

            byte[] sToken = userMap.get(host).getToken();
            String sTokenEnc = JsonUtil.AESEncrypt(salt, tempPassword, ByteUtil.toHexString(sToken));


            if (sTokenEnc.equals(tToken)) {
                isPermission = true;
            }

        } catch (ParseException e) {
            e.printStackTrace();
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
                        if (userMap.get(user).getLastTime() + REMAIN_CONNECTSTAY_PERIOD < currentTime) {
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
