package org.apis.rpc.template;

import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;

public class BalanceData {
    private String aAPIZ;
    private String aMNR;
    private String APIZ;
    private String MNR;

    public BalanceData(BigInteger aAPIZ, BigInteger aMNR) {
        this.aAPIZ = aAPIZ.toString();
        this.aMNR = aMNR.toString();
        this.APIZ = ApisUtil.readableApis(aAPIZ, ',', true);
        this.MNR = ApisUtil.readableApis(aMNR, ',', true);
    }


    @Override
    public String toString() {
        return "BalanceData{" +
                "aAPIZ='" + aAPIZ + '\'' +
                ", aMNR='" + aMNR + '\'' +
                ", APIZ='" + APIZ + '\'' +
                ", MNR='" + MNR + '\'' +
                '}';
    }
}
