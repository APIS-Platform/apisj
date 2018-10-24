package org.apis.rpc;

import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private WebSocket webSocket;
    private byte[] auth;
    private InetSocketAddress ipAddress;
    private byte[] token;
    private long connectTime;
    // 추가
    private int id; // 명령어 받을때 마다 증가
    private List<String> castOffTokenHashList; // 사용된 토큰

    Client (WebSocket webSocket, byte[] auth, InetSocketAddress ipAddress, byte[] token) {
        this.webSocket = webSocket;
        this.auth = auth;
        this.ipAddress = ipAddress;
        this.token = token;

        this.id = 0;
        this.castOffTokenHashList = new ArrayList<>();

        initConnectTime();
    }

    public void initConnectTime() {
        connectTime = System.currentTimeMillis();
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public byte[] getAuth() {
        return auth;
    }

    public InetSocketAddress getISocketAddress() {
        return ipAddress;
    }

    public String getHostName() {
        return ipAddress.getHostName();
    }

    public byte[] getToken() {
        return token;
    }

    public long getConnectTime() {
        return connectTime;
    }


    public void addCastOffTokenHash (String token) {
        castOffTokenHashList.add(token);
        addID();
    }
    public int getID() { return id; }
    public void addID() { id++; }

    public List<String> getCastOffTokenHashList() { return castOffTokenHashList; }
    public void setCastOffTokenHash (String token) { castOffTokenHashList.add(token); }
}
