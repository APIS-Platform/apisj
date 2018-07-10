package org.apis.scrap;

import org.apis.core.Transaction;
import org.apis.core.TransactionReceipt;
import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class InsertTxReceipt {

    private String txStatus, cumulativeGas, cumulativeMineral, gasUsed, mineralUsed, error, executionResult, bloom, logs, transactions_hash;

    InsertTxReceipt(TransactionReceipt receipt, byte[] transactionHash) {
        this.txStatus = Hex.toHexString(receipt.getPostTxState());
        this.cumulativeGas = BIUtil.toBI(receipt.getCumulativeGas()).toString();
        this.cumulativeMineral =  receipt.getCumulativeMineralBI().toString();
        this.gasUsed = BIUtil.toBI(receipt.getGasUsed()).toString();
        this.mineralUsed = ByteUtil.bytesToBigInteger(receipt.getMineralUsed()).toString();
        this.error = receipt.getError();
        this.executionResult = Hex.toHexString(receipt.getExecutionResult());
        this.bloom = (receipt.getBloomFilter().getData() == null ? "" : Hex.toHexString(receipt.getBloomFilter().getData()));
        this.logs = receipt.getLogInfoList().toString();
        this.transactions_hash = Hex.toHexString(transactionHash);
    }

    private String getInsertQuery() {
        return "INSERT INTO `transactionReceipts` (" +
                "`txStatus`, `cumulativeGas`, `cumulativeMineral`, `gasUsed`, " +
                "`mineralUsed`, `error`, `executionResult`, `bloom`, " +
                "`logs`, `transactions_hash`" +
                ") VALUES (" +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
                ") ON DUPLICATE KEY UPDATE " +
                "txStatus = VALUES(txStatus)," +
                "cumulativeGas = VALUES(cumulativeGas)," +
                "cumulativeMineral = VALUES(cumulativeMineral)," +
                "gasUsed = VALUES(gasUsed)," +
                "mineralUsed = VALUES(mineralUsed)," +
                "error = VALUES(error)," +
                "executionResult = VALUES(executionResult)," +
                "bloom = VALUES(bloom)," +
                "logs = VALUES(logs)," +
                "transactions_hash = VALUES(transactions_hash)";
    }

    PreparedStatement getInsertState(Connection conn) throws SQLException {
        PreparedStatement preparedState = conn.prepareStatement(getInsertQuery());
        preparedState.setString(1, txStatus);
        preparedState.setString(2, cumulativeGas);
        preparedState.setString(3, cumulativeMineral);
        preparedState.setString(4, gasUsed);
        preparedState.setString(5, mineralUsed);
        preparedState.setString(6, error);
        preparedState.setString(7, executionResult);
        preparedState.setString(8, bloom);
        preparedState.setString(9, logs);
        preparedState.setString(10, transactions_hash);

        return preparedState;
    }
}
