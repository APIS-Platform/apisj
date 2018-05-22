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

import ch.qos.logback.core.util.TimeUtil;
import org.apis.config.BlockchainConfig;
import org.apis.config.SystemProperties;
import org.apis.core.*;
import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;
import org.apis.db.BlockStore;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumImpl;
import org.apis.listener.CompositeEthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.net.server.Channel;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.RewardPointUtil;
import org.apis.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages embedded CPU mining and allows to use external miners.
 *
 * Created by Anton Nashatyrev on 10.12.2015.
 */
@Component
public class BlockMiner {
    private static final Logger logger = LoggerFactory.getLogger("mine");

    //private static ExecutorService executor = Executors.newSingleThreadExecutor();

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

    private volatile boolean isLocalMining;
    private Block miningBlock;


    /**
     * Set to true if block creation is in progress.
     */
    private static boolean isGeneratingBlock = false;


    private Timer timerCheckMining = new Timer();
    private Timer timerSubmitRP = new Timer();

    @Autowired
    public BlockMiner(final SystemProperties config, final CompositeEthereumListener listener, final Blockchain blockchain, final BlockStore blockStore, final PendingState pendingState) {
        this.listener = listener;
        this.config = config;
        this.blockchain = blockchain;
        this.blockStore = blockStore;
        this.pendingState = pendingState;
        minGasPrice = config.getMineMinGasPrice();      // ethereumj default : 15Gwei
        minBlockTimeout = config.getMineMinBlockTimeoutMsec();

        listener.addListener(new EthereumListenerAdapter() {

            @Override
            public void onSyncDone(SyncState state) {
                if (config.minerStart() && config.isSyncEnabled()) {

                    // 싱크가 되자마자 채굴을 시작하면 의미 없는 블록이 생성될 수 있다.
                    logger.info("Sync complete, start mining...");
                    startMining();
                }
            }

            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {

                // 새로 받은 블록이 네트워크의 가장 최신 블록과 일치할 경우, 채굴을 시작한다.
                if(block.getNumber() == ethereum.getSyncStatus().getBlockBestKnown() && !isMining()) {
                    startMining();
                }

                List<RewardPoint> rpList = new ArrayList<>();
                rpList.add(getMinerRP(blockStore.getBestBlock().getHash()));
                ethereum.submitRewardPoints(rpList);
            }
        });

        // 현재 시간을 NTP 서버에서 불러오도록 한다.
        // 이후에는 getRealTimestamp() 함수를 호출하면 된다.
        TimeUtils.getNtpTimestamp();


        // 싱크가 꺼져있으면 바로 채굴을 시작시킨다.. 싱크가 켜져있으면 싱크 완료 후 채굴 시작된다.
        if (config.minerStart() && !config.isSyncEnabled()) {
            logger.info("Sync disabled, start mining now...");
            startMining();
        }


        // 지속적으로 RP 값을 싱크한다
        timerSubmitRP = new Timer();
        timerSubmitRP.schedule(taskSyncRP, 10*1000L, 1000L);
    }

    /**
     * Minimum gas price that can be included in the mining block
     * @param minGasPrice atto gas
     */
    public void setMinGasPrice(BigInteger minGasPrice) {
        this.minGasPrice = minGasPrice;
    }


    public void startMining() {
        if(isLocalMining) {
            return;
        }

        logger.info("Miner started");
        broadcastMinerStarted();
        isLocalMining = true;

        timerCheckMining = new Timer();
        timerCheckMining.schedule(taskCheckMining, 0L, 100L);
    }

    public void stopMining() {
        isLocalMining = false;
        cancelCurrentBlock();

        //miningReadyCheckService.shutdown();
        timerCheckMining.cancel();
        timerCheckMining.purge();

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


    private TimerTask taskCheckMining = new TimerTask() {
        @Override
        public void run() {
            checkMiningReady();
        }
    };

    private boolean isInitSync = false;
    private long timeSyncDiff = 0;
    private boolean isCloseSync = false;
    private boolean isStartSync = false;

    private TimerTask taskSyncRP = new TimerTask() {
        @Override
        public void run() {
            RewardPointCache rpCache = RewardPointCache.getInstance();

            long bestNumber = blockStore.getBestBlock().getNumber();
            long worldNumber = ethereum.getSyncStatus().getBlockBestKnown();

            if(bestNumber == worldNumber) {
                if(rpCache.sizeOfRewardPoint(bestNumber) < 5) {
                    List<RewardPoint> rpList = new ArrayList<>();
                    rpList.add(getMinerRP(blockStore.getBestBlock().getHash()));
                    rpCache.insertUpdate(rpList, blockStore);

                    ethereum.submitRewardPoints(rpCache.getRewardPointList(worldNumber));
                }

                timeSyncDiff = 0;
                isInitSync = false;
                isCloseSync = false;
                isStartSync = false;
            }

            // 블록을 제대로 받지 못했을 경우 싱크를 다시 설정
            else if(bestNumber < worldNumber && !isInitSync) {
                ethereum.initSyncing();
                isInitSync = true;
            }

            if(bestNumber < worldNumber && timeSyncDiff == 0) {
                timeSyncDiff = TimeUtils.getRealTimestamp();
            }

            if(TimeUtils.getRealTimestamp() - timeSyncDiff > 2000 && !isCloseSync) {
                ((EthereumImpl)ethereum).getSyncManager().close();
                isCloseSync = true;
            }

            if(TimeUtils.getRealTimestamp() - timeSyncDiff > 4000 && isCloseSync && !isStartSync) {
                ethereum.initSyncing();
                isStartSync = true;
            }


            // 3개 이상의 노드에서 정보를 받았고, 제네시스 블록이고, 채굴이 시작되지 않았으면
            // 채굴을 시작한다.
            if(rpCache.sizeOfRewardPoint(blockStore.getBestBlock().getNumber()) > 0/*2*/ /*&& blockStore.getBestBlock().isGenesis()*/ && !isMining()) {
                startMining();
            }
        }
    };



    /**
     * 매 9초로 끝나는 시간마다 블록을 생성할 준비가 되었는지(RP 값이 적당한지) 확인한다.
     */
    private synchronized void checkMiningReady() {
        Block bestBlock = ((PendingStateImpl) pendingState).getBestBlock();
        long bestNumber = bestBlock.getNumber();

        Block blockStoreBest = blockStore.getBestBlock();

        // 채굴이 꺼져있으면 블록을 생성하지 않는다
        if(!isMining()) {
            return;
        }

        final long now = TimeUtils.getRealTimestamp();

        // 채굴을 시작하는 시간.. 이전 블록 생성 시간 + 10초
        final long miningStartTime =  (bestBlock.getTimestamp() + 10)*1000;

        // 새 블록 생성 시작 시간이 되지 않았으면 스킵
        if(now < miningStartTime) {
            return;
        }


        RewardPointCache rpCache = RewardPointCache.getInstance();

        // 네트워크에서 전달받은 RewardPoint의 parent block number 값이 best block 보다 크면, 채굴을 멈추고 동기회를 진행해야한다.
        if(ethereum.getSyncStatus().getBlockBestKnown() > bestNumber) {
            //stopMining();
            return;
        }


        // 네트워크에서 전달받은 RP 값이 3개 이상이어야만 채굴을 시작하도록 한다.
        if(rpCache.sizeOfRewardPoint(bestNumber) < 2) {
            return;
        }



        // RP 값이 최상위가 아니면 생성하지 않는다
        RewardPoint cacheRP = rpCache.getRewardPoint(bestNumber, 0);
        RewardPoint minerRp = getMinerRP(bestBlock.getHash());
        if(minerRp == null) {
            return;
        }



        if(!FastByteComparisons.equal(cacheRP.getParentBlockHash(), bestBlock.getHash())) {
            return;
        }
        if(cacheRP.getRP().compareTo(minerRp.getRP()) > 0) {
            return;
        }

        // Genesis 블록일 경우, 바로 시작한다.
        /*if(blockchain.getBestBlock().isGenesis() && !isGeneratingBlock) {
            isGeneratingBlock = true;
            restartMining(minerRp);
            return;
        }*/

        // 너무 오랫동안 블럭이 업데이트되지 않으면 채굴을 시작한다.
        /*if(now - miningStartTime > 30*1000 && !isGeneratingBlock) {
            isGeneratingBlock = true;
            restartMining(minerRp);
            return;
        }*/


        if(!isGeneratingBlock) {
            isGeneratingBlock = true;
            restartMining(minerRp);
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


    private Block getNewBlockForMining(byte[] worldBestHash) {
        Block bestBlockchain = blockchain.getBestBlock();
        Block bestPendingState = ((PendingStateImpl) pendingState).getBestBlock();  // original
        //Block bestPendingState = blockchain.getBlockByHash(worldBestHash);        // trying...

        logger.debug("getNewBlockForMining best blocks: PendingState: " + bestPendingState.getShortDescr() + ", Blockchain: " + bestBlockchain.getShortDescr());


        return blockchain.createNewBlock(bestPendingState, getAllPendingTransactions());
    }

    private synchronized void restartMining(RewardPoint rp) {
        cancelCurrentBlock();

        miningBlock = getNewBlockForMining(rp.getParentBlockHash());

        broadcastBlockStarted(miningBlock);


        // TODO RP 를 새로 생성해야 오류가 발생하지 않을 것 같기도...

        miningBlock.setRewardPoint(rp.getRP());
        miningBlock.setNonce(rp.getBalance());
        miningBlock.setMixHash(rp.getSeed());
        miningBlock.setExtraData(rp.getParentBlockHash());

        try {
            blockMined(miningBlock);
        } catch (InterruptedException e) {
            logger.error("Error occurred while mining blocks", e);
        }

        isGeneratingBlock = false;
    }

    private RewardPoint getMinerRP(byte[] parentHash) {
        Block parentBlock = blockStore.getBlockByHash(parentHash);

        if(parentBlock == null) {
            return null;
        }

        return RewardPointUtil.genRewardPoint(parentBlock, config.getMinerCoinbase(), blockStore, pendingState.getRepository());
    }

    private void blockMined(Block newBlock) throws InterruptedException {

        long t = TimeUtils.getRealTimestamp();
        long lastBlockMinedTime = blockchain.getBestBlock().getTimestamp();

        if (t - lastBlockMinedTime < minBlockTimeout) {
            long sleepTime = minBlockTimeout - (t - lastBlockMinedTime);
            logger.debug("Last block was mined " + (t - lastBlockMinedTime) + " ms ago. Sleeping " + sleepTime + " ms before importing...");
            Thread.sleep(sleepTime);
        }

        broadcastBlockMined(newBlock);
        logger.info("Wow, block mined !!!: {}", newBlock.toString());

        miningBlock = null;

        // broadcast the block
        logger.debug("Importing newly mined block {} {} ...", newBlock.getShortHash(), newBlock.getNumber());
        ImportResult importResult = ((EthereumImpl) ethereum).addNewMinedBlock(newBlock);
        logger.debug("Mined block import result is " + importResult);
    }

    public boolean isMining() {
        return isLocalMining;
    }



    /*****  Listener boilerplate  ******/

    public void addListener(MinerListener l) {
        listeners.add(l);
    }

    public void removeListener(MinerListener l) {
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
    }
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
