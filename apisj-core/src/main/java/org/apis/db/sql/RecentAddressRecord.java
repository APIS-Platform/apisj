package org.apis.db.sql;

import org.apis.util.ByteUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class RecentAddressRecord {
    private byte[] txHash;
    private byte[] address;
    private String alias;
    private long createdAt;

    public RecentAddressRecord() {}
    public RecentAddressRecord(ResultSet rs) throws SQLException {
        this.txHash = ByteUtil.hexStringToBytes(rs.getString("tx_hash"));
        this.address = ByteUtil.hexStringToBytes(rs.getString("address"));
        this.alias = rs.getString("alias");
        this.createdAt = rs.getLong("created_at");
    }

    public byte[] getTxHash() {
        return txHash;
    }

    public byte[] getAddress() {
        return address;
    }

    public String getAlias() {
        return alias;
    }

    public long getCreatedAt(){
        return  createdAt;
    }

    @Override
    public String toString() {
        return "MyAddressRecord{" +
                "txHash=" + Arrays.toString(txHash) +
                ", address=" + Arrays.toString(address) +
                ", alias=" + alias +
                ", createdAt=" + createdAt +
                '}';
    }
}
