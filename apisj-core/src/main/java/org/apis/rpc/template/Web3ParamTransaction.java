package org.apis.rpc.template;

import com.google.gson.internal.LinkedTreeMap;
import org.apis.util.ByteUtil;

import java.math.BigInteger;

public class Web3ParamTransaction {
    private String nonce;
    private String gas;
    private String gasPrice;
    private String to;
    private String value;
    private String from;
    private String data;
    private String walletIndex;
    private String keystorepassword;
    private String knowledgepassword;

    public Web3ParamTransaction(Object param) {
        if(param instanceof LinkedTreeMap) {
            LinkedTreeMap<String, String> map = (LinkedTreeMap<String, String>) param;

            nonce = map.get("nonce");
            gas = map.get("gas");
            gasPrice = map.get("gasPrice");
            to = map.get("to");
            value = map.get("value");
            from = map.get("from");
            data = map.get("data");
            walletIndex = map.get("walletIndex");
            keystorepassword = map.get("keystorepassword");
            knowledgepassword = map.get("knowledgepassword");
        }
    }

    public long getNonce() {
        if(nonce == null || nonce.isEmpty()) {
            return 0;
        }
        return Long.parseLong(nonce);
    }

    public long getGas() {
        return toLong(gas);
    }

    public BigInteger getGasPrice() {
        return toBI(gasPrice);
    }

    public byte[] getTo() {
        return ByteUtil.hexStringToBytes(to);
    }

    public byte[] getFrom() {
        return ByteUtil.hexStringToBytes(from);
    }

    public BigInteger getValue() {
        return toBI(value);
    }

    public byte[] getData() {
        return ByteUtil.hexStringToBytes(data);
    }

    public long getWalletIndex() {
        return toLong(walletIndex);
    }

    public String getKeystorePassword() {
        return keystorepassword;
    }

    public String getKnowledgePassword() {
        return knowledgepassword;
    }

    @Override
    public String toString() {
        return "Web3ParamTransaction{" +
                "nonce='" + nonce + '\'' +
                ", gas='" + gas + '\'' +
                ", gasPrice='" + gasPrice + '\'' +
                ", to='" + to + '\'' +
                ", value='" + value + '\'' +
                ", from='" + from + '\'' +
                ", data='" + data + '\'' +
                ", walletIndex='" + walletIndex + '\'' +
                ", keystorepassword='" + keystorepassword + '\'' +
                ", knowledgepassword='" + knowledgepassword + '\'' +
                '}';
    }

    private long toLong(String number) {
        if(number == null || number.isEmpty()) {
            return 0;
        }
        return Long.parseLong(number);
    }

    private BigInteger toBI(String number) {
        if(number == null || number.isEmpty()) {
            return BigInteger.ZERO;
        }
        return new BigInteger(number);
    }
}
