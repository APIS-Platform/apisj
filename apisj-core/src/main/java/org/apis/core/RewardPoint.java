package org.apis.core;

import org.apache.commons.codec.binary.Hex;
import org.apis.util.*;

import java.math.BigInteger;

import static org.apis.util.ByteUtil.EMPTY_BYTE_ARRAY;

public class RewardPoint {
    private long parentBlockNumber;
    private byte[] parentBlockHash;
    private BigInteger RP;
    private byte[] coinbase;
    private byte[] seed;
    private BigInteger balance;

    /* Rp in encoded form */
    protected byte[] rlpEncoded;
    private byte[] rlpRaw;
    /* Indicates if this transaction has been parsed
     * from the RLP-encoded data */
    protected boolean parsed = false;


    public RewardPoint(byte[] rawData) {
        this.rlpEncoded = rawData;
        parsed = false;
    }

    public RewardPoint(byte[] parentBlockHash, long parentBlockNumber, byte[] coinbase, byte[] seed, BigInteger balance, BigInteger RP) {
        this.parentBlockNumber = parentBlockNumber;
        this.parentBlockHash = parentBlockHash;
        this.coinbase = coinbase;
        this.seed = seed;
        this.balance = balance;
        this.RP = RP;

        parsed = true;
    }

    public long getParentBlockNumber() {
        rlpParse();
        return parentBlockNumber;
    }

    public byte[] getParentBlockHash() {
        rlpParse();
        return parentBlockHash;
    }

    public BigInteger getRP() {
        rlpParse();
        return RP;
    }

    public byte[] getCoinbase() {
        rlpParse();
        return coinbase;
    }

    public byte[] getSeed() {
        rlpParse();
        return seed;
    }

    public BigInteger getBalanceBI() {
        rlpParse();
        return balance;
    }

    public byte[] getBalance() {
        rlpParse();
        return ByteUtil.bigIntegerToBytes(balance);
    }

    public byte[] getEncoded() {

        rlpParse();
        if (rlpRaw != null) return rlpRaw;

        byte[] parentBlockHash = RLP.encodeElement(this.parentBlockHash);
        byte[] parentBlockNumber = RLP.encodeBigInteger(BigInteger.valueOf(this.parentBlockNumber));
        byte[] coinbase = RLP.encodeElement(this.coinbase);
        byte[] seed = RLP.encodeElement(this.seed);
        byte[] balance = RLP.encodeBigInteger(this.balance);
        byte[] rp = RLP.encodeBigInteger(this.RP);

        rlpRaw = RLP.encodeList(parentBlockHash, parentBlockNumber, coinbase, seed, balance, rp);

        return rlpRaw;
    }

    public synchronized void rlpParse() {
        if(parsed) return;

        try {
            RLPList decodedRpList = RLP.decode2(rlpEncoded);
            RLPList rewardPoint = (RLPList) decodedRpList.get(0);

            if(rewardPoint.size() > 6) {
                throw new RuntimeException("Too many RLP elements");
            }
            for (RLPElement rlpElement : rewardPoint) {
                if (!(rlpElement instanceof RLPItem))
                    throw new RuntimeException("RewardPoint RLP elements shouldn't be lists");
            }

            this.parentBlockHash = rewardPoint.get(0).getRLPData();
            this.parentBlockNumber = ByteUtil.bytesToBigInteger(rewardPoint.get(1).getRLPData()).longValue();
            this.coinbase = rewardPoint.get(2).getRLPData();
            this.seed = rewardPoint.get(3).getRLPData();
            this.balance = ByteUtil.bytesToBigInteger(rewardPoint.get(4).getRLPData());
            this.RP = ByteUtil.bytesToBigInteger(rewardPoint.get(5).getRLPData());

            this.parsed = true;
        } catch (Exception e) {
            throw new RuntimeException("Error on parsing RLP", e);
        }
    }


    public String toString() {
        rlpParse();
        String coinbaseHex = Hex.encodeHexString(coinbase);

        if(coinbaseHex.equals("")) {
            return "REWARD POINT [ NULL ]";
        }

        return "[" + parentBlockNumber + "th] Block(" + Hex.encodeHexString(parentBlockHash).substring(0, 4) + ") " +
                " Coinbase: " + AddressUtil.getShortAddress(coinbase) +
                " RP: " + RP.toString(10);
    }
}