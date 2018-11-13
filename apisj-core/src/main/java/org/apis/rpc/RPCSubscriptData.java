package org.apis.rpc;

import com.google.gson.annotations.SerializedName;

public class RPCSubscriptData {
    @SerializedName("jsonrpc")
    private String jsonrpc;

    private Params params;

    private static class Params {
        private String subscription;
        private Object result;
        private String error;
    }

    private String method = "apis_subscription";



    RPCSubscriptData(String subscription, String method, Object result, String error) {
        this.params = new Params();
        this.params.subscription = subscription;
        this.jsonrpc = RPCCommand.COMMAND_RPC_VERSION;
        this.method = method;
        this.params.result = result;
        if(error != null && !error.isEmpty()) {
            this.params.error = error;
        }
    }

    /*public RPCSubscriptData(String subscription, String method, Object result, Exception error) {
        this.subscription = subscription;
        this.jsonrpc = RPCCommand.COMMAND_RPC_VERSION;
        this.method = method;
        this.result = result;
        if(error != null) {
            this.error = error.getMessage();
            System.out.println(">>>--" + this.error);
        }
    }*/

    @Override
    public String toString() {
        return "RPCSubscriptData{" +
                "jsonrpc='" + jsonrpc + '\'' +
                ", params=" + params +
                ", method='" + method + '\'' +
                '}';
    }
}
