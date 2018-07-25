package org.apis.rpc.template;

import com.google.gson.GsonBuilder;

public class WalletInfo {
    String address;
    String APIS;
    String MNR;
    String nonce;

    public WalletInfo(String address, String apis, String mineral, String nonce) {
        this.address = address;
        this.APIS = apis;
        this.MNR = mineral;
        this.nonce = nonce;
    }

    public String getJson() {
        return new GsonBuilder().create().toJson(this);
    }
}
