package org.apis.rpc;

import com.google.gson.Gson;
import org.apache.http.conn.util.InetAddressUtils;
import org.apis.crypto.HashUtil;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.spongycastle.util.encoders.Hex;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;
import java.util.prefs.Preferences;

import static org.apis.rpc.SSLRPCClient.createJson;

public class SSLClient extends WebSocketClient {
    public SSLClient(URI serverUri) { super(serverUri); }

    private String host, port, id;
    private char[] password;
    private String token;

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println( "Connected" );

        // 처음 연결시 해쉬 함수전달
        System.out.println("================================");
        System.out.println("=========== send hash ==========");
        System.out.println("================================");

        byte[] auth = createAuth(id, password);
        String authJson = createJson("LOGIN", Hex.toHexString(auth));
        System.out.println(authJson);
        send(authJson);

    }

    @Override
    public void onMessage(String message) {
        System.out.println( "onMessage: " + message );

        String msgType = null;
        String msgKey = null;

        try {
            msgType = getDecodeMessage(message, "type");
            msgKey = getDecodeMessage(message, "key");

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (msgType == null) return;

        switch (msgType) {
            // 서버 접속 성공
            // 서버로 부터 토큰을 받아 저장
            case "TOKEN":
                token = msgKey;
                savePersonalInfo();
                break;
            case "LOGIN":
                System.out.println("정보를 저장합니다");
                break;
            case "account":
                System.out.println("======> " + msgKey);
                break;
        }


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


    public byte[] createAuth(String id, char[] pw) {
        byte[] byteID = HashUtil.sha3( id.getBytes() );
        byte[] bytePW = HashUtil.sha3( new String(pw).getBytes() );
        byte[] byteKey = ByteUtil.merge(byteID, bytePW);
        byteKey = HashUtil.sha3(byteKey);

        return byteKey;
    }

    public void setPersonalInfo(String host, String port,
                                String id, char[] pw) {
        this.host = host;
        this.port = port;
        this.id = id;
        this.password = pw;
    }

    // 접속 성공시 접속 정보 저장
    private void savePersonalInfo() {
        System.out.println("save PersonalInfo");

        Preferences pref = Preferences.userNodeForPackage(SSLClient.class);
        pref.put(PREF_KEY_PORT, port);
        pref.put(PREF_KEY_HOST, host);
        pref.put(PREF_KEY_ID, id);
//        pref.flush();
    }

    private String getDecodeMessage(String msg, String kind) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(msg);

        String result = (String) object.get(kind);
        return result;
    }

    public String getToken() {
        return token;
    }

    // temp
    public static final String PREF_KEY_PORT = "PORT_KEY";
    public static final String PREF_KEY_HOST = "HOST_KEY";
    public static final String PREF_KEY_ID = "ID_KEY";
}



class SSLRPCClient {
    /*
     * Keystore with certificate created like so (in JKS format):
     *
     *keytool -genkey -validity 3650 -keystore "keystore.jks" -storepass "storepassword" -keypass "keypassword" -alias "default" -dname "CN=127.0.0.1, OU=MyOrgUnit, O=MyOrg, L=MyCity, S=MyRegion, C=MyCountry"
     */
    public static void main( String[] args ) throws Exception {
        WebSocketImpl.DEBUG = true;

        Preferences pref = Preferences.userNodeForPackage(SSLClient.class);
        String loadHost = pref.get(SSLClient.PREF_KEY_HOST, "localhost");
        String loadPort = pref.get(SSLClient.PREF_KEY_PORT, "8881");
        String loadID = pref.get(SSLClient.PREF_KEY_ID, "id");


        // 입력진행
        String host = null;
        String port = null;
        String user = null;
        char[] password = null;

        try {
            // host 주소를 입력받는다
            boolean isValidHost;
            do {
                host = ConsoleUtil.readLine("Input database(MySQL) host (Default:" + loadHost + ") : ");
                if (host.isEmpty()) {
                    host = loadHost;
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
                port = ConsoleUtil.readLine("Input database port (Default:" + loadPort + "): ");
                if(port.isEmpty()) {
                    port = loadPort;
                    break;
                }
                try {
                    int portInt = Integer.parseUnsignedInt(port);
                    isValidPort = (portInt > 0 && portInt < 65536);
                } catch(NumberFormatException e) {
                    isValidPort = false;
                }
            } while (!isValidPort);

            // id를 입력받는다
            boolean isValidID = false;
            do {
                user = ConsoleUtil.readLine("Input database ID (Default:" + loadID + "): ");
                if (user.isEmpty()) {
                    user = loadID;
                    break;
                }

                if (user.length() > 0 && user.length() < 10) {
                    isValidID = true;
                }
            } while (!isValidID);

//            user = ConsoleUtil.readLine("Input database user : ");
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
            else {
//                sslClient.send( line );

                if (line.startsWith("cmd ")) {
                    String cmd = line.replace("cmd ", "");
                    String jsonString = null;

                    switch (cmd) {
                        case RPCCommand.COMMNAD_ACOUNT:
                            jsonString = createJson(RPCCommand.COMMNAD_ACOUNT, sslClient.getToken());
                            break;
                        case RPCCommand.COMMNAD_ATTACH:
                            jsonString = createJson(RPCCommand.COMMNAD_ACOUNT, sslClient.getToken());
                            break;
                    }

                    System.out.println("**********************" + jsonString);
                    if (jsonString!=null) {
                        sslClient.send(jsonString);
                    }


                }
            }


        }

    }

    public static String createJson(String type, String key) {
        RPCMessageData RPCMessageData = new RPCMessageData(type, key);
        return new Gson().toJson(RPCMessageData);
    }


}
