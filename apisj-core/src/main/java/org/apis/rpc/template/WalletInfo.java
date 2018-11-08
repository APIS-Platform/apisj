package org.apis.rpc.template;

import com.google.gson.GsonBuilder;

public class WalletInfo {
    int index;
    String address;
    String mask;
    String APIS;
    String MNR;
    String nonce;
    boolean knowledgeKey;

    public WalletInfo(int index, String address, String mask, String apis, String mineral, String nonce, boolean knowledgeKey) {
        this.index = index;
        this.address = address;
        this.mask = mask;
        this.APIS = apis;
        this.MNR = mineral;
        this.nonce = nonce;
        this.knowledgeKey = knowledgeKey;
    }

    public String getJson() {
        return new GsonBuilder().create().toJson(this);
    }
}
