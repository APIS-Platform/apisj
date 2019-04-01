package org.apis.db.sql;

import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class TokenRecord {
    private byte[] tokenAddress;
    private String tokenName;
    private String tokenSymbol;
    private long decimal;
    private BigInteger totalSupply;

    public TokenRecord() {}
    public TokenRecord(ResultSet rs) throws SQLException {
        this.tokenAddress = ByteUtil.hexStringToBytes(rs.getString("token_address"));
        this.tokenName = rs.getString("token_name");
        this.tokenSymbol = rs.getString("token_symbol");
        this.decimal = rs.getLong("decimal");
        this.totalSupply = BIUtil.toBI(ByteUtil.hexStringToBytes(rs.getString("total_supply")));
    }

    public byte[] getTokenAddress() {
        return tokenAddress;
    }

    public long getDecimal() {
        return decimal;
    }

    public BigInteger getTotalSupply() {
        return totalSupply;
    }

    public String getTokenName() {
        return tokenName;
    }

    public String getTokenSymbol() {
        return tokenSymbol;
    }


    @Override
    public String toString() {
        return "TokenRecord{" +
                "tokenAddress=" + Arrays.toString(tokenAddress) +
                ", tokenName='" + tokenName + '\'' +
                ", tokenSymbol=" + tokenSymbol +
                ", decimal='" + decimal + '\'' +
                ", totalSupply=" + totalSupply +
                '}';
    }
}
