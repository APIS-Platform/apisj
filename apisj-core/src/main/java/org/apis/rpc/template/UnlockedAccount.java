package org.apis.rpc.template;

import org.apis.crypto.ECKey;

public class UnlockedAccount {

    private ECKey key;
    private long unlockUntil;

    public UnlockedAccount(ECKey key, long unlockUntil) {
        this.key = key;
        this.unlockUntil = unlockUntil;
    }

    public ECKey getKey() {
        return key;
    }

    public long getUnlockUntil() {
        return unlockUntil;
    }
}
