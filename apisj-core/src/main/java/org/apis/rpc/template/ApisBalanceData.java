package org.apis.rpc.template;

public class ApisBalanceData {
    String address;
    String APIS;
    String MNR;

    public ApisBalanceData(String address, String apis, String mineral) {
        this.address = address;
        this.APIS = apis;
        this.MNR = mineral;
    }
}
