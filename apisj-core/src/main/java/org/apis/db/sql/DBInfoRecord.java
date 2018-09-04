package org.apis.db.sql;

import org.apis.core.CallTransaction;
import org.apis.util.ByteUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class DBInfoRecord {
    private long version;
    private long lastSyncedBlock;

    public DBInfoRecord(ResultSet rs) throws SQLException {
        this.version = rs.getLong("version");
        this.lastSyncedBlock = rs.getLong("last_synced_block");
    }

    public long getVersion() {
        return version;
    }

    public long getLastSyncedBlock() {
        return lastSyncedBlock;
    }

    @Override
    public String toString() {
        return "DBInfoRecord{" +
                "version=" + version +
                ", lastSyncedBlock=" + lastSyncedBlock +
                '}';
    }
}
