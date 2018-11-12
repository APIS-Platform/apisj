package org.apis.rpc.template;

import org.apis.util.ByteUtil;

public class SignTransactionData {
    private String raw;
    private TransactionData tx;

    public SignTransactionData(byte[] rawBytes, TransactionData tx) {
        this.raw = ByteUtil.toHexString0x(rawBytes);
        this.tx = tx;
    }


    @Override
    public String toString() {
        return "SignTransactionData{" +
                "raw='" + raw + '\'' +
                ", tx=" + tx +
                '}';
    }
}
