package org.apis.rpc.listener;

import org.apis.core.BlockSummary;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.rpc.RPCCommand;
import org.apis.rpc.adapter.CanvasAdapter;
import org.apis.rpc.template.BlockHeaderData;
import org.java_websocket.WebSocket;

import static org.apis.rpc.RPCJsonUtil.createSubscriptJson;

public class NewBlockListener extends EthereumListenerAdapter {

    private String subscription;
    private WebSocket conn;
    private String token;
    private boolean isEncrypt;
    private CanvasAdapter canvasAdapter;


    public NewBlockListener(String subscription, WebSocket conn, String token, boolean isEncrypt) {
        this.subscription = subscription;
        this.conn = conn;
        this.token = token;
        this.isEncrypt = isEncrypt;
    }

    public NewBlockListener(String subscription, CanvasAdapter canvasAdapter) {
        this.canvasAdapter = canvasAdapter;
        this.subscription = subscription;
    }

    @Override
    public void onBlock(BlockSummary blockSummary) {
        String command = createSubscriptJson(subscription, "apis_subscription", new BlockHeaderData(blockSummary.getBlock()), null);
        if(conn != null && conn.isOpen()) {
            RPCCommand.send(conn, token, command, isEncrypt);
        }

        if(canvasAdapter != null) {
            canvasAdapter.send(command);
        }
    }
}
