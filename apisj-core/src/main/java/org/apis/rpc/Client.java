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
    private long lastDiscoveredTime;
    // 추가
    private long id; // 명령어 받을때 마다 증가

    Client (WebSocket webSocket, byte[] auth, InetSocketAddress ipAddress, byte[] token) {
        this.webSocket = webSocket;
        this.auth = auth;
        this.ipAddress = ipAddress;
        this.token = token;

        this.id = 0;

        refreshLastDiscoveredTime();
    }

    void refreshLastDiscoveredTime() {
        lastDiscoveredTime = System.currentTimeMillis();
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

    public long getLastDiscoveredTime() {
        return lastDiscoveredTime;
    }


    public long getID() { return id; }
    public void addID() { id++; }
}
