package org.apis.db.sql;

import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class Contract {
    byte[] address;
    String title;
    String mask;
    String abi;
    String canvas_url;
    long firstTxBlock;

    public Contract(ResultSet rs) throws SQLException {
        this.address = ByteUtil.hexStringToBytes(rs.getString("address"));
        this.title = rs.getString("title");
        this.mask = rs.getString("mask");
        this.abi = rs.getString("abi");
        this.canvas_url = rs.getString("canvas_url");
        this.firstTxBlock = rs.getLong("first_tx_block_number");
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

    public String getCanvas_url() {
        return canvas_url;
    }

    @Override
    public String toString() {
        return "Contract{" +
                "address=" + Arrays.toString(address) +
                ", title='" + title + '\'' +
                ", mask='" + mask + '\'' +
                ", abi='" + abi + '\'' +
                ", canvas_url='" + canvas_url + '\'' +
                ", firstTxBlock=" + firstTxBlock +
                '}';
    }
}
