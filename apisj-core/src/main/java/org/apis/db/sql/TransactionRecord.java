package org.apis.db.sql;

import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.core.TransactionInfo;
import org.apis.core.TransactionReceipt;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumImpl;
import org.apis.util.ByteUtil;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionRecord {
    private String hash;
    private long blockUid;
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
    private String gasUsed;
    private BigInteger mineralUsed;
    private String error;
    private String bloom;
    private String logs;
    private String contractAddress;
    private long timestamp;

    public TransactionRecord(ResultSet rs) throws SQLException {
        this.hash = rs.getString("txHash");
        this.receiver = rs.getString("receiver");
        this.sender = rs.getString("sender");
        this.blockUid = rs.getLong("blockUid");
    }

    public TransactionRecord init(Ethereum ethereum) {
        if(hash == null || hash.isEmpty()) {
            return this;
        }
        TransactionInfo txInfo = ethereum.getTransactionInfo(Hex.decode(hash));
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
        gasUsed = ByteUtil.bytesToBigInteger(receipt.getGasUsed()).toString();
        mineralUsed = ByteUtil.bytesToBigInteger(receipt.getMineralUsed());
        error = receipt.getError();
        logs = receipt.getLogInfoList().toString();
        contractAddress = ByteUtil.toHexString(tx.getContractAddress());
        timestamp = block.getTimestamp();

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

    public long getGasUsed() {
        try{
            return hex2Decimal(gasUsed);
        }catch (Exception e){ }
        return 0;
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

    public String getContractAddress(){
        return contractAddress;
    }

    public long getTimestamp() {
        return timestamp;
    }

    private long hex2Decimal(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        long val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16*val + d;
        }
        return val;
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
                ", contractAddress='" + contractAddress + '\'' +
                '}';
    }
}
