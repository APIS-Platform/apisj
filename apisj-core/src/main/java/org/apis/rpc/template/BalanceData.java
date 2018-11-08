package org.apis.rpc.template;

import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;

public class BalanceData {
    private String aAPIS;
    private String aMNR;
    private String APIS;
    private String MNR;

    public BalanceData(BigInteger aAPIS, BigInteger aMNR) {
        this.aAPIS = aAPIS.toString();
        this.aMNR = aMNR.toString();
        this.APIS = ApisUtil.readableApis(aAPIS, ',', true);
        this.MNR = ApisUtil.readableApis(aMNR, ',', true);
    }

    @Override
    public String toString() {
        return "BalanceData{" +
                "aAPIS='" + aAPIS + '\'' +
                ", aMNR='" + aMNR + '\'' +
                ", APIS='" + APIS + '\'' +
                ", MNR='" + MNR + '\'' +
                '}';
    }
}
