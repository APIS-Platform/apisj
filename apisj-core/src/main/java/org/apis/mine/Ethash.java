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
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.apis.core.Block;
import org.apis.core.BlockHeader;
import org.apis.config.SystemProperties;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.apis.crypto.HashUtil.sha3;
import static org.apis.util.ByteUtil.longToBytes;

/**
 * More high level validator/miner class which keeps a cache for the last requested block epoch
 *
 * Created by Anton Nashatyrev on 04.12.2015.
 */
public class Ethash {
    private static final Logger logger = LoggerFactory.getLogger("mine");
    private static EthashParams ethashParams = new EthashParams();

    private static Ethash cachedInstance = null;
    private static long cachedBlockEpoch = 0;
    //    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static ListeningExecutorService executor = MoreExecutors.listeningDecorator(
            new ThreadPoolExecutor(8, 8, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("ethash-pool-%d").build()));

    /**
     * Returns instance for the specified block number either from cache or calculates a new one
     */
    public static Ethash getForBlock(SystemProperties config, long blockNumber) {
        long epoch = blockNumber / ethashParams.getEPOCH_LENGTH();
        if (cachedInstance == null || epoch != cachedBlockEpoch) {
            cachedInstance = new Ethash(config, epoch * ethashParams.getEPOCH_LENGTH());
            cachedBlockEpoch = epoch;
        }
        return cachedInstance;
    }

    private EthashAlgo ethashAlgo = new EthashAlgo(ethashParams);

    private long blockNumber;
    private SystemProperties config;

    public Ethash(SystemProperties config, long blockNumber) {
        this.config = config;
        this.blockNumber = blockNumber;
    }


    private long getFullSize() {
        return getEthashAlgo().getParams().getFullSize(blockNumber);
    }

    private EthashAlgo getEthashAlgo() {
        return ethashAlgo;
    }


    /**
     *  누구나 블록을 채굴할 수 있다.
     *  채굴이 시작되면 바로 결과가 반환된다.
     *  RewardPoint(RP) 값이 올바르지 않다면 블록을 생성하더라도 전파되지 않는다.
     *
     *  @param block The block to mine. This block is updated when mined
     *  @return the task which may be cancelled. On success returns nonce
     */
    public ListenableFuture<MinerIfc.MiningResult> mine(final Block block) {
        return new MineTask(block, () -> {
            long nonce = 0;

            return new MinerIfc.MiningResult(nonce, new byte[]{}, block);
        }).submit();
    }

    /**
     *  외부에서 전달받은 RewardPoint 보다 채굴자의 RewardPoint가 커야한다.
     */
    public boolean validate(BlockHeader header) {
        // TODO RewardPoint 검증 추가해야 함
        return true;
    }

    class MineTask extends AnyFuture<MinerIfc.MiningResult> {
        Block block;
        Callable<MinerIfc.MiningResult> miner;

        MineTask(Block block, Callable<MinerIfc.MiningResult> miner) {
            this.block = block;
            this.miner = miner;
        }

        public MineTask submit() {
            ListenableFuture<MinerIfc.MiningResult> f = executor.submit(miner);
            add(f);
            return this;
        }

        @Override
        protected void postProcess(MinerIfc.MiningResult result) {
            block.setNonce(longToBytes(result.nonce));
            block.setMixHash(result.digest);
        }
    }
}
