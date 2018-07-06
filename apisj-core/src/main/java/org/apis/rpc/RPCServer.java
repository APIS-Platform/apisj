package org.apis.rpc;

import com.google.gson.Gson;
import org.apis.crypto.HashUtil;
import org.apis.json.AuthData;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class RPCServer extends WebSocketServer {

    public Timer connectionTimeoutTimer;
    public TimerTask connectionTimeoutTimerTask;

    private boolean onPermission;
    private static final int TIMEOUT_PERIOD = 5 * 1000;

    public RPCServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public RPCServer( InetSocketAddress address ) {
        super( address );
    }

    public static void main( String[] args ) throws InterruptedException , IOException {
        WebSocketImpl.DEBUG = true;
        int port = 8887; // 843 flash policy port
        try {
            port = Integer.parseInt( args[ 0 ] );
        } catch ( Exception ex ) {
        }
        RPCServer s = new RPCServer( port );
        s.start();
        System.out.println( "ChatServer started on port: " + s.getPort() );

        BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
        while ( true ) {
            String in = sysin.readLine();
            s.broadcast( in );
            if( in.equals( "exit" ) ) {
                s.stop(1000);
                break;
            }
        }

        //  HashUtil.sha3(ByteUtil.merge())
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

    private void setOnPermission(boolean permission) {
        onPermission = permission;
    }

    private void onDeportClient(WebSocket conn) {
        setOnPermission(false);
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
        broadcast( conn + " has left the room!" );
        System.out.println( conn + " has left the room!" );
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (!onPermission) { // 접속 허가 전

            String type = "";
            String key = "";

            try {
                type = getDecodeMessage(message, "type");
                key = getDecodeMessage(message, "key");


                // 허가전 type은 LOGIN 만 허용
                if ( !type.equals("LOGIN")) {
                    onDeportClient(conn);
                    return;
                }


                if (FastByteComparisons.equal(createKey("jk","test".toCharArray()), Hex.decode(key))) {
                    System.out.println("============ pass ====================");
                    cancelTimeout();
                    setOnPermission(true);

                    conn.send("LOGIN_SUCCESS");
//                    byte[] succByte = new byte[] {
//                            (byte)0x53, (byte)0x75, (byte)0x63
//                    };
//                    conn.send(succByte);

                }else {
                    System.out.println("============ non pass ====================");
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }



            if (!onPermission) {
                onDeportClient(conn);
                return;
            }

        }
        else { // 접속 허가 후
            System.out.println("[onMessage] " + message);
            conn.send(message);
        }
    }

    private String getDecodeMessage(String msg, String kind) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(msg);

        String result = (String) object.get(kind);
        return result;
    }


    // hash 함수 체크
    /*@Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        super.onMessage(conn, message);

        System.out.println("[onMessage] - byte ");
        System.out.println("================================");
        System.out.println("=========== get hash ==========");
        System.out.println("================================");

        byte[] hash = new byte[message.remaining()];
        message.get(hash, 0, hash.length);


        if (FastByteComparisons.equal(getAuthHash(), hash)) {
            System.out.println("============ pass ====================");
            cancelTimeout();
            setOnPermission(true);

            byte[] succByte = new byte[] {
                    (byte)0x53, (byte)0x75, (byte)0x63
            };
            conn.send(succByte);

        }else {
            System.out.println("============ non pass ====================");
            setOnPermission(false);
            if (conn.isOpen()) {
                conn.close();
            }
        }



    }*/

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

    private byte[] createKey(String id, char[] pw) {
        byte[] byteID = HashUtil.sha3( id.getBytes() );
        byte[] bytePW = HashUtil.sha3( new String(pw).getBytes() );
        byte[] byteKey = ByteUtil.merge(byteID, bytePW);

        return byteKey;
    }


    public String getAuthHash() {
        String id = "jk"; // 임의
        char[] pw = "test".toCharArray();
        String authDataJson = createAuth(id, pw);
        return authDataJson;
//
//        byte[] hash = HashUtil.sha3(ByteUtil.merge(authDataJson.getBytes()));
//        return  hash;
    }

    public String createAuth(String id, char[] pw) {
        byte[] byteID = HashUtil.sha3( id.getBytes() );
        byte[] bytePW = HashUtil.sha3( new String(pw).getBytes() );
        byte[] byteKey = ByteUtil.merge(byteID, bytePW);


        AuthData authData = new AuthData("LOGIN", Hex.toHexString(byteKey));
        String authDataJson = new Gson().toJson(authData);
        return authDataJson;
    }
}

