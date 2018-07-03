package org.apis.scrap;

import org.apis.core.Transaction;
import org.apis.util.BIUtil;
import org.apis.vm.program.InternalTransaction;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

class InsertInternalTx {

    private String parentHash, hash, nonce, gasPrice, gas, sendAddress, receiveAddress, value, data, note, deep, index;
    private int rejected;

    InsertInternalTx(InternalTransaction tx) {
        this.parentHash = Hex.toHexString(tx.getParentHash());
        this.hash = Hex.toHexString(tx.getHash());
        this.nonce = BIUtil.toBI(tx.getNonce()).toString();
        this.gasPrice =  BIUtil.toBI(tx.getGasPrice()).toString();
        this.gas = BIUtil.toBI(tx.getGasLimit()).toString();
        this.receiveAddress = Hex.toHexString(tx.getReceiveAddress());
        this.sendAddress = Hex.toHexString(tx.getSender());
        this.value = BIUtil.toBI(tx.getValue()).toString();
        this.data = (tx.getData() == null ? "" : Hex.toHexString(tx.getData()));
        this.note = tx.getNote();
        this.deep = String.valueOf(tx.getDeep());
        this.index = String.valueOf(tx.getIndex());
    }

    private String getInsertQuery() {
        return "INSERT INTO `internalTransactions` (" +
                "`parentHash`, `hash`, `nonce`, `gasPrice`, " +
                "`gas`, `receiveAddress`, `sendAddress`, `value`, " +
                "`data`, `note`, `deep`, `index`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                "ON DUPLICATE KEY UPDATE " +
                "parentHash = VALUES(parentHash)," +
                "hash = VALUES(hash)," +
                "nonce = VALUES(nonce)," +
                "gasPrice = VALUES(gasPrice)," +
                "gas = VALUES(gas)," +
                "receiveAddress = VALUES(receiveAddress)," +
                "sendAddress = VALUES(sendAddress)," +
                "value = VALUES(value)," +
                "data = VALUES(data)," +
                "note = VALUES(note)," +
                "deep = VALUES(deep)," +
                "index = VALUES(index)";
    }

    PreparedStatement getInsertState(Connection conn) throws SQLException {
        PreparedStatement preparedState = conn.prepareStatement(getInsertQuery(), Statement.RETURN_GENERATED_KEYS);
        preparedState.setString(1, parentHash);
        preparedState.setString(2, hash);
        preparedState.setString(3, nonce);
        preparedState.setString(4, gasPrice);
        preparedState.setString(5, gas);
        preparedState.setString(6, receiveAddress);
        preparedState.setString(7, sendAddress);
        preparedState.setString(8, value);
        preparedState.setString(9, data);
        preparedState.setString(10, note);
        preparedState.setString(11, deep);
        preparedState.setString(12, index);

        return preparedState;
    }
}
