package org.apis.rpc;

import com.google.gson.annotations.SerializedName;

public class RPCResultData {
    @SerializedName("id")
    private long id;

    @SerializedName("jsonrpc")
    private String jsonrpc;

    @SerializedName("method")
    private String method;

    @SerializedName("result")
    private Object result;

    @SerializedName("error")
    private Error error;

    static class Error {
        int code = -1232;
        String message;
    }

    public RPCResultData(long id, String method, Object result) {
        this.id = id;
        this.jsonrpc = RPCCommand.COMMAND_RPC_VERSION;
        this.method = method;
        this.result = result;
    }

    public RPCResultData(long id, String method, Object result, String error) {
        this.id = id;
        this.jsonrpc = RPCCommand.COMMAND_RPC_VERSION;
        this.method = method;
        this.result = result;
        this.error = new Error();
        this.error.message = error;
    }

    public RPCResultData(long id, String method, Object result, Exception error) {
        this.id = id;
        this.jsonrpc = RPCCommand.COMMAND_RPC_VERSION;
        this.method = method;
        this.result = result;
        this.error = new Error();
        this.error.message = error.getMessage();
        System.out.println(">>>--"+this.error);
    }
}
