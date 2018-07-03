package org.apis.rpc;

import com.google.gson.Gson;
import org.apis.crypto.HashUtil;
import org.apis.util.ByteUtil;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;

public class SSLClientTest_wrongPW extends WebSocketClient {
    public SSLClientTest_wrongPW(URI serverUri) { super(serverUri); }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println( "Connected" );

        // 처음 연결시 해쉬 함수전달
        System.out.println("================================");
        System.out.println("=========== send hash ==========");
        System.out.println("================================");

        String id = "jk"; // 임의
        String pw = "test2";
        String auth = createAuth(id, pw);
        byte[] hash = HashUtil.sha3(ByteUtil.merge(auth.getBytes()));
        send(hash);

//        HashUtil.sha3(ByteUtil.merge(byte[]))
    }

    @Override
    public void onMessage(String message) {
        System.out.println( "onMessage: " + message );
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println( "Disconnected" );
        System.exit( 0 );
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }


    public String createAuth(String id, String pw) {
        AuthData authData = new AuthData("auth", id, pw);
        String authDataJson = new Gson().toJson(authData);
        return authDataJson;
    }
}


class SSLRPCClient_wrongPW {
    /*
     * Keystore with certificate created like so (in JKS format):
     *
     *keytool -genkey -validity 3650 -keystore "keystore.jks" -storepass "storepassword" -keypass "keypassword" -alias "default" -dname "CN=127.0.0.1, OU=MyOrgUnit, O=MyOrg, L=MyCity, S=MyRegion, C=MyCountry"
     */
    public static void main( String[] args ) throws Exception {
        WebSocketImpl.DEBUG = true;

        SSLClientTest_wrongPW sslClient = new SSLClientTest_wrongPW( new URI( "wss://localhost:8881" ) );

        // load up the key store
        String STORETYPE = "JKS";
        String KEYSTORE = "keystore.jks";
        String STOREPASSWORD = "storepassword";
        String KEYPASSWORD = "keypassword";

        URL keyUri = SSLClient.class.getClassLoader().getResource(KEYSTORE);

        KeyStore ks = KeyStore.getInstance( STORETYPE );
        File kf = new File(keyUri.toURI());//( KEYSTORE );
        ks.load( new FileInputStream( kf ), STOREPASSWORD.toCharArray() );

        KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
        kmf.init( ks, KEYPASSWORD.toCharArray() );
        TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
        tmf.init( ks );

        SSLContext sslContext = null;
        sslContext = SSLContext.getInstance( "TLS" );
        sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
        // sslContext.init( null, null, null ); // will use java's default key and trust store which is sufficient unless you deal with self-signed certificates

        SSLSocketFactory factory = sslContext.getSocketFactory();// (SSLSocketFactory) SSLSocketFactory.getDefault();

        sslClient.setSocket( factory.createSocket() );

        sslClient.connectBlocking();

        BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
        while ( true ) {
            String line = reader.readLine();
            if( line.equals( "close" ) ) {
                sslClient.close();
            } else {
                sslClient.send( line );
            }
        }

    }
}
