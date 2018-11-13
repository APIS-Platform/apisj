package org.apis.rpc.listener;

import org.apis.core.BlockSummary;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.rpc.RPCCommand;
import org.apis.rpc.template.BlockHeaderData;
import org.java_websocket.WebSocket;

import static org.apis.rpc.RPCJsonUtil.createSubscriptJson;

public class NewBlockListener extends EthereumListenerAdapter {

    private String subscription;
    private WebSocket conn;
    private String token;
    private boolean isEncrypt;


    public NewBlockListener(String subscription, WebSocket conn, String token, boolean isEncrypt) {
        this.subscription = subscription;
        this.conn = conn;
        this.token = token;
        this.isEncrypt = isEncrypt;
    }

    @Override
    public void onBlock(BlockSummary blockSummary) {
        if(conn.isOpen()) {
            String command = createSubscriptJson(subscription, "apis_subscription", new BlockHeaderData(blockSummary.getBlock()), null);
            RPCCommand.send(conn, token, command, isEncrypt);
        }
    }
}
