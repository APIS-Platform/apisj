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
import org.apis.core.*;
import org.apis.db.BlockStore;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumImpl;
import org.apis.listener.CompositeEthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.util.FastByteComparisons;
import org.apis.util.TimeUtils;
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

    private BigInteger minGasPrice;

    private Block miningBlock;

    private byte[] lastMinedParentBlockHash = null;


    private Block lastConnectedBlock = null;

    /**
     * Set to true if block creation is in progress.
     */
    private static boolean isGeneratingBlock = false;

    private boolean isSyncDone = false;


    //public static byte[] contractTxid = null;

    @Autowired
    public BlockMiner(final SystemProperties config, final CompositeEthereumListener listener, final Blockchain blockchain, final BlockStore blockStore, final PendingState pendingState) {
        this.config = config;
        this.blockchain = blockchain;
        this.blockStore = blockStore;
        this.pendingState = pendingState;
        minGasPrice = config.getMineMinGasPrice();      // ethereumj default : 15Gwei

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
            if(now - block.getTimestamp()*1000L > 10_000L) {
                if(blockStore.getBlockByHash(block.getHash()) == null) {
                    if(isSyncDone) {
                        ((EthereumImpl) ethereum).addNewMinedBlock(block);
                    }
                    /*else {
                        blockchain.tryToConnect(block);
                    }*/
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


    /**
     * 매 9초로 끝나는 시간마다 블록을 생성할 준비가 되었는지(RP 값이 적당한지) 확인한다.
     */
    private synchronized void checkMiningReady() {
        // Make sure that mining has started in the settings.
        if(!config.minerStart()) {
            System.out.println("BLOCKMINER_0");
            return;
        }
        // The key must be set because the block must include the miner's signature.
        if(config.getCoinbaseKey() == null) {
            System.out.println("BLOCKMINER_1");
            return;
        }


        final long now = TimeUtils.getRealTimestamp();

        Block bestBlock = blockchain.getBestBlock();
        Block bestPendingBlock = ((PendingStateImpl) pendingState).getBestBlock();

        if(bestBlock == null || bestPendingBlock == null) {
            System.out.println("BLOCKMINER_2");
            return;
        }

        if(bestBlock.isGenesis()) {
            if(!isGeneratingBlock) {
                //isSyncDone = true;
                restartMining();
            }
            return;
        }

        // Blocks can only be created if synchronization is complete.
        if(!isSyncDone) {
            System.out.println("BLOCKMINER_3");
            return;
        }

        // 채굴자의 잔고가 0일 경우, 채굴을 진행하지 않는다
        if(ethereum.getRepository().getBalance(config.getMinerCoinbase()).compareTo(BigInteger.ONE) < 0) {
            System.out.println("BLOCKMINER_4");
            return;
        }


        if(now - bestPendingBlock.getTimestamp()*1000L < 10_000L) {
            System.out.println("BLOCKMINER_5");
            return;
        }

        // 이미 같은 부모를 이용해서 블록을 만들었으면, 더 블록을 생성하지 않는다
        if(lastConnectedBlock != null && lastMinedParentBlockHash != null && FastByteComparisons.equal(lastConnectedBlock.getHash(), lastMinedParentBlockHash)) {
            System.out.println("BLOCKMINER_6");
            return;
        }


        // 다른 네트워크에서 전달받은 블록들과 높이 차이가 크면 블록을 생성하면 안된다.
        if(bestBlock.getNumber() < MinedBlockCache.getInstance().getBestBlockNumber() - 2) {
            System.out.println("BLOCKMINER_7");
            return;
        }

        if(!isGeneratingBlock) {
            restartMining();
            return;
        }

        System.out.println("Nothing to mining");
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
        if(blockchain.getBestBlock().getNumber() > 100 && ethereum.getChannelManager().getActivePeers().isEmpty()) {
            isGeneratingBlock = false;
            return;
        }


        miningBlock = getNewBlockForMining();

        if(miningBlock == null) {
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
