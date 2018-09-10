package org.apis.db.sql;

import org.apis.core.CallTransaction;
import org.apis.util.ByteUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class AbiRecord {
    private String creator;
    private String contract_address;
    private String contract_name;
    private String abi;
    private long created_at;

    AbiRecord(ResultSet rs) throws SQLException {
        this.creator = rs.getString("creator");
        this.contract_address = rs.getString("contract_address");
        this.contract_name = rs.getString("contract_name");
        this.abi = rs.getString("abi");
        this.created_at = rs.getLong("created_at");
    }

    public String getCreator() {
        return creator;
    }

    public String getContractAddress() {
        return contract_address;
    }

    public String getContractName(){
        return contract_name;
    }

    public String getAbi() {
        return abi;
    }

    public long getCreatedAt() {
        return created_at;
    }

    public CallTransaction.Contract getContract() {
        if(abi == null || abi.isEmpty()) {
            return null;
        }

        return new CallTransaction.Contract(abi);
    }

    @Override
    public String toString() {
        return "ContractRecord{" +
                "creator=" + creator +
                "contract_address=" + contract_address +
                "contract_name=" + contract_name +
                ", abi='" + abi + '\'' +
                ", created_at='" + created_at + '\'' +
                '}';
    }
}
