package org.apis.scrap;

import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.util.BIUtil;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class InsertTx {

    private String hash, nonce, gasPrice, gas, receiveAddress, receiveMask, sendAddress, value, data, blocks_hash, rawTx;

    InsertTx(Transaction tx, byte[] blockHash) {
        this.hash = Hex.toHexString(tx.getHash());
        this.nonce = BIUtil.toBI(tx.getNonce()).toString();
        this.gasPrice =  BIUtil.toBI(tx.getGasPrice()).toString();
        this.gas = BIUtil.toBI(tx.getGasLimit()).toString();
        this.receiveAddress = Hex.toHexString(tx.getReceiveAddress());
        this.receiveMask = new String(tx.getReceiveMask(), Charset.forName("UTF-8"));
        this.sendAddress = Hex.toHexString(tx.getSender());
        this.value = BIUtil.toBI(tx.getValue()).toString();
        this.data = (tx.getData() == null? "" : Hex.toHexString(tx.getData()));
        this.blocks_hash = Hex.toHexString(blockHash);
        this.rawTx = Hex.toHexString(tx.getRawHash());
    }

    private String getInsertQuery() {
        return "INSERT INTO `transactions` (" +
                "`hash`, `nonce`, `gasPrice`, `gas`, " +
                "`receiveAddress`, `receiveMask`, `sendAddress`, " +
                "`value`, `data`, `blocks_hash`, `rawTx`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                "ON DUPLICATE KEY UPDATE " +
                "nonce = VALUES(nonce)," +
                "gasPrice = VALUES(gasPrice)," +
                "gas = VALUES(gas)," +
                "receiveAddress = VALUES(receiveAddress)," +
                "receiveMask = VALUES(receiveMask)," +
                "sendAddress = VALUES(sendAddress)," +
                "value = VALUES(value)," +
                "data = VALUES(data)," +
                "blocks_hash = VALUES(blocks_hash)," +
                "rawTx = VALUES(rawTx)";
    }

    PreparedStatement getInsertState(Connection conn) throws SQLException {
        PreparedStatement preparedState = conn.prepareStatement(getInsertQuery());
        preparedState.setString(1, hash);
        preparedState.setString(2, nonce);
        preparedState.setString(3, gasPrice);
        preparedState.setString(4, gas);
        preparedState.setString(5, receiveAddress);
        preparedState.setString(6, receiveMask);
        preparedState.setString(7, sendAddress);
        preparedState.setString(8, value);
        preparedState.setString(9, data);
        preparedState.setString(10, blocks_hash);
        preparedState.setString(11, rawTx);

        return preparedState;
    }
}
