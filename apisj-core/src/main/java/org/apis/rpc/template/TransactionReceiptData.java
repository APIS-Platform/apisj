package org.apis.rpc.template;

import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.core.TransactionInfo;
import org.apis.core.TransactionReceipt;
import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;
import org.apis.vm.LogInfo;
import org.apis.vm.program.InternalTransaction;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.apis.util.ByteUtil.toHexString0x;
import static org.apis.util.blockchain.ApisUtil.readableApis;


public class TransactionReceiptData {
    /**
     * either 1 (success) or 0 (failure)
     */
    private String status;

    /**
     * 32 Bytes - hash of the transaction.
     */
    private String transactionHash;

    /**
     * integer of the transactions index position in the block.
     */
    private int transactionIndex;

    /**
     * 32 Bytes - hash of the block where this transaction was in.
     */
    private String blockHash;

    /**
     * block number where this transaction was in.
     */
    private long blockNumber;

    private String timestamp;

    /**
     * 20 Bytes - address of the sender.
     */
    private String from;

    /**
     * 20 Bytes - address of the receiver. null when its a contract creation transaction.
     */
    private String to;

    private String toMask;

    /**
     * 20 Bytes - The contract address created, if the transaction was a contract creation, otherwise null
     */
    private String contractAddress;


    private String value = "0";
    private String valueAPIS;

    private long nonce;

    private long gas;

    private String gasPrice;
    private String gasPriceAPIS;

    /**
     * The amount of gas used by this specific transaction alone.
     */
    private long gasUsed;

    /**
     * The amount of mineral used by this specific transaction alone.
     */
    private String fee;
    private String feeAPIS;

    private String mineralUsed;
    private String mineralUsedMNR;

    private String feePaid;
    private String feePaidAPIS;


    /**
     * The total amount of gas used when this transaction was executed in the block.
     */
    private long cumulativeGasUsed;

    /**
     * The total amount of mineral used when this transaction was executed in the block.
     */
    private String cumulativeMineralUsed;
    private String cumulativeMineralUsedMNR;

    /**
     * Array of log objects, which this transaction generated.
     */
    private List<LogInfoData> logs;

    private List<InternalTransactionData> internalTransactions;

    /**
     * 256 Bytes - Bloom filter for light clients toAddress quickly retrieve related logs.
     */
    private String logsBloom;

    private String data;

    private String error;

    public TransactionReceiptData(TransactionInfo info, Block block) {
        this(info.getReceipt());

        this.transactionIndex = info.getIndex();

        this.blockNumber = block.getNumber();

        this.blockHash = toHexString0x(info.getBlockHash());

        this.timestamp = String.valueOf(block.getTimestamp());


        if(info.getReceipt().getInternalTransactionList().size() > 0) {
            this.internalTransactions = new ArrayList<>();
            for(InternalTransaction itx : info.getReceipt().getInternalTransactionList()) {
                this.internalTransactions.add(new InternalTransactionData(itx, block));
            }
        }
    }

    private TransactionReceiptData(TransactionReceipt receipt) {
        Transaction tx = receipt.getTransaction();

        this.transactionHash = toHexString0x(tx.getHash());

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

        this.nonce = ByteUtil.byteArrayToLong(tx.getNonce());

        this.value = BIUtil.toBI(tx.getValue()).toString();
        this.valueAPIS = ApisUtil.readableApis(BIUtil.toBI(tx.getValue()), ',', true);

        BigInteger gasPrice = ByteUtil.bytesToBigInteger(tx.getGasPrice());
        this.gasPrice = gasPrice.toString();
        this.gasPriceAPIS = readableApis(gasPrice, ',', true);

        BigInteger gasUsed = ByteUtil.bytesToBigInteger(receipt.getGasUsed());
        this.gasUsed = gasUsed.longValue();

        BigInteger mineralUsed = ByteUtil.bytesToBigInteger(receipt.getMineralUsed());
        this.mineralUsed = mineralUsed.toString();
        this.mineralUsedMNR = readableApis(mineralUsed, ',', true);

        BigInteger gasLimit = ByteUtil.bytesToBigInteger(tx.getGasLimit());
        this.gas = gasLimit.longValue();

        this.cumulativeGasUsed = receipt.getCumulativeGasLong();

        this.cumulativeMineralUsed = receipt.getCumulativeMineralBI().toString();
        this.cumulativeMineralUsedMNR = readableApis(receipt.getCumulativeMineralBI(), ',', true);

        BigInteger fee = gasUsed.multiply(gasPrice);
        this.fee = gasUsed.multiply(gasPrice).toString();
        this.feeAPIS = readableApis(fee, ',', true);

        BigInteger feePaid = gasUsed.multiply(gasPrice).subtract(mineralUsed);
        this.feePaid = feePaid.toString();
        this.feePaidAPIS = readableApis(feePaid, ',', true);

        if (receipt.getLogInfoList().size() > 0) {
            this.logs = new ArrayList<>();
            this.logsBloom = toHexString0x(receipt.getBloomFilter().getData());
        }
        int logIndex = 0;
        for(LogInfo logInfo : receipt.getLogInfoList()) {
            this.logs.add(new LogInfoData(logInfo, blockHash, transactionHash, logIndex, blockNumber, transactionIndex));
            logIndex += 1;
        }

        this.status = toHexString0x(receipt.getPostTxState());

        this.data = ByteUtil.toHexString0x(tx.getData());

        if(receipt.getError() != null && !receipt.getError().isEmpty()) {
            this.error = receipt.getError();
        }
    }

    public String getStatus() {
        return status;
    }

    String getTransactionHash() {
        return transactionHash;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    String getToMask() {
        return toMask;
    }

    String getContractAddress() {
        return contractAddress;
    }

    String getValue() {
        return value;
    }

    String getValueAPIS() {
        return valueAPIS;
    }

    long getGas() {
        return gas;
    }

    long getGasUsed() {
        return gasUsed;
    }

    String getGasPrice() {
        return gasPrice;
    }

    String getGasPriceAPIS() {
        return gasPriceAPIS;
    }

    String getFee() {
        return fee;
    }

    String getFeeAPIS() {
        return feeAPIS;
    }

    String getFeePaid() {
        return feePaid;
    }

    String getFeePaidAPIS() {
        return feePaidAPIS;
    }

    String getMineralUsed() {
        return mineralUsed;
    }

    String getMineralUsedMNR() {
        return mineralUsedMNR;
    }

    @Override
    public String toString() {
        return "TransactionReceiptData{" +
                "status='" + status + '\'' +
                ", transactionHash='" + transactionHash + '\'' +
                ", transactionIndex=" + transactionIndex +
                ", blockHash='" + blockHash + '\'' +
                ", blockNumber=" + blockNumber +
                ", timestamp='" + timestamp + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", toMask='" + toMask + '\'' +
                ", contractAddress='" + contractAddress + '\'' +
                ", value='" + value + '\'' +
                ", valueAPIS='" + valueAPIS + '\'' +
                ", nonce=" + nonce +
                ", gas=" + gas +
                ", gasPrice='" + gasPrice + '\'' +
                ", gasPriceAPIS='" + gasPriceAPIS + '\'' +
                ", gasUsed=" + gasUsed +
                ", fee='" + fee + '\'' +
                ", feeAPIS='" + feeAPIS + '\'' +
                ", mineralUsed='" + mineralUsed + '\'' +
                ", mineralUsedMNR='" + mineralUsedMNR + '\'' +
                ", feePaid='" + feePaid + '\'' +
                ", feePaidAPIS='" + feePaidAPIS + '\'' +
                ", cumulativeGasUsed=" + cumulativeGasUsed +
                ", cumulativeMineralUsed='" + cumulativeMineralUsed + '\'' +
                ", cumulativeMineralUsedMNR='" + cumulativeMineralUsedMNR + '\'' +
                ", logs=" + logs +
                ", internalTransactions=" + internalTransactions +
                ", logsBloom='" + logsBloom + '\'' +
                ", data='" + data + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
