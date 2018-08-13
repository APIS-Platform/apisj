package org.apis.rpc.template;

import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/* use RPC server json string*/
public class BlockData {
    public long blockNumber;

    public String hash;

    public String parentHash;

    public String coinbase;

    public String stateRoot;

    public String txTrieHash;

    public String receiptsTrieHash;

    public String rewardPoint;

    public String gasLimit;

    public String mineralUsed;

    public String timestamp;

    public String extraData;

    public String rpSeed;

    public String nonce;

    public List<String> transactionHashList;

    // Master Node
    public String mnHash;

    public String mnReward;

    public List<String> mnGenerals;
    public List<String> mnMajors;
    public List<String> mnPrivates;


    public BlockData(Block block) {
        this.blockNumber = block.getNumber();
        this.hash = ByteUtil.toHexString(block.getHash());
        this.parentHash = ByteUtil.toHexString(block.getParentHash());
        this.coinbase = ByteUtil.toHexString(block.getCoinbase());
        this.stateRoot = ByteUtil.toHexString(block.getStateRoot());
        this.txTrieHash = ByteUtil.toHexString(block.getTxTrieRoot());
        this.receiptsTrieHash = ByteUtil.toHexString(block.getReceiptsRoot());
        this.rewardPoint = block.getRewardPoint().toString(10);
        this.gasLimit = new BigInteger(block.getGasLimit()).toString();
        this.mineralUsed = block.getMineralUsed().toString();
        this.timestamp = String.valueOf(block.getTimestamp());
        this.extraData = ByteUtil.toHexString(block.getExtraData());
        this.rpSeed = ByteUtil.toHexString(block.getMixHash());
        this.nonce = ByteUtil.toHexString(block.getNonce());
        this.mnReward = ApisUtil.readableApis(block.getMnReward());
        this.mnHash = ByteUtil.toHexString(block.getMnHash());

        transactionHashList = new ArrayList<>();
        for(Transaction tx : block.getTransactionsList()) {
            this.transactionHashList.add(ByteUtil.toHexString(tx.getHash()));
        }

        this.mnGenerals = new ArrayList<>();
        for(byte[] mn : block.getMnGeneralList()) {
            this.mnGenerals.add(ByteUtil.toHexString(mn));
        }

        this.mnMajors = new ArrayList<>();
        for(byte[] mn : block.getMnMajorList()) {
            this.mnMajors.add(ByteUtil.toHexString(mn));
        }

        this.mnPrivates = new ArrayList<>();
        for(byte[] mn : block.getMnPrivateList()) {
            this.mnPrivates.add(ByteUtil.toHexString(mn));
        }
    }
}