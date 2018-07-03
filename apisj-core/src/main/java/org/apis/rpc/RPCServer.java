package org.apis.rpc;

import com.google.gson.Gson;
import org.apis.crypto.HashUtil;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
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
        if (onPermission) {
            System.out.println("[onMessage] " + message);
            conn.send(message);
        }
    }

    // hash 함수 체크
    @Override
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

        }else {
            System.out.println("============ non pass ====================");
            setOnPermission(false);
            if (conn.isOpen()) {
                conn.close();
            }
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


    public byte[] getAuthHash() {
        String id = "jk"; // 임의
        String pw = "test";
        AuthData authData = new AuthData("auth", id, pw);
        String authDataJson = new Gson().toJson(authData);
        byte[] hash = HashUtil.sha3(ByteUtil.merge(authDataJson.getBytes()));
        return  hash;
    }
}


class AuthData {
    public String type, id, pw;

    public AuthData(String type, String id, String pw) {
        this.type = type;
        this.id = id;
        this.pw = pw;
    }
}
