package org.apis.rpc.template;

import org.apis.core.AccountState;
import org.apis.crypto.HashUtil;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;

public class WalletInfo {
    private String address;
    private String index;
    private String mask;
    private String aAPIS;
    private String aMNR;
    private String nonce;
    private String APIS;
    private String MNR;
    private String Reward;
    private String aReward;
    private String proofKey;
    private Boolean isMasternode;
    private Boolean isContract;

    public WalletInfo (long index, byte[] address, AccountState state, long blockNumber, BigInteger nonce) {
        if(index >= 0) {
            this.index = String.valueOf(index);
        }
        this.address = ByteUtil.toHexString(address);
        String mask = state.getAddressMask();
        if(mask != null && !mask.isEmpty()) {
            this.mask = state.getAddressMask();
        }
        this.APIS = ApisUtil.readableApis(state.getBalance(), ',', true);
        this.aAPIS = state.getBalance().toString();
        this.MNR = ApisUtil.readableApis(state.getMineral(blockNumber), ',', true);
        this.aMNR = state.getMineral(blockNumber).toString();
        BigInteger totalReward = state.getTotalReward();
        if(totalReward.compareTo(BigInteger.ZERO) > 0) {
            this.Reward = ApisUtil.readableApis(totalReward, ',', true);
            this.aReward = state.getTotalReward().toString();
        }

        if(proofKey != null && !FastByteComparisons.equal(state.getProofKey(), HashUtil.EMPTY_DATA_HASH)) {
            this.proofKey = ByteUtil.toHexString(state.getProofKey());
        }

        byte[] codeHash = state.getCodeHash();
        if (codeHash != null && !FastByteComparisons.equal(codeHash, HashUtil.EMPTY_DATA_HASH)) {
            this.isContract = true;
        }
        if (state.getMnStartBalance().compareTo(BigInteger.ZERO) > 0) {
            this.isMasternode = true;
        }
        this.nonce = nonce.toString();
    }

    @Override
    public String toString() {
        return "WalletInfo{" +
                "address='" + address + '\'' +
                ", index='" + index + '\'' +
                ", mask='" + mask + '\'' +
                ", aAPIS='" + aAPIS + '\'' +
                ", aMNR='" + aMNR + '\'' +
                ", nonce='" + nonce + '\'' +
                ", APIS='" + APIS + '\'' +
                ", MNR='" + MNR + '\'' +
                ", Reward='" + Reward + '\'' +
                ", aReward='" + aReward + '\'' +
                ", proofKey='" + proofKey + '\'' +
                ", isMasternode=" + isMasternode +
                ", isContract='" + isContract + '\'' +
                '}';
    }
}
