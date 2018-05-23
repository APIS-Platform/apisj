package org.apis.core;

import org.apache.commons.codec.binary.Hex;
import org.apis.util.*;

import java.math.BigInteger;
import java.util.Arrays;

public class MinerState {
    private byte protocolVersion;
    protected int networkId;

    /** 채굴자의 노드 ID */
    private byte[] nodeId;

    /** 채굴자가 마지막에 네트워크에 접속 여부를 전파한 시간 */
    private long lastLived;

    private byte[] coinbase;

    /* Rp in encoded form */
    protected byte[] rlpEncoded;
    private byte[] rlpRaw;
    /* Indicates if this transaction has been parsed
     * from the RLP-encoded data */
    protected boolean parsed = false;


    public MinerState(byte[] rawData) {
        this.rlpEncoded = rawData;
        parsed = false;
    }

    public MinerState(byte protocolVersion, int networkId, byte[] nodeId, long lastLived, byte[] coinbase) {
        this.protocolVersion = protocolVersion;
        this.networkId = networkId;
        this.nodeId = nodeId;
        this.lastLived = lastLived;
        this.coinbase = coinbase;

        parsed = true;
    }

    public byte getProtocolVersion() {
        rlpParse();
        return protocolVersion;
    }

    public int getNetworkId() {
        rlpParse();
        return networkId;
    }

    public byte[] getNodeId() {
        rlpParse();
        return nodeId;
    }

    public long getLastLived() {
        rlpParse();
        return lastLived;
    }

    public byte[] getCoinbase() {
        return coinbase;
    }

    public byte[] getEncoded() {
        rlpParse();
        if (rlpRaw != null) return rlpRaw;

        byte[] protocolVersionBytes = RLP.encodeByte(this.protocolVersion);
        byte[] networkIdBytes = RLP.encodeInt(this.networkId);
        byte[] nodeIdBytes = RLP.encodeElement(this.nodeId);
        byte[] lastLivedBytes = RLP.encodeBigInteger(BigInteger.valueOf(this.lastLived));
        byte[] coinbaseBytes = RLP.encodeElement(this.coinbase);

        rlpRaw = RLP.encodeList(protocolVersionBytes, networkIdBytes, nodeIdBytes, lastLivedBytes, coinbaseBytes);

        return rlpRaw;
    }

    public synchronized void rlpParse() {
        if(parsed) return;

        try {
            RLPList paramsList = (RLPList) RLP.decode2(rlpEncoded).get(0);

            this.protocolVersion = paramsList.get(0).getRLPData()[0];

            byte[] networkIdBytes = paramsList.get(1).getRLPData();
            this.networkId = networkIdBytes == null ? 0 : ByteUtil.byteArrayToInt(networkIdBytes);

            this.nodeId = paramsList.get(2).getRLPData();

            this.lastLived = ByteUtil.bytesToBigInteger(paramsList.get(3).getRLPData()).longValue();

            this.coinbase = paramsList.get(4).getRLPData();

            this.parsed = true;
        } catch (Exception e) {
            throw new RuntimeException("Error on parsing RLP", e);
        }
    }

    @Override
    public String toString() {
        return "MinerState{" +
                "protocolVersion=" + protocolVersion +
                ", networkId=" + networkId +
                ", nodeId=" + Arrays.toString(nodeId) +
                ", lastLived=" + lastLived +
                ", coinbase=" + Arrays.toString(coinbase) +
                '}';
    }
}