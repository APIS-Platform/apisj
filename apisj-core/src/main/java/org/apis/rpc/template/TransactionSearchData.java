package org.apis.rpc.template;

public class TransactionSearchData {
    /**
     * either 1 (success) or 0 (failure)
     */
    private String status;

    /**
     * 32 Bytes - hash of the transaction.
     */
    private String transactionHash;

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

    private long gas;

    private String gasPrice;
    private String gasPriceAPIZ;

    /**
     * The amount of gas used by this specific transaction alone.
     */
    private long gasUsed;

    /**
     * The amount of mineral used by this specific transaction alone.
     */
    private String fee;
    private String feeAPIZ;

    private String mineralUsed;
    private String mineralUsedMNR;

    private String feePaid;
    private String feePaidAPIZ;


    public TransactionSearchData(TransactionReceiptData txData) {
        this.status = txData.getStatus();
        this.transactionHash = txData.getTransactionHash();
        this.blockHash = txData.getBlockHash();
        this.blockNumber = txData.getBlockNumber();
        this.timestamp = txData.getTimestamp();
        this.from = txData.getFrom();
        this.to = txData.getTo();
        this.toMask = txData.getToMask();
        this.contractAddress = txData.getContractAddress();
        this.gas = txData.getGas();
        this.gasPrice = txData.getGasPrice();
        this.gasPriceAPIZ = txData.getGasPriceAPIZ();
        this.gasUsed = txData.getGasUsed();
        this.fee = txData.getFee();
        this.feeAPIZ = txData.getFeeAPIZ();
        this.feePaid = txData.getFeePaid();
        this.feePaidAPIZ = txData.getFeePaidAPIZ();
        this.mineralUsed = txData.getMineralUsed();
        this.mineralUsedMNR = txData.getMineralUsedMNR();
    }

    @Override
    public String toString() {
        return "TransactionSearchData{" +
                "status='" + status + '\'' +
                ", transactionHash='" + transactionHash + '\'' +
                ", blockHash='" + blockHash + '\'' +
                ", blockNumber=" + blockNumber +
                ", timestamp='" + timestamp + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", toMask='" + toMask + '\'' +
                ", contractAddress='" + contractAddress + '\'' +
                ", gas=" + gas +
                ", gasPrice='" + gasPrice + '\'' +
                ", gasPriceAPIZ='" + gasPriceAPIZ + '\'' +
                ", gasUsed=" + gasUsed +
                ", fee='" + fee + '\'' +
                ", feeAPIZ='" + feeAPIZ + '\'' +
                ", mineralUsed='" + mineralUsed + '\'' +
                ", mineralUsedMNR='" + mineralUsedMNR + '\'' +
                ", feePaid='" + feePaid + '\'' +
                ", feePaidAPIZ='" + feePaidAPIZ + '\'' +
                '}';
    }
}
