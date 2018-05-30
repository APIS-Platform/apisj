package org.apis.json;

import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.core.TransactionReceipt;
import org.apis.util.ByteUtil;
import org.apis.vm.LogInfo;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

public class TransactionReceiptData {

    public long blockNumber;

    public String blockHash;

    public String hash;

    public String nonce;

    public String gasPrice;

    public String receiveAddress;

    public String sendAddress;

    public String value;

    public String data;



    public int txStatus;

    public String cumulativeGas;

    public String cumulativeMineral;

    public String gasUsed;

    public String mineralUsed;

    public String error;

    public String executionResult;

    public String bloom;

    public String logs;


    public TransactionReceiptData(Block block, TransactionReceipt receipt) {
        this.blockNumber = block.getNumber();
        this.blockHash = Hex.toHexString(block.getHash());

        Transaction tx = receipt.getTransaction();
        this.hash = ByteUtil.toHexString(tx.getHash());
        this.nonce = ByteUtil.toHexString(tx.getNonce());
        this.gasPrice = new BigInteger(tx.getGasPrice()).toString();
        this.receiveAddress = ByteUtil.toHexString(tx.getReceiveAddress());
        this.sendAddress = ByteUtil.toHexString(tx.getSender());
        this.value = new BigInteger(tx.getValue()).toString();
        this.data = ByteUtil.toHexString(tx.getData());

        this.txStatus = new BigInteger(receipt.getPostTxState()).intValue();
        this.cumulativeGas = new BigInteger(receipt.getCumulativeGas()).toString();
        this.cumulativeMineral = new BigInteger(receipt.getCumulativeMineral()).toString();
        this.gasUsed = new BigInteger(receipt.getGasUsed()).toString();
        this.mineralUsed = new BigInteger(receipt.getMineralUsed()).toString();
        this.error = receipt.getError();
        this.executionResult = ByteUtil.toHexString(receipt.getExecutionResult());
        this.bloom = ByteUtil.toHexString(receipt.getBloomFilter().getData());

        StringBuilder logs = new StringBuilder();
        for(LogInfo info : receipt.getLogInfoList()) {
            logs.append(info.toString());
        }
        this.logs = logs.toString();
    }

}