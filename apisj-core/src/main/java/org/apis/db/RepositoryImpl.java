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
import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.blockchain.ApisUtil;
import org.apis.vm.DataWord;
import org.apis.vm.LogInfo;
import org.springframework.beans.factory.annotation.Autowired;
import sun.nio.cs.FastCharsetProvider;

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
     * @param blockNumber 현재 블록 번호
     */
    private void removeMasternode(byte[] targetNode, long blockNumber) {
        BigInteger blockNumberBi = BigInteger.valueOf(blockNumber);

        AccountState targetState = getOrCreateAccountState(targetNode);
        byte[] prevNode = targetState.getMnPrevNode();

        if(prevNode == null) {
            accountStateCache.put(targetNode, targetState.withMnStartBlock(BigInteger.ZERO).withMnStartBalance(BigInteger.ZERO).withLastBlock(blockNumberBi).withMnNextNode(null));
        }

        else {
            AccountState prevState = getOrCreateAccountState(prevNode);

            if(targetState.getMnNextNode() == null) {
                accountStateCache.put(targetNode, targetState.withMnStartBlock(BigInteger.ZERO).withMnStartBalance(BigInteger.ZERO).withLastBlock(blockNumberBi).withMnPrevNode(null));

                if(FastByteComparisons.equal(prevState.getMnNextNode(), targetNode)) {
                    accountStateCache.put(prevNode, prevState.withMnNextNode(null));
                }
            }
            else {
                byte[] nextMn = targetState.getMnNextNode();
                AccountState nextState = getOrCreateAccountState(nextMn);
                accountStateCache.put(targetNode, targetState.withMnStartBlock(BigInteger.ZERO).withMnStartBalance(BigInteger.ZERO).withLastBlock(blockNumberBi).withMnPrevNode(null).withMnNextNode(null));

                if(FastByteComparisons.equal(prevState.getMnNextNode(), targetNode)) {
                    accountStateCache.put(prevNode, prevState.withMnNextNode(nextMn));
                }

                if(FastByteComparisons.equal(nextState.getMnPrevNode(), targetNode)) {
                    accountStateCache.put(nextMn, nextState.withMnPrevNode(prevNode));
                }
            }
        }
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

        byte[] parentMn = config.getBlockchainConfig().getCommonConstants().getMASTERNODE_STORAGE();
        for(long i = 0; i < 9_000; i++) {
            byte[] currentMn = getAccountState(parentMn).getMnNextNode();

            if(currentMn != null) {
                AccountState accountState = getAccountState(currentMn);
                if(accountState != null) {
                    if(accountState.getMnStartBalance().equals(masterNodeBalance)) {
                        mnList.add(currentMn);
                    }
                } else {
                    return mnList;
                }
            } else {
                return mnList;
            }

            parentMn = currentMn;
        }

        return mnList;
    }


    /**
     * 마스터노드를 새롭게 등록하거나 정보를 갱신한다.
     * @param tx 마스터노드 상태를 갱신하는 트랜잭션
     * @param blockNumber 트랜잭션이 포함된 블록의 번호
     * @return 몇 번째 마스터노드인지에 대한 정보
     */
    //TODO 얼리버드를 제외한 만큼만 신청이 되도록 해야한다.
    @Override
    public long updateMasterNode(Transaction tx, long blockNumber) {
        Constants constants = config.getBlockchainConfig().getConfigForBlock(blockNumber).getConstants();
        AccountState accountState = getAccountState(tx.getSender());

        // 존재하지 않는 계정(거래이력 없음)일 경우, 마노 등록 불가
        if(accountState == null) return -1;


        long countMasterNodes = 0;
        BigInteger startBalance = accountState.getBalance();
        long limitMasterNodes = constants.getMASTERNODE_LIMIT(startBalance);
        if(limitMasterNodes == 0) {
            return -1;
        }

        byte[] parentMn = config.getBlockchainConfig().getCommonConstants().getMASTERNODE_STORAGE();
        for(long i = 0; i < constants.getMASTERNODE_LIMIT_TOTAL(); i++) {
            AccountState parentMnState = getAccountState(parentMn);
            byte[] mn = parentMnState.getMnNextNode();

            // 모든 목록을 확인했는데 없으면, 추가한다.
            if(mn == null) {
                insertMnState(parentMn, tx.getSender(), blockNumber, startBalance, tx.getData());
                return i;
            }
            // 목록에 존재하면 정보를 업데이트한다.
            else if(FastByteComparisons.equal(mn, tx.getSender())) {
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

            parentMn = mn;
        }

        return -1;
    }

    /**
     * 입력된 주소가 마스터노드 목록에 포함되어있는지 확인한다.
     * @param address 확인하려는 주소
     * @return 포함되어있으면 true, 포함되어있지 않으면 false
     */
    @Override
    public boolean isIncludedInMasternodes(byte[] address) {
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
        if(tx == null || tx.getReceiveAddress() == null || !receipt.isSuccessful() || !FastByteComparisons.equal(constants.getMASTERNODE_PLATFORM(), tx.getReceiveAddress())) { return; }

        CallTransaction.Contract contract = new CallTransaction.Contract(ContractLoader.readABI(ContractLoader.CONTRACT_MASTERNODE_PLATFORM));
        List<LogInfo> events = receipt.getLogInfoList();
        for(LogInfo loginfo : events) {
            CallTransaction.Invocation event = contract.parseEvent(loginfo);
            String eventName = event.function.name;

            // 얼리버드 등록
            if(eventName.equals("EarlyBirdRegister")) {
                byte[] participant  = (byte[]) event.args[0];
                byte[] masternode   = (byte[]) event.args[1];
                byte[] prevMasternode = (byte[]) event.args[2];
                BigInteger collateral = (BigInteger)event.args[3] ;

                // prevMasternode 주소가 0x0 인 경우, 이번 라운드의 첫번째 마스터노드 등록인 경우다.
                if(BIUtil.toBI(prevMasternode).equals(BigInteger.ZERO)) {
                    byte[] parentMn = getEarlyBirdBaseAddress(collateral, constants);

                    insertMnState(parentMn, masternode, blockNumber, collateral, participant);
                } else {
                    insertMnState(prevMasternode, masternode, blockNumber, collateral, participant);
                }
                return;
            }

            // 마스터노드 해지
            else if(eventName.equals("MasternodeCancel")) {
                byte[] participant  = (byte[]) event.args[0];
                byte[] masternode   = (byte[]) event.args[1];
                BigInteger collateral = (BigInteger)event.args[2] ;

                removeMasternode(masternode, blockNumber);
                return;
            }
        }
    }

    /**
     * 입력된 담보금액에 해당하는 마스터노드 등급의 시작 주소를 반환한다.
     * 이 시작 주소로부터 다음 마스터노드의 정보가 이어져나간다.
     * @param collateral of Masternode
     * @param constants <code>org.apis.config.Constants</code>
     * @return Base address of Earlybird masternode
     */
    private byte[] getEarlyBirdBaseAddress(BigInteger collateral, Constants constants) {
        if(collateral.equals(constants.getMASTERNODE_BALANCE_GENERAL())) {
            return constants.getMASTERNODE_EARLY_GENERAL();
        } else if(collateral.equals(constants.getMASTERNODE_BALANCE_MAJOR())) {
            return constants.getMASTERNODE_EARLY_MAJOR();
        } else if(collateral.equals(constants.getMASTERNODE_BALANCE_PRIVATE())) {
            return constants.getMASTERNODE_EARLY_PRIVATE();
        } else {
            return null;
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
    public void cleaningMasterNodes(long blockNumber) {
        Constants constants = config.getBlockchainConfig().getConfigForBlock(blockNumber).getConstants();

        /*
         * 마스터노드 얼리버드 목록의 초기화가 필요한지 확인한다.
         */
        if(isGeneralMasternodeResetBlock(blockNumber, constants.getMASTERNODE_PERIOD())) {
            removeMasternode(constants.getMASTERNODE_EARLY_GENERAL(), blockNumber);
        } else if(isMajorMasternodeResetBlock(blockNumber, constants.getMASTERNODE_PERIOD())) {
            removeMasternode(constants.getMASTERNODE_EARLY_MAJOR(), blockNumber);
        } else if(isPrivateMasternodeResetBlock(blockNumber, constants.getMASTERNODE_PERIOD())) {
            removeMasternode(constants.getMASTERNODE_EARLY_PRIVATE(), blockNumber);
        }


        long countGeneral = 0;
        long countMajor = 0;
        long countPrivate = 0;

        List<byte[]> mnFinishGeneralList = new ArrayList<>();
        List<byte[]> mnFinishMajorList = new ArrayList<>();
        List<byte[]> mnFinishPrivateList = new ArrayList<>();

        List<byte[]> mnExpiredList = new ArrayList<>();

        byte[] prevMn = constants.getMASTERNODE_STORAGE();
        for(long i = 0; i < constants.getMASTERNODE_LIMIT_TOTAL(); i++) {
            byte[] mn = getOrCreateAccountState(prevMn).getMnNextNode();

            if (mn == null) {
                break;
            }

            AccountState mnState = getAccountState(mn);

            if(mnState.getMnStartBalance().equals(constants.getMASTERNODE_BALANCE_GENERAL())) {
                // 리스프레시 기간이 지난 마스터노드를 걸러낸다.
                if(mnState.getMnStartBlock().longValue() < blockNumber - constants.getMASTERNODE_REWARD_PERIOD()) {
                    mnFinishGeneralList.add(mn);
                }
                countGeneral += 1;
            }
            else if(mnState.getMnStartBalance().equals(constants.getMASTERNODE_BALANCE_MAJOR())) {
                if(mnState.getMnStartBlock().longValue() < blockNumber - constants.getMASTERNODE_REWARD_PERIOD()) {
                    mnFinishMajorList.add(mn);
                }
                countMajor += 1;
            }
            else if(mnState.getMnStartBalance().equals(constants.getMASTERNODE_BALANCE_PRIVATE())) {
                if(mnState.getMnStartBlock().longValue() < blockNumber - constants.getMASTERNODE_REWARD_PERIOD()) {
                    mnFinishPrivateList.add(mn);
                }
                countPrivate += 1;
            }

            // 너무 오랫동안 업데이트되지 않았을 경우 종료시킨다.
            if(mnState.getMnLastBlock().longValue() < blockNumber - 10_000) {
                mnExpiredList.add(mn);
            }
            // 잔고가 마스터노드 시작 잔고보다 작아지면 종료시킨다
            else if(mnState.getBalance().compareTo(mnState.getMnStartBalance()) < 0) {
                mnExpiredList.add(mn);
            }

            prevMn = mn;
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

    /**
     * 입력된 blockNumber에서 General 등급의 마스터노드 참가 얼리버드가 시작되는 블록인지 확인한다.
     *
     * @param blockNumber 확인하려는 블록 번호
     * @param resetPeriod 마스터노드 리셋 주기 (약 3달)
     * @return TRUE : 리셋 블록에 해당하는 경우
     */
    private boolean isGeneralMasternodeResetBlock(long blockNumber, long resetPeriod) {
        return (blockNumber % resetPeriod == 0);
    }

    private boolean isMajorMasternodeResetBlock(long blockNumber, long resetPeriod) {
        long offset = resetPeriod/3;
        return blockNumber >= offset && ((blockNumber + offset) % resetPeriod == 0);
    }

    private boolean isPrivateMasternodeResetBlock(long blockNumber, long resetPeriod) {
        long offset = resetPeriod*2/3;
        return blockNumber >= offset && ((blockNumber + offset) % resetPeriod == 0);
    }


    private void finishMasterNodes(List<byte[]> finishedList, long blockNumber) {
        for(byte[] mnFinished : finishedList) {
            finishMasterNode(mnFinished, blockNumber);
        }
    }


    @Override
    public void finishMasterNode(byte[] finished, long blockNumber) {
        Constants constants = config.getBlockchainConfig().getCommonConstants();

        byte[] prevMn = constants.getMASTERNODE_STORAGE();
        for(long i = 0; i < constants.getMASTERNODE_LIMIT_TOTAL(); i++) {
            AccountState prevState = getAccountState(prevMn);
            byte[] mn = prevState.getMnNextNode();

            if(mn == null) {
                break;
            }

            if(FastByteComparisons.equal(mn, finished)) {
                removeMasternode(mn, blockNumber);
            }

            prevMn = mn;
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
