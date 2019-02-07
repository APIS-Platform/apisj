package org.apis.rpc.listener;

import org.apis.core.Transaction;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.rpc.RPCCommand;
import org.apis.rpc.adapter.CanvasAdapter;
import org.apis.util.ByteUtil;
import org.java_websocket.WebSocket;

import java.util.List;

import static org.apis.rpc.RPCJsonUtil.createSubscriptJson;

public class PendingTransactionListener extends EthereumListenerAdapter {

    private String subscription;
    private WebSocket conn;
    private String token;
    private boolean isEncrypt;
    private CanvasAdapter canvasAdapter;


    public PendingTransactionListener(String subscription, WebSocket conn, String token, boolean isEncrypt) {
        this.subscription = subscription;
        this.conn = conn;
        this.token = token;
        this.isEncrypt = isEncrypt;
    }

    public PendingTransactionListener(String subscription, CanvasAdapter canvasAdapter) {
        this.subscription = subscription;
        this.canvasAdapter = canvasAdapter;
    }

    @Override
    public void onPendingTransactionsReceived(List<Transaction> transactions) {
        if(canvasAdapter == null && (conn == null || !conn.isOpen() || conn.isClosed())) {
            return;
        }

        for(Transaction tx : transactions) {
            // TransactionData를 반환하려고 하였으나, 기존 web3.js 에서 hash 만을 반환하는 것에 맞추고자 주석처리 하였음
            // String command = createSubscriptJson(subscription, "apis_subscription", new TransactionData(tx, null), null);
            String command = createSubscriptJson(subscription, "apis_subscription", ByteUtil.toHexString0x(tx.getHash()), null);

            if(conn != null && conn.isOpen()) {
                RPCCommand.send(conn, token, command, isEncrypt);
            }

            if(canvasAdapter != null) {
                canvasAdapter.send(command);
            }
        }

    }
}
