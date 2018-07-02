package org.apis.rpc;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class RPCServer extends WebSocketServer {

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
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("conn getLocalSocketAddress :" + conn.getLocalSocketAddress());
        System.out.println("conn getAddress :" + conn.getRemoteSocketAddress().getAddress());
        System.out.println("conn:" + conn.getRemoteSocketAddress().getHostName() + "   string:" + conn.getRemoteSocketAddress().getHostString() );

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        broadcast( conn + " has left the room!" );
        System.out.println( conn + " has left the room!" );
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        conn.send(message);
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
}
