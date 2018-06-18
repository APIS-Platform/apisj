/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 *
 * Original Code
 * https://github.com/ethereumj/ethereumj/blob/master/ethereumj-core/src/main/java/org/ethereum/core/BlockHeader.java
 * modified by APIS
 */
package org.apis.core;

import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;
import org.apis.util.*;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.apis.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.apis.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.apis.util.ByteUtil.toHexString;

/**
 * Block header is a value object containing
 * the basic information of a block
 */
public class BlockHeader {

    public static final int NONCE_LENGTH = 8;
    public static final int HASH_LENGTH = 32;
    public static final int ADDRESS_LENGTH = 20;
    public static final int MAX_HEADER_SIZE = 800;

    /* The SHA3 256-bit hash of the parent block, in its entirety */
    private byte[] parentHash;

    /* The 160-bit address to which all fees collected from the
     * successful mining of this block be transferred; formally
     *
     * 이 블록을 성공적으로 채굴하여 얻은 모든 수수료를 전달하는 160-bit 주소
     * */
    private byte[] coinbase;

    /* The SHA3 256-bit hash of the root node of the state trie,
     * after all transactions are executed and finalisations applied */
    private byte[] stateRoot;

    /* The SHA3 256-bit hash of the root node of the trie structure
     * populated with each transaction in the transaction
     * list portion, the trie is populate by [key, val] --> [rlp(index), rlp(tx_recipe)]
     * of the block */
    private byte[] txTrieRoot;

    /* The SHA3 256-bit hash of the root node of the trie structure
     * populated with each transaction recipe in the transaction recipes
     * list portion, the trie is populate by [key, val] --> [rlp(index), rlp(tx_recipe)]
     * of the block */
    private byte[] receiptTrieRoot;

    /* The Bloom filter composed from indexable information 
     * (logger address and log topics) contained in each log entry 
     * from the receipt of each transaction in the transactions list */
    private byte[] logsBloom;

    /* A scalar value corresponding to the reward point of this block.
     * This can be calculated from the previous block’s hash
     * and miner's address and balance */
    private BigInteger rewardPoint;

    /**
     * 모든 직계 조상들과 자신의 RP 값의 총 합
     */
    private BigInteger cumulativeRewardPoint;

    /* A scalar value equal to the reasonable output of Unix's time()
     * at this block's inception */
    private long timestamp;

    /* A scalar value equal to the number of ancestor blocks.
     * The genesis block has a number of zero
     *
     * 조상 블록들의 수와 같은 스칼라 값
     * genesis 블록은 0을 갖는다
     * */
    private long number;

    /* A scalar value equal to the current limit of gas expenditure per block
     *
     * 현재 블록당 가스 소비의 한도와 동일한 스칼라 값 */
    private byte[] gasLimit;

    /* A scalar value equal to the total gas used in transactions in this block
     *
     * 이 블록의 트랜잭션에서 사용한 총 가스와 동일한 스칼라 값  */
    private long gasUsed;

    /**
     * 이 블록에서 사용된 총 미네랄 값
     */
    private BigInteger mineralUsed;

    private byte[] mixHash;

    /* An arbitrary byte array containing data relevant to this block.
     * With the exception of the genesis block, this must be 32 bytes or fewer */
    private byte[] extraData;
    /* A 256-bit hash which proves that a sufficient amount
     * of computation has been carried out on this block */
    private byte[] nonce;

    private byte[] hashCache;


    /**
     * Since EIP-155, we could encode chainId in V
     */
    private static final int CHAIN_ID_INC = 35;
    private static final int LOWER_REAL_V = 27;

    /* the elliptic curve signature
     * (including public key recovery bits) */
    private ECKey.ECDSASignature signature;


    public BlockHeader(byte[] encoded) {
        this((RLPList) RLP.decode2(encoded).get(0));
    }

    public BlockHeader(RLPList rlpHeader) {

        this.parentHash = rlpHeader.get(0).getRLPData();
        this.coinbase = rlpHeader.get(1).getRLPData();
        this.stateRoot = rlpHeader.get(2).getRLPData();

        this.txTrieRoot = rlpHeader.get(3).getRLPData();
        if (this.txTrieRoot == null)
            this.txTrieRoot = EMPTY_TRIE_HASH;

        this.receiptTrieRoot = rlpHeader.get(4).getRLPData();
        if (this.receiptTrieRoot == null)
            this.receiptTrieRoot = EMPTY_TRIE_HASH;

        this.logsBloom = rlpHeader.get(5).getRLPData();
        this.rewardPoint = ByteUtil.bytesToBigInteger(rlpHeader.get(6).getRLPData());
        this.cumulativeRewardPoint = ByteUtil.bytesToBigInteger(rlpHeader.get(7).getRLPData());
        byte[] nrBytes = rlpHeader.get(8).getRLPData();
        byte[] glBytes = rlpHeader.get(9).getRLPData();
        byte[] guBytes = rlpHeader.get(10).getRLPData();
        this.mineralUsed = ByteUtil.bytesToBigInteger(rlpHeader.get(11).getRLPData());
        byte[] tsBytes = rlpHeader.get(12).getRLPData();

        this.number = ByteUtil.byteArrayToLong(nrBytes);

        this.gasLimit = glBytes;
        this.gasUsed = ByteUtil.byteArrayToLong(guBytes);
        this.timestamp = ByteUtil.byteArrayToLong(tsBytes);

        this.extraData = rlpHeader.get(13).getRLPData();
        this.mixHash = rlpHeader.get(14).getRLPData();
        this.nonce = rlpHeader.get(15).getRLPData();


        byte[] vData =  rlpHeader.get(16).getRLPData();
        BigInteger v = ByteUtil.bytesToBigInteger(vData);
        byte[] r = rlpHeader.get(17).getRLPData();
        byte[] s = rlpHeader.get(18).getRLPData();
        this.signature = ECKey.ECDSASignature.fromComponents(r, s, getRealV(v));
    }

    public BlockHeader(byte[] parentHash, byte[] coinbase,
                       byte[] logsBloom, BigInteger rewardPoint, BigInteger cumulativeRewardPoint, long number,
                       byte[] gasLimit, long gasUsed, BigInteger mineralUsed, long timestamp,
                       byte[] extraData, byte[] mixHash, byte[] nonce) {
        this.parentHash = parentHash;
        this.coinbase = coinbase;
        this.logsBloom = logsBloom;
        this.rewardPoint = rewardPoint;
        this.cumulativeRewardPoint = cumulativeRewardPoint;
        this.number = number;
        this.gasLimit = gasLimit;
        this.gasUsed = gasUsed;
        this.mineralUsed = mineralUsed;
        this.timestamp = timestamp;
        this.extraData = extraData;
        this.mixHash = mixHash;
        this.nonce = nonce;
        this.stateRoot = EMPTY_TRIE_HASH;
    }

    public boolean isGenesis() {
        return this.getNumber() == Genesis.NUMBER;
    }

    public byte[] getParentHash() {
        return parentHash;
    }

    public byte[] getCoinbase() {
        return coinbase;
    }

    public void setCoinbase(byte[] coinbase) {
        this.coinbase = coinbase;
        hashCache = null;
    }

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(byte[] stateRoot) {
        this.stateRoot = stateRoot;
        hashCache = null;
    }

    public byte[] getTxTrieRoot() {
        return txTrieRoot;
    }

    public void setReceiptsRoot(byte[] receiptTrieRoot) {
        this.receiptTrieRoot = receiptTrieRoot;
        hashCache = null;
    }

    public byte[] getReceiptsRoot() {
        return receiptTrieRoot;
    }

    public void setTransactionsRoot(byte[] stateRoot) {
        this.txTrieRoot = stateRoot;
        hashCache = null;
    }


    public byte[] getLogsBloom() {
        return logsBloom;
    }

    public BigInteger getRewardPoint() {
        return rewardPoint;
    }

    public void setRewardPoint(BigInteger rewardPoint) {
        this.rewardPoint = rewardPoint;
        hashCache = null;
    }

    public BigInteger getCumulativeRewardPoint() {
        return cumulativeRewardPoint;
    }

    public void setCumulativeRewardPoint(BigInteger cumulativeRewardPoint) {
        this.cumulativeRewardPoint = cumulativeRewardPoint;
        hashCache = null;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        hashCache = null;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
        hashCache = null;
    }

    public byte[] getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(byte[] gasLimit) {
        this.gasLimit = gasLimit;
        hashCache = null;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = gasUsed;
        hashCache = null;
    }

    public BigInteger getMineralUsed() {
        return mineralUsed;
    }

    public void setMineralUsed(BigInteger mineralUsed) {
        this.mineralUsed = mineralUsed;
        hashCache = null;
    }

    public byte[] getMixHash() {
        return mixHash;
    }

    public void setMixHash(byte[] mixHash) {
        this.mixHash = mixHash;
        hashCache = null;
    }

    public byte[] getExtraData() {
        return extraData;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
        hashCache = null;
    }

    public void setLogsBloom(byte[] logsBloom) {
        this.logsBloom = logsBloom;
        hashCache = null;
    }

    public void setExtraData(byte[] extraData) {
        this.extraData = extraData;
        hashCache = null;
    }

    public byte[] getHash() {
        if (hashCache == null) {
            hashCache = HashUtil.sha3(getEncoded());
        }
        return hashCache;
    }

    public byte[] getEncoded() {
        return this.getEncoded(true); // with nonce
    }

    public byte[] getEncodedWithoutNonce() {
        return this.getEncoded(false);
    }

    public void sign(ECKey key) throws ECKey.MissingPrivateKeyException {
        this.signature = key.sign(getRawHash());
    }

    public ECKey.ECDSASignature getSignature() {
        return signature;
    }

    private Integer extractChainIdFromV(BigInteger bv) {
        if (bv.bitLength() > 31) return Integer.MAX_VALUE; // chainId is limited to 31 bits, longer are not valid for now
        long v = bv.longValue();
        if (v == LOWER_REAL_V || v == (LOWER_REAL_V + 1)) return null;
        return (int) ((v - CHAIN_ID_INC) / 2);
    }

    private byte getRealV(BigInteger bv) {
        if (bv.bitLength() > 31) return 0; // chainId is limited to 31 bits, longer are not valid for now
        long v = bv.longValue();
        if (v == LOWER_REAL_V || v == (LOWER_REAL_V + 1)) return (byte) v;
        int inc = 0;
        if ((int) v % 2 == 0) inc = 1;
        return (byte) ((byte) LOWER_REAL_V + inc);
    }

    public byte[] getRawHash() {
        byte[] plainMsg = this.getEncodedRaw();
        return HashUtil.sha3(plainMsg);
    }

    public byte[] getEncodedRaw() {

        byte[] parentHash = RLP.encodeElement(this.parentHash);
        byte[] coinbase = RLP.encodeElement(this.coinbase);
        byte[] stateRoot = RLP.encodeElement(this.stateRoot);

        if (txTrieRoot == null) this.txTrieRoot = EMPTY_TRIE_HASH;
        byte[] txTrieRoot = RLP.encodeElement(this.txTrieRoot);

        if (receiptTrieRoot == null) this.receiptTrieRoot = EMPTY_TRIE_HASH;
        byte[] receiptTrieRoot = RLP.encodeElement(this.receiptTrieRoot);

        byte[] logsBloom = RLP.encodeElement(this.logsBloom);
        byte[] rewardPoint = RLP.encodeBigInteger(this.rewardPoint);

        return RLP.encodeList(parentHash, coinbase, stateRoot, txTrieRoot, receiptTrieRoot, logsBloom, rewardPoint);

    }

    public byte[] getEncoded(boolean withNonce) {
        byte[] parentHash = RLP.encodeElement(this.parentHash);

        byte[] coinbase = RLP.encodeElement(this.coinbase);

        byte[] stateRoot = RLP.encodeElement(this.stateRoot);

        if (txTrieRoot == null) this.txTrieRoot = EMPTY_TRIE_HASH;
        byte[] txTrieRoot = RLP.encodeElement(this.txTrieRoot);

        if (receiptTrieRoot == null) this.receiptTrieRoot = EMPTY_TRIE_HASH;
        byte[] receiptTrieRoot = RLP.encodeElement(this.receiptTrieRoot);

        byte[] logsBloom = RLP.encodeElement(this.logsBloom);
        byte[] rewardPoint = RLP.encodeBigInteger(this.rewardPoint);
        byte[] cumulativeRewardPoint = RLP.encodeBigInteger(this.cumulativeRewardPoint);
        byte[] number = RLP.encodeBigInteger(BigInteger.valueOf(this.number));
        byte[] gasLimit = RLP.encodeElement(this.gasLimit);
        byte[] gasUsed = RLP.encodeBigInteger(BigInteger.valueOf(this.gasUsed));
        byte[] mineralUsed = RLP.encodeBigInteger(this.mineralUsed);
        byte[] timestamp = RLP.encodeBigInteger(BigInteger.valueOf(this.timestamp));
        byte[] extraData = RLP.encodeElement(this.extraData);

        byte[] v, r, s;

        if (signature != null) {
            v = RLP.encodeInt(signature.v);
            r = RLP.encodeElement(BigIntegers.asUnsignedByteArray(signature.r));
            s = RLP.encodeElement(BigIntegers.asUnsignedByteArray(signature.s));
        } else {
            v = RLP.encodeElement(EMPTY_BYTE_ARRAY);
            r = RLP.encodeElement(EMPTY_BYTE_ARRAY);
            s = RLP.encodeElement(EMPTY_BYTE_ARRAY);
        }


        if (withNonce) {
            byte[] mixHash = RLP.encodeElement(this.mixHash);
            byte[] nonce = RLP.encodeElement(this.nonce);
            return RLP.encodeList(parentHash, coinbase,
                    stateRoot, txTrieRoot, receiptTrieRoot, logsBloom, rewardPoint, cumulativeRewardPoint, number,
                    gasLimit, gasUsed, mineralUsed, timestamp, extraData, mixHash, nonce, v, r, s);
        } else {
            return RLP.encodeList(parentHash, coinbase,
                    stateRoot, txTrieRoot, receiptTrieRoot, logsBloom, rewardPoint, cumulativeRewardPoint, number,
                    gasLimit, gasUsed, mineralUsed, timestamp, extraData, v, r, s);
        }
    }



    public String toString() {
        return toStringWithSuffix("\n");
    }

    private String toStringWithSuffix(final String suffix) {
        return "  hash=" + toHexString(getHash()) + suffix +
                "  parentHash=" + toHexString(parentHash) + suffix +
                "  coinbase=" + toHexString(coinbase) + suffix +
                "  stateRoot=" + toHexString(stateRoot) + suffix +
                "  txTrieHash=" + toHexString(txTrieRoot) + suffix +
                "  receiptsTrieHash=" + toHexString(receiptTrieRoot) + suffix +
                "  rewardPoint=" + (rewardPoint.toString(10)) + suffix +
                "  cumulativeRP=" + (cumulativeRewardPoint.toString(10)) + suffix +
                "  number=" + number + suffix +
                "  gasLimit=" + toHexString(gasLimit) + suffix +
                "  gasUsed=" + gasUsed + suffix +
                "  mineralUsed=" + mineralUsed + suffix +
                "  timestamp=" + timestamp + " (" + Utils.longToDateTime(timestamp) + ")" + suffix +
                "  extraData=" + toHexString(extraData) + suffix +
                "  mixHash=" + toHexString(mixHash) + suffix +
                "  nonce=" + toHexString(nonce) + suffix +
                "  signatureV=" + (signature == null ? "" : signature.v) + suffix +
                "  signatureR=" + (signature == null ? "" : ByteUtil.toHexString(BigIntegers.asUnsignedByteArray(signature.r))) + suffix +
                "  signatureS=" + (signature == null ? "" : ByteUtil.toHexString(BigIntegers.asUnsignedByteArray(signature.s))) + suffix;
    }

    String toFlatString() {
        return toStringWithSuffix("");
    }

    public String getShortDescr() {
        return "#" + getNumber() + " (" + Hex.toHexString(getHash()).substring(0,6) + " <~ "
                + Hex.toHexString(getParentHash()).substring(0,6) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockHeader that = (BlockHeader) o;
        return FastByteComparisons.equal(getHash(), that.getHash());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getHash());
    }
}
