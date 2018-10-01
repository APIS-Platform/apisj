package org.apis.keystore;

import com.google.gson.annotations.SerializedName;

public class KeyStoreDataExp extends KeyStoreData{

    @SerializedName("balance")
    public String balance = "0";

    @SerializedName("mineral")
    public String mineral = "0";

    @SerializedName("mask")
    public String mask = "";

    @SerializedName("rewards")
    public String rewards = "0";

    public KeyStoreDataExp(KeyStoreData key) {
        this.id = key.id;
        this.version = key.version;
        this.address = key.address;
        this.alias = key.alias;
        this.crypto = key.crypto;
    }
}
