package org.apis.rpc;

import com.google.gson.Gson;
import org.apache.http.conn.util.InetAddressUtils;
import org.apis.crypto.HashUtil;
import org.apis.json.AuthData;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.apis.util.FastByteComparisons;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.spongycastle.util.encoders.Hex;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.KeyStore;

public class SSLClient extends WebSocketClient {
    public SSLClient(URI serverUri) { super(serverUri); }

    private String host, port, id;
    private char[] password;

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println( "Connected" );

        // 처음 연결시 해쉬 함수전달
        System.out.println("================================");
        System.out.println("=========== send hash ==========");
        System.out.println("================================");

        String auth = createAuth(id, password);
       // byte[] hash = HashUtil.sha3(ByteUtil.merge(auth.getBytes()));
        System.out.println(auth);
        send(auth);

    }

    @Override
    public void onMessage(String message) {
        System.out.println( "onMessage: " + message );


        if (message .equals( "LOGIN_SUCCESS")) {
            System.out.println("정보를 저장합니다");
        }
    }

    /*
    byte[] succByte = new byte[] {
            (byte)0x53, (byte)0x75, (byte)0x63
    };
    @Override
    public void onMessage(ByteBuffer bytes) {
        super.onMessage(bytes);

        System.out.println("[onMessage] - byte ");
        byte[] msg = new byte[bytes.remaining()];
        bytes.get(msg, 0, msg.length);

        if (FastByteComparisons.equal(succByte, msg)) { // 성공 연결 메세지
            System.out.println("정보를 저장합니다");
        }
    }*/

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println( "Disconnected" );
        System.exit( 0 );
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }


    public String createAuth(String id, char[] pw) {
        byte[] byteID = HashUtil.sha3( id.getBytes() );
        byte[] bytePW = HashUtil.sha3( new String(pw).getBytes() );
        byte[] byteKey = ByteUtil.merge(byteID, bytePW);


        AuthData authData = new AuthData("LOGIN", Hex.toHexString(byteKey));
        String authDataJson = new Gson().toJson(authData);
        return authDataJson;
    }

    public void setPersonalInfo(String host, String port,
                                String id, char[] pw) {
        this.host = host;
        this.port = port;
        this.id = id;
        this.password = pw;
    }
}



class SSLRPCClient {
    /*
     * Keystore with certificate created like so (in JKS format):
     *
     *keytool -genkey -validity 3650 -keystore "keystore.jks" -storepass "storepassword" -keypass "keypassword" -alias "default" -dname "CN=127.0.0.1, OU=MyOrgUnit, O=MyOrg, L=MyCity, S=MyRegion, C=MyCountry"
     */
    public static void main( String[] args ) throws Exception {
        WebSocketImpl.DEBUG = true;

        // 입력진행
        String host = null;
        String port = null;
        String user = null;
        char[] password = null;

        try {
            // host 주소를 입력받는다
            boolean isValidHost;
            do {
                host = ConsoleUtil.readLine("Input database(MySQL) host (Default:localhost) : ");
                if (host.isEmpty()) {
                    host = "localhost";
                    break;
                } else if(host.equalsIgnoreCase("localhost")) {
                    break;
                }
                isValidHost = InetAddressUtils.isIPv4Address(host);
            } while (!isValidHost);

            System.out.println("Host : " + host + "\n");

            // port 번호를 입력받는다
            boolean isValidPort;
            do {
                port = ConsoleUtil.readLine("Input database port (Default:8881): ");
                if(port.isEmpty()) {
                    port = "8881";
                    break;
                }
                try {
                    int portInt = Integer.parseUnsignedInt(port);
                    isValidPort = (portInt > 0 && portInt < 65536);
                } catch(NumberFormatException e) {
                    isValidPort = false;
                }
            } while (!isValidPort);

            user = ConsoleUtil.readLine("Input database user : ");
            password = ConsoleUtil.readPassword("Input database password : ");

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }



        // host 연결
        String url = "wss://" + host + ":" + port;
        SSLClient sslClient = new SSLClient( new URI( url ) );
        sslClient.setPersonalInfo(host, port, user, password); // save

        System.out.println("========== host connecting ===========");

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
            }
            else if (line.equals("cmd")) {

            }
            else {
                sslClient.send( line );
            }
        }

    }
}
