package org.apis.rpc;

public class RPCRequestData {
    private String type;
    private String auth;
    private Object data;

    public RPCRequestData(String type, String auth) {
        this.type = type;
        this.auth = auth;
    }

    public RPCRequestData(String type, String auth, Object data) {
        this.type = type;
        this.auth = auth;
        this.data = data;
    }
}
