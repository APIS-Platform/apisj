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
package org.apis.sync;

import org.apis.core.*;
import org.apis.net.server.Channel;
import org.apis.net.server.ChannelManager;
import org.apis.config.SystemProperties;
import org.apis.core.Blockchain;
import org.apis.facade.SyncStatus;
import org.apis.listener.CompositeEthereumListener;
import org.apis.listener.EthereumListener;
import org.apis.util.ExecutorPipeline;
import org.apis.validator.BlockHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static java.lang.Math.max;
import static java.util.Collections.singletonList;
import static org.apis.util.Utils.longToTimePeriod;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
@Component
public class SyncManager extends BlockDownloader {

    private final static Logger logger = LoggerFactory.getLogger("sync");

    private final static AtomicLong blockQueueByteSize = new AtomicLong(0);
    private final static int BLOCK_BYTES_ADDON = 4;

    // Transaction.getSender() is quite heavy operation so we are prefetching this value on several threads
    // to unload the main block importing cycle
    private ExecutorPipeline<BlockWrapper,BlockWrapper> exec1 = new ExecutorPipeline<>
            (4, 1000, true, blockWrapper -> {
                for (Transaction tx : blockWrapper.getBlock().getTransactionsList()) {
                    tx.getSender();
                }
                return blockWrapper;
            }, throwable -> logger.error("Unexpected exception: ", throwable));

    private ExecutorPipeline<BlockWrapper, Void> exec2 = exec1.add(1, 1, new Consumer<BlockWrapper>() {
        @Override
        public void accept(BlockWrapper blockWrapper) {
            blockQueueByteSize.addAndGet(estimateBlockSize(blockWrapper));
            blockQueue.add(blockWrapper);
        }
    });

    /**
     * Queue with validated blocks to be added to the blockchain
     */
    private BlockingQueue<BlockWrapper> blockQueue = new LinkedBlockingQueue<>();

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private CompositeEthereumListener compositeEthereumListener;

    @Autowired
    private FastSyncManager fastSyncManager;

    ChannelManager channelManager;

    private SystemProperties config;

    private SyncPool pool;

    private SyncQueueImpl syncQueue;

    private Thread syncQueueThread;

    private long blockBytesLimit = 32 * 1024 * 1024;
    private long lastKnownBlockNumber = 0;
    private boolean syncDone = false;
    private AtomicLong importIdleTime = new AtomicLong();
    private long importStart;
    private EthereumListener.SyncState syncDoneType = EthereumListener.SyncState.COMPLETE;
    private ScheduledExecutorService logExecutor = Executors.newSingleThreadScheduledExecutor();

    public SyncManager() {
        super(null);
    }

    @Autowired
    public SyncManager(final SystemProperties config, BlockHeaderValidator validator) {
        super(validator);
        this.config = config;
        blockBytesLimit = config.blockQueueSize();
        setHeaderQueueLimit(config.headerQueueSize() / BlockHeader.MAX_HEADER_SIZE);
    }

    public synchronized void init(final ChannelManager channelManager, final SyncPool pool) {
        if (this.channelManager == null) {  // First init
            this.pool = pool;
            this.channelManager = channelManager;
            logExecutor.scheduleAtFixedRate(() -> {
                try {
                    // 다른 노드에서 갖고있는 최신의 블록보다 번호가 낮으면 싱크가 완료된게 아니다.
                    /*if(lastKnownBlockNumber > blockchain.getBestBlock().getNumber()) {
                        syncDone = false;
                    }*/

                    logger.info("Sync state: " + getSyncStatus() +
                            (isSyncDone() || importStart == 0 ? "" : "; Import idle time " +
                            longToTimePeriod(importIdleTime.get()) + " of total " + longToTimePeriod(System.currentTimeMillis() - importStart)));
                } catch (Exception e) {
                    logger.error("Unexpected", e);
                }
            }, 10, 2, TimeUnit.SECONDS);
        }

        if (!config.isSyncEnabled()) {
            logger.info("Sync Manager: OFF");
            return;
        }
        logger.info("Sync Manager: ON");

        if (pool.getChannelManager() == null) {  // Never were on this stage of init
            logger.info("Initializing SyncManager.");
            pool.init(channelManager);

            if (config.isFastSyncEnabled()) {
                fastSyncManager.init();
            } else {
                initRegularSync(EthereumListener.SyncState.COMPLETE);
            }
        }
    }

    void initRegularSync(EthereumListener.SyncState syncDoneType) {
        logger.info("Initializing SyncManager regular sync.");
        this.syncDoneType = syncDoneType;

        syncQueue = new SyncQueueImpl(blockchain);
        super.init(syncQueue, pool);

        Runnable queueProducer = this::produceQueue;

        syncQueueThread = new Thread (queueProducer, "SyncQueueThread");
        syncQueueThread.start();
    }

    public SyncStatus getSyncStatus() {
        if (config.isFastSyncEnabled()) {
            SyncStatus syncStatus = fastSyncManager.getSyncState();
            if (syncStatus.getStage() == SyncStatus.SyncStage.Complete) {
                return getSyncStateImpl();
            } else {
                return new SyncStatus(syncStatus, blockchain.getBestBlock().getNumber(), getLastKnownBlockNumber());
            }
        } else {
            return getSyncStateImpl();
        }
    }

    private SyncStatus getSyncStateImpl() {
        if (!config.isSyncEnabled())
            return new SyncStatus(SyncStatus.SyncStage.Off, 0, 0, blockchain.getBestBlock().getNumber(), blockchain.getBestBlock().getNumber());

        return new SyncStatus(isSyncDone() ? SyncStatus.SyncStage.Complete : SyncStatus.SyncStage.Regular,
                0, 0, blockchain.getBestBlock().getNumber(), getLastKnownBlockNumber());
    }

    @Override
    protected void pushBlocks(List<BlockWrapper> blockWrappers) {
        if (!exec1.isShutdown()) {
            exec1.pushAll(blockWrappers);
        }
    }

    @Override
    protected void pushHeaders(List<BlockHeaderWrapper> headers) {}

    @Override
    protected int getBlockQueueFreeSize() {
        int blockQueueSize = blockQueue.size();
        long blockByteSize = blockQueueByteSize.get();
        int availableBlockSpace = max(0, getBlockQueueLimit() - blockQueueSize);
        long availableBytesSpace = Math.max(0, blockBytesLimit - blockByteSize);

        Thread.State state = syncQueueThread.getState();

        int bytesSpaceInBlocks;
        if (blockByteSize == 0 || blockQueueSize == 0) {
            bytesSpaceInBlocks = Integer.MAX_VALUE;
        } else {
            bytesSpaceInBlocks = (int) Math.floor(availableBytesSpace / (blockByteSize / blockQueueSize));
        }

        return Math.min(bytesSpaceInBlocks, availableBlockSpace);
    }

    private long estimateBlockSize(BlockWrapper blockWrapper) {
        return blockWrapper.getEncoded().length + BLOCK_BYTES_ADDON;
    }

    /**
     * Processing the queue adding blocks to the chain.
     */
    private void produceQueue() {

        DecimalFormat timeFormat = new DecimalFormat("0.000");
        timeFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));

        try {
            Thread.sleep(10*1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (!Thread.currentThread().isInterrupted()) {

            BlockWrapper wrapper = null;
            try {

                long stale = !isSyncDone() && importStart > 0 && blockQueue.isEmpty() ? System.nanoTime() : 0;
                //wrapper = blockQueue.take();
                wrapper = blockQueue.poll(10L*1000L, TimeUnit.MILLISECONDS);
                if(wrapper == null) {
                    continue;
                }
                blockQueueByteSize.addAndGet(-estimateBlockSize(wrapper));

                if (stale > 0) {
                    importIdleTime.addAndGet((System.nanoTime() - stale) / 1_000_000);
                }
                if (importStart == 0) importStart = System.currentTimeMillis();

                logger.debug("BlockQueue size: {}, headers queue size: {}", blockQueue.size(), syncQueue.getHeadersCount());

                long s = System.nanoTime();
                long sl;
                ImportResult importResult;
                synchronized (blockchain) {
                    sl = System.nanoTime();
                    importResult = blockchain.tryToConnect(wrapper.getBlock());
                }
                long f = System.nanoTime();
                long t = (f - s) / 1_000_000;
                String ts = timeFormat.format(t / 1000d) + "s";
                t = (sl - s) / 1_000_000;
                ts += t < 10 ? "" : " (lock: " + timeFormat.format(t / 1000d) + "s)";

                if (importResult == ImportResult.IMPORTED_BEST) {
                    logger.info("Success importing BEST: block.number: {}, block.hash: {}, tx.size: {}, time: {}",
                            wrapper.getNumber(), wrapper.getBlock().getShortHash(),
                            wrapper.getBlock().getTransactionsList().size(), ts);

                    // TODO sendNewBlock을 사용하지 않기 대문에 wrapper.isNewBlock() 값은 true가 나올 수 없다. 나중에 검증 필요
                    //if (wrapper.isNewBlock() && !syncDone) {
                    if (wrapper.getNumber() == getLastKnownBlockNumber() && !syncDone) {
                        syncDone = true;
                        channelManager.onSyncDone(true);
                        compositeEthereumListener.onSyncDone(syncDoneType);
                    }
                }

                if (importResult == ImportResult.IMPORTED_NOT_BEST)
                    logger.info("Success importing NOT_BEST: block.number: {}, block.hash: {}, tx.size: {}, time: {}",
                            wrapper.getNumber(),
                            wrapper.getBlock().getShortHash(),
                            wrapper.getBlock().getTransactionsList().size(),
                            ts);

                if (syncDone && (importResult == ImportResult.IMPORTED_BEST || importResult == ImportResult.IMPORTED_NOT_BEST)) {
                    if (logger.isDebugEnabled()) logger.debug("Block dump: " + Hex.toHexString(wrapper.getBlock().getEncoded()));
                    // Propagate block to the net after successful import asynchronously
                    if (wrapper.isNewBlock()) channelManager.onNewForeignBlock(wrapper);
                }

                // In case we don't have a parent on the chain
                // return the try and wait for more blocks to come.
                if (importResult == ImportResult.NO_PARENT) {
                    logger.error("No parent on the chain for block.number: {} block.hash: {}",
                            wrapper.getNumber(), wrapper.getBlock().getShortHash());
                }

            } catch (InterruptedException e) {
                break;
            } catch (Throwable e) {
                if (wrapper != null) {
                    logger.error("Error processing block {}: ", wrapper.getBlock().getShortDescr(), e);
                    logger.error("Block dump: {}", Hex.toHexString(wrapper.getBlock().getEncoded()));
                } else {
                    logger.error("Error processing unknown block", e);
                }
            }
        }
    }


    /**
     * Adds NEW block to the queue
     *
     * @param block new block
     * @param nodeId nodeId of the remote peer which this block is received from
     *
     * @return true if block passed validations and was added to the queue,
     *         otherwise it returns false
     */
    public boolean validateAndAddNewBlock(Block block, byte[] nodeId) {

        if (syncQueue == null) return true;

        // run basic checks
        if (!isValid(block.getHeader())) {
            return false;
        }

        lastKnownBlockNumber = block.getNumber();

        logger.debug("Adding new block to sync queue: " + block.getShortDescr());
        syncQueue.addHeaders(singletonList(new BlockHeaderWrapper(block.getHeader(), nodeId)));

        synchronized (this) {
            List<Block> newBlocks = syncQueue.addBlocks(singletonList(block));

            // TODO newBLocks 의 size 값이 0일 경우, 제대로 등록되지 못한 것.
            // TODO 현재 갖고 있는 블록들의 해더가 중간에 비어있을 경우 발생하는 것으로 보임
            // TODO 그럴 경우에는 중간에 빈 해더들을 불러들이게 해야 함.




            List<BlockWrapper> wrappers = new ArrayList<>();
            for (Block b : newBlocks) {
                boolean newBlock = Arrays.equals(block.getHash(), b.getHash());
                BlockWrapper wrapper = new BlockWrapper(b, newBlock, nodeId);
                wrapper.setReceivedAt(System.currentTimeMillis());
                wrappers.add(wrapper);
            }

            logger.debug("Pushing " + wrappers.size() + " new blocks to import queue: " + (wrappers.isEmpty() ? "" :
                    wrappers.get(0).getBlock().getShortDescr() + " ... " + wrappers.get(wrappers.size() - 1).getBlock().getShortDescr()));
            pushBlocks(wrappers);
        }

        logger.debug("Blocks waiting to be proceed:  queue.size: [{}] lastBlock.number: [{}]",
                blockQueue.size(),
                block.getNumber());

        return true;
    }

    public long getBlockNumberBreaked() {
        return syncQueue.getBlockBreakedNumber();
    }


    public boolean isSyncDone() {
        return syncDone;
    }

    public boolean isFastSyncRunning() {
        return fastSyncManager.isFastSyncInProgress();
    }

    public long getLastKnownBlockNumber() {
        long ret = max(blockchain.getBestBlock().getNumber(), lastKnownBlockNumber);
        for (Channel channel : pool.getActivePeers()) {
            BlockIdentifier bestKnownBlock = channel.getEthHandler().getBestKnownBlock();
            if (bestKnownBlock != null) {
                ret = max(bestKnownBlock.getNumber(), ret);
            }
        }
        return ret;
    }

    public void close() {
        try {
            logger.info("Shutting down SyncManager");
            exec1.shutdown();
            exec1.join();
            logExecutor.shutdown();
            pool.close();
            if (syncQueueThread != null) {
                syncQueueThread.interrupt();
                syncQueueThread.join(10 * 1000);
            }
            if (config.isFastSyncEnabled()) fastSyncManager.close();
        } catch (Exception e) {
            logger.warn("Problems closing SyncManager", e);
        }
        super.close();
    }
}
