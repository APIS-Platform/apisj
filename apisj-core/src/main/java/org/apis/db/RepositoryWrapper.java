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
package org.apis.db;

import org.apis.core.*;
import org.apis.vm.DataWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.*;

/**
 * Repository delegating all calls to the last Repository
 *
 * Created by Anton Nashatyrev on 22.12.2016.
 */
@Component
public class RepositoryWrapper implements Repository {

    @Autowired
    BlockchainImpl blockchain;

    public RepositoryWrapper() {
    }

    @Override
    public AccountState createAccount(byte[] addr) {
        return blockchain.getRepository().createAccount(addr);
    }

    @Override
    public boolean isExist(byte[] addr) {
        return blockchain.getRepository().isExist(addr);
    }

    @Override
    public AccountState getAccountState(byte[] addr) {
        return blockchain.getRepository().getAccountState(addr);
    }

    @Override
    public void delete(byte[] addr) {
        blockchain.getRepository().delete(addr);
    }

    @Override
    public BigInteger increaseNonce(byte[] addr) {
        return blockchain.getRepository().increaseNonce(addr);
    }

    @Override
    public BigInteger setNonce(byte[] addr, BigInteger nonce) {
        return blockchain.getRepository().setNonce(addr, nonce);
    }

    @Override
    public BigInteger getNonce(byte[] addr) {
        return blockchain.getRepository().getNonce(addr);
    }

    @Override
    public ContractDetails getContractDetails(byte[] addr) {
        return blockchain.getRepository().getContractDetails(addr);
    }

    @Override
    public boolean hasContractDetails(byte[] addr) {
        return blockchain.getRepository().hasContractDetails(addr);
    }

    @Override
    public void saveCode(byte[] addr, byte[] code) {
        blockchain.getRepository().saveCode(addr, code);
    }

    @Override
    public byte[] getCode(byte[] addr) {
        return blockchain.getRepository().getCode(addr);
    }

    @Override
    public byte[] getCodeHash(byte[] addr) {
        return blockchain.getRepository().getCodeHash(addr);
    }

    @Override
    public void addStorageRow(byte[] addr, DataWord key, DataWord value) {
        blockchain.getRepository().addStorageRow(addr, key, value);
    }

    @Override
    public DataWord getStorageValue(byte[] addr, DataWord key) {
        return blockchain.getRepository().getStorageValue(addr, key);
    }

    @Override
    public BigInteger getBalance(byte[] addr) {
        return blockchain.getRepository().getBalance(addr);
    }

    @Override
    public BigInteger getMineral(byte[] addr, long blockNumber) {
        return blockchain.getRepository().getMineral(addr, blockNumber);
    }

    @Override
    public BigInteger getTotalReward(byte[] addr) {
        return blockchain.getRepository().getTotalReward(addr);
    }

    @Override
    public BigInteger addBalance(byte[] addr, BigInteger value) {
        return blockchain.getRepository().addBalance(addr, value);
    }

    @Override
    public BigInteger addReward(byte[] addr, BigInteger reward) {
        return blockchain.getRepository().addReward(addr, reward);
    }

    @Override
    public BigInteger setMineral(byte[] addr, BigInteger value, long blockNumber) {
        return blockchain.getRepository().setMineral(addr, value, blockNumber);
    }

    @Override
    public String getMaskByAddress(byte[] addr) {
        return blockchain.getRepository().getMaskByAddress(addr);
    }

    @Override
    public byte[] getAddressByMask(String mask) {
        return blockchain.getRepository().getAddressByMask(mask);
    }

    @Override
    public byte[] getProofKey(byte[] addr) {
        return blockchain.getRepository().getProofKey(addr);
    }

    @Override
    public long getMnStartBlock(byte[] addr) {
        return blockchain.getRepository().getMnStartBlock(addr);
    }

    @Override
    public long setMnStartBlock(byte[] addr, long blockNumber) {
        return blockchain.getRepository().setMnStartBlock(addr, blockNumber);
    }

    @Override
    public long getMnLastBlock(byte[] addr) {
        return blockchain.getRepository().getMnLastBlock(addr);
    }

    @Override
    public long setMnLastBlock(byte[] addr, long blockNumber) {
        return blockchain.getRepository().setMnLastBlock(addr, blockNumber);
    }

    @Override
    public byte[] getMnRecipient(byte[] addr) {
        return blockchain.getRepository().getMnRecipient(addr);
    }

    @Override
    public byte[] setMnRecipient(byte[] addr, byte[] recipient) {
        return blockchain.getRepository().setMnRecipient(addr, recipient);
    }

    @Override
    public BigInteger getMnStartBalance(byte[] addr) {
        return blockchain.getRepository().getMnStartBalance(addr);
    }

    @Override
    public BigInteger setMnStartBalance(byte[] addr, BigInteger balance) {
        return blockchain.getRepository().setMnStartBalance(addr, balance);
    }

    @Override
    public void cleaningMasterNodes(long blockNumber) {
        blockchain.getRepository().cleaningMasterNodes(blockNumber);
    }

    @Override
    public long updateMasterNode(Transaction tx, long blockNumber) {
        return blockchain.getRepository().updateMasterNode(tx, blockNumber);
    }

    @Override
    public void updateAddressMask(TransactionReceipt receipt) {
        blockchain.getRepository().updateAddressMask(receipt);
    }

    @Override
    public void insertMnState(byte[] prevMn, byte[] addr, long blockNumber, BigInteger startBalance, byte[] recipient) {
        blockchain.getRepository().insertMnState(prevMn, addr, blockNumber, startBalance, recipient);
    }

    @Override
    public void finishMasterNode(byte[] finished, long blockNumber) {
        blockchain.getRepository().finishMasterNode(finished, blockNumber);
    }

    @Override
    public List<byte[]> getMasterNodeList(int type) {
        return blockchain.getRepository().getMasterNodeList(type);
    }

    @Override
    public byte[] setProofKey(byte[] addr, byte[] proofKey) {
        return blockchain.getRepository().setProofKey(addr, proofKey);
    }

    @Override
    public BigInteger addMineral(byte[] addr, BigInteger value, long blockNumber) {
        return blockchain.getRepository().addMineral(addr, value, blockNumber);
    }

    @Override
    public String setAddressMask(byte[] addr, String mask) {
        return blockchain.getRepository().setAddressMask(addr, mask);
    }

    @Override
    public Set<byte[]> getAccountsKeys() {
        return blockchain.getRepository().getAccountsKeys();
    }

    @Override
    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {
        blockchain.getRepository().dumpState(block, gasUsed, txNumber, txHash);
    }

    @Override
    public Repository startTracking() {
        return blockchain.getRepository().startTracking();
    }

    @Override
    public void flush() {
        blockchain.getRepository().flush();
    }

    @Override
    public void flushNoReconnect() {
        blockchain.getRepository().flushNoReconnect();
    }

    @Override
    public void commit() {
        blockchain.getRepository().commit();
    }

    @Override
    public void rollback() {
        blockchain.getRepository().rollback();
    }

    @Override
    public void syncToRoot(byte[] root) {
        blockchain.getRepository().syncToRoot(root);
    }

    @Override
    public boolean isClosed() {
        return blockchain.getRepository().isClosed();
    }

    @Override
    public void close() {
        blockchain.getRepository().close();
    }

    @Override
    public void reset() {
        blockchain.getRepository().reset();
    }

    @Override
    public void updateBatch(HashMap<ByteArrayWrapper, AccountState> accountStates, HashMap<ByteArrayWrapper, ContractDetails> contractDetailes) {
        blockchain.getRepository().updateBatch(accountStates, contractDetailes);
    }

    @Override
    public byte[] getRoot() {
        return blockchain.getRepository().getRoot();
    }

    @Override
    public void loadAccount(byte[] addr, HashMap<ByteArrayWrapper, AccountState> cacheAccounts, HashMap<ByteArrayWrapper, ContractDetails> cacheDetails) {
        blockchain.getRepository().loadAccount(addr, cacheAccounts, cacheDetails);
    }

    @Override
    public Repository getSnapshotTo(byte[] root) {
        return blockchain.getRepository().getSnapshotTo(root);
    }

    public Repository getSnapshotTo(long blockNumber) {
        return blockchain.getRepository().getSnapshotTo(blockchain.getBlockByNumber(blockNumber).getStateRoot());
    }

    public Block getBlock(long blockNumber) {
        return blockchain.getBlockByNumber(blockNumber);
    }

    @Override
    public int getStorageSize(byte[] addr) {
        return blockchain.getRepository().getStorageSize(addr);
    }

    @Override
    public Set<DataWord> getStorageKeys(byte[] addr) {
        return blockchain.getRepository().getStorageKeys(addr);
    }

    @Override
    public Map<DataWord, DataWord> getStorage(byte[] addr, @Nullable Collection<DataWord> keys) {
        return blockchain.getRepository().getStorage(addr, keys);
    }
}
