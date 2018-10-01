package org.apis.rpc.template;

import com.google.gson.GsonBuilder;

public class WalletInfo {
    String address;
    String mask;
    String APIS;
    String MNR;
    String nonce;

    public WalletInfo(String address, String mask, String apis, String mineral, String nonce) {
        this.address = address;
        this.mask = mask;
        this.APIS = apis;
        this.MNR = mineral;
        this.nonce = nonce;
    }

    public String getJson() {
        return new GsonBuilder().create().toJson(this);
    }
}
