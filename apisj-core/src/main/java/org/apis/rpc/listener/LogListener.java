package org.apis.rpc.listener;

import org.apis.core.*;
import org.apis.crypto.HashUtil;
import org.apis.facade.Ethereum;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.rpc.RPCCommand;
import org.apis.rpc.template.LogInfoData;
import org.apis.rpc.template.TransactionReceiptData;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.apis.vm.DataWord;
import org.apis.vm.LogInfo;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.List;

import static org.apis.rpc.RPCJsonUtil.createSubscriptJson;

public class LogListener extends EthereumListenerAdapter {

    private String subscription;
    private WebSocket conn;
    private String token;
    private boolean isEncrypt;
    private List<byte[]> addresses;
    private List<TopicBloom> tbs;
    private Ethereum core;


    public LogListener(String subscription, WebSocket conn, String token, boolean isEncrypt, List<byte[]> addresses, List<byte[]> topics, Ethereum core) {
        this.subscription = subscription;
        this.conn = conn;
        this.token = token;
        this.isEncrypt = isEncrypt;
        this.addresses = addresses;
        tbs = new ArrayList<>();
        if(topics != null) {
            for(byte[] topic : topics) {
                this.tbs.add(new TopicBloom(topic));
            }
        }

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
        boolean isExistBloom = false;
        if(tbs != null && !tbs.isEmpty()) {
            Bloom blockBloom = new Bloom(block.getLogBloom());

            for(TopicBloom tb : tbs) {
                if(blockBloom.matches(tb.getBloom())) {
                    isExistBloom = true;
                    break;
                }
            }

            if(!isExistBloom) {
                return;
            }
        }


        if(addresses != null && !addresses.isEmpty()) {
            for(TransactionReceipt receipt : receipts) {
                for(byte[] address : addresses) {
                    if (FastByteComparisons.equal(receipt.getTransaction().getReceiveAddress(), address)) {
                        if(tbs != null && tbs.size() > 0) {
                            for(TopicBloom tb : tbs) {
                                if(receipt.getBloomFilter().matches(tb.getBloom())) {
                                    int logIndex = 0;
                                    for(LogInfo logInfo : receipt.getLogInfoList()) {
                                        boolean isMatch = false;
                                        for(DataWord topic : logInfo.getTopics()) {
                                            if(topic.equals(new DataWord(tb.getTopic()))) {
                                                isMatch = true;
                                                break;
                                            }
                                        }
                                        if(isMatch) {
                                            sendCommand(receipt, logInfo, block, logIndex);
                                        }
                                        logIndex += 1;
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
            if(tbs != null && !tbs.isEmpty()) {
                for (TransactionReceipt receipt : receipts) {
                    for(TopicBloom tb : tbs) {
                        // TransactionReceipt 내에 찾고자 하는 토픽의 bloom이 존재하는 경우에만 상세 탐색
                        if(receipt.getBloomFilter().matches(tb.getBloom())) {
                            int logSize = receipt.getLogInfoList().size();
                            for(int logIndex = 0; logIndex < logSize; logIndex++) {
                                LogInfo info = receipt.getLogInfoList().get(logIndex);

                                if(isMatchesExactly(info, tb.getTopic())) {
                                    sendCommand(receipt, info, block, logIndex);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isMatchesExactly(LogInfo logInfo, byte[] topic) {
        for(DataWord logTopic : logInfo.getTopics()) {
            if(logTopic.equals(new DataWord(topic))) {
                return true;
            }
        }

        return false;
    }


    private void sendCommand(TransactionReceipt receipt, Block block) {
        TransactionInfo info = core.getTransactionInfo(receipt.getTransaction().getHash());
        String command = createSubscriptJson(subscription, "apis_subscription", new TransactionReceiptData(info, block), null);
        RPCCommand.send(conn, token, command, isEncrypt);
    }

    private void sendCommand(TransactionReceipt receipt, LogInfo logInfo, Block block, int logIndex) {
        Transaction tx = receipt.getTransaction();

        int txIndex = 0;
        boolean txExist = false;
        for(Transaction txBlock : block.getTransactionsList()) {
            if(FastByteComparisons.equal(tx.getHash(), txBlock.getHash())) {
                txExist = true;
                break;
            }
            txIndex += 1;
        }

        LogInfoData data = new LogInfoData(logInfo, ByteUtil.toHexString0x(block.getHash()), ByteUtil.toHexString0x(tx.getHash()), logIndex, block.getNumber(), (txExist ? txIndex : 0));
        String command = createSubscriptJson(subscription, "apis_subscription", data, null);
        RPCCommand.send(conn, token, command, isEncrypt);
    }

    class TopicBloom {
        private byte[] topic;
        private Bloom bloom;

        TopicBloom(byte[] topic) {
            this.topic = topic;
            this.bloom = Bloom.create(HashUtil.sha3(topic));
        }

        public byte[] getTopic() {
            return topic;
        }

        public Bloom getBloom() {
            return bloom;
        }
    }
}
