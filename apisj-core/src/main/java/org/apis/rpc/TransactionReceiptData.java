package org.apis.rpc;

import java.math.BigInteger;

public class TransactionReceiptData {
    private String transactionHash;
    private int transactionIndex;
    private long blockNumber;
    private String blockHash;
    private BigInteger cumulativeGasUsed;
    private String log, logsBloom;
    private int status;

    public TransactionReceiptData(String transactionHash, int transactionIndex, long blockNumber, String blockHash,
                                  BigInteger cumulativeGasUsed, String log, String logsBloom, int status) {
        this.transactionHash = transactionHash;
        this.transactionIndex = transactionIndex;
        this.blockNumber = blockNumber;
        this.blockHash = blockHash;
        this.cumulativeGasUsed = cumulativeGasUsed;
        this.log = log;
        this.logsBloom = logsBloom;
        this.status = status;
    }
}
