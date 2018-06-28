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

import org.apis.config.SystemProperties;
import org.apis.contract.ContractLoader;
import org.apis.core.*;
import org.apis.db.BlockStore;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumImpl;
import org.apis.listener.CompositeEthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.net.server.Channel;
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


    /** 이 시간이 되면 채굴을 시작한다. */
    private long miningStartTime = 0;
    private long lastBlockMined = 0;
    private byte[] lastMinedParentBlockHash = null;

    /** 마지막으로 채굴을 확인하는 테스크가 실행된 시간 */
    private long lastMiningTaskRun = 0;

    private Block lastConnectedBlock = null;

    /**
     * Set to true if block creation is in progress.
     */
    private static boolean isGeneratingBlock = false;


    private Timer timerCheckMining = new Timer();
    private Timer timerSubmitMinerState;

    private boolean isSyncDone = false;
    private long lastReceivedBlockNumber = 0;
    private boolean isSyncMinerStateTaskRun = false;

    public static byte[] contractTxid = null;

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
                isSyncDone = true;

                if (config.minerStart() && config.isSyncEnabled()) {
                    logger.info("Sync complete, start mining...");
                    startMining();
                }
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


        // 지속적으로 채굴자 리스트를 동기화한다.
        timerSubmitMinerState = new Timer();
        timerSubmitMinerState.schedule(getSyncMinerState(), 30L*1000L, 100L);

        lastBlockMined = TimeUtils.getRealTimestamp();
    }


    /**
     * Minimum gas price that can be included in the mining block
     * @param minGasPrice atto gas
     */
    public void setMinGasPrice(BigInteger minGasPrice) {
        this.minGasPrice = minGasPrice;
    }


    public void startMining() {
        if(isMining() || !config.minerStart() || config.getCoinbaseKey() == null) {
            return;
        }

        logger.info("Miner started");
        broadcastMinerStarted();
        isLocalMining = true;

        timerCheckMining = new Timer();
        timerCheckMining.schedule(getCheckMiningTask(), 0L, 1000L);
    }

    public void stopMining() {
        isLocalMining = false;
        cancelCurrentBlock();

        timerCheckMining.cancel();
        timerCheckMining.purge();

        broadcastMinerStopped();
        logger.info("Miner stopped");
    }

    private TimerTask getCheckMiningTask() {
        return new TimerTask() {
            @Override
            public void run() {
                checkMiningReady();
            }
        };
    }


    private TimerTask getSyncMinerState() {
        return new TimerTask() {
            @Override
            public void run() {
                isSyncMinerStateTaskRun = true;

                // 객체가 생성되고 1분 뒤에 이 부분이 실행된다. 1분 뒤에도 제네시스 블록이면, 채굴을 시작한다.
                if (blockchain.getBestBlock().isGenesis()) {
                    startMining();
                    return;
                }


                long now = TimeUtils.getRealTimestamp();

                /* 블록을 채굴하는 중에 오류가 발생해서 Task가 종료되면 lastMiningTaskRun 값이 갱신되지 않는다.
                 * 이를 이용해 오류로 인해서 채굴이 정지되는 것을 방지한다.
                 */
                //if (isMining() && now - lastMiningTaskRun > 10L * 1000L) {
                if (isMining() && now - lastBlockMined > 40L * 1000L) {
                    //isLocalMining = false;
                    stopMining();
                }



                // 모든 노드에서 채굴이 멈춰진 채로 1분이 경과하면 바로 채굴을 시작한다.
                if (now - lastBlockMined > 60*1000L && blockchain.getBestBlock().getNumber() == ethereum.getSyncStatus().getBlockBestKnown()) {
                    startMining();
                }

                MinedBlockCache cache = MinedBlockCache.getInstance();
                List<Block> receivedBlocks = cache.getBestMinedBlocks();

                for (Block block : receivedBlocks) {
                    if(now - block.getTimestamp()*1000L > 10_000L) {
                        if(blockchain.getBlockByHash(block.getHash()) == null) {
                            blockchain.tryToConnect(block);
                        }
                        lastConnectedBlock = block;
                    }
                }
            }
        };
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


    /**
     * 매 9초로 끝나는 시간마다 블록을 생성할 준비가 되었는지(RP 값이 적당한지) 확인한다.
     */
    private synchronized void checkMiningReady() {
        // SYNC가 먼저 되서 채굴이 시작하더라도, 상태 확인하는 테스크가 시작된 이후에 채굴을 하도록 한다.
        if(!isSyncMinerStateTaskRun) {
            return;
        }

        // 채굴이 꺼져있으면 블록을 생성하지 않는다
        if(!isMining()) {
            return;
        }

        final long now = TimeUtils.getRealTimestamp();
        lastMiningTaskRun = now;

        //BestBlock으로부터 새 블록이 생성된다. BestBlock이 존재하지 않으면 mining을 진행하지 않는다.
        Block bestBlock = blockchain.getBestBlock();
        Block bestPendingBlock = ((PendingStateImpl) pendingState).getBestBlock();
        if(bestBlock == null || bestPendingBlock == null) {
            return;
        }


        if(bestBlock.isGenesis()) {
            if(!isGeneratingBlock) {
                restartMining();
            }
            return;
        }


        if(now - bestPendingBlock.getTimestamp()*1000L < 10_000L) {
            return;
        }

        // 이미 같은 부모를 이용해서 블록을 만들었으면, 더 블록을 생성하지 않는다
        if(lastConnectedBlock != null && lastMinedParentBlockHash != null && FastByteComparisons.equal(lastConnectedBlock.getHash(), lastMinedParentBlockHash)) {
            return;
        }


        // 다른 네트워크에서 전달받은 블록들과 높이 차이가 크면 블록을 생성하면 안된다.
        /*if(bestBlock.getNumber() < MinedBlockCache.getInstance().getBestBlockNumber() - 2) {
            return;
        }*/

        if(!isGeneratingBlock) {
            restartMining();
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

        return blockchain.createNewBlock(bestPendingState, getAllPendingTransactions());
    }


    private synchronized void restartMining() {
        isGeneratingBlock = true;
        cancelCurrentBlock();

        // 부모가 genesis일 경우, 컨트렉트들을 생성한다.
        /*if(blockchain.getBestBlock().isGenesis()) {
            BigInteger nonce = ethereum.getRepository().getNonce(config.getMinerCoinbase());
            Transaction tx = ContractLoader.getAddressMaskingContractCreation(nonce, ethereum.getChainIdForNextBlock());
            tx.sign(config.getCoinbaseKey());

            contractTxid = tx.getHash();
            System.err.println("TXID : " + Hex.toHexString(tx.getHash()));

            ethereum.submitTransaction(tx);
        }*/


        miningBlock = getNewBlockForMining();
        lastMinedParentBlockHash = miningBlock.getParentHash();

        Block newMiningBlock = new Block(miningBlock.getEncoded());

        broadcastBlockStarted(newMiningBlock);
        logger.debug("New block mining started: {}", newMiningBlock.getShortHash());

        blockMined(newMiningBlock);
    }


    private synchronized void blockMined(Block newBlock) {

        lastBlockMined = TimeUtils.getRealTimestamp();

        broadcastBlockMined(newBlock);
        logger.info("Wow, block mined !!!: {}", newBlock.getShortDescr());

        miningBlock = null;


        // 제네시스 블록이면 바로 DB에 저장한다.
        if(blockchain.getBlockByHash(newBlock.getParentHash()).isGenesis()) {
            logger.debug("Importing newly mined block {} {} ...", newBlock.getShortHash(), newBlock.getNumber());
            ImportResult importResult = ((EthereumImpl) ethereum).addNewMinedBlock(newBlock);
            logger.debug("Mined block import result is " + importResult);
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
            ethereum.submitMinedBlock(minedBlocks);
        }

        isGeneratingBlock = false;
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
