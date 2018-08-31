package org.apis.db.sql;

import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class AccountRecord {
    private byte[] address;
    private String title;
    private BigInteger balance;
    private String mask;
    private BigInteger rewards;
    private long firstTxBlock;

    AccountRecord(ResultSet rs) throws SQLException {
        this.address = ByteUtil.hexStringToBytes(rs.getString("address"));
        this.title = rs.getString("title");
        this.balance = BIUtil.toBI(ByteUtil.hexStringToBytes(rs.getString("balance")));
        this.mask = rs.getString("mask");
        this.rewards = BIUtil.toBI(ByteUtil.hexStringToBytes(rs.getString("rewards")));
        this.firstTxBlock = rs.getLong("first_tx_block_number");
    }

    public BigInteger getBalance() {
        return balance;
    }

    public BigInteger getRewards() {
        return rewards;
    }

    public byte[] getAddress() {
        return address;
    }

    public long getFirstTxBlock() {
        return firstTxBlock;
    }

    public String getMask() {
        return mask;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "AccountRecord{" +
                "address=" + Arrays.toString(address) +
                ", title='" + title + '\'' +
                ", balance=" + balance +
                ", mask='" + mask + '\'' +
                ", rewards=" + rewards +
                ", firstTxBlock=" + firstTxBlock +
                '}';
    }
}
