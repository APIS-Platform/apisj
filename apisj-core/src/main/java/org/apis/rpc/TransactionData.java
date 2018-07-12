package org.apis.rpc;

import org.apis.util.ByteUtil;

import java.math.BigInteger;

public class TransactionData {
    private String hash;
    private BigInteger nonce;
    private String blockHash;
    private long blockNumber;
    private int transactionIndex;
    private String from, to;
    private BigInteger value;
    private BigInteger gasLimit, gasPrice, gasUsed, mineralUsed;
    private String input;

    public TransactionData(byte[] hash, byte[] nonce, byte[] blockHash, long blockNumber,
                           int transactionIndex, byte[] from, byte[] to, byte[] value,
                           byte[] gasLimit, byte[] gasPrice, byte[] gasUsed, byte[] mineralUsed,
                           byte[] input) {
        this.hash = ByteUtil.toHexString(hash);
        this.nonce = ByteUtil.bytesToBigInteger(nonce);
        this.blockHash = ByteUtil.toHexString(blockHash);
        this.blockNumber = blockNumber;
        this.transactionIndex = transactionIndex;
        this.from = ByteUtil.toHexString(from);
        this.to = ByteUtil.toHexString(to);
        this.value = ByteUtil.bytesToBigInteger(value);
        this.gasLimit = ByteUtil.bytesToBigInteger(gasLimit);
        this.gasPrice = ByteUtil.bytesToBigInteger(gasPrice);
        this.gasUsed = ByteUtil.bytesToBigInteger(gasUsed);
        this.mineralUsed = ByteUtil.bytesToBigInteger(mineralUsed);
        this.input = ByteUtil.toHexString(input);

    }

}
