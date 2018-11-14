package org.apis.rpc.listener;

import org.apis.core.Block;
import org.apis.core.Bloom;
import org.apis.core.TransactionInfo;
import org.apis.core.TransactionReceipt;
import org.apis.facade.Ethereum;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.rpc.RPCCommand;
import org.apis.rpc.template.TransactionReceiptData;
import org.apis.util.FastByteComparisons;
import org.apis.vm.LogInfo;
import org.java_websocket.WebSocket;

import java.util.List;

import static org.apis.rpc.RPCJsonUtil.createSubscriptJson;

public class LogListener extends EthereumListenerAdapter {

    private String subscription;
    private WebSocket conn;
    private String token;
    private boolean isEncrypt;
    private List<byte[]> addresses;
    private List<byte[]> topics;
    private Ethereum core;


    public LogListener(String subscription, WebSocket conn, String token, boolean isEncrypt, List<byte[]> addresses, List<byte[]> topics, Ethereum core) {
        this.subscription = subscription;
        this.conn = conn;
        this.token = token;
        this.isEncrypt = isEncrypt;
        this.addresses = addresses;
        this.topics = topics;
        this.core = core;
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {
        if(conn == null || !conn.isOpen()) {
            return;
        }

        /*
         * 필터링 조건에 이벤트 토픽이 등록되어 있는데
         * 블록의 블룸 필터에 해당하는 이벤트가 존재하지 않는다면, 더 탐색할 필요가 없다.
         */
        if(topics != null && !topics.isEmpty()) {
            Bloom totalBloom = Bloom.create(topics.get(0));
            for(int i = 1; i < topics.size(); i++) {
                totalBloom.or(Bloom.create(topics.get(i)));
            }

            if(!Bloom.create(block.getLogBloom()).matches(totalBloom)) {
                return;
            }
        }


        if(addresses != null && !addresses.isEmpty()) {
            for(TransactionReceipt receipt : receipts) {
                for(byte[] address : addresses) {
                    if (FastByteComparisons.equal(receipt.getTransaction().getReceiveAddress(), address)) {
                        if(topics != null && topics.size() > 0) {
                            for(byte[] topic : topics) {
                                Bloom topicBloom = Bloom.create(topic);
                                if(receipt.getBloomFilter().matches(topicBloom)) {
                                    for(LogInfo logInfo : receipt.getLogInfoList()) {
                                        if(logInfo.getBloom().matches(topicBloom)) {
                                            sendCommand(receipt, block);
                                        }
                                    }
                                }
                            }
                        } else {
                            sendCommand(receipt, block);
                        }
                    }
                }
            }
        } else {
            if(topics != null && !topics.isEmpty()) {
                for (TransactionReceipt receipt : receipts) {
                    for (byte[] topic : topics) {
                        if (receipt.getBloomFilter().matches(Bloom.create(topic))) {
                            sendCommand(receipt, block);
                        }
                    }
                }
            }
        }
    }

    private void sendCommand(TransactionReceipt receipt, Block block) {
        TransactionInfo info = core.getTransactionInfo(receipt.getTransaction().getHash());
        String command = createSubscriptJson(subscription, "apis_subscription", new TransactionReceiptData(info, block), null);
        RPCCommand.send(conn, token, command, isEncrypt);
    }
}
