package org.apis.rpc;

public class RPCRequestData {
    private String jsonrpc;
    private String method;
    private String[] params;
    private int id;

    public RPCRequestData(String method, String[] params, int id) {
        this.jsonrpc = RPCCommand.COMMAND_RPC_VERSION;
        this.method = method;
        this.params = params;
        this.id = id;
    }
}
