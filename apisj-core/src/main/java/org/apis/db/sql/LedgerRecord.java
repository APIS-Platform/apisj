package org.apis.db.sql;

import org.apis.util.ByteUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class LedgerRecord {
    private byte[] address;
    private String path;
    private String alias;

    public LedgerRecord(){}
    public LedgerRecord(ResultSet rs) throws SQLException {
        this.address = ByteUtil.hexStringToBytes(rs.getString("address"));
        this.path = rs.getString("path");
        this.alias = rs.getString("alias");
    }

    public byte[] getAddress() {
        return address;
    }

    public String getPath() {
        return path;
    }

    public String getAlias() {
        return alias;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return "LedgerRecord{" +
               "address=" + Arrays.toString(address) +
               ", path=" + path +
               ", alias=" + alias +
               '}';
    }
}
