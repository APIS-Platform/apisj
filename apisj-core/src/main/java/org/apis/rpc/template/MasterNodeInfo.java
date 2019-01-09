package org.apis.rpc.template;

public class MasterNodeInfo {
    private long startBlock;
    private long lastBlock;
    private String receiptAddress;
    private String balance;

    public MasterNodeInfo(long startBlock, long lastBlock,
                          String receiptAddress, String balance) {
        this.startBlock = startBlock;
        this.lastBlock = lastBlock;
        this.receiptAddress = receiptAddress;
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "MasterNodeInfo{" +
                "startBlock=" + startBlock +
                ", lastBlock=" + lastBlock +
                ", receiptAddress='" + receiptAddress + '\'' +
                ", balance='" + balance + '\'' +
                '}';
    }
}
