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
    private boolean isMasternode;
    private String isContract;

    public WalletInfo (long index, byte[] address, AccountState state, long blockNumber, BigInteger nonce) {
        if(index >= 0) {
            this.index = String.valueOf(index);
        }
        this.address = ByteUtil.toHexString(address);
        this.mask = state.getAddressMask();
        this.APIS = ApisUtil.readableApis(state.getBalance(), ',', true);;
        this.aAPIS = state.getBalance().toString();
        this.MNR = ApisUtil.readableApis(state.getMineral(blockNumber), ',', true);
        this.aMNR = state.getMineral(blockNumber).toString();
        this.Reward = ApisUtil.readableApis(state.getTotalReward(), ',', true);
        this.aReward = state.getTotalReward().toString();
        this.proofKey = ByteUtil.toHexString(state.getProofKey());



        String isContract = null;
        boolean isMasternode = false;
        byte[] codeHash = state.getCodeHash();
        if (codeHash != null && !FastByteComparisons.equal(codeHash, HashUtil.EMPTY_DATA_HASH)) {
            isContract = Boolean.toString(true);
        }
        if (state.getMnStartBlock().compareTo(BigInteger.ZERO) > 0) {
            isMasternode = true;
        }
        this.isContract = isContract;
        this.isMasternode = isMasternode;
        this.nonce = nonce.toString();
    }

    public WalletInfo(int index, byte[] address, String mask, BigInteger aAPIS, BigInteger aMNR, BigInteger nonce, byte[] proofKey, String isContract, boolean isMasternode) {
        if(index >= 0) {
            this.index = String.valueOf(index);
        }

        this.address = ByteUtil.toHexString0x(address);

        if(mask != null && !mask.isEmpty()) {
            this.mask = mask;
        } else {
            this.mask = "";
        }

        this.aAPIS = aAPIS.toString();
        this.APIS = ApisUtil.readableApis(aAPIS, ',', true);
        this.aMNR = aMNR.toString();
        this.MNR = ApisUtil.readableApis(aMNR, ',', true);

        this.nonce = nonce.toString();

        if(proofKey != null && !FastByteComparisons.equal(proofKey, HashUtil.EMPTY_DATA_HASH)) {
            this.proofKey = ByteUtil.toHexString0x(proofKey);
        } else {
            this.proofKey = "";
        }

        if(isContract != null && !isContract.isEmpty()) {
            this.isContract = isContract;
        }

        this.isMasternode = isMasternode;
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
