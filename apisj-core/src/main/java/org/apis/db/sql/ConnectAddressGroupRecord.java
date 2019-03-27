package org.apis.db.sql;

import org.apis.util.ByteUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class ConnectAddressGroupRecord {
    private byte[] address;
    private String groupName;

    public ConnectAddressGroupRecord() {}
    public ConnectAddressGroupRecord(ResultSet rs) throws SQLException {
        this.address = ByteUtil.hexStringToBytes(rs.getString("address"));
        this.groupName = rs.getString("group_name");
    }

    public byte[] getAddress() {
        return address;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public String toString() {
        return "ConnectAddressGroupRecord{" +
                "address=" + Arrays.toString(address) +
                ", groupName=" + groupName +
                '}';
    }
}
