package org.apis.rpc;

public class RPCCommandData_useless {
    private String requestId;
    private String type;
    private Object data;
    private String error;

    public RPCCommandData_useless(String requestId, String type, Object data) {
        this.requestId = requestId;
        this.type = type;
        this.data = data;
    }

    public RPCCommandData_useless(String requestId, String type, Object data, String error) {
        this.requestId = requestId;
        this.type = type;
        this.data = data;
        this.error = error;
    }

    public RPCCommandData_useless(String requestId, String type, Object data, Exception error) {
        this.requestId = requestId;
        this.type = type;
        this.data = data;
        this.error = error.getMessage();
    }
}
