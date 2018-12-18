package org.apis.rpc.template;

import org.apis.core.Block;
import org.apis.vm.program.InternalTransaction;

import static org.apis.util.ByteUtil.*;

public class InternalTransactionData {

    private String from;
    private String to;
    private String value = "0";
    private String nonce;
    private String data;
    private String timestamp;
    private String blockNumber;
    private String hash;

    public InternalTransactionData(InternalTransaction tx, Block block) {
        this.from = toHexString(tx.getSender());
        this.to = toHexString(tx.getReceiveAddress());
        this.value = bytesToBigInteger(tx.getValue()).toString();
        this.nonce = String.valueOf(byteArrayToLong(tx.getNonce()));
        this.data = toHexString(tx.getData());
        this.timestamp = String.valueOf(block.getTimestamp());
        this.blockNumber = String.valueOf(block.getNumber());
        this.hash = toHexString(tx.getHash());
    }


    @Override
    public String toString() {
        return "InternalTransactionData{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", value='" + value + '\'' +
                ", nonce='" + nonce + '\'' +
                ", data='" + data + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", blockNumber='" + blockNumber + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
