package org.apis.db.sql;

import org.apis.core.CallTransaction;
import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class ContractRecord {
    private byte[] address;
    private String title;
    private String mask;
    private String abi;
    private String canvas_url;
    private long firstTxBlock;
    private long lastSyncedBlock;

    public ContractRecord() {}
    public ContractRecord(ResultSet rs) throws SQLException {
        this.address = ByteUtil.hexStringToBytes(rs.getString("address"));
        this.title = rs.getString("title");
        this.mask = rs.getString("mask");
        this.abi = rs.getString("abi");
        this.canvas_url = rs.getString("canvas_url");
        this.firstTxBlock = rs.getLong("first_tx_block_number");
        this.lastSyncedBlock = rs.getLong("last_synced_block");
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

    public String getAbi() {
        return abi;
    }

    public CallTransaction.Contract getContract() {
        if(abi == null || abi.isEmpty()) {
            return null;
        }

        return new CallTransaction.Contract(abi);
    }

    public String getCanvas_url() {
        return canvas_url;
    }

    public long getLastSyncedBlock() {
        return lastSyncedBlock;
    }

    @Override
    public String toString() {
        return "ContractRecord{" +
                "address=" + Arrays.toString(address) +
                ", title='" + title + '\'' +
                ", mask='" + mask + '\'' +
                ", abi='" + abi + '\'' +
                ", canvas_url='" + canvas_url + '\'' +
                ", firstTxBlock=" + firstTxBlock +
                ", lastSyncedBlock=" + lastSyncedBlock +
                '}';
    }
}
