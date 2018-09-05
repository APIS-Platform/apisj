package org.apis.keystore;

import com.google.gson.annotations.SerializedName;

public class KeyStoreDataExp extends KeyStoreData{

    @SerializedName("balance")
    public String balance;

    @SerializedName("mineral")
    public String mineral;

    @SerializedName("mask")
    public String mask;

    @SerializedName("rewards")
    public String rewards;
}
