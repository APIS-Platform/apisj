package org.apis.db.sql;

import org.apis.util.ByteUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class MyAddressRecord {
    private byte[] address;
    private String alias;
    private int exist;

    public MyAddressRecord(){}
    public MyAddressRecord(ResultSet rs) throws SQLException {
        this.address = ByteUtil.hexStringToBytes(rs.getString("address"));
        this.alias = rs.getString("alias");
        this.exist = rs.getInt("exist");
    }

    public byte[] getAddress() {
        return address;
    }

    public String getAlias() {
        return alias;
    }

    public int getExist() { return exist; }

    @Override
    public String toString() {
        return "MyAddressRecord{" +
                "address=" + Arrays.toString(address) +
                ", alias=" + alias +
                ", exist=" + exist +
                '}';
    }
}

