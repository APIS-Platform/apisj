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
        if(tbs != null && !tbs.isEmpty()) {
            Bloom blockBloom = new Bloom(block.getLogBloom());

            for(TopicBloom tb : tbs) {
                if(!blockBloom.matches(tb.getBloom())) {
                    return;
                }
            }
        }


        if(addresses != null && !addresses.isEmpty()) {
            for(TransactionReceipt receipt : receipts) {
                for(byte[] address : addresses) {
                    if (FastByteComparisons.equal(receipt.getTransaction().getReceiveAddress(), address)) {

                        // 이벤트 토픽 필터링 조건이 등록된 경우, 필터링에 일치할 때에만 전송한다.
                        if(tbs != null && tbs.size() > 0) {
                            checkTopicsAndSend(receipt, block);
                        }
                        // 토픽 필터링이 없으면, TransactionReceipt를 전송한다.
                        else {
                            sendCommand(receipt, block);
                        }
                    }
                }
            }
        } else {
            if(tbs != null && !tbs.isEmpty()) {
                for (TransactionReceipt receipt : receipts) {
                    checkTopicsAndSend(receipt, block);
                }
            }
        }
    }

    private void checkTopicsAndSend(TransactionReceipt receipt, Block block) {
        if (isBloomInBloom(receipt.getBloomFilter())) {

            int logSize = receipt.getLogInfoList().size();
            for(int logIndex = 0; logIndex < logSize; logIndex++) {
                LogInfo info = receipt.getLogInfoList().get(logIndex);

                if(isMatchesExactly(info)) {
                    sendCommand(receipt, info, block, logIndex);
                }
            }
        }
    }

    /**
     * 필터링 조건으로 등록된 토픽들이 입력되는 블룸필터에 포함되는지 확인한다.
     * @param bigBloom 필터가 포함되어있을 것으로 예상되는 블룸필터
     * @return TRUE : 모든 필터가 bigBloom 내에 포함될 경우
     */
    private boolean isBloomInBloom(Bloom bigBloom) {
        for(TopicBloom tb : tbs) {
            if(!bigBloom.matches(tb.getBloom())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 이벤트 필터로 등록된 토픽들이 순서도 정확하게 일치하는지 확인한다.
     * @param logInfo 필터와 일치하는지 비교하려는 LogInfo
     * @return TURE : 필터와 값과 순서가 일치하는 경우
     */
    private boolean isMatchesExactly(LogInfo logInfo) {
        int topicSize = logInfo.getTopics().size();
        int filterSize = tbs.size();
        for(int i = 0; i < topicSize && i < filterSize; i++) {
            DataWord topic = logInfo.getTopics().get(i);
            byte[] filterTopic = tbs.get(i).getTopic();

            if(filterTopic == null) {
                continue;
            }

            DataWord filter = new DataWord(filterTopic);
            if(!topic.equals(filter)) {
                return false;
            }
        }

        return true;
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
            if(topic == null) {
                this.bloom = new Bloom();
            } else {
                this.bloom = Bloom.create(HashUtil.sha3(topic));
            }
        }

        public byte[] getTopic() {
            return topic;
        }

        public Bloom getBloom() {
            return bloom;
        }
    }
}
