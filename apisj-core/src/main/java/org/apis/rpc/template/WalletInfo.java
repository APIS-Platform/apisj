package org.apis.rpc.template;

import com.google.gson.GsonBuilder;
import org.apis.crypto.HashUtil;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;

public class WalletInfo {
    private String address;
    private int index;
    private String mask;
    private String attoAPIS;
    private String attoMNR;
    private String nonce;
    private String APIS;
    private String MNR;
    private String proofKey;

    public WalletInfo(int index, byte[] address, String mask, BigInteger aapis, BigInteger amineral, BigInteger nonce, byte[] proofKey) {
        this.index = index;
        this.address = ByteUtil.toHexString0x(address);

        if(mask != null && !mask.isEmpty()) {
            this.mask = mask;
        }

        this.attoAPIS = aapis.toString();
        this.APIS = ApisUtil.readableApis(aapis, '_', true);
        this.attoMNR = amineral.toString();
        this.MNR = ApisUtil.readableApis(amineral, '_', true);

        this.nonce = nonce.toString();

        if(proofKey != null && !FastByteComparisons.equal(proofKey, HashUtil.EMPTY_DATA_HASH)) {
            this.proofKey = ByteUtil.toHexString0x(proofKey);
        }
    }


    @Override
    public String toString() {
        return "WalletInfo{" +
                "index=" + index +
                ", address='" + address + '\'' +
                ", mask='" + mask + '\'' +
                ", attoAPIS='" + attoAPIS + '\'' +
                ", APIS='" + APIS + '\'' +
                ", attoMNR='" + attoMNR + '\'' +
                ", MNR='" + MNR + '\'' +
                ", nonce='" + nonce + '\'' +
                ", proofKey='" + proofKey + '\'' +
                '}';
    }
}
