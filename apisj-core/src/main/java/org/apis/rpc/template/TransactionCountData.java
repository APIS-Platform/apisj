package org.apis.rpc.template;

public class TransactionCountData {
    private long fromBlock;
    private long toBlock;
    private long count;

    public TransactionCountData(long fromBlock, long toBlock, long count) {
        this.fromBlock = fromBlock;
        this.toBlock = toBlock;
        this.count = count;
    }

    @Override
    public String toString() {
        return "TransactionCountData{" +
                "fromBlock=" + fromBlock +
                ", toBlock=" + toBlock +
                ", count=" + count +
                '}';
    }
}
