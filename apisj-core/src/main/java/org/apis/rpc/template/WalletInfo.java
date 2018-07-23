package org.apis.rpc.template;

import com.google.gson.GsonBuilder;

public class WalletInfo {
    String address;
    String APIS;
    String mineral;

    public WalletInfo(String address, String apis, String mineral) {
        this.address = address;
        this.APIS = apis;
        this.mineral = mineral;
    }

    public String getJson() {
        return new GsonBuilder().create().toJson(this);
    }
}
