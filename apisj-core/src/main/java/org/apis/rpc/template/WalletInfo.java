package org.apis.rpc.template;

import org.apis.crypto.HashUtil;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;

public class WalletInfo {
    private String address;
    private String index;
    private String mask;
    private String aAPIZ;
    private String aMNR;
    private String nonce;
    private String APIZ;
    private String MNR;
    private String proofKey;
    private boolean isMasternode;
    private String isContract;

    public WalletInfo(int index, byte[] address, String mask, BigInteger aAPIZ, BigInteger amineral, BigInteger nonce, byte[] proofKey, String isContract, boolean isMasternode) {
        if(index >= 0) {
            this.index = String.valueOf(index);
        }

        this.address = ByteUtil.toHexString0x(address);

        if(mask != null && !mask.isEmpty()) {
            this.mask = mask;
        } else {
            this.mask = "";
        }

        this.aAPIZ = aAPIZ.toString();
        this.APIZ = ApisUtil.readableApis(aAPIZ, ',', true);
        this.aMNR = amineral.toString();
        this.MNR = ApisUtil.readableApis(amineral, ',', true);

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
                ", aAPIZ='" + aAPIZ + '\'' +
                ", aMNR='" + aMNR + '\'' +
                ", nonce='" + nonce + '\'' +
                ", APIZ='" + APIZ + '\'' +
                ", MNR='" + MNR + '\'' +
                ", proofKey='" + proofKey + '\'' +
                ", isMasternode=" + isMasternode +
                ", isContract='" + isContract + '\'' +
                '}';
    }
}
