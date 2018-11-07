package org.apis.rpc.template;

import com.google.gson.GsonBuilder;
import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.core.TransactionInfo;
import org.apis.core.TransactionReceipt;
import org.apis.util.ByteUtil;

import java.math.BigInteger;
import java.nio.charset.Charset;

import static org.apis.util.ByteUtil.toHexString;
import static org.apis.util.ByteUtil.bytesToBigInteger;
import static org.apis.util.blockchain.ApisUtil.readableApis;

public class TransactionData {
    private String hash;
    private long nonce;
    private String blockHash;
    private long blockNumber;
    private String fromAddress;
    private String toAddress, toMask;
    private String value;
    private String gasPrice;
    private long gasLimit;
    private String feeLimit;
    private String input;

    public TransactionData(Transaction tx, Block block) {

        this.hash = ByteUtil.toHexString(tx.getHash());
        this.nonce = ByteUtil.bytesToBigInteger(tx.getNonce()).longValue();
        if(block == null) {
            this.blockHash = "";
        } else {
            this.blockHash = ByteUtil.toHexString(block.getHash());
            this.blockNumber = block.getNumber();
        }

        this.fromAddress = toHexString(tx.getSender());
        this.toAddress = toHexString(tx.getReceiveAddress());
        this.toMask = new String(tx.getReceiveMask(), Charset.forName("UTF-8"));
        this.value = readableApis(bytesToBigInteger(tx.getValue())) + " APIS";
        this.gasLimit = bytesToBigInteger(tx.getGasLimit()).longValue();
        this.gasPrice = readableApis(bytesToBigInteger(tx.getGasPrice())) + " APIS";
        this.feeLimit = readableApis(bytesToBigInteger(tx.getGasLimit()).multiply(bytesToBigInteger(tx.getGasPrice()))) + " APIS";
        this.input = toHexString(tx.getData());
    }

    public String getJson() {
        return new GsonBuilder().create().toJson(this);
    }

}
