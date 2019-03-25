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
import org.apis.contract.ContractLoader;
import org.apis.core.*;
import org.apis.datasource.CachedSource;
import org.apis.datasource.MultiCache;
import org.apis.datasource.Source;
import org.apis.datasource.WriteCache;
import org.apis.config.SystemProperties;
import org.apis.crypto.HashUtil;
import org.apis.facade.Repository;
import org.apis.util.*;
import org.apis.vm.DataWord;
import org.apis.vm.LogInfo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.*;

import static org.apis.crypto.HashUtil.EMPTY_DATA_HASH;

/**
 * Created by Anton Nashatyrev on 07.10.2016.
 */
public class RepositoryImpl implements org.apis.core.Repository, Repository {

    protected RepositoryImpl parent;

    Source<byte[], AccountState> accountStateCache;
    protected Source<byte[], byte[]> codeCache;
    Source<byte[], byte[]> addressMaskCache;
    MultiCache<? extends CachedSource<DataWord, DataWord>> storageCache;

    @Autowired
    protected SystemProperties config = SystemProperties.getDefault();

    RepositoryImpl() {}

    public RepositoryImpl(Source<byte[], AccountState> accountStateCache, Source<byte[], byte[]> codeCache, MultiCache<? extends CachedSource<DataWord, DataWord>> storageCache, Source<byte[], byte[]> addressMaskCache) {
        init(accountStateCache, codeCache, storageCache, addressMaskCache);
    }

    protected void init(Source<byte[], AccountState> accountStateCache, Source<byte[], byte[]> codeCache, MultiCache<? extends CachedSource<DataWord, DataWord>> storageCache, Source<byte[], byte[]> addressMaskCache) {
        this.accountStateCache = accountStateCache;
        this.codeCache = codeCache;
        this.storageCache = storageCache;
        this.addressMaskCache = addressMaskCache;
    }

    @Override
    public synchronized AccountState createAccount(byte[] addr) {
        AccountState state = new AccountState(config.getBlockchainConfig().getCommonConstants().getInitialNonce(), BigInteger.ZERO);
        accountStateCache.put(addr, state);
        return state;
    }

    @Override
    public synchronized AccountState createAccount(byte[] addr, long blockNumber) {
        AccountState state = new AccountState(config.getBlockchainConfig().getCommonConstants().getInitialNonce(), BigInteger.ZERO, BigInteger.valueOf(blockNumber));
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

    private synchronized AccountState getOrCreateAccountState(byte[] addr) {
        AccountState ret = accountStateCache.get(addr);
        if (ret == null) {
            ret = createAccount(addr);
        }
        return ret;
    }

    private synchronized AccountState getOrCreateAccountState(byte[] addr, long blockNumber) {
        AccountState ret = accountStateCache.get(addr);
        if (ret == null) {
            ret = createAccount(addr, blockNumber);
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
        BigInteger defaultBalance = config.getBlockchainConfig().getCommonConstants().getINIT_BALANCE();

        AccountState accountState = getAccountState(addr);
        return accountState == null ? defaultBalance : accountState.getBalance();
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
    public BigInteger getTotalReward(byte[] addr) {
        AccountState accountState = getAccountState(addr);

        return accountState == null ? BigInteger.ZERO : accountState.getTotalReward();
    }

    @Override
    public synchronized BigInteger addBalance(byte[] addr, BigInteger value) {
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withBalanceIncrement(value));
        return accountState.getBalance();
    }

    @Override
    public synchronized BigInteger addBalance(byte[] addr, BigInteger value, long blockNumber) {
        AccountState accountState = getOrCreateAccountState(addr, blockNumber);
        accountStateCache.put(addr, accountState.withBalanceIncrement(value));
        return accountState.getBalance();
    }

    @Override
    public BigInteger addReward(byte[] addr, BigInteger reward) {
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withTotalRewardIncrement(reward));
        return accountState.getTotalReward();
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
        //ConsoleUtil.printlnRed(String.format("RepositoryImpl AddMineral value{%d} blockNumber{%d}", value, blockNumber));

        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withMineralIncrement(value, blockNumber).withLastBlock(BigInteger.valueOf(blockNumber)));
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
        String existMask = getMaskByAddress(addr);
        if(existMask != null && !existMask.isEmpty()) {
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
    public byte[] getProofKey(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? HashUtil.EMPTY_DATA_HASH : accountState.getProofKey();
    }

    @Override
    public byte[] setProofKey(byte[] addr, byte[] proofKey) {
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withProofKey(proofKey));
        return accountState.getProofKey();
    }


    @Override
    public long getMnStartBlock(byte[] addr) {
        if(addr == null) {
            return 0;
        }
        AccountState accountState = getAccountState(addr);
        return accountState == null ? 0 : accountState.getMnStartBlock().longValue();
    }

    @Override
    public long setMnStartBlock(byte[] addr, long blockNumber) {
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withMnStartBlock(BigInteger.valueOf(blockNumber)));
        return accountState.getMnStartBlock().longValue();
    }


    @Override
    public long getMnLastBlock(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? 0 : accountState.getMnLastBlock().longValue();
    }

    @Override
    public long setMnLastBlock(byte[] addr, long blockNumber) {
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withMnLastBlock(BigInteger.valueOf(blockNumber)));
        return accountState.getMnLastBlock().longValue();
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


    /**
     * 입력된 블록 번호에서 업데이트 대상이 되는 마스터노드의 목록을 반환한다.
     * @param blockNumber 보통, best block
     * @return 업데이트가 필요한 마스터노드 주소 목록
     */
    @Override
    public List<byte[]> getUpdatingMnList(long blockNumber) {

        if(blockNumber %10 != 0) {
            return new ArrayList<>();
        }

        List<byte[]> updatingList;
        Constants constants = config.getBlockchainConfig().getConfigForBlock(blockNumber).getConstants();

        int updatingStart = (int) (blockNumber % constants.getMASTERNODE_LIMIT_TOTAL());
        int updatingEnd = updatingStart + 10;

        List<byte[]> allNodes = new ArrayList<>(getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_NORMAL()));
        allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_LATE()));
        allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_NORMAL()));
        allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_LATE()));
        allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_NORMAL()));
        allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_LATE()));

        if(updatingStart >= allNodes.size()) {
            return new ArrayList<>();
        }

        if(updatingEnd >= allNodes.size()) {
            updatingEnd = allNodes.size() - 1;
        }

        updatingList = allNodes.subList(updatingStart, updatingEnd);

        return updatingList;
    }

    @Override
    public long getMasternodeSize(BigInteger collateral) {
        Constants constants = config.getBlockchainConfig().getCommonConstants();
        List<byte[]> allNodes = new ArrayList<>();

        if(collateral.equals(constants.getMASTERNODE_BALANCE_GENERAL())) {
            allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_EARLY_RUN()));
            allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_NORMAL()));
            allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_LATE()));
        } else if(collateral.equals(constants.getMASTERNODE_BALANCE_MAJOR())) {
            allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_EARLY_RUN()));
            allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_NORMAL()));
            allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_LATE()));
        } else if(collateral.equals(constants.getMASTERNODE_BALANCE_PRIVATE())) {
            allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_EARLY_RUN()));
            allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_NORMAL()));
            allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_LATE()));
        } else {
            return 0;
        }
        return allNodes.size();
    }

    /**
     * 입력된 주소가 마스터노드 이자를 받는 지갑으로 설정되어있는지 확인한다.
     *
     * @param address 이자를 받는 지갑인지 확인하려는 주소
     * @return TRUE : 하나 이상의 마스터노드로부터 이자를 받는 경우
     */
    public boolean isRecipientOfMasternode (byte[] address) {
        Constants constants = config.getBlockchainConfig().getCommonConstants();

        List<byte[]> generalLates = new ArrayList<>(getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_LATE()));
        if(compareRecipient(generalLates, address)) {
            return true;
        }
        List<byte[]> majorLates = new ArrayList<>(getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_LATE()));
        if(compareRecipient(majorLates, address)) {
            return true;
        }
        List<byte[]> privateLates = new ArrayList<>(getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_LATE()));
        if(compareRecipient(privateLates, address)) {
            return true;
        }

        List<byte[]> generalNormals = new ArrayList<>(getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_NORMAL()));
        if(compareRecipient(generalNormals, address)) {
            return true;
        }
        List<byte[]> majorNormals = new ArrayList<>(getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_NORMAL()));
        if(compareRecipient(majorNormals, address)) {
            return true;
        }
        List<byte[]> privateNormals = new ArrayList<>(getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_NORMAL()));
        if(compareRecipient(privateNormals, address)) {
            return true;
        }

        List<byte[]> generalEarlys = new ArrayList<>(getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_EARLY_RUN()));
        if(compareRecipient(generalEarlys, address)) {
            return true;
        }
        List<byte[]> majorEarlys = new ArrayList<>(getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_EARLY_RUN()));
        if(compareRecipient(majorEarlys, address)) {
            return true;
        }
        List<byte[]> privateEarlys = new ArrayList<>(getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_EARLY_RUN()));
        if(compareRecipient(privateEarlys, address)) {
            return true;
        }

        return false;
    }

    private boolean compareRecipient(List<byte[]> mnList, byte[] addr) {
        for(byte[] mn : mnList) {
            AccountState mnState = getAccountState(mn);
            if(FastByteComparisons.equal(mnState.getMnRecipient(), addr)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 현재 블록에서 마스터노드의 상태를 만료시킬 것인지 확인 대상이 되는 주소들의 리스트를 반환한다.
     * @param blockNumber 현재 블록 번호
     * @return 마스터노드 주소들의 리스트
     */
    @Override
    public List<byte[]> getNodeListToCheckExpiration(long blockNumber) {
        List<byte[]> expiringList = new ArrayList<>();

        Constants constants = config.getBlockchainConfig().getConfigForBlock(blockNumber).getConstants();

        List<byte[]> allNodes = new ArrayList<>();
        allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_NORMAL()));
        allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_LATE()));
        allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_NORMAL()));
        allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_LATE()));
        allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_NORMAL()));
        allNodes.addAll(getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_LATE()));
        int allNodeSize = allNodes.size();

        int firstCheckNodeNumber = (int) (blockNumber % constants.getMASTERNODE_LIMIT_TOTAL());

        for(int i = 0; i < 20; i++) {
            int index = (int) ((firstCheckNodeNumber + i) % constants.getMASTERNODE_LIMIT_TOTAL());
            if(index < allNodeSize) {
                expiringList.add(allNodes.get(index));
            }
        }

        return expiringList;
    }


    @Override
    public void insertMnState(byte[] parentMn, byte[] currentMn, long startBlock, BigInteger startBalance, byte[] recipient) {
        BigInteger blockNumberBi = BigInteger.valueOf(startBlock);

        AccountState parentState = getOrCreateAccountState(parentMn);
        accountStateCache.put(parentMn, parentState.withMnNextNode(currentMn));

        AccountState accountState = getOrCreateAccountState(currentMn);
        accountStateCache.put(currentMn, accountState.withMnStartBlock(blockNumberBi).withMnLastBlock(blockNumberBi).withMnStartBalance(startBalance).withMnRecipient(recipient).withMnPrevNode(parentMn));
    }


    /**
     * 해당 노드의 마스터노드 정보를 제거한다.
     * 마스터노드 기간이 종료되었을 때 호출된다.
     *
     * @param targetNode 마스터노드 정보를 제거하려는 주소
     */
    private void removeMasternode(byte[] targetNode, long blockNumber) {

        AccountState targetState = getOrCreateAccountState(targetNode);
        byte[] prevNode = targetState.getMnPrevNode();

        if(prevNode == null) {
            if(blockNumber > config.getBlockchainConfig().getConfigForBlock(blockNumber).getConstants().getINIT_MINERAL_APPLY_BLOCK()) {
                accountStateCache.put(targetNode, targetState.withMnStartBlock(BigInteger.ZERO).withMnStartBalance(BigInteger.ZERO).withMnLastBlock(BigInteger.ZERO).withMnNextNode(null));
            } else {
                accountStateCache.put(targetNode, targetState.withMnStartBlock(BigInteger.ZERO).withMnStartBalance(BigInteger.ZERO).withLastBlock(BigInteger.ZERO).withMnNextNode(null));
            }
        }

        else {
            AccountState prevState = getOrCreateAccountState(prevNode);

            if(targetState.getMnNextNode() == null) {
                if(blockNumber > config.getBlockchainConfig().getConfigForBlock(blockNumber).getConstants().getINIT_MINERAL_APPLY_BLOCK()) {
                    accountStateCache.put(targetNode, targetState.withMnStartBlock(BigInteger.ZERO).withMnStartBalance(BigInteger.ZERO).withMnLastBlock(BigInteger.ZERO).withMnPrevNode(null));
                } else {
                    accountStateCache.put(targetNode, targetState.withMnStartBlock(BigInteger.ZERO).withMnStartBalance(BigInteger.ZERO).withLastBlock(BigInteger.ZERO).withMnPrevNode(null));
                }

                if(prevState.getMnNextNode() != null && FastByteComparisons.equal(prevState.getMnNextNode(), targetNode)) {
                    accountStateCache.put(prevNode, prevState.withMnNextNode(null));
                }
            }
            else {
                byte[] nextMn = targetState.getMnNextNode();
                AccountState nextState = getOrCreateAccountState(nextMn);
                if(blockNumber > config.getBlockchainConfig().getConfigForBlock(blockNumber).getConstants().getINIT_MINERAL_APPLY_BLOCK()) {
                    accountStateCache.put(targetNode, targetState.withMnStartBlock(BigInteger.ZERO).withMnStartBalance(BigInteger.ZERO).withMnLastBlock(BigInteger.ZERO).withMnPrevNode(null).withMnNextNode(null));
                } else {
                    accountStateCache.put(targetNode, targetState.withMnStartBlock(BigInteger.ZERO).withMnStartBalance(BigInteger.ZERO).withLastBlock(BigInteger.ZERO).withMnPrevNode(null).withMnNextNode(null));
                }

                if(prevState.getMnNextNode() != null && FastByteComparisons.equal(prevState.getMnNextNode(), targetNode)) {
                    accountStateCache.put(prevNode, prevState.withMnNextNode(nextMn));
                }

                if(nextState.getMnPrevNode() != null && FastByteComparisons.equal(nextState.getMnPrevNode(), targetNode)) {
                    accountStateCache.put(nextMn, nextState.withMnPrevNode(prevNode));
                }
            }
        }
    }


    @Override
    public List<byte[]> getMasterNodeList(byte[] baseNode) {
        List<byte[]> mnList = new ArrayList<>();

        byte[] prevMn = baseNode;
        while(true) {
            AccountState currentState = getAccountState(prevMn);
            byte[] currentMn = currentState.getMnNextNode();

            if(currentMn == null) {
                return mnList;
            }
            else {
                mnList.add(currentMn);
                prevMn = currentMn;
            }
        }
    }

    @Override
    public List<byte[]> getMasterNodeList(byte[] baseNode, long blockNumber) {
        Constants constants = config.getBlockchainConfig().getCommonConstants();
        List<byte[]> mnList = new ArrayList<>();

        byte[] prevMn = baseNode;
        while(true) {
            AccountState currentState = getAccountState(prevMn);
            byte[] currentMn = currentState.getMnNextNode();

            if(currentMn == null) {
                return mnList;
            }
            else {
                currentState = getAccountState(currentMn);

                if(baseNode == constants.getMASTERNODE_GENERAL_BASE_LATE()
                        || baseNode == constants.getMASTERNODE_MAJOR_BASE_LATE()
                        || baseNode == constants.getMASTERNODE_PRIVATE_BASE_LATE()) {

                    if(blockNumber - currentState.getMnStartBlock().longValue() >= constants.getMASTERNODE_REWARD_PERIOD()) {
                        mnList.add(currentMn);
                    }
                } else {
                    mnList.add(currentMn);
                }
                prevMn = currentMn;
            }
        }
    }


    /**
     * 마스터노드를 새롭게 등록하거나 정보를 갱신한다.
     * @param tx 마스터노드 상태를 갱신하는 트랜잭션
     * @param blockNumber 트랜잭션이 포함된 블록의 번호
     */
    @Override
    public void updateMasterNode(Transaction tx, long blockNumber) {
        Constants constants = config.getBlockchainConfig().getConfigForBlock(blockNumber).getConstants();
        AccountState accountState = getAccountState(tx.getSender());

        // 존재하지 않는 계정(거래이력 없음)일 경우, 마노 등록 불가
        if(accountState == null) return;

        /*   EarlyBird     Normal              Late
         * //------------//------------------//--------------------//
         *          End-->|--> Masternode Start
         */

        // 마스터노드를 신청하는 경우, 잔고가 기준 담보금액과 일치해야한다.
        if(accountState.getMnStartBlock().longValue() == 0) {
            BigInteger collateral = accountState.getBalance();

            byte[] baseEarlyNode    = constants.getMASTERNODE_BASE_EARLY_RUN(collateral);
            byte[] baseNormalNode   = constants.getMASTERNODE_BASE_NORMAL(collateral);
            byte[] baseLateNode     = constants.getMASTERNODE_BASE_LATE(collateral);

            // base 주소가 null로 반환된 것은 잔고가 담보금액과 일치하지 않는 다는 의미
            if(baseEarlyNode == null || baseNormalNode == null || baseLateNode == null) {
                return;
            }

            // 얼리버드, 나머지 마스터노드에서 신청된 갯수를 확인한다.
            MasternodeSize mnNormalSize = sizeofMasterNode(baseNormalNode);
            MasternodeSize mnLateSize   = sizeofMasterNode(baseLateNode);

            long sizeofEarlyNode    = sizeofMasterNode(baseEarlyNode).getSize();
            long sizeofNormalNode   = mnNormalSize.getSize();
            long sizeofLateNode     = mnLateSize.getSize();

            // 전달된 담보금액에 해당하는 마스터노드의 최대 갯수
            long mnLimit = constants.getMASTERNODE_LIMIT(collateral);

            // 마스터노드 시작하고 하루 이내일 경우에는 NormalNode, 늦으면 LateNode
            if (sizeofEarlyNode + sizeofNormalNode + sizeofLateNode < mnLimit) {
                boolean isNormalPeriod  = constants.isMasternodeNormalPeriod(blockNumber);
                if (isNormalPeriod) {
                    insertMnState(mnNormalSize.getLastNode(), tx.getSender(), blockNumber, collateral, tx.getData());
                }
                // 이후에는 late 노드로 등록해야 한다.
                else {
                    insertMnState(mnLateSize.getLastNode(), tx.getSender(), blockNumber, collateral, tx.getData());
                }
            }
        } else {
            // 트랜잭션 데이터에 이자 수령 주소가 포함된 경우, 업데이트한다.
            if(Utils.isValidAddress(tx.getData())) {
                setMnRecipient(tx.getSender(), tx.getData());
            }

            // 마지막으로 마스터노드 정보가 업데이트 된 블록 번호를 반영한다.
            setMnLastBlock(tx.getSender(), blockNumber);
        }
    }

    @Override
    public MasternodeSize sizeofMasterNode(byte[] baseNode) {
        MasternodeSize mnSize = new MasternodeSize();
        byte[] currNode = baseNode;
        while(true) {
            mnSize.setLastNode(currNode);
            AccountState currState = getAccountState(currNode);
            byte[] nextNode = currState.getMnNextNode();

            if(nextNode == null) {
                break;
            }
            mnSize.setSize(mnSize.getSize() + 1);
            currNode = nextNode;
        }
        return mnSize;
    }

    /**
     * 입력된 주소가 마스터노드 목록에 포함되어있는지 확인한다.
     * @param address 확인하려는 주소
     * @return 포함되어있으면 true, 포함되어있지 않으면 false
     */
    @Override
    public boolean isIncludedInMasternodes(byte[] address) {
        if(address == null) { return false; }

        Constants constants = config.getBlockchainConfig().getCommonConstants();
        byte[] parentMn = config.getBlockchainConfig().getCommonConstants().getMASTERNODE_STORAGE();

        for(long i = 0; i < constants.getMASTERNODE_LIMIT_TOTAL(); i++) {
            AccountState parentMnState = getAccountState(parentMn);
            byte[] mn = parentMnState.getMnNextNode();

            // 모든 목록을 확인했는데 없으면, 추가한다.
            if(mn == null) {
                return false;
            } else if(FastByteComparisons.equal(mn, address)) {
                return true;
            }

            parentMn = mn;
        }

        return false;
    }


    @Override
    public void updateMasterNodeEarlyBird(TransactionReceipt receipt, long blockNumber) {
        Constants constants = config.getBlockchainConfig().getConfigForBlock(blockNumber).getConstants();

        if(receipt == null) { return; }
        Transaction tx = receipt.getTransaction();
        if(tx == null || tx.getReceiveAddress() == null || !receipt.isSuccessful() || !FastByteComparisons.equal(constants.getMASTERNODE_PLATFORM_CONTRACT(), tx.getReceiveAddress())) { return; }

        CallTransaction.Contract contract = new CallTransaction.Contract(ContractLoader.readABI(ContractLoader.CONTRACT_MASTERNODE_PLATFORM));
        List<LogInfo> events = receipt.getLogInfoList();
        for(LogInfo loginfo : events) {
            CallTransaction.Invocation event = contract.parseEvent(loginfo);
            String eventName = event.function.name;

            // 얼리버드 등록
            if(eventName.equals("EarlyBirdRegister")) {
                byte[] masternode   = (byte[]) event.args[1];
                byte[] recipient = (byte[])event.args[2] ;
                BigInteger collateral = (BigInteger)event.args[3];
                byte[] baseNode = constants.getMASTERNODE_BASE_EARLY(collateral);
                if(baseNode == null) {
                    return;
                }

                MasternodeSize sizeofEarlyBird = sizeofMasterNode(baseNode);
                if(sizeofEarlyBird.getSize() < constants.getMASTERNODE_LIMIT(collateral)) {
                    insertMnState(sizeofEarlyBird.getLastNode(), masternode, blockNumber, collateral, recipient);
                }

                return;
            }

            // 마스터노드 해지
            else if(eventName.equals("MasternodeCancel")) {
                byte[] masternode   = (byte[]) event.args[1];

                removeMasternode(masternode, blockNumber);
                return;
            }
        }
    }



    @Override
    public void updateAddressMask(TransactionReceipt receipt) {
        Constants constants = config.getBlockchainConfig().getCommonConstants();

        if(receipt == null) { return; }
        Transaction tx = receipt.getTransaction();
        if(tx == null || tx.getReceiveAddress() == null || !receipt.isSuccessful() || !FastByteComparisons.equal(constants.getADDRESS_MASKING_ADDRESS(), tx.getReceiveAddress())) { return; }

        CallTransaction.Contract contract = new CallTransaction.Contract(ContractLoader.readABI(ContractLoader.CONTRACT_ADDRESS_MASKING));
        List<LogInfo> events = receipt.getLogInfoList();
        for(LogInfo loginfo : events) {
            CallTransaction.Invocation event = contract.parseEvent(loginfo);
            String eventName = event.function.name;

            if(eventName.equals("MaskAddition")) {
                applyAddressMask(event);
                return;
            } else if(eventName.equals("MaskHandOver")) {
                handOverAddressMask(event);
                return;
            }
        }
    }


    private void applyAddressMask(CallTransaction.Invocation event) {
        setAddressMask((byte[])event.args[0], String.valueOf(event.args[1]));
    }

    private void handOverAddressMask(CallTransaction.Invocation event) {
        byte[] oldAddress = (byte[])event.args[1];
        byte[] newAddress = (byte[])event.args[2];
        String mask = String.valueOf(event.args[0]);

        AccountState accountState = getOrCreateAccountState(oldAddress);
        accountStateCache.put(oldAddress, accountState.withAddressMask(""));

        AccountState nextAccountState = getOrCreateAccountState(newAddress);
        accountStateCache.put(newAddress, nextAccountState.withAddressMask(mask));

        byte[] maskBytes = mask.getBytes(Charset.forName("UTF-8"));
        byte[] maskHash = HashUtil.sha3(maskBytes);

        addressMaskCache.put(maskHash, newAddress);
    }


    @Override
    public void updateProofOfKnowledge(TransactionReceipt receipt) {
        Constants constants = config.getBlockchainConfig().getCommonConstants();

        if(receipt == null) { return; }
        Transaction tx = receipt.getTransaction();
        if(tx == null || tx.getReceiveAddress() == null || !receipt.isSuccessful() || !FastByteComparisons.equal(constants.getPROOF_OF_KNOWLEDGE(), tx.getReceiveAddress())) { return; }

        CallTransaction.Contract contract = new CallTransaction.Contract(ContractLoader.readABI(ContractLoader.CONTRACT_PROOF_OF_KNOWLEDGE));
        List<LogInfo> events = receipt.getLogInfoList();
        for(LogInfo loginfo : events) {
            CallTransaction.Invocation event = contract.parseEvent(loginfo);
            String eventName = event.function.name;

            if(eventName.equals("RegisterProofKey")) {
                applyProofKey(tx.getSender(), event);
                return;
            } else if(eventName.equals("RemoveProofKey")) {
                removeProofKey(tx.getSender());
                return;
            }
        }
    }

    private void applyProofKey(byte[] sender, CallTransaction.Invocation event) {
        setProofKey(sender, (byte[])event.args[0]);
    }

    private void removeProofKey(byte[] sender) {
        setProofKey(sender, EMPTY_DATA_HASH);
    }


    @Override
    public void updatePurchasedMineral(TransactionReceipt receipt, long blockNumber) {
        Constants constants = config.getBlockchainConfig().getConfigForBlock(blockNumber).getConstants();

        if(receipt == null) { return; }
        Transaction tx = receipt.getTransaction();
        if(tx == null || tx.getReceiveAddress() == null || !receipt.isSuccessful() || !FastByteComparisons.equal(constants.getBUY_MINERAL(), tx.getReceiveAddress())) { return; }

        CallTransaction.Contract contract = new CallTransaction.Contract(ContractLoader.readABI(ContractLoader.CONTRACT_BUY_MINERAL));
        List<LogInfo> events = receipt.getLogInfoList();
        for(LogInfo loginfo : events) {
            CallTransaction.Invocation event = contract.parseEvent(loginfo);
            String eventName = event.function.name;

            if(eventName.equals("MNR")) {
                byte[] beneficiary = (byte[]) event.args[1];
                BigInteger mineral = ((BigInteger) event.args[3]);
                applyMineral(beneficiary, mineral, blockNumber);
                return;
            }
        }
    }

    private void applyMineral(byte[] buyer, BigInteger mineral, long blockNumber) {
        addMineral(buyer, mineral, blockNumber);
    }


    @Override
    public void checkMasternodeCollateral(byte[] sender, long blockNumber) {
        AccountState senderState = getAccountState(sender);
        if(senderState.getMnStartBalance().compareTo(BigInteger.ZERO) == 0) {
            // 마스터노드가 아닐 경우 별도로 체크할 것은 없다
            return;
        }

        // 잔고가 마스터노드 참여 담보 금액보다 낮아지면 해제한다
        if(senderState.getBalance().compareTo(senderState.getMnStartBalance()) < 0) {
            removeMasternode(sender, blockNumber);
        }
    }


    @Override
    public void cleaningMasterNodes(long blockNumber) {
        Constants constants = config.getBlockchainConfig().getConfigForBlock(blockNumber).getConstants();

        /*
         * 마스터노드 얼리버드 접수가 시작되는 블록이 되면, 얼리버드 목록을 초기화한다
         */
        if(isMnEbStartBlock(blockNumber, constants.getMASTERNODE_PERIOD())) {
            removeAllLinkedMasternode(constants.getMASTERNODE_GENERAL_BASE_EARLY(), constants, blockNumber);
            removeAllLinkedMasternode(constants.getMASTERNODE_MAJOR_BASE_EARLY(), constants, blockNumber);
            removeAllLinkedMasternode(constants.getMASTERNODE_PRIVATE_BASE_EARLY(), constants, blockNumber);
        }

        /*
         * 마스터노드 초기화 블록에 도달했는지 확인한다.
         * 얼리버드 노드들을, 실행중인(RUN) 얼리버드 노드와 연결한다.
         */
        if(isMnStartBlock(blockNumber, constants.getMASTERNODE_PERIOD(), constants.getMASTERNODE_EARLYBIRD_PERIOD())) {
            removeAllLinkedMasternode(constants.getMASTERNODE_GENERAL_BASE_EARLY_RUN(), constants, blockNumber);
            removeAllLinkedMasternode(constants.getMASTERNODE_GENERAL_BASE_NORMAL(), constants, blockNumber);
            removeAllLinkedMasternode(constants.getMASTERNODE_GENERAL_BASE_LATE(), constants, blockNumber);

            removeAllLinkedMasternode(constants.getMASTERNODE_MAJOR_BASE_EARLY_RUN(), constants, blockNumber);
            removeAllLinkedMasternode(constants.getMASTERNODE_MAJOR_BASE_NORMAL(), constants, blockNumber);
            removeAllLinkedMasternode(constants.getMASTERNODE_MAJOR_BASE_LATE(), constants, blockNumber);

            removeAllLinkedMasternode(constants.getMASTERNODE_PRIVATE_BASE_EARLY_RUN(), constants, blockNumber);
            removeAllLinkedMasternode(constants.getMASTERNODE_PRIVATE_BASE_NORMAL(), constants, blockNumber);
            removeAllLinkedMasternode(constants.getMASTERNODE_PRIVATE_BASE_LATE(), constants, blockNumber);

            connectEarlybirdToRun(constants.getMASTERNODE_GENERAL_BASE_EARLY(), constants.getMASTERNODE_GENERAL_BASE_EARLY_RUN(), blockNumber);
            connectEarlybirdToRun(constants.getMASTERNODE_MAJOR_BASE_EARLY(), constants.getMASTERNODE_MAJOR_BASE_EARLY_RUN(), blockNumber);
            connectEarlybirdToRun(constants.getMASTERNODE_PRIVATE_BASE_EARLY(), constants.getMASTERNODE_PRIVATE_BASE_EARLY_RUN(), blockNumber);
        }


        List<byte[]> expiredList = new ArrayList<>();


        /*
         * 업데이트가 만료된 마스터노드를 탐색하고 제거한다.
         */
        List<byte[]> checkingNodeList = getNodeListToCheckExpiration(blockNumber);

        for(byte[] mn : checkingNodeList) {
            AccountState mnState = getAccountState(mn);
            if(blockNumber - mnState.getMnLastBlock().longValue() < constants.getMASTERNODE_LIMIT_TOTAL() + 10L) {
                continue;
            }
            expiredList.add(mn);
        }

        finishMasterNodes(expiredList, blockNumber);
    }

    /**
     * staringNode로부터 연결된 모든 마스터노드를 초기화한다.
     * @param startingNode 마스터노드를 초기화하기 시작하는 노드 주소
     * @param constants 블록체인 상수
     */
    private void removeAllLinkedMasternode(byte[] startingNode, Constants constants, long blockNumber) {
        List<byte[]> expiredNodes = new ArrayList<>();
        for(int i = 0; i < constants.getMASTERNODE_LIMIT_TOTAL(); i++) {
            if(startingNode == null) {
                break;
            }
            AccountState currMn = getAccountState(startingNode);
            expiredNodes.add(startingNode);

            startingNode = currMn.getMnNextNode();
        }

        finishMasterNodes(expiredNodes, blockNumber);
    }

    /**
     * 마스터노드의 새로운 라운드가 시작되면 얼리버드 베이스 노드에 연결됐던 노드들을 얼리버드 러닝 베이스 노드로 연결해야 한다.
     * 얼리버드 베이스 노드는 라운드가 종료되기 8640 블록 전에, 새로운 얼리버드를 접수하기 위해 초기화되기 때문이다.
     * @param baseNode 노드 연결을 변경하려는 주소
     * @param runningBaseNode baseNode와 연결된 노드들의 prevMn 노드로 새롭게 연결되려는 주소
     * @param blockNumber 현재 블록 번호
     */
    private void connectEarlybirdToRun(byte[] baseNode, byte[] runningBaseNode, long blockNumber) {
        AccountState baseState = getAccountState(baseNode);
        byte[] firstNode = baseState.getMnNextNode();

        if(firstNode != null) {
            AccountState firstNodeState = getAccountState(firstNode);
            if(firstNodeState != null) {
                insertMnState(runningBaseNode, firstNode, blockNumber, firstNodeState.getMnStartBalance(), firstNodeState.getMnRecipient());
            }

            removeMasternode(baseNode, blockNumber);
        }
    }

    /**
     * startPoint 주소에서 연결된 마스터노드들을 조사한다.
     * 마스터노드가 등록된 이후, 일정 기간 내에 업데이트가 지속적으로 되야만 계속 유지될 수 있다.
     * @param startingPoint 마스터노드를 찾기 시작하는 주소
     * @param blockNumber 현재 블록 번호
     * @param constants 블록체인 관련 상수
     * @return 만기된 노드들의 주소 리스트를 반환한다.
     */
    private List<byte[]> findExpiredNodes(byte[] startingPoint, long blockNumber, Constants constants) {
        List<byte[]> expiredNodes = new ArrayList<>();

        for(long i = 0; i < constants.getMASTERNODE_LIMIT_TOTAL(); i++) {
            byte[] mn = getOrCreateAccountState(startingPoint).getMnNextNode();

            if (mn == null) {
                break;
            }
            AccountState mnState = getAccountState(mn);

            // 너무 오랫동안 업데이트되지 않았을 경우 종료시킨다.
            if(blockNumber - mnState.getMnLastBlock().longValue() > constants.getMASTERNODE_UPDATING_LIMIT()) {
                expiredNodes.add(mn);
            }
            // 잔고가 마스터노드 시작 잔고보다 작아지면 종료시킨다
            else if(mnState.getBalance().compareTo(mnState.getMnStartBalance()) < 0) {
                expiredNodes.add(mn);
            }

            startingPoint = mn;
        }

        return expiredNodes;
    }


    /**
     * 입력된 blockNumber에서 General 등급의 마스터노드 참가 얼리버드가 시작되는 블록인지 확인한다.
     *
     * @param blockNumber 확인하려는 블록 번호
     * @param resetPeriod 마스터노드 리셋 주기 (약 3달)
     * @return TRUE : 리셋 블록에 해당하는 경우
     */
    private boolean isMnEbStartBlock(long blockNumber, long resetPeriod) {
        return (blockNumber % resetPeriod == 0);
    }


    /**
     * 입력된 blockNumber에서 General 등급의 마스터노드가 시작되는 블록인지 확인한다.
     *
     * @param blockNumber 확인하려는 블록 번호
     * @param resetPeriod 마스터노드 리셋 주기 (약 3달)
     * @param blocksPerDay 하루에 생성되는 블록의 수 (하루 동안 얼리버드 신청을 받은 뒤, 마스터노드가 시작되기 때문)
     * @return TRUE : 리셋 블록에 해당하는 경우
     */
    private boolean isMnStartBlock(long blockNumber, long resetPeriod, long blocksPerDay) {
        return (blockNumber % resetPeriod == blocksPerDay);
    }


    private void finishMasterNodes(List<byte[]> finishedList, long blockNumber) {
        for(byte[] mnFinished : finishedList) {
            removeMasternode(mnFinished, blockNumber);
        }
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

        RepositoryImpl ret = new RepositoryImpl(trackAccountStateCache, trackCodeCache, trackStorageCache, trackAddressMaskCache);
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
