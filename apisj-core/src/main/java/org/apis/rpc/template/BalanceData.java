package org.apis.rpc.template;

import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;

public class BalanceData {
    private String attoAPIS;
    private String attoMNR;
    private String APIS;
    private String MNR;

    public BalanceData(BigInteger aAPIS, BigInteger aMNR) {
        this.attoAPIS = aAPIS.toString();
        this.attoMNR = aMNR.toString();
        this.APIS = ApisUtil.readableApis(aAPIS, '_', true);
        this.MNR = ApisUtil.readableApis(aMNR, '_', true);
    }


    @Override
    public String toString() {
        return "BalanceData{" +
                "attoAPIS='" + attoAPIS + '\'' +
                ", attoMNR='" + attoMNR + '\'' +
                ", APIS='" + APIS + '\'' +
                ", MNR='" + MNR + '\'' +
                '}';
    }
}
