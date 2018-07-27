package org.apis.rpc;

import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;

public class Client {
    private WebSocket webSocket;
    private byte[] auth;
    private InetSocketAddress ipAddress;
    private byte[] token;
    private long connectTime;


    Client (WebSocket webSocket, byte[] auth, InetSocketAddress ipAddress, byte[] token) {
        this.webSocket = webSocket;
        this.auth = auth;
        this.ipAddress = ipAddress;
        this.token = token;

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
}
