package org.apis.rpc.template;

import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.core.TransactionInfo;
import org.apis.core.TransactionReceipt;
import org.apis.rpc.template.LogInfoData;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;
import org.apis.vm.LogInfo;

import java.math.BigInteger;
import java.util.List;

import static org.apis.util.blockchain.ApisUtil.readableApis;
import static org.apis.util.ByteUtil.toHexString;


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
     * block number where this transaction was in.
     */
    private long blockNumber;

    /**
     * 32 Bytes - hash of the block where this transaction was in.
     */
    private String blockHash;

    /**
     * 20 Bytes - address of the sender.
     */
    private String from;

    /**
     * 20 Bytes - address of the receiver. null when its a contract creation transaction.
     */
    private String to;

    /**
     * 20 Bytes - The contract address created, if the transaction was a contract creation, otherwise null
     */
    private String contractAddress;

    private String gasPrice;

    /**
     * The amount of gas used by this specific transaction alone.
     */
    private long gasUsed;

    /**
     * The amount of mineral used by this specific transaction alone.
     */
    private String mineralUsed;
    private long gasLimit;
    private String fee;

    /**
     * The total amount of gas used when this transaction was executed in the block.
     */
    private long cumulativeGasUsed;

    /**
     * The total amount of mineral used when this transaction was executed in the block.
     */
    private String cumulativeMineralUsed;

    /**
     * Array of log objects, which this transaction generated.
     */
    private List<LogInfoData> logs;

    /**
     * 256 Bytes - Bloom filter for light clients to quickly retrieve related logs.
     */
    private String logsBloom;

    public TransactionReceiptData(TransactionInfo info, Block block) {
        TransactionReceipt receipt = info.getReceipt();
        Transaction tx = receipt.getTransaction();

        this.transactionHash = toHexString(tx.getHash());

        this.transactionIndex = info.getIndex();

        this.blockNumber = block.getNumber();

        this.blockHash = toHexString(info.getBlockHash());

        this.from = toHexString(tx.getSender());

        this.to = toHexString(tx.getReceiveAddress());

        this.contractAddress = toHexString(tx.getContractAddress());

        BigInteger gasPrice = ByteUtil.bytesToBigInteger(tx.getGasPrice());
        this.gasPrice = readableApis(gasPrice) + " APIS";

        BigInteger gasUsed = ByteUtil.bytesToBigInteger(receipt.getGasUsed());
        this.gasUsed = gasUsed.longValue();

        BigInteger mineralUsed = ByteUtil.bytesToBigInteger(receipt.getMineralUsed());
        this.mineralUsed = readableApis(mineralUsed) + " MNR";

        BigInteger gasLimit = ByteUtil.bytesToBigInteger(tx.getGasLimit());
        this.gasLimit = gasLimit.longValue();

        this.cumulativeGasUsed = receipt.getCumulativeGasLong();

        this.cumulativeMineralUsed = readableApis(receipt.getCumulativeMineralBI()) + " MNR";

        BigInteger fee = gasUsed.multiply(gasPrice).subtract(mineralUsed);
        this.fee = readableApis(fee) + " APIS";

        for(LogInfo logInfo : receipt.getLogInfoList()) {
            this.logs.add(new LogInfoData(logInfo));
        }
        this.logsBloom = toHexString(receipt.getBloomFilter().getData());

        this.status = toHexString(receipt.getPostTxState());
    }

    /*public TransactionReceiptData(String transactionHash, int transactionIndex, long blockNumber, String blockHash,
                                  BigInteger cumulativeGasUsed, String log, String logsBloom, int status) {
        this.transactionHash = transactionHash;
        this.transactionIndex = transactionIndex;
        this.blockNumber = blockNumber;
        this.blockHash = blockHash;
        this.cumulativeGasUsed = cumulativeGasUsed;
        this.log = log;
        this.logsBloom = logsBloom;
        this.status = status;
    }*/
}
