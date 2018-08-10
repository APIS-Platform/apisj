/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 *
 * Original code
 * https://github.com/ethereum/ethereumj/blob/develop/ethereumj-core/src/main/java/org/ethereum/core/Block.java
 * modified by APIS
 */
package org.apis.core;

import org.apis.crypto.HashUtil;
import org.apis.datasource.MemSizeEstimator;
import org.apis.trie.Trie;
import org.apis.trie.TrieImpl;
import org.apis.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.apis.datasource.MemSizeEstimator.ByteArrayEstimator;

/**
 * The block in APIS is the collection of relevant pieces of information
 * (known as the blockheader), H, together with information corresponding to
 * the comprised transactions, R
 *
 * @author Roman Mandeleil
 * @author Nick Savers
 * @author Daniel
 * @since 20.05.2014
 */
public class Block {

    private static final Logger logger = LoggerFactory.getLogger("blockchain");

    private BlockHeader header;

    /* Transactions */
    private List<Transaction> transactionsList = new CopyOnWriteArrayList<>();

    private List<byte[]> mnGeneralList = new CopyOnWriteArrayList<>();
    private List<byte[]> mnMajorList = new CopyOnWriteArrayList<>();
    private List<byte[]> mnPrivateList = new CopyOnWriteArrayList<>();

    /* Private */

    private byte[] rlpEncoded;
    private boolean parsed = false;

    /* Constructors */

    private Block() {
    }

    public Block(byte[] rawData) {
        logger.debug("new from [" + Hex.toHexString(rawData) + "]");
        this.rlpEncoded = rawData;
    }

    public Block(BlockHeader header, List<Transaction> transactionsList, List<byte[]> mnGeneralList, List<byte[]> mnMajorList, List<byte[]> mnPrivateList) {

        this(header.getParentHash(),
                header.getCoinbase(),
                header.getLogsBloom(),
                header.getRewardPoint(),
                header.getCumulativeRewardPoint(),
                header.getNumber(),
                header.getGasLimit(),
                header.getGasUsed(),
                header.getMineralUsed(),
                header.getTimestamp(),
                header.getExtraData(),
                header.getMixHash(),
                header.getNonce(),
                header.getReceiptsRoot(),
                header.getTxTrieRoot(),
                header.getStateRoot(),
                header.getMnReward(),
                header.getMnHash(),
                transactionsList,
                mnGeneralList,
                mnMajorList,
                mnPrivateList);
    }

    public Block(byte[] parentHash, byte[] coinbase, byte[] logsBloom,
                 BigInteger rewardPoint, BigInteger cumulativeRewardPoint, long number, byte[] gasLimit,
                 long gasUsed, BigInteger mineralUsed, long timestamp, byte[] extraData,
                 byte[] mixHash, byte[] nonce, byte[] receiptsRoot,
                 byte[] transactionsRoot, byte[] stateRoot, BigInteger mnReward, byte[] mnHash,
                 List<Transaction> transactionsList,
                 List<byte[]> mnGeneralList, List<byte[]> mnMajorList, List<byte[]> mnPrivateList) {

        this(parentHash, coinbase, logsBloom, rewardPoint, cumulativeRewardPoint, number, gasLimit,
                gasUsed, mineralUsed, timestamp, extraData, mixHash, nonce, mnReward, mnHash, transactionsList, mnGeneralList, mnMajorList, mnPrivateList);

        this.header.setTransactionsRoot(BlockchainImpl.calcTxTrie(transactionsList));
        if (!Hex.toHexString(transactionsRoot).
                equals(Hex.toHexString(this.header.getTxTrieRoot())))
            logger.debug("Transaction root miss-calculate, block: {}", getNumber());

        this.header.setStateRoot(stateRoot);
        this.header.setReceiptsRoot(receiptsRoot);
    }


    public Block(byte[] parentHash, byte[] coinbase, byte[] logsBloom,
                 BigInteger rewardPoint, BigInteger cumulativeRewardPoint, long number, byte[] gasLimit,
                 long gasUsed, BigInteger mineralUsed, long timestamp,
                 byte[] extraData, byte[] mixHash, byte[] nonce, BigInteger mnReward, byte[] mnHash,
                 List<Transaction> transactionsList,
                 List<byte[]> mnGeneralList, List<byte[]> mnMajorList, List<byte[]> mnPrivateList) {
        this.header = new BlockHeader(parentHash, coinbase, logsBloom,
                rewardPoint, cumulativeRewardPoint, number, gasLimit, gasUsed, mineralUsed,
                timestamp, extraData, mixHash, nonce, mnReward, mnHash);

        this.transactionsList = transactionsList;
        if (this.transactionsList == null) {
            this.transactionsList = new CopyOnWriteArrayList<>();
        }

        this.mnGeneralList = mnGeneralList;
        if(this.mnGeneralList == null) {
            this.mnGeneralList = new CopyOnWriteArrayList<>();
        }

        this.mnMajorList = mnMajorList;
        if(this.mnMajorList == null) {
            this.mnMajorList = new CopyOnWriteArrayList<>();
        }

        this.mnPrivateList = mnPrivateList;
        if(this.mnPrivateList == null) {
            this.mnPrivateList = new CopyOnWriteArrayList<>();
        }

        this.parsed = true;
    }

    /*
     RLP는 임의로 중첩된 바이너리 데이터를 인코딩하는 것-
     이더리움에서 객체를 직렬화할 때 사용되는 기본 인코딩 방법이다.
     이더리움에서 Integer 데이터는 앞부분(왼쪽)에 0이 채워지지 않은 빅엔디안 형식이다.
     따라서 정수 0은 빈 바이트 배열과 동일하다.
     */
    private synchronized void parseRLP() {
        if (parsed) return;

        RLPList params = RLP.decode2(rlpEncoded);
        RLPList block = (RLPList) params.get(0);

        // Parse Header
        RLPList header = (RLPList) block.get(0);
        this.header = new BlockHeader(header);

        // Parse Transactions
        RLPList txTransactions = (RLPList) block.get(1);
        this.parseTxs(this.header.getTxTrieRoot(), txTransactions, false);

        RLPList mnGeneralList = (RLPList) block.get(2);
        for (RLPElement aMnGeneralList : mnGeneralList) {
            this.mnGeneralList.add(aMnGeneralList.getRLPData());
        }
        RLPList mnMajorList = (RLPList) block.get(3);
        for (RLPElement aMnMajorList : mnMajorList) {
            this.mnMajorList.add(aMnMajorList.getRLPData());
        }
        RLPList mnPrivateList = (RLPList) block.get(4);
        for (RLPElement aMnPrivateList : mnPrivateList) {
            this.mnPrivateList.add(aMnPrivateList.getRLPData());
        }

        this.parsed = true;
    }

    public BlockHeader getHeader() {
        parseRLP();
        return this.header;
    }

    public byte[] getHash() {
        parseRLP();
        return this.header.getHash();
    }

    public byte[] getParentHash() {
        parseRLP();
        return this.header.getParentHash();
    }

    public byte[] getCoinbase() {
        parseRLP();
        return this.header.getCoinbase();
    }

    public byte[] getStateRoot() {
        parseRLP();
        return this.header.getStateRoot();
    }

    public void setStateRoot(byte[] stateRoot) {
        parseRLP();
        this.header.setStateRoot(stateRoot);
    }

    public byte[] getTxTrieRoot() {
        parseRLP();
        return this.header.getTxTrieRoot();
    }

    public byte[] getReceiptsRoot() {
        parseRLP();
        return this.header.getReceiptsRoot();
    }


    public byte[] getLogBloom() {
        parseRLP();
        return this.header.getLogsBloom();
    }

    public BigInteger getRewardPoint() {
        parseRLP();
        return this.header.getRewardPoint();
    }

    public void setRewardPoint(BigInteger rewardPoint) {
        this.header.setRewardPoint(rewardPoint);
        rlpEncoded = null;
    }

    public BigInteger getCumulativeRewardPoint() {
        parseRLP();
        return this.header.getCumulativeRewardPoint();
    }

    public void setCumulativeRewardPoint(BigInteger cumulativeRewardPoint) {
        this.header.setCumulativeRewardPoint(cumulativeRewardPoint);
        rlpEncoded = null;
    }


    /*
    기존 소스코드에서는 자신과 엉클의 모든 Difficulty를 합한 값을 리턴했었다.
    public BigInteger getCumulativeRewardPoint() {
        parseRLP();

        return new BigInteger(1, this.header.getRewardPoint());
    }*/

    public long getTimestamp() {
        parseRLP();
        return this.header.getTimestamp();
    }

    public long getNumber() {
        parseRLP();
        return this.header.getNumber();
    }

    public byte[] getGasLimit() {
        parseRLP();
        return this.header.getGasLimit();
    }

    public BigInteger getMineralUsed() {
        parseRLP();
        return this.header.getMineralUsed();
    }

    public long getGasUsed() {
        parseRLP();
        return this.header.getGasUsed();
    }


    public byte[] getExtraData() {
        parseRLP();
        return this.header.getExtraData();
    }

    public byte[] getMixHash() {
        parseRLP();
        return this.header.getMixHash();
    }


    public byte[] getNonce() {
        parseRLP();
        return this.header.getNonce();
    }

    public void setNonce(byte[] nonce) {
        this.header.setNonce(nonce);
        rlpEncoded = null;
    }

    public void setMixHash(byte[] hash) {
        this.header.setMixHash(hash);
        rlpEncoded = null;
    }

    public void setExtraData(byte[] data) {
        this.header.setExtraData(data);
        rlpEncoded = null;
    }

    public List<Transaction> getTransactionsList() {
        parseRLP();
        return transactionsList;
    }

    public List<byte[]> getMnGeneralList() {
        parseRLP();
        return mnGeneralList;
    }

    public List<byte[]> getMnMajorList() {
        parseRLP();
        return mnMajorList;
    }

    public List<byte[]> getMnPrivateList() {
        parseRLP();
        return mnPrivateList;
    }

    public void setMnGeneralList(List<byte[]> list) {
        this.mnGeneralList = list;
        rlpEncoded = null;
    }
    public void setMnMajorList(List<byte[]> list) {
        this.mnMajorList = list;
        rlpEncoded = null;
    }
    public void setMnPrivateList(List<byte[]> list) {
        this.mnPrivateList = list;
        rlpEncoded = null;
    }

    public byte[] getMnHash() {
        parseRLP();
        return this.header.getMnHash();
    }

    public BigInteger getMnReward() {
        parseRLP();
        return this.header.getMnReward();
    }

    public void setMnHash(byte[] mnHash) {
        this.header.setMnHash(mnHash);
        rlpEncoded = null;
    }

    public void setMnReward(BigInteger reward) {
        this.header.setMnReward(reward);
        rlpEncoded = null;
    }


    private StringBuffer toStringBuff = new StringBuffer();
    // [parent_hash, coinbase, state_root, tx_trie_root,
    // difficulty, number, minGasPrice, gasLimit, gasUsed, timestamp,
    // extradata, nonce]

    @Override
    public String toString() {
        parseRLP();

        toStringBuff.setLength(0);
        //toStringBuff.append(Hex.toHexString(this.getEncoded())).append("\n");
        toStringBuff.append("BlockData [ ").append("\n");
        toStringBuff.append(header.toString());

        if (!getTransactionsList().isEmpty()) {
            toStringBuff.append("Txs [\n");
            for (Transaction tx : getTransactionsList()) {
                toStringBuff.append(tx);
                toStringBuff.append("\n");
            }
            toStringBuff.append("]\n");
        } else {
            toStringBuff.append("Txs []\n");
        }

        toStringBuff.append(getMasternodeListString(getMnGeneralList(), "MN General"));
        toStringBuff.append(getMasternodeListString(getMnMajorList(), "MN Major"));
        toStringBuff.append(getMasternodeListString(getMnPrivateList(), "MN Private"));

        toStringBuff.append("]");

        return toStringBuff.toString();
    }

    private String getMasternodeListString(List<byte[]> list, String name) {
        if(!list.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            sb.append(" [\n");
            for (byte[] mn : list) {
                sb.append(ByteUtil.toHexString(mn));
                sb.append("\n");
            }
            sb.append("]\n");

            return sb.toString();
        } else {
            return "";
        }
    }


    public String toFlatString() {
        parseRLP();

        toStringBuff.setLength(0);
        toStringBuff.append("BlockData [");
        //toStringBuff.append("hash=").append(ByteUtil.toHexString(this.getHash()));
        toStringBuff.append(header.toFlatString());

        for (Transaction tx : getTransactionsList()) {
            toStringBuff.append("\n");
            toStringBuff.append(tx.toString());
        }

        toStringBuff.append("]");
        return toStringBuff.toString();
    }

    private byte[] parseTxs(RLPList txTransactions, boolean validate) {

        Trie<byte[]> txsState = new TrieImpl();
        for (int i = 0; i < txTransactions.size(); i++) {
            RLPElement transactionRaw = txTransactions.get(i);
            Transaction tx = new Transaction(transactionRaw.getRLPData());
            if (validate) tx.verify();
            this.transactionsList.add(tx);
            txsState.put(RLP.encodeInt(i), transactionRaw.getRLPData());
        }
        return txsState.getRootHash();
    }


    private boolean parseTxs(byte[] expectedRoot, RLPList txTransactions, boolean validate) {

        byte[] rootHash = parseTxs(txTransactions, validate);
        String calculatedRoot = Hex.toHexString(rootHash);
        if (!calculatedRoot.equals(Hex.toHexString(expectedRoot))) {
            logger.debug("Transactions trie root validation failed for block #{}", this.header.getNumber());
            return false;
        }

        return true;
    }

    /**
     * check if param block is son of this block
     *
     * @param block - possible a son of this
     * @return - true if this block is parent of param block
     */
    public boolean isParentOf(Block block) {
        return Arrays.areEqual(this.getHash(), block.getParentHash());
    }

    public boolean isGenesis() {
        return this.header.isGenesis();
    }

    public boolean isEqual(Block block) {
        return Arrays.areEqual(this.getHash(), block.getHash());
    }

    private byte[] getTransactionsEncoded() {

        byte[][] transactionsEncoded = new byte[transactionsList.size()][];
        int i = 0;
        for (Transaction tx : transactionsList) {
            transactionsEncoded[i] = tx.getEncoded();
            ++i;
        }
        return RLP.encodeList(transactionsEncoded);
    }

    private byte[] getMnGeneralEncoded(int type) {
        List<byte[]> mnList;
        switch(type) {
            case 0:
                mnList = mnGeneralList;
                break;
            case 1:
                mnList = mnMajorList;
                break;
            case 2:
                mnList = mnPrivateList;
                break;
            default:
                return null;
        }
        byte[][] mnListEncoded = new byte[mnList.size()][];
        int i = 0;
        for (byte[] mn : mnList) {
            mnListEncoded[i] = RLP.encodeElement(mn);
            ++i;
        }
        return RLP.encodeList(mnListEncoded);
    }

    public byte[] getEncoded() {
        if (rlpEncoded == null) {
            byte[] header = this.header.getEncoded();

            List<byte[]> block = getBodyElements();
            block.add(0, header);
            byte[][] elements = block.toArray(new byte[block.size()][]);

            this.rlpEncoded = RLP.encodeList(elements);
        }
        return rlpEncoded;
    }

    public byte[] getEncodedWithoutNonce() {
        parseRLP();
        return this.header.getEncodedWithoutNonce();
    }

    public byte[] getEncodedBody() {
        List<byte[]> body = getBodyElements();
        byte[][] elements = body.toArray(new byte[body.size()][]);
        return RLP.encodeList(elements);
    }

    private List<byte[]> getBodyElements() {
        parseRLP();

        byte[] transactions = getTransactionsEncoded();

        List<byte[]> body = new ArrayList<>();
        body.add(transactions);
        body.add(getMnGeneralEncoded(0));
        body.add(getMnGeneralEncoded(1));
        body.add(getMnGeneralEncoded(2));

        return body;
    }

    public String getShortHash() {
        parseRLP();
        return Hex.toHexString(getHash()).substring(0, 6);
    }

    public String getShortDescr() {
        return "#" + getNumber() + " (" + Hex.toHexString(getParentHash()).substring(0,4) + " ~> "
                + Hex.toHexString(getHash()).substring(0,4) + ") Txs:" + getTransactionsList().size() + " Miner:" + AddressUtil.getShortAddress(getCoinbase());
    }

    public static class Builder {

        private BlockHeader header;
        private byte[] body;

        public Builder withHeader(BlockHeader header) {
            this.header = header;
            return this;
        }

        public Builder withBody(byte[] body) {
            this.body = body;
            return this;
        }

        public Block create() {
            if (header == null || body == null) {
                return null;
            }

            Block block = new Block();
            block.header = header;
            block.parsed = true;

            RLPList items = (RLPList) RLP.decode2(body).get(0);

            RLPList transactions = (RLPList) items.get(0);

            if (!block.parseTxs(header.getTxTrieRoot(), transactions, false)) {
                return null;
            }

            return block;
        }
    }

    public static final MemSizeEstimator<Block> MemEstimator = block -> {
        if (block == null) return 0;
        long txSize = block.transactionsList.stream().mapToLong(Transaction.MemEstimator::estimateSize).sum() + 16;
        return BlockHeader.MAX_HEADER_SIZE +
                txSize +
                ByteArrayEstimator.estimateSize(block.rlpEncoded) +
                1 + // parsed flag
                16; // Object header + ref
    };
}
