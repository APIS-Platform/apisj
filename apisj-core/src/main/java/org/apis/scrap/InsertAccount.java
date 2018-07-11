package org.apis.scrap;

import org.apis.core.Block;
import org.apis.util.BIUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class InsertAccount {

    private String address, balance;

    InsertAccount(byte[] address, BigInteger balance) {
        this.address = Hex.toHexString(address);
        this.balance = balance.toString();
    }

    private String getInsertQuery() {
        return "INSERT INTO `accounts` " +
                "(`address`, `balance`) " +
                "VALUES " +
                "(?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "balance = VALUES(balance)";
    }

    PreparedStatement getInsertState(Connection conn) throws SQLException {
        PreparedStatement preparedState = conn.prepareStatement(getInsertQuery());
        preparedState.setString(1, address);
        preparedState.setString(2, balance);

        return preparedState;
    }
}
