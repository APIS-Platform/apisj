package org.apis.db.sql;

import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.core.TransactionInfo;
import org.apis.core.TransactionReceipt;
import org.apis.facade.Ethereum;
import org.apis.util.ByteUtil;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionRecord {
    private byte[] hash;
    private long blockUid;
    private long block_number;
    private String block_hash;
    private long nonce;
    private BigInteger gasPrice;
    private long gasLimit;
    private byte[] receiver;
    private byte[] sender;
    private String receiverMask;
    private BigInteger amount;
    private String data;
    private int status;
    private BigInteger gasUsed;
    private BigInteger mineralUsed;
    private String error;
    private String bloom;
    private String logs;
    private String contractAddress;
    private long timestamp;

    public TransactionRecord(ResultSet rs) throws SQLException {
        this.hash = rs.getBytes("txHash");
        this.receiver = rs.getBytes("receiver");
        this.sender = rs.getBytes("sender");
        this.blockUid = rs.getLong("blockUid");
    }

    public TransactionRecord init(Ethereum ethereum) {
        if(hash == null || hash.length == 0) {
            return this;
        }
        TransactionInfo txInfo = ethereum.getTransactionInfo(hash);
        if(txInfo != null) {
            TransactionReceipt receipt = txInfo.getReceipt();
            Transaction tx = receipt.getTransaction();

            Block block = ethereum.getBlockchain().getBlockByHash(txInfo.getBlockHash());
            block_hash = ByteUtil.toHexString(block.getHash());
            block_number = block.getNumber();
            nonce = ByteUtil.bytesToBigInteger(tx.getNonce()).longValue();
            gasPrice = ByteUtil.bytesToBigInteger(tx.getGasPrice());
            gasLimit = ByteUtil.bytesToBigInteger(tx.getGasLimit()).longValue();
            receiverMask = new String(tx.getReceiveMask(), Charset.forName("UTF-8"));
            amount = ByteUtil.bytesToBigInteger(tx.getValue());
            data = ByteUtil.toHexString(tx.getData());
            status = (int) ByteUtil.bytesToBigInteger(receipt.getPostTxState()).longValue();
            gasUsed = ByteUtil.bytesToBigInteger(receipt.getGasUsed());
            mineralUsed = ByteUtil.bytesToBigInteger(receipt.getMineralUsed());
            error = receipt.getError();
            logs = receipt.getLogInfoList().toString();
            contractAddress = ByteUtil.toHexString(tx.getContractAddress());
            timestamp = block.getTimestamp();
        }

        return this;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public BigInteger getMineralUsed() {
        return mineralUsed;
    }

    public int getStatus() {
        return status;
    }

    public long getBlock_number() {
        return block_number;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public long getNonce() {
        return nonce;
    }

    public String getBlock_hash() {
        return block_hash;
    }

    public String getBloom() {
        return bloom;
    }

    public String getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public String getHash() {
        return ByteUtil.toHexString(hash);
    }

    public String getReceiver() {
        return ByteUtil.toHexString(receiver);
    }

    public String getLogs() {
        return logs;
    }

    public String getReceiverMask() {
        return receiverMask;
    }

    public String getSender() {
        return ByteUtil.toHexString(sender);
    }

    public String getContractAddress(){
        return contractAddress;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "TransactionRecord{" +
                "hash='" + hash + '\'' +
                ", blockUid=" + blockUid +
                ", block_number=" + block_number +
                ", block_hash='" + block_hash + '\'' +
                ", nonce=" + nonce +
                ", gasPrice=" + gasPrice +
                ", gasLimit=" + gasLimit +
                ", receiver='" + receiver + '\'' +
                ", sender='" + sender + '\'' +
                ", receiverMask='" + receiverMask + '\'' +
                ", amount=" + amount +
                ", data='" + data + '\'' +
                ", status=" + status +
                ", gasUsed=" + gasUsed +
                ", mineralUsed=" + mineralUsed +
                ", error='" + error + '\'' +
                ", bloom='" + bloom + '\'' +
                ", logs='" + logs + '\'' +
                ", contractAddress='" + contractAddress + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
