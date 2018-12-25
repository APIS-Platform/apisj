package org.apis.rpc.template;

import org.apis.core.Transaction;
import org.apis.core.TransactionInfo;
import org.apis.core.TransactionReceipt;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;

import java.nio.charset.Charset;

public class TransactionInBlockData {
    private String hash;
    private String from;
    private String fromMask;
    private String to;
    private String toMask;
    private String valueApis;
    private String feeApis;
    private boolean success;

    public TransactionInBlockData(byte[] hash) {
        this.hash = ByteUtil.toHexString(hash);
    }

    public TransactionInBlockData(TransactionInfo info) {
        TransactionReceipt receipt = info.getReceipt();
        Transaction tx = receipt.getTransaction();

        this.hash = ByteUtil.toHexString(tx.getHash());
        this.from = ByteUtil.toHexString(tx.getSender());
        if(tx.getReceiveAddress() == null) {
            this.to = "";
        } else {
            this.to = ByteUtil.toHexString(tx.getReceiveAddress());
        }
        if(tx.getReceiveMask() != null) {
            this.toMask = new String(tx.getReceiveMask(), Charset.forName("UTF-8"));
        }
        this.valueApis = ApisUtil.readableApis(ByteUtil.bytesToBigInteger(tx.getValue()), ',', true);
        this.feeApis = ApisUtil.readableApis(ByteUtil.bytesToBigInteger(receipt.getGasUsed()).multiply(ByteUtil.bytesToBigInteger(tx.getGasPrice())), ',', true);
        this.success = receipt.isSuccessful();
    }

    @Override
    public String toString() {
        return "TransactionInBlockData{" +
                "hash='" + hash + '\'' +
                ", from='" + from + '\'' +
                ", fromMask='" + fromMask + '\'' +
                ", to='" + to + '\'' +
                ", toMask='" + toMask + '\'' +
                ", valueApis='" + valueApis + '\'' +
                ", feeApis='" + feeApis + '\'' +
                ", success=" + success +
                '}';
    }
}
