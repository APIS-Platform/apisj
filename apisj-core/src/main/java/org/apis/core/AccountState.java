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
 */
package org.apis.core;

import org.apis.config.BlockchainConfig;
import org.apis.config.SystemProperties;
import org.apis.crypto.HashUtil;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.RLP;
import org.apis.util.RLPList;

import org.apis.util.blockchain.ApisUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.logging.Logger;

import static org.apis.crypto.HashUtil.*;
import static org.apis.util.FastByteComparisons.equal;

public class AccountState {

    private byte[] rlpEncoded;

    /* A value equal to the number of transactions sent
     * from this address, or, in the case of contract accounts,
     * the number of contract-creations made by this account
     *
     * 이 주소에서 보내진 트랜잭션 수와 같은 숫자
     * 또는 컨트렉트 계정의 경우, 이 주소에서 생성한 컨트렉트의 갯수 */
    private final BigInteger nonce;


    /* A scalar value equal to the number of Wei owned by this address
    *
    * 이 주소가 소유한 Wei 단위의 수와 같은 스칼라 값 */
    private final BigInteger balance;


    /**
     * 트랜잭션 수수료로만 사용되는 '미네랄'
     * 단위는 atto
     */
    private final BigInteger mineral;

    /**
     * 마지막으로 트랜잭션이 기록된 블록 번호
     * 미네랄 값을 계산하는데 이용됨
     */
    private final BigInteger lastBlock;

    /* A 256-bit hash of the root node of a trie structure
     * that encodes the storage contents of the contract,
     * itself a simple mapping between byte arrays of size 32.
     * The hash is formally denoted σ[a] s .
     *
     * 컨트렉트의 저장소 내용을 인코딩하는 trie 구조의 루트 노드에 대한 256bit 해쉬
     *
     * Since I typically wish to refer not to the trie’s root hash
     * but to the underlying set of key/value pairs stored within,
     * I define a convenient equivalence TRIE (σ[a] s ) ≡ σ[a] s .
     * It shall be understood that σ[a] s is not a ‘physical’ member
     * of the account and does not contribute to its later serialisation */
    private final byte[] stateRoot;

    /* The hash of the EVM code of this contract—this is the code
     * that gets executed should this address receive a message call;
     * it is immutable and thus, unlike all other fields, cannot be changed
     * after construction. All such code fragments are contained in
     * the state database under their corresponding hashes for later
     * retrieval
     *
     * 이 계약의 EVM 코드 해쉬 - 이 주소가 메시지 호출을 수신하면 실행되는 코드이다.
     * 불변이므로 다른 모든 필드와 달리 생성 후에는 변경할 수 없다.
     * 이러한 코드 파편들은 나중에 검색할 수 있도록 해당 해쉬 아래에 상태 데이터베이스에 포함된다.
     * */
    private final byte[] codeHash;

    /**
     * gateKeeper가 설정될 경우, transaction을 승인할 때 gateKeeper 일치 여부를 확인하게 된다.
     * gateKeeper는 private key와 사용자가 직접 입력한 password로부터 파생된 또다른 private key(2nd Priv.key)로써
     * address 형태로 저장된다.
     * gateKeeper는 다음과 같이 생성된다.
     * ADDRESS(SHA(SHA(SHA(private key), SHA(address)), SHA(password)))
     */
    private final byte[] gateKeeper;

    /**
     * addressMask가 설정될 경우, transaction의 to address로 지정될 수 있다.
     * addressMask 는 사용자가 입력한 이름에 SHA 해시를 적용해서 저장된다.
     * addressMask = SHA(StringOfMask)
     */
    private final byte[] addressMask;

    public AccountState(SystemProperties config) {
        this(config.getBlockchainConfig().getCommonConstants().getInitialNonce(), BigInteger.ZERO, EMPTY_DATA_HASH);
    }

    // Genesis 블록으로 계정을 생성할 때 사용
    public AccountState(BigInteger nonce, BigInteger balance, byte[] addressMask) {
        this(nonce, balance, BigInteger.ZERO, BigInteger.ZERO, EMPTY_TRIE_HASH, EMPTY_DATA_HASH, addressMask, EMPTY_DATA_HASH);
    }

    public AccountState(BigInteger nonce, BigInteger balance, BigInteger mineral, BigInteger lastBlock, byte[] stateRoot, byte[] codeHash, byte[] addressMask, byte[] gateKeeper) {
        this.nonce = nonce;
        this.balance = balance;
        this.mineral = mineral;
        this.lastBlock = lastBlock;
        this.stateRoot = stateRoot == EMPTY_TRIE_HASH || equal(stateRoot, EMPTY_TRIE_HASH) ? EMPTY_TRIE_HASH : stateRoot;
        this.codeHash = codeHash == EMPTY_DATA_HASH || equal(codeHash, EMPTY_DATA_HASH) ? EMPTY_DATA_HASH : codeHash;
        this.addressMask = addressMask == EMPTY_DATA_HASH || equal(addressMask, EMPTY_DATA_HASH) ? EMPTY_DATA_HASH : addressMask;
        this.gateKeeper = gateKeeper == EMPTY_DATA_HASH || equal(gateKeeper, EMPTY_DATA_HASH) ? EMPTY_DATA_HASH : gateKeeper;
    }

    public AccountState(byte[] rlpData) {
        this.rlpEncoded = rlpData;

        RLPList items = (RLPList) RLP.decode2(rlpEncoded).get(0);
        this.nonce = ByteUtil.bytesToBigInteger(items.get(0).getRLPData());
        this.balance = ByteUtil.bytesToBigInteger(items.get(1).getRLPData());
        this.mineral = ByteUtil.bytesToBigInteger(items.get(2).getRLPData());
        this.lastBlock = ByteUtil.bytesToBigInteger(items.get(3).getRLPData());
        this.stateRoot = items.get(4).getRLPData();
        this.codeHash = items.get(5).getRLPData();
        this.addressMask = items.get(6).getRLPData();
        this.gateKeeper = items.get(7).getRLPData();
    }

    public BigInteger getNonce() {
        return nonce;
    }

    // Genesis 블록에서 생성
    public AccountState withNonce(BigInteger nonce) {
        return new AccountState(nonce, balance, mineral, lastBlock, stateRoot, codeHash, addressMask, gateKeeper);
    }

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public AccountState withStateRoot(byte[] stateRoot) {
        return new AccountState(nonce, balance, mineral, lastBlock, stateRoot, codeHash, addressMask, gateKeeper);
    }

    public AccountState withIncrementedNonce() {
        return new AccountState(nonce.add(BigInteger.ONE), balance, mineral, lastBlock, stateRoot, codeHash, addressMask, gateKeeper);
    }

    public AccountState withLastBlock(BigInteger lastBlock) {
        return new AccountState(nonce, balance, mineral, lastBlock, stateRoot, codeHash, addressMask, gateKeeper);
    }

    public byte[] getCodeHash() {
        return codeHash;
    }

    public AccountState withCodeHash(byte[] codeHash) {
        return new AccountState(nonce, balance, mineral, lastBlock, stateRoot, codeHash, addressMask, gateKeeper);
    }

    public byte[] getAddressMask() {
        return addressMask;
    }

    public AccountState withAddressMask(byte[] addressMask) {
        return new AccountState(nonce, balance, mineral, lastBlock, stateRoot, codeHash, addressMask, gateKeeper);
    }

    public byte[] getGateKeeper() {
        return gateKeeper;
    }

    public AccountState withGateKeeper(byte[] gateKeeper) {
        return new AccountState(nonce, balance, mineral, lastBlock, stateRoot, codeHash, addressMask, gateKeeper);
    }

    public BigInteger getBalance() {
        return balance;
    }

    public BigInteger getMineral(long blockNumber) {
        if(lastBlock.compareTo(BigInteger.valueOf(blockNumber)) > 0) {
            return BigInteger.ZERO;
        }

        BigInteger countCollected = BigInteger.valueOf(blockNumber).subtract(lastBlock);
        BigInteger collectedMineral = getCollectedMineral(countCollected);

        BigInteger totalMineral = mineral.add(collectedMineral);

        //System.out.println(String.format("BlockNumber{%d} LastBlock{%d} collectCount{%d} collectedMineral{%d} totalMineral{%d}", blockNumber, lastBlock, countCollected, collectedMineral, totalMineral));

        // 계정의 등급에 따라서 한도를 설정한다.
        BigInteger limitMineral = getLimitMineral();
        if(totalMineral.compareTo(limitMineral) >= 0) {
            return limitMineral;
        } else {
            return totalMineral;
        }
    }

    /**
     * 많은 APIS를 보유할 수록 더 빨리 미네랄을 수집할 수 있다.
     * Balance 값에 따라서 모이는 미네랄 양을 다르게 적용한다.
     * 미네랄의 가격은
     * @param countCollected
     * @return 현재 블럭까지 생성된 Mineral 양
     */
    private BigInteger getCollectedMineral(BigInteger countCollected) {
        BigInteger collectRate;

        if(balance.compareTo(ApisUtil.convert(1, ApisUtil.Unit.APIS)) < 0) {
            collectRate = BigInteger.ZERO;
        }
        else if(balance.compareTo(ApisUtil.convert(10, ApisUtil.Unit.APIS)) < 0) {
            collectRate = ApisUtil.convert(9, ApisUtil.Unit.nAPIS);
        }
        else if(balance.compareTo(ApisUtil.convert(100, ApisUtil.Unit.APIS)) < 0) {
            collectRate = ApisUtil.convert(19, ApisUtil.Unit.nAPIS);
        }
        else if(balance.compareTo(ApisUtil.convert(1_000, ApisUtil.Unit.APIS)) < 0) {
            collectRate = ApisUtil.convert(30, ApisUtil.Unit.nAPIS);
        } else {
            collectRate = ApisUtil.convert(42, ApisUtil.Unit.nAPIS);
        }

        return countCollected.multiply(collectRate);
    }

    private BigInteger getLimitMineral() {
        if(nonce.compareTo(BigInteger.valueOf(3)) < 0) {
            return ApisUtil.convert(40, ApisUtil.Unit.uAPIS);
        } else if(nonce.compareTo(BigInteger.valueOf(10)) < 0) {
            return ApisUtil.convert(60, ApisUtil.Unit.uAPIS);
        } else if(nonce.compareTo(BigInteger.valueOf(20)) < 0) {
            return ApisUtil.convert(80, ApisUtil.Unit.uAPIS);
        } else if(nonce.compareTo(BigInteger.valueOf(50)) < 0) {
            return ApisUtil.convert(100, ApisUtil.Unit.uAPIS);
        } else if(nonce.compareTo(BigInteger.valueOf(100)) < 0) {
            return ApisUtil.convert(120, ApisUtil.Unit.uAPIS);
        } else if(nonce.compareTo(BigInteger.valueOf(1_000)) < 0) {
            return ApisUtil.convert(140, ApisUtil.Unit.uAPIS);
        } else if(nonce.compareTo(BigInteger.valueOf(10_000)) < 0) {
            return ApisUtil.convert(200, ApisUtil.Unit.uAPIS);
        } else {
            return ApisUtil.convert(500, ApisUtil.Unit.uAPIS);
        }
    }

    public BigInteger getLastBlock() {
        return lastBlock;
    }

    public AccountState withBalanceIncrement(BigInteger value) {
        return new AccountState(nonce, balance.add(value), mineral, lastBlock, stateRoot, codeHash, addressMask, gateKeeper);
    }

    public AccountState withMineral(BigInteger value) {
        return new AccountState(nonce, balance, value, lastBlock, stateRoot, codeHash, addressMask, gateKeeper);
    }

    public AccountState withMineralIncrement(BigInteger value) {
        return new AccountState(nonce, balance, mineral.add(value), lastBlock, stateRoot, codeHash, addressMask, gateKeeper);
    }

    public byte[] getEncoded() {
        if (rlpEncoded == null) {
            byte[] nonce = RLP.encodeBigInteger(this.nonce);
            byte[] balance = RLP.encodeBigInteger(this.balance);
            byte[] mineral = RLP.encodeBigInteger(this.mineral);
            byte[] lastBlock = RLP.encodeBigInteger(this.lastBlock);
            byte[] stateRoot = RLP.encodeElement(this.stateRoot);
            byte[] codeHash = RLP.encodeElement(this.codeHash);
            byte[] addressMask = RLP.encodeElement(this.addressMask);
            byte[] gateKeeper = RLP.encodeElement(this.gateKeeper);
            this.rlpEncoded = RLP.encodeList(nonce, balance, mineral, lastBlock, stateRoot, codeHash, addressMask, gateKeeper);
        }
        return rlpEncoded;
    }

    public boolean isContractExist(BlockchainConfig blockchainConfig) {
        return !FastByteComparisons.equal(codeHash, EMPTY_DATA_HASH) ||
                !blockchainConfig.getConstants().getInitialNonce().equals(nonce);
    }

    public boolean isEmpty() {
        return FastByteComparisons.equal(codeHash, EMPTY_DATA_HASH) &&
                BigInteger.ZERO.equals(balance) &&
                BigInteger.ZERO.equals(nonce);
    }


    public String toString() {
        return "  Nonce: " + this.getNonce().toString() + "\n" +
                "  Balance: " + getBalance() + "\n" +
                "  Mineral: " + getMineral(0) + "\n" +
                "  LastBlock: " + getLastBlock() + "\n" +
                "  State Root: " + Hex.toHexString(this.getStateRoot()) + "\n" +
                "  Code Hash: " + Hex.toHexString(this.getCodeHash()) + "\n" +
                "  Address Mask: " + Hex.toHexString(this.getAddressMask()) + "\n" +
                "  Gate Keeper: " + Hex.toHexString(this.getGateKeeper());
    }

    public String toString(long blockNumber) {
        return "  Nonce: " + this.getNonce().toString() + "\n" +
                "  Balance: " + getBalance() + "\n" +
                "  Mineral: " + getMineral(blockNumber) + "\n" +
                "  LastBlock: " + getLastBlock() + "\n" +
                "  State Root: " + Hex.toHexString(this.getStateRoot()) + "\n" +
                "  Code Hash: " + Hex.toHexString(this.getCodeHash()) + "\n" +
                "  Address Mask: " + Hex.toHexString(this.getAddressMask()) + "\n" +
                "  Gate Keeper: " + Hex.toHexString(this.getGateKeeper());
    }
}
