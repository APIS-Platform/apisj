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

import org.apis.db.ByteArrayWrapper;
import org.apis.db.ContractDetails;
import org.apis.vm.DataWord;

import java.math.BigInteger;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author Roman Mandeleil
 * @since 08.09.2014
 */
public interface Repository extends org.apis.facade.Repository {

    /**
     * Create a new account in the database
     *
     * @param addr of the contract
     * @return newly created account state
     */
    AccountState createAccount(byte[] addr);


    /**
     * @param addr - account to check
     * @return - true if account exist,
     *           false otherwise
     */
    boolean isExist(byte[] addr);

    /**
     * Retrieve an account
     *
     * @param addr of the account
     * @return account state as stored in the database
     */
    AccountState getAccountState(byte[] addr);

    /**
     * Deletes the account
     *
     * @param addr of the account
     */
    void delete(byte[] addr);

    /**
     * Increase the account nonce of the given account by one
     *
     * @param addr of the account
     * @return new value of the nonce
     */
    BigInteger increaseNonce(byte[] addr);

    /**
     * Sets the account nonce of the given account
     *
     * @param addr of the account
     * @param nonce new nonce
     * @return new value of the nonce
     */
    BigInteger setNonce(byte[] addr, BigInteger nonce);

    /**
     * Get current nonce of a given account
     *
     * @param addr of the account
     * @return value of the nonce
     */
    BigInteger getNonce(byte[] addr);

    /**
     * Retrieve contract details for a given account from the database
     *
     * @param addr of the account
     * @return new contract details
     */
    ContractDetails getContractDetails(byte[] addr);

    boolean hasContractDetails(byte[] addr);

    /**
     * Store code associated with an account
     *
     * @param addr for the account
     * @param code that will be associated with this account
     */
    void saveCode(byte[] addr, byte[] code);

    /**
     * Retrieve the code associated with an account
     *
     * @param addr of the account
     * @return code in byte-array format
     */
    byte[] getCode(byte[] addr);

    /**
     * Retrieve the code hash associated with an account
     *
     * @param addr of the account
     * @return code hash
     */
    byte[] getCodeHash(byte[] addr);

    /**
     * Put a value in storage of an account at a given key
     *
     * @param addr of the account
     * @param key of the data to store
     * @param value is the data to store
     */
    void addStorageRow(byte[] addr, DataWord key, DataWord value);


    /**
     * Retrieve storage value from an account for a given key
     *
     * @param addr of the account
     * @param key associated with this value
     * @return data in the form of a <code>DataWord</code>
     */
    DataWord getStorageValue(byte[] addr, DataWord key);


    /**
     * Retrieve balance of an account
     *
     * @param addr of the account
     * @return balance of the account as a <code>BigInteger</code> value
     */
    BigInteger getBalance(byte[] addr);

    BigInteger getMineral(byte[] addr, long blockNumber);

    BigInteger getTotalReward(byte[] addr);
    /**
     * Add value to the balance of an account
     *
     * @param addr of the account
     * @param value to be added
     * @return new balance of the account
     */
    BigInteger addBalance(byte[] addr, BigInteger value);

    BigInteger addReward(byte[] addr, BigInteger reward);

    BigInteger addMineral(byte[] addr, BigInteger value, long blockNumber);

    /**
     * 계좌에 미네랄 value를 설정한다
     *
     * @param addr 계정의 주소
     * @param value 설정할 양
     * @param blockNumber 미네랄이 추가되는 블록의 번호
     * @return 미네랄이 추가된 계정의 미네랄 양
     */
    BigInteger setMineral(byte[] addr, BigInteger value, long blockNumber);


    String setAddressMask(byte[] addr, String mask);

    /**
     * 주소에 매칭되어있는 addressMasking
     * @param addr 별칭을 찾으려는 주소
     * @return 별칭이 존재하지 않을 경우 null
     */
    String getMaskByAddress(byte[] addr);

    byte[] getAddressByMask(String mask);


    byte[] setProofKey(byte[] addr, byte[] proofKey);

    byte[] getProofKey(byte[] addr);


    long getMnStartBlock(byte[] addr);
    long setMnStartBlock(byte[] addr, long blockNumber);


    long getMnLastBlock(byte[] addr);
    long setMnLastBlock(byte[] addr, long blockNumber);


    byte[] getMnRecipient(byte[] addr);
    byte[] setMnRecipient(byte[] addr, byte[] recipient);

    BigInteger getMnStartBalance(byte[] addr);
    BigInteger setMnStartBalance(byte[] addr, BigInteger balance);

    void insertMnState(byte[] parentAddr, byte[] addr, long blockNumber, BigInteger startBalance, byte[] recipient);

    void cleaningMasterNodes(long blockNumber);
    void updateMasterNode(Transaction tx, long blockNumber);

    /**
     * 플랫폼에서 등록하는 얼리버드 신청을 등록하거나 얼리버드 마스터노드를 해지한다.
     * @param receipt 얼리버드와 관련된 이벤트가 포함된 TransactionReceipt
     * @param blockNumber 얼리버드 신청이 포함된 블록 번호
     */
    void updateMasterNodeEarlyBird(TransactionReceipt receipt, long blockNumber);
    void updateAddressMask(TransactionReceipt receipt);
    boolean isIncludedInMasternodes(byte[] address);
    void updateProofOfKnowledge(TransactionReceipt receipt);
    void updatePurchasedMineral(TransactionReceipt receipt, long blockNumber);


    List<byte[]> getMasterNodeList(int type);

    /**
     * @return Returns set of all the account addresses
     */
    Set<byte[]> getAccountsKeys();


    /**
     * Dump the full state of the current repository into a file with JSON format
     * It contains all the contracts/account, their attributes and
     *
     * @param block of the current state
     * @param gasUsed the amount of gas used in the block until that point
     * @param txNumber is the number of the transaction for which the dump has to be made
     * @param txHash is the hash of the given transaction.
     * If null, the block state post coinbase reward is dumped.
     */
    void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash);

    /**
     * Save a snapshot and start tracking future changes
     *
     * @return the tracker repository
     */
    Repository startTracking();

    void flush();
    void flushNoReconnect();


    /**
     * Store all the temporary changes made
     * to the repository in the actual database
     */
    void commit();

    /**
     * Undo all the changes made so far
     * to a snapshot of the repository
     */
    void rollback();

    /**
     * Return to one of the previous snapshots
     * by moving the root.
     *
     * @param root - new root
     */
    void syncToRoot(byte[] root);

    /**
     * Check to see if the current repository has an open connection to the database
     *
     * @return <tt>true</tt> if connection to database is open
     */
    boolean isClosed();

    /**
     * Close the database
     */
    void close();

    /**
     * Reset
     */
    void reset();

    void updateBatch(HashMap<ByteArrayWrapper, AccountState> accountStates,
                            HashMap<ByteArrayWrapper, ContractDetails> contractDetailes);


    byte[] getRoot();

    void loadAccount(byte[] addr, HashMap<ByteArrayWrapper, AccountState> cacheAccounts,
                     HashMap<ByteArrayWrapper, ContractDetails> cacheDetails);

    Repository getSnapshotTo(byte[] root);

    //Repository getSnapshotTo(long blockNumber);

    //Block getBlock(long blockNumber);
}
