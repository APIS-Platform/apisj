package org.apis.keystore;

import java.math.BigInteger;

public class KeyStoreDataExp extends KeyStoreData{

    public BigInteger balance = BigInteger.ZERO;
    public BigInteger mineral = BigInteger.ZERO;
    public BigInteger rewards = BigInteger.ZERO;
    public String mask = "";
    public String ledgerPath = "";
    public boolean isUsedProofkey = false;
    public boolean isLedger = false;

    public KeyStoreDataExp(KeyStoreData key) {
        this.id = key.id;
        this.version = key.version;
        this.address = key.address;
        this.alias = key.alias;
        this.crypto = key.crypto;
    }
}
