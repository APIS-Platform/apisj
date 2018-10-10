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
    private int requestId; // 명령어 받을때 마다 증가
    private List<String> castOffTokenList;

    Client (WebSocket webSocket, byte[] auth, InetSocketAddress ipAddress, byte[] token) {
        this.webSocket = webSocket;
        this.auth = auth;
        this.ipAddress = ipAddress;
        this.token = token;

        this.requestId = 0;
        this.castOffTokenList = new ArrayList<>();

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


    public void addCastOffToken (String token) {
        castOffTokenList.add(token);
        addRequestId();
    }
    public int getRequestId() { return requestId; }
    public void addRequestId() { requestId++; }

    public List<String> getCastOffTokenList() { return castOffTokenList; }
    public void setCastOffToken (String token) { castOffTokenList.add(token); }
}
