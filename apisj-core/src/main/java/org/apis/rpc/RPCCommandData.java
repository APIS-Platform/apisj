package org.apis.rpc;

public class RPCCommandData {
    private String requestId;
    private String type;
    private Object data;
    private String error;

    public RPCCommandData(String requestId, String type, Object data) {
        this.requestId = requestId;
        this.type = type;
        this.data = data;
    }

    public RPCCommandData(String requestId, String type, Object data, String error) {
        this.requestId = requestId;
        this.type = type;
        this.data = data;
        this.error = error;
    }

    public RPCCommandData(String requestId, String type, Object data, Exception error) {
        this.requestId = requestId;
        this.type = type;
        this.data = data;
        this.error = error.getMessage();
    }
}
