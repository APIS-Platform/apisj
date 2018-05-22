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

import org.apis.core.Block;
import org.apis.core.BlockHeader;
import org.apis.core.BlockHeaderWrapper;
import org.apis.core.BlockWrapper;
import org.apis.db.DbFlushManager;
import org.apis.db.IndexedBlockStore;
import org.apis.net.server.Channel;
import org.apis.core.*;
import org.apis.crypto.HashUtil;
import org.apis.datasource.DataSourceArray;
import org.apis.util.FastByteComparisons;
import org.apis.validator.BlockHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 27.10.2016.
 */
@Component
@Scope("prototype")
public class BlockBodiesDownloader extends BlockDownloader {
    private final static Logger logger = LoggerFactory.getLogger("sync");

    public final static byte[] EMPTY_BODY = new byte[] {-62, -64, -64};

    @Autowired
    SyncPool syncPool;

    @Autowired
    IndexedBlockStore blockStore;

    @Autowired @Qualifier("headerSource")
    DataSourceArray<BlockHeader> headerStore;

    @Autowired
    DbFlushManager dbFlushManager;

    long t;

    private SyncQueueIfc syncQueue;
    private int curBlockIdx = 1;
    private BigInteger curTotalRewardPoint;

    private Thread headersThread;
    private int downloadCnt = 0;

    @Autowired
    public BlockBodiesDownloader(BlockHeaderValidator headerValidator) {
        super(headerValidator);
    }

    void startImporting() {
        Block genesis = blockStore.getChainBlockByNumber(0);
        syncQueue = new SyncQueueImpl(Collections.singletonList(genesis));
        curTotalRewardPoint = genesis.getRewardPoint();

        headersThread = new Thread(this::headerLoop, "FastsyncHeadersFetchThread");
        headersThread.start();

        setHeadersDownload(false);

        init(syncQueue, syncPool);
    }

    private void headerLoop() {
        while (curBlockIdx < headerStore.size() && !Thread.currentThread().isInterrupted()) {
            List<BlockHeaderWrapper> wrappers = new ArrayList<>();
            List<BlockHeader> emptyBodyHeaders =  new ArrayList<>();
            for (int i = 0; i < 10000 - syncQueue.getHeadersCount() && curBlockIdx < headerStore.size(); i++) {
                BlockHeader header = headerStore.get(curBlockIdx++);
                wrappers.add(new BlockHeaderWrapper(header, new byte[0]));

                // Skip bodies download for blocks with empty body
                boolean emptyBody = FastByteComparisons.equal(header.getTxTrieRoot(), HashUtil.EMPTY_TRIE_HASH);
                if (emptyBody) emptyBodyHeaders.add(header);
            }

            synchronized (this) {
                syncQueue.addHeaders(wrappers);
                if (!emptyBodyHeaders.isEmpty()) {
                    addEmptyBodyBlocks(emptyBodyHeaders);
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
        headersDownloadComplete = true;
    }

    private void addEmptyBodyBlocks(List<BlockHeader> blockHeaders) {
        logger.debug("Adding {} empty body blocks to sync queue: {} ... {}", blockHeaders.size(),
                blockHeaders.get(0).getShortDescr(), blockHeaders.get(blockHeaders.size() - 1).getShortDescr());

        List<Block> finishedBlocks = new ArrayList<>();
        for (BlockHeader header : blockHeaders) {
            Block block = new Block.Builder()
                    .withHeader(header)
                    .withBody(EMPTY_BODY)
                    .create();
            finishedBlocks.add(block);
        }

        List<Block> startTrimmedBlocks = syncQueue.addBlocks(finishedBlocks);
        List<BlockWrapper> trimmedBlockWrappers = new ArrayList<>();
        for (Block b : startTrimmedBlocks) {
            trimmedBlockWrappers.add(new BlockWrapper(b, null));
        }

        pushBlocks(trimmedBlockWrappers);
    }

    @Override
    protected void pushBlocks(List<BlockWrapper> blockWrappers) {
        if (!blockWrappers.isEmpty()) {

            for (BlockWrapper blockWrapper : blockWrappers) {
                curTotalRewardPoint = curTotalRewardPoint.add(blockWrapper.getBlock().getRewardPoint());
                blockStore.saveBlock(blockWrapper.getBlock(), curTotalRewardPoint, true);
                downloadCnt++;
            }
            dbFlushManager.commit();

            long c = System.currentTimeMillis();
            if (c - t > 5000) {
                t = c;
                logger.info("FastSync: downloaded blocks. Last: " + blockWrappers.get(blockWrappers.size() - 1).getBlock().getShortDescr());
            }
        }
    }

    /**
     * Download could block chain synchronization occupying all peers
     * Prevents this by leaving one peer without work
     * Fallbacks to any peer when low number of active peers available
     */
    @Override
    Channel getAnyPeer() {
        return syncPool.getActivePeersCount() > 2 ? syncPool.getNotLastIdle() : syncPool.getAnyIdle();
    }

    @Override
    protected void pushHeaders(List<BlockHeaderWrapper> headers) {}

    @Override
    protected int getBlockQueueFreeSize() {
        return Integer.MAX_VALUE;
    }

    public int getDownloadedCount() {
        return downloadCnt;
    }

    @Override
    public void stop() {
        headersThread.interrupt();
        super.stop();
    }

    @Override
    protected void finishDownload() {
        stop();
    }
}
