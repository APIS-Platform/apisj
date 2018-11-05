package org.apis.contract;

import org.apis.core.CallTransaction;

import java.math.BigInteger;

public class Wink {
    private byte[] beneficiary;
    private byte[] winker;

    Wink(CallTransaction.Invocation event) {
        if(event != null && event.args != null && event.args.length == 2) {
            this.beneficiary = (byte[]) event.args[0];
            this.winker = (byte[]) event.args[1];
        }
    }

    public byte[] getBeneficiary() {
        return beneficiary;
    }

    public byte[] getWinker() {
        return winker;
    }
}
