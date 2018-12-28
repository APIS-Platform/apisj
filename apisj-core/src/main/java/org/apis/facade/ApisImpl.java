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
package org.apis.facade;

import org.apis.config.BlockchainConfig;
import org.apis.config.CommonConfig;
import org.apis.config.SystemProperties;
import org.apis.contract.ContractLoader;
import org.apis.contract.EstimateTransaction;
import org.apis.core.*;
import org.apis.core.PendingState;
import org.apis.core.Repository;
import org.apis.crypto.ECKey;
import org.apis.db.sql.DBManager;
import org.apis.mine.BlockMiner;
import org.apis.net.client.PeerClient;
import org.apis.net.rlpx.Node;
import org.apis.net.server.ChannelManager;
import org.apis.net.shh.Whisper;
import org.apis.net.submit.*;
import org.apis.net.submit.TransactionExecutor;
import org.apis.sync.SyncManager;
import org.apis.util.ByteUtil;
import org.apis.vm.program.ProgramResult;
import org.apis.vm.program.invoke.ProgramInvokeFactory;
import org.apis.listener.CompositeEthereumListener;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.listener.GasPriceTracker;
import org.apis.manager.AdminInfo;
import org.apis.manager.BlockLoader;
import org.apis.manager.WorldManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.FutureAdapter;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Roman Mandeleil
 * @since 27.07.2014
 */
@Component
public class ApisImpl implements Apis, SmartLifecycle {

    private static final Logger logger = LoggerFactory.getLogger("facade");
    private static final Logger gLogger = LoggerFactory.getLogger("general");

    @Autowired
    WorldManager worldManager;

    @Autowired
    AdminInfo adminInfo;

    @Autowired
    ChannelManager channelManager;

    @Autowired
    ApplicationContext ctx;

    @Autowired
    BlockLoader blockLoader;

    @Autowired
    ProgramInvokeFactory programInvokeFactory;

    @Autowired
    Whisper whisper;

    @Autowired
    PendingState pendingState;

    @Autowired
    SyncManager syncManager;

    @Autowired
    CommonConfig commonConfig = CommonConfig.getDefault();

    private SystemProperties config;

    private CompositeEthereumListener compositeEthereumListener;


    private GasPriceTracker gasPriceTracker = new GasPriceTracker();

    @Autowired
    public ApisImpl(final SystemProperties config, final CompositeEthereumListener compositeEthereumListener) {
        this.compositeEthereumListener = compositeEthereumListener;
        this.config = config;
        System.out.println();
        this.compositeEthereumListener.addListener(gasPriceTracker);
        gLogger.info("ApisJ node started: enode://" + Hex.toHexString(config.nodeId()) + "@" + config.externalIp() + ":" + config.listenPort());

        ContractLoader.makeABI();
        DBManager.getInstance();
        EstimateTransaction.getInstance(this);
    }

    @Override
    public void startPeerDiscovery() {
        worldManager.startPeerDiscovery();
    }

    @Override
    public void stopPeerDiscovery() {
        worldManager.stopPeerDiscovery();
    }

    @Override
    public void connect(InetAddress addr, int port, String remoteId) {
        connect(addr.getHostName(), port, remoteId);
    }

    @Override
    public void connect(final String ip, final int port, final String remoteId) {
        logger.debug("Connecting to: {}:{}", ip, port);
        if(worldManager != null)
            worldManager.getActivePeer().connectAsync(ip, port, remoteId, false);
    }

    @Override
    public void connect(Node node) {
        connect(node.getHost(), node.getPort(), Hex.toHexString(node.getId()));
    }

    @Override
    public Blockchain getBlockchain() {
        return (Blockchain) worldManager.getBlockchain();
    }

    public synchronized ImportResult addNewMinedBlock(Block block) {
        ImportResult importResult = worldManager.getBlockchain().tryToConnect(block);
        if (importResult == ImportResult.IMPORTED_BEST) {
            channelManager.sendNewBlock(block);
        }

        System.out.println("Block : " + block.getShortDescr());

        return importResult;
    }

    @Override
    public BlockMiner getBlockMiner() {
        return ctx.getBean(BlockMiner.class);
    }

    @Override
    public void addListener(EthereumListener listener) {
        worldManager.addListener(listener);
    }

    @Override
    public void removeListener(EthereumListener listener) {
        worldManager.removeListener(listener);
    }

    @Override
    public void close() {
        logger.info("### Shutdown initiated ### ");
        ((AbstractApplicationContext) getApplicationContext()).close();
    }

    @Override
    public SyncStatus getSyncStatus() {
        return syncManager.getSyncStatus();
    }

    public SyncManager getSyncManager() { return syncManager; }

    @Override
    public PeerClient getDefaultPeer() {
        return worldManager.getActivePeer();
    }

    @Override
    public boolean isConnected() {
        return worldManager.getActivePeer() != null;
    }

    @Override
    public Transaction createTransaction(BigInteger nonce,
                                         BigInteger gasPrice,
                                         BigInteger gas,
                                         byte[] receiveAddress,
                                         BigInteger value, byte[] data) {

        byte[] nonceBytes = ByteUtil.bigIntegerToBytes(nonce);
        byte[] gasPriceBytes = ByteUtil.bigIntegerToBytes(gasPrice);
        byte[] gasBytes = ByteUtil.bigIntegerToBytes(gas);
        byte[] valueBytes = ByteUtil.bigIntegerToBytes(value);

        return new Transaction(nonceBytes, gasPriceBytes, gasBytes,
                receiveAddress, valueBytes, data, getChainIdForNextBlock());
    }


    @Override
    public Future<List<Block>> submitMinedBlock(List<Block> minedBlockHeaders) {
        MinedBlockTask minedBlockTask = new MinedBlockTask(minedBlockHeaders, channelManager);

        final Future<List<Block>> listFuture = MinedBlockExecutor.instance.submitMinedBlock(minedBlockTask);

        return new FutureAdapter<List<Block>, List<Block>> (listFuture) {

            @Override
            protected List<Block> adapt(List<Block> adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    @Override
    public Future<Transaction> submitTransaction(Transaction transaction) {

        TransactionTask transactionTask = new TransactionTask(transaction, channelManager);

        final Future<List<Transaction>> listFuture = TransactionExecutor.instance.submitTransaction(transactionTask);

        pendingState.addPendingTransaction(transaction);

        return new FutureAdapter<Transaction, List<Transaction>>(listFuture) {
            @Override
            protected Transaction adapt(List<Transaction> adapteeResult) throws ExecutionException {
                return adapteeResult.get(0);
            }
        };
    }


    /*@Override
    public Future<List<RewardPoint>> submitRewardPoints(List<RewardPoint> rewardPoints) {

        RewardPointTask rewardPointTask = new RewardPointTask(rewardPoints, channelManager);

        final Future<List<RewardPoint>> future = RewardPointExecutor.instance.submitRewardPoint(rewardPointTask);

        return new FutureAdapter<List<RewardPoint>, List<RewardPoint>>(future) {

            @Override
            protected List<RewardPoint> adapt(List<RewardPoint> adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }*/

    @Override
    public TransactionReceipt callConstant(Transaction tx, Block block) {
        if (tx.getSignature() == null) {
            tx.sign(ECKey.fromPrivate(new byte[32]));
        }
        return callConstantImpl(tx, block).getReceipt();
    }

    @Override
    public BlockSummary replayBlock(Block block) {
        List<TransactionReceipt> receipts = new ArrayList<>();
        List<TransactionExecutionSummary> summaries = new ArrayList<>();

        Block parent;
        try {
            parent = worldManager.getBlockchain().getBlockByHash(block.getParentHash());
        } catch (NullPointerException e) {
            return new BlockSummary(block, new HashMap<>(), receipts, summaries);
        }

        if (parent == null) {
            logger.info("Failed to replay block #{}, its ancestor is not presented in the db", block.getNumber());
            return new BlockSummary(block, new HashMap<>(), receipts, summaries);
        }

        org.apis.core.Repository track = ((org.apis.core.Repository) worldManager.getRepository())
                .getSnapshotTo(parent.getStateRoot());

        try {
            long totalGasUsed = 0;
            for (Transaction tx : block.getTransactionsList()) {

                org.apis.core.Repository txTrack = track.startTracking();
                org.apis.core.TransactionExecutor executor = new org.apis.core.TransactionExecutor(
                        tx, block.getCoinbase(), txTrack, worldManager.getBlockStore(),
                        programInvokeFactory, block, worldManager.getListener(), totalGasUsed, BigInteger.ZERO)
                        .withCommonConfig(commonConfig);

                executor.init();
                executor.execute();
                executor.go();

                TransactionExecutionSummary summary = executor.finalization();
                totalGasUsed += executor.getGasUsed();

                txTrack.commit();

                TransactionReceipt receipt = executor.getReceipt();
                receipt.setPostTxState(track.getRoot());
                receipts.add(receipt);
                summaries.add(summary);
            }
        } finally {
            track.rollback();
        }

        return new BlockSummary(block, new HashMap<>(), receipts, summaries);
    }

    private org.apis.core.TransactionExecutor callConstantImpl(Transaction tx, Block block) {

        org.apis.core.Repository repository = ((org.apis.core.Repository) worldManager.getRepository())
                .getSnapshotTo(block.getStateRoot())
                .startTracking();

        try {
            org.apis.core.TransactionExecutor executor = new org.apis.core.TransactionExecutor
                    (tx, block.getCoinbase(), repository, worldManager.getBlockStore(),
                            programInvokeFactory, block, new EthereumListenerAdapter(), 0, BigInteger.ZERO)
                    .withCommonConfig(commonConfig)
                    .setLocalCall(true);

            executor.init();
            executor.execute();
            executor.go();
            executor.finalization();

            return executor;
        } finally {
            repository.rollback();
        }
    }

    @Override
    public ProgramResult callConstantFunction(String receiveAddress,
                                              CallTransaction.Function function, Object... funcArgs) {
        return callConstantFunction(receiveAddress, ECKey.DUMMY, function, funcArgs);
    }

    @Override
    public ProgramResult callConstantFunction(String receiveAddress, ECKey senderPrivateKey,
                                              CallTransaction.Function function, Object... funcArgs) {
        Transaction tx = CallTransaction.createCallTransaction(0, 0, 100000000000000L,
                receiveAddress, 0, function, funcArgs);
        tx.sign(senderPrivateKey);
        Block bestBlock = worldManager.getBlockchain().getBestBlock();

        return callConstantImpl(tx, bestBlock).getResult();
    }

    @Override
    public org.apis.facade.Repository getRepository() {
        return worldManager.getRepository();
    }

    @Override
    public org.apis.facade.Repository getLastRepositorySnapshot() {
        return getSnapshotTo(getBlockchain().getBestBlock().getStateRoot());
    }

    @Override
    public org.apis.facade.Repository getPendingState() {
        return worldManager.getPendingState().getRepository();
    }

    @Override
    public org.apis.facade.Repository getSnapshotTo(byte[] root) {

        org.apis.core.Repository repository = (Repository) worldManager.getRepository();
        org.apis.facade.Repository snapshot = repository.getSnapshotTo(root);

        return snapshot;
    }

    @Override
    public AdminInfo getAdminInfo() {
        return adminInfo;
    }

    @Override
    public ChannelManager getChannelManager() {
        return channelManager;
    }


    @Override
    public List<Transaction> getWireTransactions() {
        return worldManager.getPendingState().getPendingTransactions();
    }

    @Override
    public List<Transaction> getPendingStateTransactions() {
        return worldManager.getPendingState().getPendingTransactions();
    }

    @Override
    public BlockLoader getBlockLoader() {
        return blockLoader;
    }

    @Override
    public Whisper getWhisper() {
        return whisper;
    }

    @Override
    public long getGasPrice() {
        return gasPriceTracker.getGasPrice();
    }

    @Override
    public Integer getChainIdForNextBlock() {
        BlockchainConfig nextBlockConfig = config.getBlockchainConfig().getConfigForBlock(getBlockchain().getBestBlock().getNumber() + 1);
        return nextBlockConfig.getChainId();
    }

    @Override
    public void exitOn(long number) {
        worldManager.getBlockchain().setExitOn(number);
    }

    @Override
    public void initSyncing() {
        worldManager.initSyncing();
    }


    /**
     * For testing purposes and 'hackers'
     */
    public ApplicationContext getApplicationContext() {
        return ctx;
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    /**
     * Shutting down all app beans
     */
    @Override
    public void stop(Runnable callback) {
        logger.info("Shutting down Apis instance...");
        worldManager.close();
        callback.run();
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public boolean isRunning() {
        return true;
    }

    /**
     * Called first on shutdown lifecycle
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public TransactionInfo getTransactionInfo(byte[] hash) {
        return worldManager.getBlockchain().getTransactionInfo(hash);
    }
}
