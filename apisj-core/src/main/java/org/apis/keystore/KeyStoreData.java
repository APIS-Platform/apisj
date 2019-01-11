package org.apis.keystore;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class KeyStoreData {

    @SerializedName("version")
    public int version;

    @SerializedName("id")
    public String id;

    @SerializedName("alias")
    public String alias;

    @SerializedName("address")
    public String address;

    @SerializedName("crypto")
    public Crypto crypto;


    static class Crypto {
        @SerializedName("ciphertext")
        public String ciphertext;

        @SerializedName("cipherparams")
        public cipherparams cipherparams;

        @SerializedName("cipher")
        public String cipher;

        @SerializedName("kdf")
        public String kdf;

        @SerializedName("kdfparams")
        public kdfparams kdfparams;

        @SerializedName("mac")
        public String mac;
    }

    static class cipherparams {
        @SerializedName("iv")
        public String iv;
    }

    static class kdfparams {
        @SerializedName("dklen")
        public int dklen;

        @SerializedName("salt")
        public String salt;

        @SerializedName("n")
        public int n;

        @SerializedName("r")
        public int r;

        @SerializedName("p")
        public int p;

        @SerializedName("c")
        public int c;

        @SerializedName("prf")
        public String prf;
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}