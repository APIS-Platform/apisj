package org.apis.rpc.template;

import com.google.gson.GsonBuilder;
import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;

import java.math.BigInteger;
import java.nio.charset.Charset;

import static org.apis.util.ByteUtil.bytesToBigInteger;
import static org.apis.util.ByteUtil.toHexString0x;

public class TransactionData {
    private String hash;
    private String nonce = "-1";
    private String blockHash;
    private String blockNumber;
    private String transactionIndex;
    private String from;
    private String to, toMask;
    private String contractAddress;
    private String value = "0";
    private String valueApis;
    private String gas;
    private String gasPrice;
    private String gasPriceApis;
    private String feeLimit;
    private String data;
    private String r;
    private String s;
    private String v;
    private String certR;
    private String certS;
    private String certV;

    public TransactionData(Transaction tx, Block block) {

        this.hash = toHexString0x(tx.getHash());
        this.nonce = ByteUtil.toHexString0x(tx.getNonce());
        if(block != null) {
            this.blockHash = toHexString0x(block.getHash());
            this.blockNumber = String.valueOf(block.getNumber());
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
        this.gas = bytesToBigInteger(tx.getGasLimit()).toString();
        this.gasPrice = bytesToBigInteger(tx.getGasPrice()).toString();
        this.gasPriceApis = ApisUtil.readableApis(BIUtil.toBI(tx.getGasPrice()), ApisUtil.Unit.nAPIS, ',', true) + " nAPIS";
        this.feeLimit = ApisUtil.readableApis(bytesToBigInteger(tx.getGasLimit()).multiply(bytesToBigInteger(tx.getGasPrice())), ',', true) + " APIS";
        this.data = toHexString0x(tx.getData());

        if(tx.getSignature() != null) {
            this.r = ByteUtil.toHexString0x(ByteUtil.bigIntegerToBytes(tx.getSignature().r));
            this.s = ByteUtil.toHexString0x(ByteUtil.bigIntegerToBytes(tx.getSignature().s));
            this.v = "0x" + ByteUtil.oneByteToHexString(tx.getSignature().v);
        }

        if(tx.getCertificate() != null) {
            this.certR = ByteUtil.toHexString0x(ByteUtil.bigIntegerToBytes(tx.getCertificate().r));
            this.certS = ByteUtil.toHexString0x(ByteUtil.bigIntegerToBytes(tx.getCertificate().s));
            this.certV = "0x" + ByteUtil.oneByteToHexString(tx.getCertificate().v);
        }
    }

    public void setTransactionIndex(long transactionIndex) {
        this.transactionIndex = ByteUtil.toHexString0x(ByteUtil.longToBytes(transactionIndex));
    }

    public String getJson() {
        return new GsonBuilder().create().toJson(this);
    }

    public String getData() {
        return data;
    }

    private boolean isEmptyData() {
        return data == null || data.isEmpty();
    }

    public String getValue() {
        return value;
    }

    public long getNonce() {
        return ByteUtil.bytesToBigInteger(ByteUtil.hexStringToBytes(nonce)).longValue();
    }

    public void setNonce(long nonce) {
        this.nonce = ByteUtil.toHexString0x(ByteUtil.longToBytes(nonce));
    }

    public String getGas() {
        return gas;
    }

    public void setGas(BigInteger gas) {
        this.gas = ByteUtil.toHexString0x(ByteUtil.bigIntegerToBytes(gas));
    }

    public boolean isEmptyGas() {
        return this.gas == null || this.gas.isEmpty();
    }

    public String getFrom() {
        return from;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(byte[] gasPriceHex) {
        this.gasPrice = ByteUtil.toHexString0x(gasPriceHex);
    }

    public boolean isGasPriceEmpty() {
        return this.gasPrice == null || this.gasPrice.isEmpty();
    }

    public String getTo() {
        return to;
    }

    public void setTo(byte[] to) {
        this.to = ByteUtil.toHexString(to);
    }

    public boolean isEmptyTo() {
        return this.to == null || this.to.isEmpty();
    }

    public String getToMask() {
        return toMask;
    }

    public boolean isEmptyToMask() {
        return this.toMask == null || this.toMask.isEmpty();
    }

    public Transaction getTransaction(Integer chainId) {
        byte[] nonce = ByteUtil.hexStringToBytes(this.nonce);

        byte[] to;
        if(this.to == null || this.to.isEmpty()) {
            to = null;
        }  else {
            to = ByteUtil.hexStringToBytes(this.to);
        }

        if(this.toMask == null) {
            this.toMask = "";
        }
        byte[] toMask = this.toMask.getBytes(Charset.forName("UTF-8"));

        byte[] gas = ByteUtil.hexStringToBytes(this.gas);
        byte[] gasPrice = ByteUtil.hexStringToBytes(this.gasPrice);

        byte[] data;
        if(isEmptyData()) {
            data = null;
        } else {
            data = ByteUtil.hexStringToBytes(this.data);
        }
        byte[] value = ByteUtil.hexStringToBytes(this.value);

        return new Transaction(nonce, gasPrice, gas, to, toMask, value, data, chainId);
    }


    @Override
    public String toString() {
        return "TransactionData{" +
                "hash='" + hash + '\'' +
                ", nonce=" + nonce +
                ", blockHash='" + blockHash + '\'' +
                ", blockNumber=" + blockNumber +
                ", transactionIndex=" + transactionIndex +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", toMask='" + toMask + '\'' +
                ", contractAddress='" + contractAddress + '\'' +
                ", value='" + value + '\'' +
                ", valueApis='" + valueApis + '\'' +
                ", gas=" + gas +
                ", gasPrice='" + gasPrice + '\'' +
                ", gasPriceApis='" + gasPriceApis + '\'' +
                ", feeLimit='" + feeLimit + '\'' +
                ", data='" + data + '\'' +
                ", r='" + r + '\'' +
                ", s='" + s + '\'' +
                ", v='" + v + '\'' +
                ", certR='" + certR + '\'' +
                ", certS='" + certS + '\'' +
                ", certV='" + certV + '\'' +
                '}';
    }
}
