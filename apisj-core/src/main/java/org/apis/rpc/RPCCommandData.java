package org.apis.rpc;

public class RPCCommandData {
    private String type;
    private Object data;
    private String error;

    public RPCCommandData(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public RPCCommandData(String type, Object data, boolean error) {
        this.type = type;
        this.data = data;
        if (error) this.error = "error";
    }

    public RPCCommandData(String type, Object data, String error) {
        this.type = type;
        this.data = data;
        this.error = error;
    }
}
