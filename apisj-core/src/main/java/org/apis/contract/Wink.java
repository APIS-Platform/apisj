package org.apis.contract;

import org.apis.core.CallTransaction;

import java.util.Arrays;

public class Wink {
    private byte[] address;
    private byte[] beneficiary;

    Wink(byte[] address, CallTransaction.Invocation event) {
        if(event != null && event.args != null && event.args.length == 1) {
            this.address = address;
            this.beneficiary = (byte[]) event.args[0];
        }
    }

    public byte[] getAddress() { return address; }

    public byte[] getBeneficiary() {
        return beneficiary;
    }


    @Override
    public String toString() {
        return "Wink{" +
                "address=" + Arrays.toString(address) +
                ", beneficiary=" + Arrays.toString(beneficiary) +
                '}';
    }
}
