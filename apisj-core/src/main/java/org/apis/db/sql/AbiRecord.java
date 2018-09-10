package org.apis.db.sql;

import org.apis.core.CallTransaction;
import org.apis.util.ByteUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class AbiRecord {
    private byte[] contractAddress;
    private String contractName;
    private String abi;
    private long createdAt;
    private long uid;

    AbiRecord(ResultSet rs) throws SQLException {
        this.contractAddress = ByteUtil.hexStringToBytes(rs.getString("contract_address"));
        this.contractName = rs.getString("contract_name");
        this.abi = rs.getString("abi");
        this.createdAt = rs.getLong("created_at");
        this.uid = rs.getLong("uid");
    }

    public String getAbi() {
        return abi;
    }

    public byte[] getContractAddress() {
        return contractAddress;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUid() {
        return uid;
    }

    public String getContractName() {
        return contractName;
    }

    @Override
    public String toString() {
        return "AbiRecord{" +
                "contractAddress=" + Arrays.toString(contractAddress) +
                ", contractName='" + contractName + '\'' +
                ", abi='" + abi + '\'' +
                ", createdAt=" + createdAt +
                ", uid=" + uid +
                '}';
    }
}
