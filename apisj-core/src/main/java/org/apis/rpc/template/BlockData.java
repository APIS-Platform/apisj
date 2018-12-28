package org.apis.rpc.template;

import org.apis.config.Constants;
import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.util.ByteUtil;
import org.apis.util.MasternodeRewardUtil;
import org.apis.util.blockchain.ApisUtil;
import org.apis.util.blockchain.MasternodeRewardData;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/* use RPC server json string*/
public class BlockData {
    public long number;

    public String hash;

    public String parentHash;

    public String coinbase;

    public String coinbaseMask;

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

    public long txSize;
    public List<String> transactions;

    public String logsBloom;

    // Master Node
    public String mnHash;

    //public String mnReward;
    private String mnRewardGeneralNormal;
    private String mnRewardGeneralLate;
    private String mnRewardMajorNormal;
    private String mnRewardMajorLate;
    private String mnRewardPrivateNormal;
    private String mnRewardPrivateLate;

    private List<String> mnGeneralNormals;
    private List<String> mnMajorNormals;
    private List<String> mnPrivateNormals;
    private List<String> mnGeneralLates;
    private List<String> mnMajorLates;
    private List<String> mnPrivateLates;

    public long size;


    public BlockData(Block block, boolean isContainFullTx, String coinbaseMask, Constants constants) {
        this(block, isContainFullTx, constants);
        if(coinbaseMask != null) {
            this.coinbaseMask = coinbaseMask;
        }
    }

    public BlockData(Block block, boolean isContainFullTx, Constants constants) {
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
        if(block.getMnHash() != null && block.getMnHash().length > 0) {
            this.mnHash = ByteUtil.toHexString0x(block.getMnHash());

            MasternodeRewardData mnRewardData = MasternodeRewardUtil.calcRewards(
                    constants,
                    block.getMnReward(),
                    block.getMnGeneralList().size(), block.getMnGeneralLateList().size(),
                    block.getMnMajorList().size(), block.getMnMajorLateList().size(),
                    block.getMnPrivateList().size(), block.getMnPrivateLateList().size());

            if(block.getMnGeneralList().size() > 0) {
                this.mnGeneralNormals = new ArrayList<>();
                for(byte[] mn : block.getMnGeneralList()) {
                    this.mnGeneralNormals.add(ByteUtil.toHexString(mn));
                }
                this.mnRewardGeneralNormal = ApisUtil.readableApis(mnRewardData.getGeneralNormal(), true);
            }
            if(block.getMnGeneralLateList().size() > 0) {
                this.mnGeneralLates = new ArrayList<>();
                for(byte[] mn : block.getMnGeneralLateList()) {
                    this.mnGeneralLates.add(ByteUtil.toHexString(mn));
                }
                this.mnRewardGeneralLate = ApisUtil.readableApis(mnRewardData.getGeneralLate(), true);
            }

            if(block.getMnMajorList().size() > 0) {
                this.mnMajorNormals = new ArrayList<>();
                for(byte[] mn : block.getMnMajorList()) {
                    this.mnMajorNormals.add(ByteUtil.toHexString(mn));
                }
                this.mnRewardMajorNormal = ApisUtil.readableApis(mnRewardData.getMajorNormal(), true);
            }
            if(block.getMnMajorLateList().size() > 0) {
                this.mnMajorLates = new ArrayList<>();
                for(byte[] mn : block.getMnMajorLateList()) {
                    this.mnMajorLates.add(ByteUtil.toHexString(mn));
                }
                this.mnRewardMajorLate= ApisUtil.readableApis(mnRewardData.getMajorLate(), true);
            }

            if(block.getMnPrivateList().size() > 0) {
                this.mnPrivateNormals = new ArrayList<>();
                for(byte[] mn : block.getMnPrivateList()) {
                    this.mnPrivateNormals.add(ByteUtil.toHexString(mn));
                }
                this.mnRewardPrivateNormal = ApisUtil.readableApis(mnRewardData.getPrivateNormal(), true);
            }
            if(block.getMnPrivateLateList().size() > 0) {
                this.mnPrivateLates = new ArrayList<>();
                for(byte[] mn : block.getMnPrivateLateList()) {
                    this.mnPrivateLates.add(ByteUtil.toHexString(mn));
                }
                this.mnRewardPrivateLate = ApisUtil.readableApis(mnRewardData.getPrivateLate(), true);
            }
        }

        this.txSize = block.getTransactionsList().size();

        this.transactions = new ArrayList<>();

        for(Transaction tx : block.getTransactionsList()) {
            if(isContainFullTx) {
                this.transactions.add(new TransactionData(tx, block).getJson());
            } else {
                this.transactions.add(ByteUtil.toHexString0x(tx.getHash()));
            }
        }



        this.size = block.getEncoded().length;
    }


    @Override
    public String toString() {
        return "BlockData{" +
                "number=" + number +
                ", hash='" + hash + '\'' +
                ", parentHash='" + parentHash + '\'' +
                ", coinbase='" + coinbase + '\'' +
                ", coinbaseMask='" + coinbaseMask + '\'' +
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
                ", txSize=" + txSize +
                ", transactions=" + transactions +
                ", logsBloom='" + logsBloom + '\'' +
                ", mnHash='" + mnHash + '\'' +
                ", mnRewardGeneralNormal='" + mnRewardGeneralNormal + '\'' +
                ", mnRewardGeneralLate='" + mnRewardGeneralLate + '\'' +
                ", mnRewardMajorNormal='" + mnRewardMajorNormal + '\'' +
                ", mnRewardMajorLate='" + mnRewardMajorLate + '\'' +
                ", mnRewardPrivateNormal='" + mnRewardPrivateNormal + '\'' +
                ", mnRewardPrivateLate='" + mnRewardPrivateLate + '\'' +
                ", mnGeneralNormals=" + mnGeneralNormals +
                ", mnMajorNormals=" + mnMajorNormals +
                ", mnPrivateNormals=" + mnPrivateNormals +
                ", mnGeneralLates=" + mnGeneralLates +
                ", mnMajorLates=" + mnMajorLates +
                ", mnPrivateLates=" + mnPrivateLates +
                ", size=" + size +
                '}';
    }
}