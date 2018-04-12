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
package org.apis.mine;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.collections4.CollectionUtils;
import org.apis.core.*;
import org.apis.db.BlockStore;
import org.apis.db.ByteArrayWrapper;
import org.apis.db.IndexedBlockStore;
import org.apis.config.SystemProperties;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumImpl;
import org.apis.listener.CompositeEthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.Math.max;

/**
 * Manages embedded CPU mining and allows to use external miners.
 *
 * Created by Anton Nashatyrev on 10.12.2015.
 */
@Component
public class BlockMiner {
    private static final Logger logger = LoggerFactory.getLogger("mine");

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    private Blockchain blockchain;

    private BlockStore blockStore;

    @Autowired
    private Ethereum ethereum;

    protected PendingState pendingState;

    private CompositeEthereumListener listener;

    private SystemProperties config;

    private List<MinerListener> listeners = new CopyOnWriteArrayList<>();

    private BigInteger minGasPrice;
    private long minBlockTimeout;
    private int cpuThreads;
    private boolean fullMining = true;

    private volatile boolean isLocalMining;
    private Block miningBlock;
    private volatile MinerIfc externalMiner;

    private final Queue<ListenableFuture<MinerIfc.MiningResult>> currentMiningTasks = new ConcurrentLinkedQueue<>();
    private long lastBlockMinedTime;


    @Autowired
    public BlockMiner(final SystemProperties config, final CompositeEthereumListener listener, final Blockchain blockchain, final BlockStore blockStore, final PendingState pendingState) {
        this.listener = listener;
        this.config = config;
        this.blockchain = blockchain;
        this.blockStore = blockStore;
        this.pendingState = pendingState;
        minGasPrice = config.getMineMinGasPrice();      // ethereumj default : 15Gwei
        minBlockTimeout = config.getMineMinBlockTimeoutMsec();
        cpuThreads = config.getMineCpuThreads();
        fullMining = config.isMineFullDataset();
        listener.addListener(new EthereumListenerAdapter() {
            @Override
            public void onPendingStateChanged(PendingState pendingState) {
                BlockMiner.this.onPendingStateChanged();
            }

            @Override
            public void onSyncDone(SyncState state) {
                if (config.minerStart() && config.isSyncEnabled()) {
                    logger.info("Sync complete, start mining...");
                    startMining();
                }
            }
        });

        if (config.minerStart() && !config.isSyncEnabled()) {
            logger.info("Sync disabled, start mining now...");
            startMining();
        }
    }

    public void setFullMining(boolean fullMining) {
        this.fullMining = fullMining;
    }

    public void setCpuThreads(int cpuThreads) {
        this.cpuThreads = cpuThreads;
    }

    public void setMinGasPrice(BigInteger minGasPrice) {
        this.minGasPrice = minGasPrice;
    }

    public void setExternalMiner(MinerIfc miner) {
        externalMiner = miner;
        restartMining();
    }

    public void startMining() {
        isLocalMining = true;
        broadcastMinerStarted();
        logger.info("Miner started");
        restartMining();
    }

    public void stopMining() {
        isLocalMining = false;
        cancelCurrentBlock();
        broadcastMinerStopped();
        logger.info("Miner stopped");
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

    private void onPendingStateChanged() {
        if (!isLocalMining && externalMiner == null) return;

        logger.debug("onPendingStateChanged()");
        if (miningBlock == null) {
            restartMining();
        } else if (miningBlock.getNumber() <= ((PendingStateImpl) pendingState).getBestBlock().getNumber()) {
            logger.debug("Restart mining: new best block: " + blockchain.getBestBlock().getShortDescr());
            restartMining();
        } else if (!CollectionUtils.isEqualCollection(miningBlock.getTransactionsList(), getAllPendingTransactions())) {
            logger.debug("Restart mining: pending transactions changed");
            restartMining();
        } else {
            if (logger.isDebugEnabled()) {
                StringBuilder s = new StringBuilder("onPendingStateChanged() event, but pending Txs the same as in currently mining block: ");
                for (Transaction tx : getAllPendingTransactions()) {
                    s.append("\n    ").append(tx);
                }
                logger.debug(s.toString());
            }
        }
    }

    private boolean isAcceptableTx(Transaction tx) {
        return minGasPrice.compareTo(new BigInteger(1, tx.getGasPrice())) <= 0;
    }

    private synchronized void cancelCurrentBlock() {
        for (ListenableFuture<MinerIfc.MiningResult> task : currentMiningTasks) {
            if (task != null && !task.isCancelled()) {
                task.cancel(true);
            }
        }
        currentMiningTasks.clear();

        if (miningBlock != null) {
            broadcastBlockCancelled(miningBlock);
            logger.debug("Tainted block mining cancelled: {}", miningBlock.getShortDescr());
            miningBlock = null;
        }
    }


    private Block getNewBlockForMining() {
        Block bestBlockchain = blockchain.getBestBlock();
        Block bestPendingState = ((PendingStateImpl) pendingState).getBestBlock();

        logger.debug("getNewBlockForMining best blocks: PendingState: " + bestPendingState.getShortDescr() + ", Blockchain: " + bestBlockchain.getShortDescr());

        return blockchain.createNewBlock(bestPendingState, getAllPendingTransactions());
    }

    private void restartMining() {
        Block newMiningBlock = getNewBlockForMining();

        synchronized(this) {
            cancelCurrentBlock();
            miningBlock = newMiningBlock;

            if (externalMiner != null) {
                currentMiningTasks.add(externalMiner.mine(cloneBlock(miningBlock)));
            }
            if (isLocalMining) {
                MinerIfc localMiner = config.getBlockchainConfig()
                        .getConfigForBlock(miningBlock.getNumber())
                        .getMineAlgorithm(config);
                currentMiningTasks.add(localMiner.mine(cloneBlock(miningBlock)));
            }

            for (final ListenableFuture<MinerIfc.MiningResult> task : currentMiningTasks) {
                task.addListener(() -> {
                    try {
                        // wow, block mined!
                        final Block minedBlock = task.get().block;
                        blockMined(minedBlock);
                    } catch (InterruptedException | CancellationException e) {
                        // OK, we've been cancelled, just exit
                    } catch (Exception e) {
                        logger.warn("Exception during mining: ", e);
                    }
                }, MoreExecutors.sameThreadExecutor());
            }
        }
        broadcastBlockStarted(newMiningBlock);
        logger.debug("New block mining started: {}", newMiningBlock.getShortHash());
    }

    /**
     * Block cloning is required before passing block to concurrent miner env.
     * In success result miner will modify this block instance.
     */
    private Block cloneBlock(Block block) {
        return new Block(block.getEncoded());
    }

    protected void blockMined(Block newBlock) throws InterruptedException {
        long t = System.currentTimeMillis();
        if (t - lastBlockMinedTime < minBlockTimeout) {
            long sleepTime = minBlockTimeout - (t - lastBlockMinedTime);
            logger.debug("Last block was mined " + (t - lastBlockMinedTime) + " ms ago. Sleeping " +
                    sleepTime + " ms before importing...");
            Thread.sleep(sleepTime);
        }

        broadcastBlockMined(newBlock);
        logger.info("Wow, block mined !!!: {}", newBlock.toString());

        lastBlockMinedTime = t;
        miningBlock = null;
        // cancel all tasks
        cancelCurrentBlock();

        // broadcast the block
        logger.debug("Importing newly mined block {} {} ...", newBlock.getShortHash(), newBlock.getNumber());
        ImportResult importResult = ((EthereumImpl) ethereum).addNewMinedBlock(newBlock);
        logger.debug("Mined block import result is " + importResult);
    }

    public boolean isMining() {
        return isLocalMining || externalMiner != null;
    }

    /*****  Listener boilerplate  ******/

    public void addListener(MinerListener l) {
        listeners.add(l);
    }

    public void removeListener(MinerListener l) {
        listeners.remove(l);
    }

    protected void broadcastMinerStarted() {
        for (MinerListener l : listeners) {
            l.miningStarted();
        }
    }
    protected void broadcastMinerStopped() {
        for (MinerListener l : listeners) {
            l.miningStopped();
        }
    }
    protected void broadcastBlockStarted(Block b) {
        for (MinerListener l : listeners) {
            l.blockMiningStarted(b);
        }
    }
    protected void broadcastBlockCancelled(Block b) {
        for (MinerListener l : listeners) {
            l.blockMiningCanceled(b);
        }
    }
    protected void broadcastBlockMined(Block b) {
        for (MinerListener l : listeners) {
            l.blockMined(b);
        }
    }
}
