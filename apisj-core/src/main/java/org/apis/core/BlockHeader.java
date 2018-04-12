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

import org.apis.config.BlockchainNetConfig;
import org.apis.crypto.HashUtil;
import org.apis.util.*;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;

import static org.apis.crypto.HashUtil.EMPTY_TRIE_HASH;
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

    /* A scalar value corresponding to the difficulty level of this block.
     * This can be calculated from the previous block’s difficulty level
     * and the timestamp */
    private byte[] difficulty;

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
        this.difficulty = rlpHeader.get(6).getRLPData();

        byte[] nrBytes = rlpHeader.get(7).getRLPData();
        byte[] glBytes = rlpHeader.get(8).getRLPData();
        byte[] guBytes = rlpHeader.get(9).getRLPData();
        this.mineralUsed = ByteUtil.bytesToBigInteger(rlpHeader.get(10).getRLPData());
        byte[] tsBytes = rlpHeader.get(11).getRLPData();

        this.number = ByteUtil.byteArrayToLong(nrBytes);

        this.gasLimit = glBytes;
        this.gasUsed = ByteUtil.byteArrayToLong(guBytes);
        this.timestamp = ByteUtil.byteArrayToLong(tsBytes);

        this.extraData = rlpHeader.get(12).getRLPData();
        this.mixHash = rlpHeader.get(13).getRLPData();
        this.nonce = rlpHeader.get(14).getRLPData();
    }

    public BlockHeader(byte[] parentHash, byte[] coinbase,
                       byte[] logsBloom, byte[] difficulty, long number,
                       byte[] gasLimit, long gasUsed, BigInteger mineralUsed, long timestamp,
                       byte[] extraData, byte[] mixHash, byte[] nonce) {
        this.parentHash = parentHash;
        this.coinbase = coinbase;
        this.logsBloom = logsBloom;
        this.difficulty = difficulty;
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

    public byte[] getDifficulty() {
        return difficulty;
    }

    public BigInteger getDifficultyBI() {
        return new BigInteger(1, difficulty);
    }


    public void setDifficulty(byte[] difficulty) {
        this.difficulty = difficulty;
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

    public byte[] getEncoded(boolean withNonce) {
        byte[] parentHash = RLP.encodeElement(this.parentHash);

        byte[] coinbase = RLP.encodeElement(this.coinbase);

        byte[] stateRoot = RLP.encodeElement(this.stateRoot);

        if (txTrieRoot == null) this.txTrieRoot = EMPTY_TRIE_HASH;
        byte[] txTrieRoot = RLP.encodeElement(this.txTrieRoot);

        if (receiptTrieRoot == null) this.receiptTrieRoot = EMPTY_TRIE_HASH;
        byte[] receiptTrieRoot = RLP.encodeElement(this.receiptTrieRoot);

        byte[] logsBloom = RLP.encodeElement(this.logsBloom);
        byte[] difficulty = RLP.encodeBigInteger(new BigInteger(1, this.difficulty));
        byte[] number = RLP.encodeBigInteger(BigInteger.valueOf(this.number));
        byte[] gasLimit = RLP.encodeElement(this.gasLimit);
        byte[] gasUsed = RLP.encodeBigInteger(BigInteger.valueOf(this.gasUsed));
        byte[] mineralUsed = RLP.encodeBigInteger(this.mineralUsed);
        byte[] timestamp = RLP.encodeBigInteger(BigInteger.valueOf(this.timestamp));

        byte[] extraData = RLP.encodeElement(this.extraData);
        if (withNonce) {
            byte[] mixHash = RLP.encodeElement(this.mixHash);
            byte[] nonce = RLP.encodeElement(this.nonce);
            return RLP.encodeList(parentHash, coinbase,
                    stateRoot, txTrieRoot, receiptTrieRoot, logsBloom, difficulty, number,
                    gasLimit, gasUsed, mineralUsed, timestamp, extraData, mixHash, nonce);
        } else {
            return RLP.encodeList(parentHash, coinbase,
                    stateRoot, txTrieRoot, receiptTrieRoot, logsBloom, difficulty, number,
                    gasLimit, gasUsed, mineralUsed, timestamp, extraData);
        }
    }

    public byte[] getPowBoundary() {
        return BigIntegers.asUnsignedByteArray(32, BigInteger.ONE.shiftLeft(256).divide(getDifficultyBI()));
    }

    public byte[] calcPowValue() {

        // nonce bytes are expected in Little Endian order, reverting
        byte[] nonceReverted = Arrays.reverse(nonce);
        byte[] hashWithoutNonce = HashUtil.sha3(getEncodedWithoutNonce());

        byte[] seed = Arrays.concatenate(hashWithoutNonce, nonceReverted);
        byte[] seedHash = HashUtil.sha512(seed);

        byte[] concat = Arrays.concatenate(seedHash, mixHash);
        return HashUtil.sha3(concat);
    }

    public BigInteger calcDifficulty(BlockchainNetConfig config, BlockHeader parent) {
        return config.getConfigForBlock(getNumber()).calcDifficulty(this, parent);
    }


    public String toString() {
        return toStringWithSuffix("\n");
    }

    private String toStringWithSuffix(final String suffix) {
        //        toStringBuff.append("  gasLimit=").append(gasLimit).append(suffix);
        return "  hash=" + toHexString(getHash()) + suffix +
                "  parentHash=" + toHexString(parentHash) + suffix +
                "  coinbase=" + toHexString(coinbase) + suffix +
                "  stateRoot=" + toHexString(stateRoot) + suffix +
                "  txTrieHash=" + toHexString(txTrieRoot) + suffix +
                "  receiptsTrieHash=" + toHexString(receiptTrieRoot) + suffix +
                "  difficulty=" + toHexString(difficulty) + suffix +
                "  number=" + number + suffix +
                "  gasLimit=" + toHexString(gasLimit) + suffix +
                "  gasUsed=" + gasUsed + suffix +
                "  mineralUsed=" + mineralUsed + suffix +
                "  timestamp=" + timestamp + " (" + Utils.longToDateTime(timestamp) + ")" + suffix +
                "  extraData=" + toHexString(extraData) + suffix +
                "  mixHash=" + toHexString(mixHash) + suffix +
                "  nonce=" + toHexString(nonce) + suffix;
    }

    public String toFlatString() {
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
