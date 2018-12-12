package org.apis.contract;

import org.apis.core.TransactionExecutor;
import org.apis.core.TransactionReceipt;
import org.apis.util.ByteUtil;

public class EstimateTransactionResult {
    private boolean isSuccess;
    private long gasUsed;
    private TransactionExecutor executor;
    private String error;

    EstimateTransactionResult(TransactionExecutor executor) {
        TransactionReceipt receipt = executor.getReceipt();
        this.isSuccess = receipt.isSuccessful();
        this.gasUsed = ByteUtil.bytesToBigInteger(receipt.getGasUsed()).longValue();
        this.executor = executor;
    }

    EstimateTransactionResult(TransactionExecutor executor, long gasUsed) {
        this(executor);
        this.gasUsed = gasUsed;
    }

    EstimateTransactionResult(String error) {
        this.error = error;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public TransactionReceipt getReceipt() {
        if(executor == null) {
            return null;
        }
        return executor.getReceipt();
    }

    public TransactionExecutor getExecutor() {
        return executor;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public byte[] getDeployBytes() {
        if(executor != null) {
            return executor.getResult().getHReturn();
        } else {
            return null;
        }
    }
}
