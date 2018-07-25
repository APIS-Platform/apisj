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

import org.apis.config.Constants;
import org.apis.core.Transaction;
import org.apis.datasource.CachedSource;
import org.apis.datasource.MultiCache;
import org.apis.datasource.Source;
import org.apis.datasource.WriteCache;
import org.apis.config.SystemProperties;
import org.apis.core.AccountState;
import org.apis.core.Block;
import org.apis.crypto.HashUtil;
import org.apis.facade.Repository;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.apis.vm.DataWord;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by Anton Nashatyrev on 07.10.2016.
 */
public class RepositoryImpl implements org.apis.core.Repository, Repository {

    protected RepositoryImpl parent;

    Source<byte[], AccountState> accountStateCache;
    protected Source<byte[], byte[]> codeCache;
    Source<byte[], byte[]> addressMaskCache;
    Source<byte[], byte[]> masterNodeStateCache;
    MultiCache<? extends CachedSource<DataWord, DataWord>> storageCache;

    @Autowired
    protected SystemProperties config = SystemProperties.getDefault();

    RepositoryImpl() {}

    public RepositoryImpl(Source<byte[], AccountState> accountStateCache, Source<byte[], byte[]> codeCache, MultiCache<? extends CachedSource<DataWord, DataWord>> storageCache, Source<byte[], byte[]> addressMaskCache, Source<byte[], byte[]> masterNodeStateCache) {
        init(accountStateCache, codeCache, storageCache, addressMaskCache, masterNodeStateCache);
    }

    protected void init(Source<byte[], AccountState> accountStateCache, Source<byte[], byte[]> codeCache, MultiCache<? extends CachedSource<DataWord, DataWord>> storageCache, Source<byte[], byte[]> addressMaskCache, Source<byte[], byte[]> masterNodeStateCache) {
        this.accountStateCache = accountStateCache;
        this.codeCache = codeCache;
        this.storageCache = storageCache;
        this.addressMaskCache = addressMaskCache;
        this.masterNodeStateCache = masterNodeStateCache;
    }

    @Override
    public synchronized AccountState createAccount(byte[] addr) {
        AccountState state = new AccountState(config.getBlockchainConfig().getCommonConstants().getInitialNonce(), BigInteger.ZERO);
        accountStateCache.put(addr, state);
        return state;
    }

    @Override
    public synchronized boolean isExist(byte[] addr) {
        return getAccountState(addr) != null;
    }

    @Override
    public synchronized AccountState getAccountState(byte[] addr) {
        return accountStateCache.get(addr);
    }

    synchronized AccountState getOrCreateAccountState(byte[] addr) {
        AccountState ret = accountStateCache.get(addr);
        if (ret == null) {
            ret = createAccount(addr);
        }
        return ret;
    }

    @Override
    public synchronized void delete(byte[] addr) {
        accountStateCache.delete(addr);
        storageCache.delete(addr);
    }

    @Override
    public synchronized BigInteger increaseNonce(byte[] addr) {
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withIncrementedNonce());
        return accountState.getNonce();
    }

    @Override
    public synchronized BigInteger setNonce(byte[] addr, BigInteger nonce) {
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withNonce(nonce));
        return accountState.getNonce();
    }

    @Override
    public synchronized BigInteger getNonce(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? config.getBlockchainConfig().getCommonConstants().getInitialNonce() :
                accountState.getNonce();
    }

    @Override
    public synchronized ContractDetails getContractDetails(byte[] addr) {
        return new ContractDetailsImpl(addr);
    }

    @Override
    public synchronized boolean hasContractDetails(byte[] addr) {
        return getContractDetails(addr) != null;
    }

    @Override
    public synchronized void saveCode(byte[] addr, byte[] code) {
        byte[] codeHash = HashUtil.sha3(code);
        codeCache.put(codeHash, code);
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withCodeHash(codeHash));
    }

    @Override
    public synchronized byte[] getCode(byte[] addr) {
        byte[] codeHash = getCodeHash(addr);
        return FastByteComparisons.equal(codeHash, HashUtil.EMPTY_DATA_HASH) ?
                ByteUtil.EMPTY_BYTE_ARRAY : codeCache.get(codeHash);
    }

    @Override
    public byte[] getCodeHash(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState != null ? accountState.getCodeHash() : HashUtil.EMPTY_DATA_HASH;
    }

    @Override
    public synchronized void addStorageRow(byte[] addr, DataWord key, DataWord value) {
        getOrCreateAccountState(addr);

        Source<DataWord, DataWord> contractStorage = storageCache.get(addr);
        contractStorage.put(key, value.isZero() ? null : value);
    }

    @Override
    public synchronized DataWord getStorageValue(byte[] addr, DataWord key) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? null : storageCache.get(addr).get(key);
    }

    @Override
    public synchronized BigInteger getBalance(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? BigInteger.ZERO : accountState.getBalance();
    }

    @Override
    public synchronized BigInteger getMineral(byte[] addr, long blockNumber) {
        AccountState accountState = getAccountState(addr);

        if(accountState == null) {
            return BigInteger.ZERO;
        }

        return accountState.getMineral(blockNumber);
    }

    @Override
    public synchronized BigInteger addBalance(byte[] addr, BigInteger value) {
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withBalanceIncrement(value));
        return accountState.getBalance();
    }

    @Override
    public BigInteger setMineral(byte[] addr, BigInteger value, long blockNumber) {
        //System.out.println(String.format("RepositoryImpl 190 SetMineral value{%d} blockNumber{%d}", value, blockNumber));

        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withMineral(value).withLastBlock(BigInteger.valueOf(blockNumber)));
        return accountState.getMineral(blockNumber);
    }

    @Override
    public BigInteger addMineral(byte[] addr, BigInteger value, long blockNumber) {
        //System.out.println(String.format("RepositoryImpl 199 AddMineral value{%d} blockNumber{%d}", value, blockNumber));

        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withMineralIncrement(value).withLastBlock(BigInteger.valueOf(blockNumber)));
        return accountState.getMineral(blockNumber);
    }

    @Override
    public String getMaskByAddress(byte[] addr) {
        AccountState accountState = getAccountState(addr);

        if(accountState == null) {
            return "";
        }

        return accountState.getAddressMask();
    }

    @Override
    public byte[] getAddressByMask(String mask) {
        if(mask == null || mask.isEmpty()) {
            return null;
        }
        byte[] maskHash = HashUtil.sha3(mask.getBytes(Charset.forName("UTF-8")));
        return addressMaskCache.get(maskHash);
    }

    @Override
    public String setAddressMask(byte[] addr, String mask) {
        if(mask == null || mask.isEmpty()) {
            return "";
        }
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withAddressMask(mask));

        byte[] maskBytes = mask.getBytes(Charset.forName("UTF-8"));
        byte[] maskHash = HashUtil.sha3(maskBytes);

        addressMaskCache.put(maskHash, addr);

        return accountState.getAddressMask();
    }

    @Override
    public byte[] getGateKeeper(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? HashUtil.EMPTY_DATA_HASH : accountState.getGateKeeper();
    }

    @Override
    public byte[] setGateKeeper(byte[] addr, byte[] gateKeeper) {
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withGateKeeper(gateKeeper));
        return accountState.getGateKeeper();
    }


    @Override
    public long getMnStartBlock(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? 0 : accountState.getMnStartBlock();
    }

    @Override
    public void setMnStartBlock(byte[] addr, long blockNumber) {
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withMnStartBlock(blockNumber));
    }


    @Override
    public long getMnLastBlock(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? 0 : accountState.getMnLastBlock();
    }

    @Override
    public long setMnLastBlock(byte[] addr, long blockNumber) {
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withMnLastBlock(blockNumber));
        return accountState.getMnLastBlock();
    }

    @Override
    public byte[] getMnRecipient(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? null : accountState.getMnRecipient();
    }

    @Override
    public byte[] setMnRecipient(byte[] addr, byte[] recipient) {
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withMnRecipient(recipient));
        return accountState.getMnRecipient();
    }

    @Override
    public BigInteger getMnStartBalance(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? BigInteger.ZERO : accountState.getMnStartBalance();
    }

    @Override
    public BigInteger setMnStartBalance(byte[] addr, BigInteger balance) {
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withMnStartBalance(balance));
        return accountState.getMnStartBalance();
    }

    @Override
    public List<byte[]> getMasterNodeList(int type) {
        List<byte[]> mnList = new ArrayList<>();

        BigInteger masterNodeBalance;
        switch(type) {
            case 0:
                masterNodeBalance = config.getBlockchainConfig().getCommonConstants().getMASTERNODE_BALANCE_GENERAL();
                break;
            case 1:
                masterNodeBalance = config.getBlockchainConfig().getCommonConstants().getMASTERNODE_BALANCE_MAJOR();
                break;
            case 2:
                masterNodeBalance = config.getBlockchainConfig().getCommonConstants().getMASTERNODE_BALANCE_PRIVATE();
                break;
            default:
                return mnList;
        }


        for(long i = 0; i < 10_000; i++) {
            byte[] index = ByteUtil.longToBytes(i);

            byte[] mnAddr = masterNodeStateCache.get(index);
            if(mnAddr != null) {
                AccountState accountState = getAccountState(mnAddr);
                if(accountState != null) {
                    if(accountState.getBalance().equals(masterNodeBalance)) {
                        mnList.add(mnAddr);
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        return mnList;
    }

    @Override
    public long updateMasterNode(Transaction tx, long blockNumber) {
        Constants constants = config.getBlockchainConfig().getCommonConstants();
        AccountState accountState = getAccountState(tx.getSender());
        if(accountState == null) return -1;


        long countMasterNodes = 0;
        long limitMasterNodes = constants.getMASTERNODE_LIMIT(accountState.getBalance());
        if(limitMasterNodes == 0) {
            return -1;
        }


        for(long i = 0; i < constants.getMASTERNODE_LIMIT_TOTAL(); i++) {
            byte[] index = ByteUtil.longToBytes(i);
            byte[] mn = masterNodeStateCache.get(index);

            // 모든 목록을 확인했는데 없으면, 추가한다.
            if(mn == null) {
                masterNodeStateCache.put(index, tx.getSender());
                setMnStartBlock(tx.getSender(), blockNumber);
                setMnLastBlock(tx.getSender(), blockNumber);
                setMnRecipient(tx.getSender(), tx.getData());
                setMnStartBalance(tx.getSender(), accountState.getBalance());
                return i;
            } else if(FastByteComparisons.equal(mn, tx.getSender())) {
                setMnRecipient(tx.getSender(), tx.getData());
                setMnLastBlock(tx.getSender(), blockNumber);
                return i;
            }

            AccountState mnState = getAccountState(mn);
            if(mnState.getBalance().equals(accountState.getBalance())) {
                countMasterNodes += 1;
            }

            // 해당 등급의 마스터노드가 가득찼으면, 더 추가할 필요가 없다
            if(countMasterNodes >= limitMasterNodes) {
                return -1;
            }
        }

        return -1;
    }

    @Override
    public void cleaningMasterNodes(long blockNumber) {
        Constants constants = config.getBlockchainConfig().getCommonConstants();

        long countGeneral = 0;
        long countMajor = 0;
        long countPrivate = 0;

        List<byte[]> mnFinishGeneralList = new ArrayList<>();
        List<byte[]> mnFinishMajorList = new ArrayList<>();
        List<byte[]> mnFinishPrivateList = new ArrayList<>();

        List<byte[]> mnExpiredList = new ArrayList<>();

        for(long i = 0; i < constants.getMASTERNODE_LIMIT_TOTAL(); i++) {
            byte[] index = ByteUtil.longToBytes(i);
            byte[] mn = masterNodeStateCache.get(index);

            if (mn == null) {
                break;
            }

            AccountState mnState = getAccountState(mn);

            if(mnState.getBalance().equals(constants.getMASTERNODE_BALANCE_GENERAL())) {
                // 리스프레시 기간이 지난 마스터노드를 걸러낸다.
                if(mnState.getMnStartBlock() < blockNumber - 777_777) {
                    mnFinishGeneralList.add(mn);
                }
                countGeneral += 1;
            }
            else if(mnState.getBalance().equals(constants.getMASTERNODE_BALANCE_MAJOR())) {
                if(mnState.getMnStartBlock() < blockNumber - 777_777) {
                    mnFinishMajorList.add(mn);
                }
                countMajor += 1;
            }
            else if(mnState.getBalance().equals(constants.getMASTERNODE_BALANCE_PRIVATE())) {
                if(mnState.getMnStartBlock() < blockNumber - 777_777) {
                    mnFinishPrivateList.add(mn);
                }
                countPrivate += 1;
            }

            // 너무 오랫동안 업데이트되지 않았을 경우 종료시킨다.
            if(mnState.getMnLastBlock() < blockNumber - 10_000) {
                mnExpiredList.add(mn);
            }
            // 잔고가 마스터노드 시작 잔고보다 작아지면 종료시킨다
            else if(mnState.getBalance().compareTo(mnState.getMnStartBalance()) < 0) {
                mnExpiredList.add(mn);
            }
        }


        if(countGeneral >= constants.getMASTERNODE_LIMIT_GENERAL()) {
            finishMasterNodes(mnFinishGeneralList, blockNumber);
        }
        if(countMajor >= constants.getMASTERNODE_LIMIT_MAJOR()) {
            finishMasterNodes(mnFinishMajorList, blockNumber);
        }
        if(countPrivate >= constants.getMASTERNODE_LIMIT_PRIVATE()) {
            finishMasterNodes(mnFinishPrivateList, blockNumber);
        }

        finishMasterNodes(mnExpiredList, blockNumber);
    }


    private void finishMasterNodes(List<byte[]> finishedList, long blockNumber) {
        for(byte[] mnFinished : finishedList) {
            setMnStartBlock(mnFinished, 0);
            setMnLastBlock(mnFinished, blockNumber);
            setMnStartBalance(mnFinished, BigInteger.ZERO);

            finishMasterNode(mnFinished);
        }
    }


    @Override
    public void finishMasterNode(byte[] finished) {
        long lastIndex = 0;
        byte[] lastMasterNode = null;
        long finishedIndex = 0;
        Constants constants = config.getBlockchainConfig().getCommonConstants();

        for(long i = 0; i < constants.getMASTERNODE_LIMIT_TOTAL(); i++) {
            byte[] index = ByteUtil.longToBytes(i);
            byte[] mn = masterNodeStateCache.get(index);

            if(mn == null) {
                break;
            }

            if(FastByteComparisons.equal(mn, finished)) {
                finishedIndex = i;
            }

            lastIndex = i;
            lastMasterNode = mn;
        }

        if(lastMasterNode == null) {
            return;
        }

        masterNodeStateCache.put(ByteUtil.longToBytes(finishedIndex), lastMasterNode);
        masterNodeStateCache.delete(ByteUtil.longToBytes(lastIndex));
    }


    @Override
    public synchronized RepositoryImpl startTracking() {
        Source<byte[], AccountState> trackAccountStateCache = new WriteCache.BytesKey<>(accountStateCache, WriteCache.CacheType.SIMPLE);
        Source<byte[], byte[]> trackCodeCache = new WriteCache.BytesKey<>(codeCache, WriteCache.CacheType.SIMPLE);
        MultiCache<CachedSource<DataWord, DataWord>> trackStorageCache = new MultiCache(storageCache) {
            @Override
            protected CachedSource create(byte[] key, CachedSource srcCache) {
                return new WriteCache<>(srcCache, WriteCache.CacheType.SIMPLE);
            }
        };
        Source<byte[], byte[]> trackAddressMaskCache = new WriteCache.BytesKey<>(addressMaskCache, WriteCache.CacheType.SIMPLE);
        Source<byte[], byte[]> trackMasterNodeStateCache = new WriteCache.BytesKey<>(masterNodeStateCache, WriteCache.CacheType.SIMPLE);

        RepositoryImpl ret = new RepositoryImpl(trackAccountStateCache, trackCodeCache, trackStorageCache, trackAddressMaskCache, trackMasterNodeStateCache);
        ret.parent = this;
        return ret;
    }

    @Override
    public synchronized org.apis.core.Repository getSnapshotTo(byte[] root) {
        return parent.getSnapshotTo(root);
    }


    @Override
    public synchronized void commit() {
        org.apis.core.Repository parentSync = parent == null ? this : parent;
        // need to synchronize on parent since between different caches flush
        // the parent repo would not be in consistent state
        // when no parent just take this instance as a mock
        synchronized (parentSync) {
            storageCache.flush();
            codeCache.flush();
            accountStateCache.flush();
            addressMaskCache.flush();
            masterNodeStateCache.flush();
        }
    }

    @Override
    public synchronized void rollback() {
        // nothing to do, will be GCed
    }

    @Override
    public byte[] getRoot() {
        throw new RuntimeException("Not supported");
    }

    public synchronized String getTrieDump() {
        return dumpStateTrie();
    }

    public String dumpStateTrie() {
        throw new RuntimeException("Not supported");
    }

    class ContractDetailsImpl implements ContractDetails {
        private byte[] address;

        public ContractDetailsImpl(byte[] address) {
            this.address = address;
        }

        @Override
        public void put(DataWord key, DataWord value) {
            RepositoryImpl.this.addStorageRow(address, key, value);
        }

        @Override
        public DataWord get(DataWord key) {
            return RepositoryImpl.this.getStorageValue(address, key);
        }

        @Override
        public byte[] getCode() {
            return RepositoryImpl.this.getCode(address);
        }

        @Override
        public byte[] getCode(byte[] codeHash) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void setCode(byte[] code) {
            RepositoryImpl.this.saveCode(address, code);
        }

        @Override
        public byte[] getStorageHash() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void decode(byte[] rlpCode) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void setDirty(boolean dirty) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void setDeleted(boolean deleted) {
            RepositoryImpl.this.delete(address);
        }

        @Override
        public boolean isDirty() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public boolean isDeleted() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public byte[] getEncoded() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public int getStorageSize() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public Set<DataWord> getStorageKeys() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public Map<DataWord, DataWord> getStorage(@Nullable Collection<DataWord> keys) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public Map<DataWord, DataWord> getStorage() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void setStorage(Map<DataWord, DataWord> storage) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public byte[] getAddress() {
            return address;
        }

        @Override
        public void setAddress(byte[] address) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public ContractDetails clone() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void syncStorage() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public ContractDetails getSnapshotTo(byte[] hash) {
            throw new RuntimeException("Not supported");
        }
    }


    @Override
    public Set<byte[]> getAccountsKeys() {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void flush() {
        throw new RuntimeException("Not supported");
    }


    @Override
    public void flushNoReconnect() {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void syncToRoot(byte[] root) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public boolean isClosed() {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void close() {
    }

    @Override
    public void reset() {
        throw new RuntimeException("Not supported");
    }

    @Override
    public int getStorageSize(byte[] addr) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public Set<DataWord> getStorageKeys(byte[] addr) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public Map<DataWord, DataWord> getStorage(byte[] addr, @Nullable Collection<DataWord> keys) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void updateBatch(HashMap<ByteArrayWrapper, AccountState> accountStates, HashMap<ByteArrayWrapper, ContractDetails> contractDetailes) {
        for (Map.Entry<ByteArrayWrapper, AccountState> entry : accountStates.entrySet()) {
            accountStateCache.put(entry.getKey().getData(), entry.getValue());
        }
        for (Map.Entry<ByteArrayWrapper, ContractDetails> entry : contractDetailes.entrySet()) {
            ContractDetails details = getContractDetails(entry.getKey().getData());
            for (DataWord key : entry.getValue().getStorageKeys()) {
                details.put(key, entry.getValue().get(key));
            }
            byte[] code = entry.getValue().getCode();
            if (code != null && code.length > 0) {
                details.setCode(code);
            }
        }
    }

    @Override
    public void loadAccount(byte[] addr, HashMap<ByteArrayWrapper, AccountState> cacheAccounts, HashMap<ByteArrayWrapper, ContractDetails> cacheDetails) {
        throw new RuntimeException("Not supported");
    }

}
