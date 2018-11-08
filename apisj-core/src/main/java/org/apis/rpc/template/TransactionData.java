package org.apis.rpc.template;

import com.google.gson.GsonBuilder;
import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;

import java.nio.charset.Charset;

import static org.apis.util.ByteUtil.bytesToBigInteger;
import static org.apis.util.ByteUtil.toHexString0x;

public class TransactionData {
    private String hash;
    private long nonce;
    private String blockHash;
    private long blockNumber;
    private long transactionIndex;
    private String from;
    private String to, toMask;
    private String contractAddress;
    private String value;
    private String valueApis;
    private long gas;
    private String gasPrice;
    private String gasPriceApis;
    private String feeLimit;
    private String input;

    public TransactionData(Transaction tx, Block block) {

        this.hash = toHexString0x(tx.getHash());
        this.nonce = ByteUtil.bytesToBigInteger(tx.getNonce()).longValue();
        if(block == null) {
            this.blockHash = "";
        } else {
            this.blockHash = toHexString0x(block.getHash());
            this.blockNumber = block.getNumber();
        }

        this.from = toHexString0x(tx.getSender());

        if(!ByteUtil.isNullOrZeroArray(tx.getReceiveAddress())) {
            this.to = toHexString0x(tx.getReceiveAddress());
        }

        if(!ByteUtil.isNullOrZeroArray(tx.getReceiveMask())) {
            this.toMask = new String(tx.getReceiveMask(), Charset.forName("UTF-8"));
        }

        if(!ByteUtil.isNullOrZeroArray(tx.getContractAddress())) {
            this.contractAddress = toHexString0x(tx.getContractAddress());
        }

        this.value = BIUtil.toBI(tx.getValue()).toString();
        this.valueApis = ApisUtil.readableApis(BIUtil.toBI(tx.getValue()), ',', true) + " APIS";
        this.gas = bytesToBigInteger(tx.getGasLimit()).longValue();
        this.gasPrice = bytesToBigInteger(tx.getGasPrice()).toString();
        this.gasPriceApis = ApisUtil.readableApis(BIUtil.toBI(tx.getGasPrice()), ApisUtil.Unit.nAPIS, ',', true) + " nAPIS";
        this.feeLimit = ApisUtil.readableApis(bytesToBigInteger(tx.getGasLimit()).multiply(bytesToBigInteger(tx.getGasPrice())), ',', true) + " APIS";
        this.input = toHexString0x(tx.getData());
    }

    public void setTransactionIndex(long transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public String getJson() {
        return new GsonBuilder().create().toJson(this);
    }

}
