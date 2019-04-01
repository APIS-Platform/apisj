package org.apis.db.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AddressGroupRecord {
    private String groupName;

    public AddressGroupRecord(){}
    public AddressGroupRecord(ResultSet rs) throws SQLException {
        this.groupName = rs.getString("group_name");
    }
    public String getGroupName() {
        return groupName;
    }

    @Override
    public String toString() {
        return "AddressGroupRecord{" +
                ", groupName=" + groupName +
                '}';
    }
}
