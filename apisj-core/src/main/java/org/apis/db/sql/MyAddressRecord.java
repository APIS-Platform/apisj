package org.apis.db.sql;

import org.apis.util.ByteUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class MyAddressRecord {
    private byte[] address;
    private String alias;

    MyAddressRecord(ResultSet rs) throws SQLException {
        this.address = ByteUtil.hexStringToBytes(rs.getString("address"));
        this.alias = rs.getString("alias");
    }

    public byte[] getAddress() {
        return address;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return "MyAddressRecord{" +
                "address=" + Arrays.toString(address) +
                ", alias=" + alias +
                '}';
    }
}

