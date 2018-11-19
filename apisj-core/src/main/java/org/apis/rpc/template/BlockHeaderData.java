package org.apis.rpc.template;

import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/* use RPC server json string*/
public class BlockHeaderData {
    public long number;

    public String hash;

    public String parentHash;

    public String coinbase;

    public String stateRoot;

    public String txTrieHash;

    public String receiptsTrieHash;

    public String rewardPoint;

    public String cumulativeRewardPoint;

    public long gasLimit;
    public long gasUsed;

    public String mineralUsed;

    public String timestamp;

    public String extraData;

    public String rpSeed;

    public String nonce;

    public String logsBloom;



    public BlockHeaderData(Block block) {
        this.number = block.getNumber();
        this.hash = ByteUtil.toHexString0x(block.getHash());
        this.parentHash = ByteUtil.toHexString0x(block.getParentHash());
        this.coinbase = ByteUtil.toHexString0x(block.getCoinbase());
        this.stateRoot = ByteUtil.toHexString0x(block.getStateRoot());
        this.txTrieHash = ByteUtil.toHexString0x(block.getTxTrieRoot());
        this.receiptsTrieHash = ByteUtil.toHexString0x(block.getReceiptsRoot());
        this.rewardPoint = block.getRewardPoint().toString();
        this.cumulativeRewardPoint = block.getCumulativeRewardPoint().toString();
        this.gasLimit = new BigInteger(block.getGasLimit()).longValue();
        this.gasUsed = block.getGasUsed();
        this.mineralUsed = block.getMineralUsed().toString();
        this.timestamp = String.valueOf(block.getTimestamp());

        if(block.getTransactionsList().size() > 0) {
            this.logsBloom = ByteUtil.toHexString0x(block.getLogBloom());
        }

        this.extraData = ByteUtil.toHexString0x(block.getExtraData());
        this.rpSeed = ByteUtil.toHexString0x(block.getMixHash());
        this.nonce = ByteUtil.toHexString0x(block.getNonce());
    }


    @Override
    public String toString() {
        return "BlockHeaderData{" +
                "number=" + number +
                ", hash='" + hash + '\'' +
                ", parentHash='" + parentHash + '\'' +
                ", coinbase='" + coinbase + '\'' +
                ", stateRoot='" + stateRoot + '\'' +
                ", txTrieHash='" + txTrieHash + '\'' +
                ", receiptsTrieHash='" + receiptsTrieHash + '\'' +
                ", rewardPoint='" + rewardPoint + '\'' +
                ", cumulativeRewardPoint='" + cumulativeRewardPoint + '\'' +
                ", gasLimit=" + gasLimit +
                ", gasUsed=" + gasUsed +
                ", mineralUsed='" + mineralUsed + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", extraData='" + extraData + '\'' +
                ", rpSeed='" + rpSeed + '\'' +
                ", nonce='" + nonce + '\'' +
                ", logsBloom='" + logsBloom + '\'' +
                '}';
    }
}