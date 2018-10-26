package org.apis.rpc;

public class RPCMessageData {
    private String hash;
    private String payload;

    public RPCMessageData(String hash, String payload) {
        this.hash = hash;
        this.payload = payload;
    }
}
