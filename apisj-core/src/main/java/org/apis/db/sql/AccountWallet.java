package org.apis.db.sql;

import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountWallet {
    byte[] address;
    String title;
    BigInteger balance;
    String mask;
    BigInteger rewards;
    long firstTxBlock;

    public AccountWallet(byte[] address, String title, String balance, String mask, String rewards, long firstTxBlock) {
        this.address = address;
        this.title = title;
        this.balance = BIUtil.toBI(ByteUtil.hexStringToBytes(balance));
        this.mask = mask;
        this.rewards = BIUtil.toBI(ByteUtil.hexStringToBytes(rewards));
        this.firstTxBlock = firstTxBlock;
    }

    public AccountWallet(ResultSet rs) throws SQLException {
        this.address = ByteUtil.hexStringToBytes(rs.getString("address"));
        this.title = rs.getString("title");
        this.balance = BIUtil.toBI(ByteUtil.hexStringToBytes(rs.getString("balance")));
        this.title = rs.getString("mask");
        this.rewards = BIUtil.toBI(ByteUtil.hexStringToBytes(rs.getString("balance")));
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
}
