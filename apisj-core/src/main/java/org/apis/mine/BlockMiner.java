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
 * Modified by APIS. 2018
 * Daniel
 */
package org.apis.mine;

import org.apis.config.Constants;
import org.apis.config.SystemProperties;
import org.apis.contract.ContractLoader;
import org.apis.core.*;
import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;
import org.apis.db.BlockStore;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumImpl;
import org.apis.listener.CompositeEthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Manages embedded CPU mining and allows to use external miners.
 *
 * Created by Anton Nashatyrev on 10.12.2015.
 */
@Component
public class BlockMiner {
    private static final Logger logger = LoggerFactory.getLogger("mine");

    private Blockchain blockchain;

    private BlockStore blockStore;

    @Autowired
    private Ethereum ethereum;

    protected PendingState pendingState;

    private SystemProperties config;

    private List<MinerListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * 블록을 생성할 때, 트랜잭션의 gasPrice가 이 값보다 작으면 포함시키지 않는다.
     * When creating a block, do not include it if the transaction's gasPrice is less than this value.
     */
    private BigInteger minGasPrice;

    private Block miningBlock;

    private byte[] lastMinedParentBlockHash = null;


    private Block lastConnectedBlock = null;

    /**
     * Set to true if block creation is in progress.
     */
    private static boolean isGeneratingBlock = false;

    private boolean isSyncDone = false;

    @Autowired
    public BlockMiner(final SystemProperties config, final CompositeEthereumListener listener, final Blockchain blockchain, final BlockStore blockStore, final PendingState pendingState) {
        this.config = config;
        this.blockchain = blockchain;
        this.blockStore = blockStore;
        this.pendingState = pendingState;
        minGasPrice = config.getMineMinGasPrice();      // default : 50GAPIS

        listener.addListener(new EthereumListenerAdapter() {
            @Override
            public void onSyncDone(SyncState state) {
                isSyncDone = true;
            }
        });

        // 싱크가 꺼져있으면 바로 채굴을 시작시킨다.. 싱크가 켜져있으면 싱크 완료 후 채굴 시작된다.
        if (!config.isSyncEnabled()) {
            isSyncDone = true;
        }

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                updateMinedBlocks();
                checkMiningReady();
                checkMasterNode();
            }
            catch (Error | Exception e) {
                logger.error("Unhandled exception", e);
            }

        }, 30, 1, TimeUnit.SECONDS);
    }


    /**
     * Minimum gas price that can be included in the mining block
     * @param minGasPrice atto gas
     */
    public void setMinGasPrice(BigInteger minGasPrice) {
        this.minGasPrice = minGasPrice;
    }

    private void updateMinedBlocks() {
        long now = TimeUtils.getRealTimestamp();

        MinedBlockCache cache = MinedBlockCache.getInstance();
        List<Block> receivedBlocks = cache.getBestMinedBlocks();

        for (Block block : receivedBlocks) {
            if(block != null && now - block.getTimestamp()*1000L > 10_000L) {
                if(blockStore.getBlockByHash(block.getHash()) == null) {
                    if(isSyncDone) {
                        ((EthereumImpl) ethereum).addNewMinedBlock(block);
                    }
                    else {
                        blockchain.tryToConnect(block);
                    }
                }
                lastConnectedBlock = block;
            }
        }
    }



    private List<Transaction> getAllPendingTransactions() {
        PendingStateImpl.TransactionSortedSet ret = new PendingStateImpl.TransactionSortedSet();
        ret.addAll(pendingState.getPendingTransactions());

        Iterator<Transaction> it = ret.iterator();

        while(it.hasNext()) {
            Transaction tx = it.next();

            // 최소 요구 가스값보다 낮으면 포함되지 않는다
            if (!isAcceptableTx(tx)) {
                logger.debug("Miner excluded the transaction: {}", tx);
                it.remove();
            }
        }
        return new ArrayList<>(ret);
    }



    /** Last block number that checked the master node status */
    private long lastMnCheckedBlock = 0;

    /** Last block number that updated the master node status */
    private long lastMnUpdatedBlock = 0;

    /**
     * Continuously check the status of the account registered as the master node in the configuration.
     */
    private synchronized void checkMasterNode() {
        ECKey mnKey = config.getMasternodeKey();
        if(mnKey == null) {
            // You do not need to run this function because the master node account does not exist.
            return;
        }

        // It prevents checking multiple times in the same block number.
        Block bestBlock = blockchain.getBestBlock();
        if(bestBlock.getNumber() - lastMnCheckedBlock == 0) {
            return;
        }
        long blockNumber = bestBlock.getNumber();
        lastMnCheckedBlock = blockNumber;


        // Get the state of the master node.
        Repository mnRepo = ((Repository)ethereum.getRepository()).getSnapshotTo(bestBlock.getStateRoot());
        AccountState mnState = mnRepo.getAccountState(mnKey.getAddress());

        /*
         * 마스터노드는 2단계 인증이 되어 있어야만 등록이 가능하다.
         * Registration is possible only if 2-step authorization is applied to the address.
         */
        if(mnState.getProofKey() == null || FastByteComparisons.equal(mnState.getProofKey(), HashUtil.EMPTY_DATA_HASH)) {
            return;
        }

        /*
         * 잔고가 마스터노드 기준 금액과 동일해야한다.
         * The balance should be equal to the master node reference amount.
         */
        Constants constants = config.getBlockchainConfig().getConfigForBlock(bestBlock.getNumber()).getConstants();
        BigInteger mnBalance = mnRepo.getBalance(mnKey.getAddress());
        if (constants.getMASTERNODE_LIMIT(mnBalance) == 0) {
            return;
        }

        /*
         * 상태 값의 MnStartBalance 값이 0일 경우, 마스터노드가 아니라는 뜻이다.
         * If the MnStartBalance value of the AccountStatus  is 0, it means that this address is not masternode.
         */
        if(mnState.getMnStartBalance().compareTo(BigInteger.ZERO) == 0) {
            sendMasternodeTransaction(mnKey, blockNumber);

        }
        /*
         * 현재 마스터노드가 업데이트 대상 목록에 존재하는지 확인하고 존재한다면 업데이트를 실시한다.
         * Check whether the current masternode exists in the updating target list, and update if it exists.
         */
        else {
            List<byte[]> updatingList = mnRepo.getUpdatingMnList(blockNumber);
            for(byte[] mn : updatingList) {
                if(FastByteComparisons.equal(mn, mnKey.getAddress())) {
                    updateMano(mnKey, blockNumber);
                    break;
                }
            }
        }
    }

    /**
     * 마스터노드를 등록하거나 상태를 업데이트하는 트랜잭션을 전송한다.
     * @param mnKey 마스터노드의 계정
     * @param bestNumber best block
     */
    private void sendMasternodeTransaction(ECKey mnKey, long bestNumber) {

        BigInteger nonce = ethereum.getRepository().getNonce(mnKey.getAddress());
        BigInteger gasPrice = config.getMasternodeGasPrice();
        BigInteger gasLimit = BigInteger.valueOf(220_000L);
        long value = 0L;

        BigInteger nodeMineral = ethereum.getRepository().getMineral(mnKey.getAddress(), bestNumber);
        if(nodeMineral.compareTo(gasPrice.multiply(gasLimit)) < 0) {
            // Do not run if you do not have enough minerals to transfer the transaction.
            // Insufficient minerals will consume APIS to transfer transactions.
            // Therefore, if the balance is changed, it can not be registered as a master node.
            return;
        }

        Transaction tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                ByteUtil.longToBytesNoLeadZeroes(gasPrice.longValue()),
                ByteUtil.longToBytesNoLeadZeroes(gasLimit.longValue()),
                mnKey.getAddress(),
                ByteUtil.longToBytesNoLeadZeroes(value),
                config.getMasternodeRecipient(),
                ethereum.getChainIdForNextBlock());
        tx.sign(mnKey);

        logger.debug(ConsoleUtil.colorBBlue("Submit Masternode Update TX : {}"), tx.toString());

        ethereum.submitTransaction(tx);
        lastMnUpdatedBlock = bestNumber;
    }

    private void updateMano(ECKey mnKey, long bestNumber) {
        long limitTotalMasternode = config.getBlockchainConfig().getConfigForBlock(bestNumber).getConstants().getMASTERNODE_LIMIT_TOTAL();

        if(lastMnUpdatedBlock > 0 && bestNumber - lastMnUpdatedBlock < limitTotalMasternode) {
            // Prevent duplicate execution
            return;
        }

        sendMasternodeTransaction(mnKey, bestNumber);
    }


    private int countMiningCheck = 0;
    private long loggedBlockNumber = 0;
    private long nothingToMining = 0;
    /**
     * 매 9초로 끝나는 시간마다 블록을 생성할 준비가 되었는지(RP 값이 적당한지) 확인한다.
     */
    private synchronized void checkMiningReady() {
        // Check whether the mining function is activated.
        // Coinbase key must be set because the block must include the miner's signature.
        if(!config.minerStart() || config.getCoinbaseKey() == null || config.getMinerCoinbase() == null) {
            return;
        }


        final long now = TimeUtils.getRealTimestamp();

        Block bestBlock = blockchain.getBestBlock();
        Block bestPendingBlock = ((PendingStateImpl) pendingState).getBestBlock();
        if(bestBlock == null || bestPendingBlock == null) {
            logger.debug("The parent block is not prepared.");
            return;
        }

        if(bestBlock.isGenesis()) {
            if(!isGeneratingBlock) {
                isSyncDone = true;
                restartMining();
            }
            return;
        }

        // 마스터노드는 채굴이 불가능하다.
        try {
            if (((Repository) ethereum.getRepository()).getSnapshotTo(bestBlock.getStateRoot()).getAccountState(config.getMinerCoinbase()).getMnStartBlock().compareTo(BigInteger.ZERO) > 0) {
                printMiningMessage("The master node can not perform mining.", bestBlock.getNumber());
                return;
            }
        } catch (Exception e) { return; }


        // 직전 n블록 내에 내 블록이 존재할 경우 블록을 생성하지 않도록 한다.
        if(bestBlock.getNumber() > 100) {
            long blockMiningBreak = config.getBlockchainConfig().getConfigForBlock(bestBlock.getNumber()).getConstants().getBLOCK_MINING_BREAK();
            Block parentBlock = blockchain.getBlockByHash(bestBlock.getHash());
            for (int i = 0; i < blockMiningBreak; i++) {
                if (FastByteComparisons.equal(parentBlock.getCoinbase(), config.getMinerCoinbase())) {
                    printMiningMessage("If there is a block created by coinbase within " + blockMiningBreak + " blocks, omit mining.", bestBlock.getNumber());
                    return;
                }
                parentBlock = blockchain.getBlockByHash(parentBlock.getParentHash());
            }
        }


        long diff = now/1_000L - bestBlock.getTimestamp();
        if(isSyncDone && diff > 20 && diff % 10 == 1) {
            if(!isGeneratingBlock) {
                restartMining();
            }
            return;
        }

        // Blocks can only be created if synchronization is complete.
        if(!isSyncDone) {
            printMiningMessage("Syncing is not complete yet.", bestBlock.getNumber());
            return;
        }

        // If miner's balance is zero, mining will not proceed.
        if(ethereum.getRepository().getBalance(config.getMinerCoinbase()).compareTo(BigInteger.ONE) < 0) {
            printMiningMessage("Miner's balance is zero.", bestBlock.getNumber());
            return;
        }


        if(now - bestPendingBlock.getTimestamp()*1000L < 10_000L) {
            printMiningMessage("It is too early to create a block.", bestBlock.getNumber());
            return;
        }

        // 이미 같은 부모를 이용해서 블록을 만들었으면, 더 블록을 생성하지 않는다
        if(lastConnectedBlock != null && lastMinedParentBlockHash != null && FastByteComparisons.equal(lastConnectedBlock.getHash(), lastMinedParentBlockHash)) {
            countMiningCheck += 1;

            if(countMiningCheck > 10 && countMiningCheck%10 == 2) {
                ethereum.submitMinedBlock(MinedBlockCache.getInstance().getBestMinedBlocks());
            }

            printMiningMessage("You have already created a block.", bestBlock.getNumber());
            return;
        }


        // 다른 네트워크에서 전달받은 블록들과 높이 차이가 크면 블록을 생성하면 안된다.
        if(bestBlock.getNumber() < MinedBlockCache.getInstance().getBestBlockNumber() - 2) {
            printMiningMessage("Synchronization is slow.", bestBlock.getNumber());
            return;
        }

        if(!isGeneratingBlock) {
            countMiningCheck = 0;
            restartMining();
            return;
        }

        System.out.println("Nothing to mining");
        nothingToMining += 1;
        if(nothingToMining > 5) {
            isGeneratingBlock = false;
        }
    }


    private void printMiningMessage(String msg, long blockNumber) {
        if(loggedBlockNumber < blockNumber) {
            ConsoleUtil.printlnPurple(msg);
            loggedBlockNumber = blockNumber;
        }
    }



    /**
     * 트랜잭션의 수수료(Gas price)가 채굴자의 요구 조건 이상인 경우에만 블록에 기록될 수 있다.
     * @param tx Transactions to be verified
     * @return TRUE : acceptable
     */
    private boolean isAcceptableTx(Transaction tx) {
        return minGasPrice.compareTo(new BigInteger(1, tx.getGasPrice())) <= 0;
    }

    private void cancelCurrentBlock() {
         if (miningBlock != null) {
            broadcastBlockCancelled(miningBlock);
            logger.debug("Tainted block mining cancelled: {}", miningBlock.getShortDescr());
            miningBlock = null;
        }
    }


    private synchronized Block getNewBlockForMining() {
        Block bestBlockchain = blockchain.getBestBlock();
        Block bestPendingState = ((PendingStateImpl) pendingState).getBestBlock();

        logger.debug("getNewBlockForMining best blocks: PendingState: " + bestPendingState.getShortDescr() + ", Blockchain: " + bestBlockchain.getShortDescr());

        List<Transaction> pendingTransactions = new ArrayList<>();
        if(TimeUtils.getRealTimestamp()/1000L - bestPendingState.getTimestamp() < 20) {
            pendingTransactions = getAllPendingTransactions();
        }

        return blockchain.createNewBlock(bestPendingState, pendingTransactions);
    }


    private synchronized void restartMining() {
        isGeneratingBlock = true;
        cancelCurrentBlock();

        // 부모가 genesis일 경우, 컨트렉트들을 생성한다.
        if(blockchain.getBestBlock().isGenesis()) {
            ContractLoader.initFoundationContracts(ethereum);
        } else if(blockchain.getBestBlock().getNumber() == 2) {
            ContractLoader.initAddressMaskingContracts(ethereum);
        } else if(blockchain.getBestBlock().getNumber() == 3) {
            ContractLoader.initBuyMineralContract(ethereum);
        } else if(blockchain.getBestBlock().getNumber() == 4) {
            ContractLoader.initEarlyBirdManagerContracts(ethereum);
        }

        if(blockchain.getBestBlock().getNumber() > 100 && ethereum.getChannelManager().getActivePeers().isEmpty()) {
            isGeneratingBlock = false;
            return;
        }


        miningBlock = getNewBlockForMining();

        if(miningBlock == null) {
            isGeneratingBlock = false;
            return;
        }

        lastMinedParentBlockHash = miningBlock.getParentHash();

        Block newMiningBlock = new Block(miningBlock.getEncoded());

        broadcastBlockStarted(newMiningBlock);
        logger.debug("New block mining started: {}", newMiningBlock.getShortHash());

        blockMined(newMiningBlock);
    }


    private synchronized void blockMined(Block newBlock) {
        broadcastBlockMined(newBlock);
        logger.info("Wow, block mined !!!: {}", newBlock.getShortDescr());

        miningBlock = null;
        long now = TimeUtils.getRealTimestamp()/1_000L;


        // 제네시스 블록이면 바로 DB에 저장한다.
        if(newBlock.getNumber() <= 1 && blockchain.getBlockByHash(newBlock.getParentHash()).isGenesis()) {
            logger.debug("Importing newly mined block {} {} ...", newBlock.getShortHash(), newBlock.getNumber());
            ImportResult importResult = ((EthereumImpl) ethereum).addNewMinedBlock(newBlock);
            logger.debug("Mined block import result is " + importResult);
        }

        /*
         * 일정 블록 수 이상에서, 무분별한 블록 생성으로 인한 네트워크 부하를 감소시키기 위해
         * 채굴자가 생성한 블록의 RP 값이, 직전 100개의 블록 중 가장 작은 RP 값의 0.1%보다 작을 경우
         * 다른 노드들에게 전파하지 않는다.
         * 만약 5초 이내(블록타임의 1/2)에 새로운 블록이 전달되지 않는다면 5초 이후에 즉시 전파한다.
         */
        if(newBlock.getNumber() > 100) {
            Block parentBlock = blockStore.getBlockByHash(newBlock.getParentHash());
            BigInteger minRp = parentBlock.getRewardPoint();

            for(int i = 0; i < 99; i++) {
                parentBlock = blockStore.getBlockByHash(parentBlock.getParentHash());

                if(minRp.compareTo(parentBlock.getRewardPoint()) > 0) {
                    minRp = parentBlock.getRewardPoint();
                }
            }

            // omitRp 보다 작은 RP 값으로 블럭이 생성되었으면, 전파하지 않는다
            BigInteger omitRp = minRp.divide(BigInteger.valueOf(1000L));

            parentBlock = blockStore.getBlockByHash(newBlock.getParentHash());
            if(newBlock.getRewardPoint().compareTo(omitRp) < 0 && newBlock.getTimestamp() - parentBlock.getTimestamp() < 20) {
                logger.info("Avoid propagation because the RP value of the newly created block is small.");

                isGeneratingBlock = false;
                return;
            }
        }

        List<Block> minedBlocks = new ArrayList<>();
        minedBlocks.add(newBlock);
        Block parentBlock = newBlock;
        for(int i = 0; i < 4 && parentBlock.getNumber() > 1; i++) {
            parentBlock = blockchain.getBlockByHash(parentBlock.getParentHash());
            minedBlocks.add(0, parentBlock);
        }

        // 새로운 정보가 더 좋을 경우, 블록을 전파한다.
        if(MinedBlockCache.getInstance().compareMinedBlocks(minedBlocks)) {
            Block parent = blockStore.getBlockByHash(newBlock.getParentHash());
            if(now - parent.getTimestamp() > 23) {
                ((EthereumImpl) ethereum).addNewMinedBlock(newBlock);
            }

            ethereum.submitMinedBlock(minedBlocks);
        }

        isGeneratingBlock = false;
    }



    /*****  Listener boilerplate  ******/

    public void addListener(MinerListener l) {
        listeners.add(l);
    }

    /*public void removeListener(MinerListener l) {
        listeners.remove(l);
    }

    private void broadcastMinerStarted() {
        for (MinerListener l : listeners) {
            l.miningStarted();
        }
    }
    private void broadcastMinerStopped() {
        for (MinerListener l : listeners) {
            l.miningStopped();
        }
    }*/
    private void broadcastBlockStarted(Block b) {
        for (MinerListener l : listeners) {
            l.blockMiningStarted(b);
        }
    }
    private void broadcastBlockCancelled(Block b) {
        for (MinerListener l : listeners) {
            l.blockMiningCanceled(b);
        }
    }
    private void broadcastBlockMined(Block b) {
        for (MinerListener l : listeners) {
            l.blockMined(b);
        }
    }


    /**
     * Converts a long value into a byte array.
     *
     * @param val - long value to convert
     * @return <code>byte[]</code> of length 8, representing the long value
     */
    public static byte[] longToBytes(long val) {
        return ByteBuffer.allocate(Long.BYTES).putLong(val).array();
    }

}
