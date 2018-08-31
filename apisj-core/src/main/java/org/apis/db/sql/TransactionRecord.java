package org.apis.db.sql;

import org.apis.util.ByteUtil;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionRecord {
    private String hash;
    private long block_number;
    private String block_hash;
    private long nonce;
    private BigInteger gasPrice;
    private long gasLimit;
    private String receiver;
    private String sender;
    private String receiverMask;
    private BigInteger amount;
    private String data;
    private int status;
    private long gasUsed;
    private BigInteger mineralUsed;
    private String error;
    private String bloom;
    private String logs;

    public TransactionRecord(ResultSet rs) throws SQLException {
        this.hash = rs.getString("hash");
        this.block_number = rs.getLong("block_number");
        this.block_hash = rs.getString("block_hash");
        this.nonce = rs.getLong("nonce");
        this.gasPrice = ByteUtil.bytesToBigInteger(ByteUtil.hexStringToBytes(rs.getString("gasPrice")));
        this.gasLimit = rs.getLong("gasLimit");
        this.receiver = rs.getString("to");
        this.sender = rs.getString("from");
        this.receiverMask = rs.getString("mask");
        this.amount = ByteUtil.bytesToBigInteger(ByteUtil.hexStringToBytes(rs.getString("amount")));
        this.data = rs.getString("data");
        this.status = rs.getInt("status");
        this.gasUsed = rs.getLong("gasUsed");
        this.mineralUsed = ByteUtil.bytesToBigInteger(ByteUtil.hexStringToBytes(rs.getString("mineralUsed")));
        this.error = rs.getString("error");
        this.bloom = rs.getString("bloom");
        this.logs = rs.getString("logs");
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

    public long getGasUsed() {
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
        return hash;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getLogs() {
        return logs;
    }

    public String getReceiverMask() {
        return receiverMask;
    }

    public String getSender() {
        return sender;
    }

    @Override
    public String toString() {
        return "TransactionRecord{" +
                "hash='" + hash + '\'' +
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
                '}';
    }
}
