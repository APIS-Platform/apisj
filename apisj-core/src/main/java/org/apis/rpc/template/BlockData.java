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

    public List<String> transactions;

    public String logsBloom;

    // Master Node
    public String mnHash;

    public String mnReward;

    public List<String> mnGenerals;
    public List<String> mnMajors;
    public List<String> mnPrivates;

    public long size;


    public BlockData(Block block, boolean isContainFullTx) {
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
        this.logsBloom = ByteUtil.toHexString0x(block.getLogBloom());
        this.extraData = ByteUtil.toHexString0x(block.getExtraData());
        this.rpSeed = ByteUtil.toHexString0x(block.getMixHash());
        this.nonce = ByteUtil.toHexString0x(block.getNonce());
        this.mnReward = ApisUtil.readableApis(block.getMnReward());
        this.mnHash = ByteUtil.toHexString0x(block.getMnHash());

        this.transactions = new ArrayList<>();

        for(Transaction tx : block.getTransactionsList()) {
            if(isContainFullTx) {
                this.transactions.add(new TransactionData(tx, block).getJson());
            } else {
                this.transactions.add(ByteUtil.toHexString(tx.getHash()));
            }
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

        this.size = block.getEncoded().length;
    }
}