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
 * Original Code
 * https://github.com/ethereum/ethereumj/blob/develop/ethereumj-core/src/main/java/org/ethereum/core/BlockchainImpl.java
 * Modified by APIS
 */
package org.apis.core;

import org.apis.config.CommonConfig;
import org.apis.config.Constants;
import org.apis.config.SystemProperties;
import org.apis.crypto.HashUtil;
import org.apis.datasource.inmem.HashMapDB;
import org.apis.db.*;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.manager.AdminInfo;
import org.apis.sync.SyncManager;
import org.apis.trie.Trie;
import org.apis.trie.TrieImpl;
import org.apis.util.*;
import org.apis.validator.DependentBlockHeaderRule;
import org.apis.validator.ParentBlockHeaderValidator;
import org.apis.vm.program.invoke.ProgramInvokeFactory;
import org.apis.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;
import static java.util.Collections.emptyList;
import static org.apis.core.ImportResult.*;
import static org.apis.crypto.HashUtil.sha3;

/**
 * The APIS blockchain is in many ways similar to the Ethereum blockchain,
 * although it does have some differences.
 * <p>
 * The main difference between APIS and Ethereum with regard to the blockchain architecture
 * is that, unlike Ethereum, APIS blocks remove uncle blocks and contain sum of used mineral
 * </p>
 * The block validation algorithm in APIS is as follows:
 * <ol>
 * <li>Check if the previous block referenced exists and is valid.</li>
 * <li>Check that the timestamp of the block is greater than that of the referenced previous block and less than 10 minutes into the future</li>
 * <li>Check that the block number, difficulty, transaction root and gas limit are valid.</li>
 * <li>Check that the proof of work on the block is valid.</li>
 * <li>Let S[0] be the STATE_ROOT of the previous block.</li>
 * <li>Let TX be the block's transaction list, with n transactions.
 * For all in in 0...n-1, set S[i+1] = APPLY(S[i],TX[i]).
 * If any applications returns an error, or if the total gas consumed in the block
 * up until this point exceeds the GAS_LIMIT, return an error.</li>
 * <li>Let S_FINAL be S[n], but adding the block reward paid to the miner and master-nodes.</li>
 * <li>Check if S_FINAL is the same as the STATE_ROOT. If it is, the block is valid; otherwise, it is not valid.</li>
 * </ol>
 *
 * @author Roman Mandeleil
 * @author Nick Savers
 * @author Daniel
 * @since 20.05.2014
 */
@Component
public class BlockchainImpl implements Blockchain, org.apis.facade.Blockchain {

    private static final Logger logger = LoggerFactory.getLogger("blockchain");
    private static final Logger stateLogger = LoggerFactory.getLogger("state");

    // to avoid using minGasPrice=0 from Genesis for the wallet
    // private static final long INITIAL_MIN_GAS_PRICE = 10 * nAPIS.longValue();
    public static final byte[] EMPTY_LIST_HASH = sha3(RLP.encodeList(new byte[0]));

    @Autowired @Qualifier("defaultRepository")
    private Repository repository;

    @Autowired
    protected BlockStore blockStore;

    @Autowired
    private TransactionStore transactionStore;

    private Block bestBlock;

    private BigInteger totalRewardPoint = ZERO;

    @Autowired
    private EthereumListener listener;

    @Autowired
    private
    ProgramInvokeFactory programInvokeFactory;

    @Autowired
    private AdminInfo adminInfo;

    @Autowired
    private DependentBlockHeaderRule parentHeaderValidator;

    @Autowired
    private PendingState pendingState;

    @Autowired
    private
    EventDispatchThread eventDispatchThread;

    @Autowired
    private
    CommonConfig commonConfig = CommonConfig.getDefault();

    @Autowired
    private
    SyncManager syncManager;

    @Autowired
    private
    PruneManager pruneManager;

    @Autowired
    StateSource stateDataSource;

    @Autowired
    private
    DbFlushManager dbFlushManager;

    SystemProperties config = SystemProperties.getDefault();

    private List<Chain> altChains = new ArrayList<>();
    private List<Block> garbage = new ArrayList<>();

    private long exitOn = Long.MAX_VALUE;

    public boolean byTest = false;
    private boolean fork = false;

    private byte[] minerCoinbase;
    private byte[] minerExtraData;


    private Stack<State> stateStack = new Stack<>();

    /** Tests only **/
    public BlockchainImpl() {
    }

    @Autowired
    public BlockchainImpl(final SystemProperties config) {
        this.config = config;
        initConst(config);
    }

    //todo: autowire over constructor
    public BlockchainImpl(final BlockStore blockStore, final Repository repository) {
        this.blockStore = blockStore;
        this.repository = repository;
        this.adminInfo = new AdminInfo();
        this.listener = new EthereumListenerAdapter();
        this.parentHeaderValidator = null;
        this.transactionStore = new TransactionStore(new HashMapDB());
        this.eventDispatchThread = EventDispatchThread.getDefault();
        this.programInvokeFactory = new ProgramInvokeFactoryImpl();
        initConst(Objects.requireNonNull(SystemProperties.getDefault()));
    }

    public BlockchainImpl withTransactionStore(TransactionStore transactionStore) {
        this.transactionStore = transactionStore;
        return this;
    }

    public BlockchainImpl withAdminInfo(AdminInfo adminInfo) {
        this.adminInfo = adminInfo;
        return this;
    }

    public BlockchainImpl withEthereumListener(EthereumListener listener) {
        this.listener = listener;
        return this;
    }

    public BlockchainImpl withSyncManager(SyncManager syncManager) {
        this.syncManager = syncManager;
        return this;
    }

    public BlockchainImpl withParentBlockHeaderValidator(ParentBlockHeaderValidator parentHeaderValidator) {
        this.parentHeaderValidator = parentHeaderValidator;
        return this;
    }

    //TODO 채굴자 정보를 지갑 설정에서 변경할 수 있게 해야한다.
    private void initConst(SystemProperties config) {
        minerCoinbase = config.getMinerCoinbase();
        minerExtraData = config.getMineExtraData();
    }

    @Override
    public byte[] getBestBlockHash() {
        return getBestBlock().getHash();
    }

    @Override
    public long getSize() {
        return bestBlock.getNumber() + 1;
    }

    @Override
    public Block getBlockByNumber(long blockNr) {
        return blockStore.getChainBlockByNumber(blockNr);
    }

    @Override
    public TransactionInfo getTransactionInfo(byte[] hash) {

        List<TransactionInfo> infoList = transactionStore.get(hash);

        if (infoList == null || infoList.isEmpty())
            return null;

        /* 트랜잭션이 하나만 존재하는 경우, 해당하는 정보를 반환하고
         * 다수가 검색될 경우, 메인 체인에 등록된 트랜잭션을 반환한다. */
        TransactionInfo txInfo = null;
        if (infoList.size() == 1) {
            txInfo = infoList.get(0);
        } else {
            for (TransactionInfo info : infoList) {
                Block block = blockStore.getBlockByHash(info.blockHash);
                Block mainBlock = blockStore.getChainBlockByNumber(block.getNumber());
                if (FastByteComparisons.equal(info.blockHash, mainBlock.getHash())) {
                    txInfo = info;
                    break;
                }
            }
        }
        if (txInfo == null) {
            logger.warn("Can't find block from main chain for transaction " + Hex.toHexString(hash));
            return null;
        }

        Transaction tx = this.getBlockByHash(txInfo.getBlockHash()).getTransactionsList().get(txInfo.getIndex());
        txInfo.setTransaction(tx);

        return txInfo;
    }

    @Override
    public Block getBlockByHash(byte[] hash) {
        return blockStore.getBlockByHash(hash);
    }

    @Override
    public synchronized List<byte[]> getListOfHashesStartFrom(byte[] hash, int qty) {
        return blockStore.getListHashesEndWith(hash, qty);
    }

    @Override
    public synchronized List<byte[]> getListOfHashesStartFromBlock(long blockNumber, int qty) {
        long bestNumber = bestBlock.getNumber();

        if (blockNumber > bestNumber) {
            return emptyList();
        }

        if (blockNumber + qty - 1 > bestNumber) {
            qty = (int) (bestNumber - blockNumber + 1);
        }

        long endNumber = blockNumber + qty - 1;

        Block block = getBlockByNumber(endNumber);

        List<byte[]> hashes = blockStore.getListHashesEndWith(block.getHash(), qty);

        // asc order of hashes is required in the response
        Collections.reverse(hashes);

        return hashes;
    }

    public static byte[] calcTxTrie(List<Transaction> transactions) {

        Trie txsState = new TrieImpl();

        if (transactions == null || transactions.isEmpty())
            return HashUtil.EMPTY_TRIE_HASH;

        for (int i = 0; i < transactions.size(); i++) {
            txsState.put(RLP.encodeInt(i), transactions.get(i).getEncoded());
        }
        return txsState.getRootHash();
    }

    public Repository getRepository() {
        return repository;
    }

    public Repository getRepositorySnapshot() {
        return repository.getSnapshotTo(blockStore.getBestBlock().getStateRoot());
    }

    @Override
    public BlockStore getBlockStore() {
        return blockStore;
    }

    public ProgramInvokeFactory getProgramInvokeFactory() {
        return programInvokeFactory;
    }

    private State pushState(byte[] bestBlockHash) {
        State push = stateStack.push(new State());
        this.bestBlock = blockStore.getBlockByHash(bestBlockHash);
        totalRewardPoint = blockStore.getTotalRewardPointForHash(bestBlockHash);
        this.repository = this.repository.getSnapshotTo(this.bestBlock.getStateRoot());
        return push;
    }

    private void popState() {
        State state = stateStack.pop();
        this.repository = repository.getSnapshotTo(state.root);
        this.bestBlock = state.savedBest;
        this.totalRewardPoint = state.savedTotalRewardPoint;
    }

    public void dropState() {
        stateStack.pop();
    }

    /**
     * 새로운 블록을 전달받았는데 조상들 중에 부모가 존재하는 경우
     * 판단에 따라서 갈라서게 될 수도 있다.
     */
    private synchronized BlockSummary tryConnectAndFork(final Block block) {
        Block oldBest = blockStore.getChainBlockByNumber(block.getNumber());
        BigInteger oldRP = BigInteger.ZERO;
        if(oldBest != null) {
            oldRP = oldBest.getCumulativeRewardPoint();
        }

        State savedState = pushState(block.getParentHash());
        this.fork = true;

        final BlockSummary summary;
        Repository repo;
        try {

            // FIXME: adding block with no option for flush
            Block parentBlock = getBlockByHash(block.getParentHash());
            repo = repository.getSnapshotTo(parentBlock.getStateRoot());
            summary = add(repo, block);
            if (summary == null) {
                return null;
            }
        } catch (Throwable th) {
            logger.error("Unexpected error: ", th);
            return null;
        } finally {
            this.fork = false;
        }

        logger.info("----------------------");
        if(oldRP.compareTo(block.getCumulativeRewardPoint()) < 0) {
            logger.info("New RP is bigger than {}", block.getCumulativeRewardPoint().subtract(oldRP));
        } else {
            logger.info("Old RP is bigger than {}", oldRP.subtract(block.getCumulativeRewardPoint()));
        }
        logger.info("----------------------");

        // 새로운 블록의 TotalRewardPoint 값이 더 크면 fork
        //if (summary.betterThan(bestBlock.getCumulativeRewardPoint())) {
        if(oldRP.compareTo(block.getCumulativeRewardPoint()) < 0) {

            logger.info("Rebranching: {} ~> {}", savedState.savedBest.getShortHash(), block.getShortHash());

            // main branch become this branch
            // cause we proved that total difficulty
            // is greateer
            blockStore.reBranch(block);

            // The main repository rebranch
            this.repository = repo;
//            this.repository.syncToRoot(block.getStateRoot());

            dropState();
        } else {
            // Stay on previous branch
            popState();
        }

        return summary;
    }


    public synchronized ImportResult tryToConnect(final Block block) {

        if (logger.isDebugEnabled())
            logger.debug("Try connect block hash: {}, number: {}",
                    Hex.toHexString(block.getHash()).substring(0, 6),
                    block.getNumber());

        // 받은 블록이 이미 존재하면 연결하지 않는다
        if (blockStore.getMaxNumber() >= block.getNumber() && blockStore.isBlockExist(block.getHash())) {
            if (logger.isDebugEnabled())
                logger.debug("Block already exist hash: {}, number: {}",
                        Hex.toHexString(block.getHash()).substring(0, 6),
                        block.getNumber());

            // retry of well known block
            return EXIST;
        }

        final ImportResult ret;

        // The simple case got the block to connect to the main chain
        final BlockSummary summary;

        // 새로 받은 블록이 마지막 블록의 자식 블록일 경우
        // 가장 먼저 채굴된 블록일 경우
        if (bestBlock.isParentOf(block)) {
            recordBlock(block);
//            Repository repoSnap = repository.getSnapshotTo(bestBlock.getStateRoot());
            summary = add(repository, block);

            if(summary == null) {
                ret = INVALID_BLOCK;
            } else {
                ret = IMPORTED_BEST;
            }
        }


        else {
            if(blockStore.isBlockExist(block.getParentHash())) {
                recordBlock(block);

                // 추가하려는 블록의 형제 블록이 없는 경우
                // 부모 블록이 체인에 존재하지만 Best 블록이 아닌 경우다..
                // 추가 되는 블록도 Best가 될 수 없다
                if(blockStore.getBlockHashByNumber(block.getNumber()) == null) {
                    summary = tryConnectAndFork(block);

                    if(summary == null) {
                        ret = INVALID_BLOCK;
                    }
                    else if(block.getNumber() < blockStore.getBestBlock().getNumber()) {
                        ret = IMPORTED_NOT_BEST;
                    }
                    else {
                        if(FastByteComparisons.equal(blockStore.getBlockHashByNumber(block.getNumber()), block.getHash())) {
                            ret = IMPORTED_BEST;
                        } else {
                            logger.info("Block is Not BEST");
                            ret = IMPORTED_NOT_BEST;
                        }
                    }
                }

                // 이미 해당 번호에 블록이 존재하는 경우.. RP 값으로 우열을 가려야 한다.
                else {
                    BigInteger oldRP = blockStore.getChainBlockByNumber(block.getNumber()).getCumulativeRewardPoint();
                    summary = tryConnectAndFork(block);

                    if(summary == null) {
                        ret = INVALID_BLOCK;
                    }
                    else if(block.getNumber() < blockStore.getBestBlock().getNumber()) {
                        ret = IMPORTED_NOT_BEST;
                    }
                    else if(block.getCumulativeRewardPoint().compareTo(oldRP) > 0) {
                        ret = IMPORTED_BEST;
                    }
                    else {
                        ret = IMPORTED_NOT_BEST;
                    }
                }
            } else {
                summary = null;
                ret = NO_PARENT;
            }
        }


        if (ret.isSuccessful()) {
            listener.onBlock(summary);
            listener.trace(String.format("Block chain size: [ %d ]", this.getSize()));

            if (ret == IMPORTED_BEST) {
                pendingState.processBest(block, summary.getReceipts());

                //TODO processBest 동작이 불려지지 않아서 스레드 밖으로 뺐음...
                /*eventDispatchThread.invokeLater(() ->
                        pendingState.processBest(block, summary.getReceipts()));*/
            }
        }

        return ret;
    }

    public synchronized Block createNewBlock(Block parent, List<Transaction> txs) {
        long time = TimeUtils.getRealTimestamp() / 1000;

        // adjust time to parent block this may happen due to system clocks difference
        if (parent.getTimestamp() >= time)
            time = parent.getTimestamp() + 1;

        return createNewBlock(parent, txs, time);
    }



    public synchronized Block createNewBlock(Block parent, List<Transaction> txs, long time) {

        final long blockNumber = parent.getNumber() + 1;

        final byte[] extraData = config.getBlockchainConfig().getConfigForBlock(blockNumber).getExtraData(minerExtraData, blockNumber);

        Block block = new Block(parent.getHash(),
                config.getCoinbaseKey().getAddress(),
                new byte[0], // log bloom - from tx receipts
                BigInteger.ZERO, // RewardPoint
                BigInteger.ZERO,
                blockNumber,
                parent.getGasLimit(), // (add to config ?)
                0,  // gas used - computed after running all transactions
                BigInteger.ZERO,    // mineral used - computed after running all transactions
                time,  // block time
                extraData,  // extra data
                new byte[0],  // mixHash (to mine)
                new byte[0],  // nonce   (to mine)
                new byte[0],  // receiptsRoot - computed after running all transactions
                calcTxTrie(txs),    // TransactionsRoot - computed after running all transactions
                new byte[] {0}, // stateRoot - computed after running all transactions
                txs);


        Repository track = repository.getSnapshotTo(parent.getStateRoot());


        // 블록의 RewardPoint를 계산한다.
        // TODO 블록 생성 후에 등록하도록 수정했는데, stateRoot 값에 영향을 주지 않는지 확인해서, 만약 영향을 주면 다시 주석을 해제해야한다.
        //block.getHeader().setRewardPoint(RewardPointUtil.calcRewardPoint(minerCoinbase, track.getBalance(minerCoinbase), parent.getHash()));
        Block balanceBlock = parent;
        for(int i = 0 ; i < 10 ; i++) {
            if(balanceBlock.getNumber() > 0) {
                balanceBlock = getBlockByHash(balanceBlock.getParentHash());
            } else {
                break;
            }
        }
        Repository repo = pendingState.getRepository().getSnapshotTo(balanceBlock.getStateRoot());


        BigInteger balance = repo.getBalance(minerCoinbase);
        byte[] seed = RewardPointUtil.calcSeed(minerCoinbase, balance, parent.getHash());
        BigInteger rp = RewardPointUtil.calcRewardPoint(seed, balance);
        BigInteger cumulativeRP = parent.getCumulativeRewardPoint().add(rp);

        block.setNonce(ByteUtil.bigIntegerToBytes(balance));
        block.setMixHash(seed);
        block.setRewardPoint(rp);
        block.setCumulativeRewardPoint(cumulativeRP);


        // 블록 내용을 실행-
        BlockSummary summary = applyBlock(track, block);
        List<TransactionReceipt> receipts = summary.getReceipts();
        block.setStateRoot(track.getRoot());

        Bloom logBloom = new Bloom();
        for (TransactionReceipt receipt : receipts) {
            logBloom.or(receipt.getBloomFilter());
        }
        block.getHeader().setLogsBloom(logBloom.getData());
        block.getHeader().setGasUsed(receipts.size() > 0 ? receipts.get(receipts.size() - 1).getCumulativeGasLong() : 0);
        block.getHeader().setMineralUsed(receipts.size() > 0 ? receipts.get(receipts.size() - 1).getCumulativeMineralBI() : BigInteger.ZERO);
        block.getHeader().setReceiptsRoot(calcReceiptsTrie(receipts));

        /* 블록에도 채굴자의 서명을 기입하도록 변경하였다.
         * 왜냐하면, 채굴의 우선순위를 정하는 RP 값은 채굴자의 잔고에 따라서 달라지는데
         * 악의적인 사용자가 RP 값이 높은 주소로 블록을 생성함으로써
         * transaction을 조작할 여지가 존재한다고 판단했기 때문 */
        block.getHeader().sign(config.getCoinbaseKey());

        return block;
    }

    @Override
    public BlockSummary add(Block block) {
        throw new RuntimeException("Not supported");
    }

    //    @Override
    public synchronized BlockSummary add(Repository repo, final Block block) {
        BlockSummary summary = addImpl(repo, block);

        if (summary == null) {
            stateLogger.warn("Trying to reimport the block for debug...");

            try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace(); }

            BlockSummary summary1 = addImpl(repo.getSnapshotTo(getBestBlock().getStateRoot()), block);
            stateLogger.warn("Second import trial " + (summary1 == null ? "FAILED" : "OK"));
            if (summary1 != null) {
                if (config.exitOnBlockConflict()) {
                    stateLogger.error("Inconsistent behavior, exiting...");
                    System.exit(-1);
                } else {
                    return summary1;
                }
            }
        }
        return summary;
    }

    private synchronized BlockSummary addImpl(Repository repo, final Block block) {

        if (block == null)
            return null;

        if (block.getNumber() > exitOn) {
            System.out.print("Exiting after block.number: " + bestBlock.getNumber());
            dbFlushManager.flushSync();
            System.exit(-1);
        }

        if (!isValid(repo, block)) {
            logger.warn("Invalid block with number: {}", block.getNumber());
            return null;
        }

//        Repository track = repo.startTracking();
        byte[] origRoot = repo.getRoot();


        // keep chain continuity
//        if (!Arrays.equals(bestBlock.getHash(),
//                block.getParentHash())) return null;

        if (block.getNumber() >= config.traceStartBlock() && config.traceStartBlock() != -1) {
            AdvancedDeviceUtils.adjustDetailedTracing(config, block.getNumber());
        }

        BlockSummary summary = processBlock(repo, block);
        final List<TransactionReceipt> receipts = summary.getReceipts();

        // Sanity checks

        if (!FastByteComparisons.equal(block.getReceiptsRoot(), calcReceiptsTrie(receipts))) {
            logger.warn("Block's given Receipt Hash doesn't match: {} != {}", Hex.toHexString(block.getReceiptsRoot()), Hex.toHexString(calcReceiptsTrie(receipts)));
            logger.warn("Calculated receipts: " + receipts);
            repo.rollback();
            summary = null;
        }

        if (!FastByteComparisons.equal(block.getLogBloom(), calcLogBloom(receipts))) {
            logger.warn("Block's given logBloom Hash doesn't match: {} != {}", Hex.toHexString(block.getLogBloom()), Hex.toHexString(calcLogBloom(receipts)));
            repo.rollback();
            summary = null;
        }

        Block parentBlock = blockStore.getBlockByHash(block.getParentHash());

        Block balanceBlock = parentBlock;
        for(int i = 0 ; i < 10 ; i++) {
            if(balanceBlock.getNumber() > 0) {
                balanceBlock = getBlockByHash(balanceBlock.getParentHash());
            } else {
                break;
            }
        }
        Repository balanceRepo = repo.getSnapshotTo(balanceBlock.getStateRoot());

        BigInteger balance = balanceRepo.getBalance(block.getCoinbase());
        byte[] seed = RewardPointUtil.calcSeed(block.getCoinbase(), balance, block.getParentHash());
        BigInteger calculatedRP = RewardPointUtil.calcRewardPoint(seed, balance);
        //RewardPoint calculatedRP = RewardPointUtil.genRewardPoint(parentBlock, block.getCoinbase(), parentRepo);


        if(!FastByteComparisons.equal(block.getNonce(), ByteUtil.bigIntegerToBytes(balance))) {
            logger.warn("Block({})'s given nonce doesn't match: {} != {}",block.getNumber(), Hex.toHexString(block.getNonce()), Hex.toHexString(ByteUtil.bigIntegerToBytes(balance)));
            repo.rollback();
            summary = null;
        }

        if(!FastByteComparisons.equal(block.getMixHash(), seed)) {
            logger.warn("Block[{}]'s given RP seed doesn't match: {} != {}", block.getNumber(), Hex.toHexString(block.getMixHash()), Hex.toHexString(seed));
            repo.rollback();
            summary = null;
        }

        // Verify reward-point
        if(BIUtil.isNotEqual(block.getRewardPoint(), calculatedRP)) {
            logger.info("{} | {}", Hex.toHexString(parentBlock.getStateRoot()), Hex.toHexString(repo.getRoot()));

            logger.info("Block({})'s given nonce : {} != {}",block.getNumber(), Hex.toHexString(block.getNonce()), Hex.toHexString(ByteUtil.bigIntegerToBytes(balance)));
            logger.warn("Block[{}]'s given RP seed : {} != {}", block.getNumber(), Hex.toHexString(block.getMixHash()), Hex.toHexString(seed));
            logger.warn("Block[{}]'s given RP seed2 : {} != {}", block.getNumber(), Hex.toHexString(RewardPointUtil.calcSeed(block.getCoinbase(), repository.getSnapshotTo(blockStore.getBlockByHash(blockStore.getBlockHashByNumber(0)).getStateRoot()).getBalance(block.getCoinbase()), parentBlock.getHash())), Hex.toHexString(RewardPointUtil.calcSeed(parentBlock.getCoinbase(), repository.getSnapshotTo(blockStore.getBlockByHash(blockStore.getBlockHashByNumber(0)).getStateRoot()).getBalance(parentBlock.getCoinbase()), parentBlock.getHash())));
            logger.warn("Block's given rewardPoint doesn't match: {} != {}", block.getRewardPoint().toString(), calculatedRP.toString());
            repo.rollback();
            summary = null;
        }

        // Verify cumulative RP
        if(BIUtil.isNotEqual(block.getCumulativeRewardPoint(), parentBlock.getCumulativeRewardPoint().add(calculatedRP))) {
            logger.warn("Block's given cumulativeRewardPoint doesn't match: {} != {}", block.getCumulativeRewardPoint().toString(), parentBlock.getCumulativeRewardPoint().add(calculatedRP));
            repo.rollback();
            summary = null;
        }

        // 자식 블록은 부모 블록이 생성된 시간 + 10초 마다 생성되어야 한다.
        // 따라서 부모가 생성된 이후에 9초 이내에 생성된 블록은 비정상적인 블록이다.
        if(block.getTimestamp() < parentBlock.getTimestamp() + 9) {
            logger.warn("Block creation time is too fast.\n: {} < {} + 9", block.getTimestamp(), parentBlock.getTimestamp());
            repo.rollback();
            summary = null;
        }

        // 블록이 생성된 시간이 미래의 시점일 경우 비정상적인 블록이다.
        long now = TimeUtils.getRealTimestamp() + 500;
        if(block.getTimestamp()*1_000L > now) {
            logger.warn("Block was created in the future.\n.\n: Block's timestamp : {} > Real Timestamp {}", block.getTimestamp()*1_000L, now);
            repo.rollback();
            summary = null;
        }

        if (!FastByteComparisons.equal(block.getStateRoot(), repo.getRoot())) {

            stateLogger.warn("BLOCK: State conflict or received invalid block. block: {} worldstate {} mismatch", block.getNumber(), Hex.toHexString(repo.getRoot()));
            stateLogger.warn("Conflict block dump: {}", Hex.toHexString(block.getEncoded()));

//            track.rollback();
//            repository.rollback();
            repository = repository.getSnapshotTo(origRoot);

            // block is bad so 'rollback' the state root to the original state
//            ((RepositoryImpl) repository).setRoot(origRoot);

//            track.rollback();
            // block is bad so 'rollback' the state root to the original state
//            ((RepositoryImpl) repository).setRoot(origRoot);

            if (config.exitOnBlockConflict()) {
                adminInfo.lostConsensus();
                System.out.println("CONFLICT: BLOCK #" + block.getNumber() + ", dump: " + Hex.toHexString(block.getEncoded()));
                System.exit(1);
            } else {
                summary = null;
            }
        }


        // 너무 오래전의 블록이 추가된 경우 오류로..
        /*if(block.getNumber() < getBestBlock().getNumber() - 3) {*/
        if (block.getNumber() < getBestBlock().getNumber()) {
            logger.warn("A block was created that should have been made long ago : {} < Best block {}", block.getNumber(), getBestBlock().getNumber());
            repo.rollback();
            summary = null;
        }


        // 일단은, 받은 블록이 오류가 없으면 BlockStore에 저장하도록 한다.
        // 새로 전달받은 블록의 부모가 BestBlock의 부모와 같을 경우, RP 값이 더 커야만 받아들이도록 한다.
        /*if(FastByteComparisons.equal(block.getParentHash(), getBestBlock().getParentHash())) {
            if(block.getRewardPoint().compareTo(getBestBlock().getRewardPoint()) <= 0) {
                logger.warn("Forking block's RP is smaller : {} < Best block {}", block.getRewardPoint(), getBestBlock().getRewardPoint());
                repo.rollback();
                summary = null;
            }
        }

        // 부모는 다른데, 높이가 같을 경우, 어떤 체인의 RP 값이 더 큰지 비교한다.
        else if(block.getNumber() == getBestBlock().getNumber()) {
            BigInteger parentTotalRP = blockStore.getTotalRewardPointForHash(block.getParentHash());
            BigInteger bestParentTotalRP = blockStore.getTotalRewardPointForHash(getBestBlock().getParentHash());

            if(parentTotalRP.compareTo(bestParentTotalRP) <= 0) {
                logger.warn("New chain's total RP is smaller : {} < Best block {}", parentTotalRP, bestParentTotalRP);
                repo.rollback();
                summary = null;
            }
        }*/


        // TODO 같은 번호에 다른 블록이 있는 경우 전달받은 블록이, 기존에 있던 블록보다 RP 값이 커야만 들어가야한다.



        if (summary != null) {
            repo.commit();
            updateTotalRewardPoint(block);
            summary.setTotalRewardPoint(block.getCumulativeRewardPoint());

            if (!byTest) {
                dbFlushManager.commit(() -> {
                    storeBlock(block, receipts);
                    repository.commit();
                });
            } else {
                storeBlock(block, receipts);
            }
        }

        return summary;
    }

    @Override
    public void flush() {
//        repository.flush();
//        stateDataSource.flush();
//        blockStore.flush();
//        transactionStore.flush();
//
//        repository = repository.getSnapshotTo(repository.getRoot());
//
//        if (isMemoryBoundFlush()) {
//            System.gc();
//        }
    }

    private boolean needFlushByMemory(double maxMemoryPercents) {
        return getRuntime().freeMemory() < (getRuntime().totalMemory() * (1 - maxMemoryPercents));
    }

    public static byte[] calcReceiptsTrie(List<TransactionReceipt> receipts) {
        Trie receiptsTrie = new TrieImpl();

        if (receipts == null || receipts.isEmpty())
            return HashUtil.EMPTY_TRIE_HASH;

        for (int i = 0; i < receipts.size(); i++) {
            receiptsTrie.put(RLP.encodeInt(i), receipts.get(i).getReceiptTrieEncoded());
        }
        return receiptsTrie.getRootHash();
    }

    private byte[] calcLogBloom(List<TransactionReceipt> receipts) {

        Bloom retBloomFilter = new Bloom();

        if (receipts == null || receipts.isEmpty())
            return retBloomFilter.getData();

        for (TransactionReceipt receipt : receipts) {
            retBloomFilter.or(receipt.getBloomFilter());
        }

        return retBloomFilter.getData();
    }



    public Block getParent(BlockHeader header) {

        return blockStore.getBlockByHash(header.getParentHash());
    }


    public boolean isValid(BlockHeader header) {
        if (parentHeaderValidator == null) return true;

        Block parentBlock = getParent(header);

        if (!parentHeaderValidator.validate(header, parentBlock.getHeader())) {

            if (logger.isErrorEnabled())
                parentHeaderValidator.logErrors(logger);

            return false;
        }

        return true;
    }

    /**
     * This mechanism enforces a homeostasis in terms of the time between blocks;
     * a smaller period between the last two blocks results in an increase in the
     * difficulty level and thus additional computation required, lengthening the
     * likely next period. Conversely, if the period is too large, the difficulty,
     * and expected time to the next block, is reduced.
     */
    private boolean isValid(Repository repo, Block block) {

        boolean isValid = true;

        if (!block.isGenesis()) {
            isValid = isValid(block.getHeader());

            // Sanity checks
            String trieHash = Hex.toHexString(block.getTxTrieRoot());
            String trieListHash = Hex.toHexString(calcTxTrie(block.getTransactionsList()));


            if (!trieHash.equals(trieListHash)) {
                logger.warn("Block's given Trie Hash doesn't match: {} != {}", trieHash, trieListHash);
                return false;
            }

//            if (!validateUncles(block)) return false;

            List<Transaction> txs = block.getTransactionsList();
            if (!txs.isEmpty()) {
//                Repository parentRepo = repository;
//                if (!Arrays.equals(bestBlock.getHash(), block.getParentHash())) {
//                    parentRepo = repository.getSnapshotTo(getBlockByHash(block.getParentHash()).getStateRoot());
//                }

                Map<ByteArrayWrapper, BigInteger> curNonce = new HashMap<>();

                for (Transaction tx : txs) {
                    byte[] txSender = tx.getSender();
                    ByteArrayWrapper key = new ByteArrayWrapper(txSender);
                    BigInteger expectedNonce = curNonce.get(key);
                    if (expectedNonce == null) {
                        expectedNonce = repo.getNonce(txSender);
                    }
                    curNonce.put(key, expectedNonce.add(ONE));
                    BigInteger txNonce = new BigInteger(1, tx.getNonce());
                    if (!expectedNonce.equals(txNonce)) {
                        logger.warn("Invalid transaction: Tx nonce {} != expected nonce {} (parent nonce: {}): {}",
                                txNonce, expectedNonce, repo.getNonce(txSender), tx);
                        return false;
                    }
                }
            }
        }

        return isValid;
    }



    public static Set<ByteArrayWrapper> getAncestors(BlockStore blockStore, Block testedBlock, int limitNum, boolean isParentBlock) {
        Set<ByteArrayWrapper> ret = new HashSet<>();
        limitNum = (int) max(0, testedBlock.getNumber() - limitNum);
        Block it = testedBlock;
        if (!isParentBlock) {
            it = blockStore.getBlockByHash(it.getParentHash());
        }
        while(it != null && it.getNumber() >= limitNum) {
            ret.add(new ByteArrayWrapper(it.getHash()));
            it = blockStore.getBlockByHash(it.getParentHash());
        }
        return ret;
    }

    /*public Set<ByteArrayWrapper> getUsedUncles(BlockStore blockStore, Block testedBlock, boolean isParentBlock) {
        Set<ByteArrayWrapper> ret = new HashSet<>();
        long limitNum = max(0, testedBlock.getNumber() - UNCLE_GENERATION_LIMIT);
        Block it = testedBlock;
        if (!isParentBlock) {
            it = blockStore.getBlockByHash(it.getParentHash());
        }
        while(it.getNumber() > limitNum) {
            for (BlockHeader uncle : it.getUncleList()) {
                ret.add(new ByteArrayWrapper(uncle.getHash()));
            }
            it = blockStore.getBlockByHash(it.getParentHash());
        }
        return ret;
    }*/

    private BlockSummary processBlock(Repository track, Block block) {

        if (!block.isGenesis() && !config.blockChainOnly()) {
            return applyBlock(track, block);
        }
        else {
            return new BlockSummary(block, new HashMap<>(), new ArrayList<>(), new ArrayList<>());
        }
    }

    private BlockSummary applyBlock(Repository track, Block block) {

        logger.debug("applyBlock: block: [{}] tx.list: [{}]", block.getNumber(), block.getTransactionsList().size());

        long saveTime = System.nanoTime();
        int txIndex = 1;
        long totalGasUsed = 0;
        BigInteger totalMineralUsed = BigInteger.ZERO;
        List<TransactionReceipt> receipts = new ArrayList<>();
        List<TransactionExecutionSummary> summaries = new ArrayList<>();

        for (Transaction tx : block.getTransactionsList()) {
            stateLogger.debug("apply block: [{}] tx: [{}] ", block.getNumber(), txIndex);

            Repository txTrack = track.startTracking();
            TransactionExecutor executor = new TransactionExecutor(
                    tx,
                    block.getCoinbase(),
                    txTrack,
                    blockStore,
                    programInvokeFactory,
                    block,
                    listener,
                    totalGasUsed,
                    totalMineralUsed)
                    .withCommonConfig(commonConfig);

            executor.init();
            executor.execute();
            executor.go();
            TransactionExecutionSummary summary = executor.finalization();

            totalGasUsed += executor.getGasUsed();
            totalMineralUsed = totalMineralUsed.add(executor.getMineralUsed());

            txTrack.commit();
            final TransactionReceipt receipt = executor.getReceipt();

            receipt.setTxStatus(receipt.isSuccessful());


            stateLogger.info("block: [{}] executed tx: [{}] \n  state: [{}]", block.getNumber(), txIndex,
                    Hex.toHexString(track.getRoot()));

            stateLogger.info("[{}] ", receipt.toString());

            if (stateLogger.isInfoEnabled()) {
                stateLogger.info("tx[{}].receipt: [{}] ", txIndex, Hex.toHexString(receipt.getEncoded()));
            }

            txIndex++;

            receipts.add(receipt);
            if (summary != null) {
                summaries.add(summary);
            }
        }

        Map<byte[], BigInteger> rewards = addReward(track, block, summaries);

        stateLogger.info("applied reward for block: [{}]  \n  state: [{}]",
                block.getNumber(),
                Hex.toHexString(track.getRoot()));


        // TODO
//        if (block.getNumber() >= config.traceStartBlock())
//            repository.dumpState(block, totalGasUsed, 0, null);

        long totalTime = System.nanoTime() - saveTime;
        adminInfo.addBlockExecTime(totalTime);
        logger.debug("block: num: [{}] hash: [{}], executed after: [{}]nano", block.getNumber(), block.getShortHash(), totalTime);

        return new BlockSummary(block, rewards, receipts, summaries);
    }

    /**
     * Add reward to block- and every uncle coinbase
     * assuming the entire block is valid.
     *
     * @param block object containing the header and uncles
     */
    private Map<byte[], BigInteger> addReward(Repository track, Block block, List<TransactionExecutionSummary> summaries) {

        Map<byte[], BigInteger> rewards = new HashMap<>();

        BigInteger blockReward = config.getBlockchainConfig().getConfigForBlock(block.getNumber()).getConstants().getBLOCK_REWARD(block.getNumber());
        BigInteger totalFees = BigInteger.ZERO;

        // 트랜잭션 수수료 보상에서 미네랄로 사용된 부분은 제외한다.
        for (TransactionExecutionSummary summary : summaries) {
            totalFees = totalFees.add(summary.getFee()).subtract(summary.getMineralUsed());
        }

        // 블록 보상의 45%는 채굴자에게, 45%는 마스터 노드에게, 10%는 APIS 재단으로 배분해야한다
        Constants constants = config.getBlockchainConfig().getConfigForBlock(block.getNumber()).getConstants();

        BigInteger totalReward = blockReward.add(totalFees);
        BigInteger minerReward = totalReward .multiply(constants.getREWARD_PORTION_MINER()).divide(constants.getREWARD_PORTION_DENOMINATOR());
        BigInteger masternodesReward = totalReward .multiply(constants.getREWARD_PORTION_MASTERNODES()).divide(constants.getREWARD_PORTION_DENOMINATOR());
        BigInteger managementReward = totalReward .subtract(minerReward).subtract(masternodesReward);

        //TODO 마스터노드와 재단 멀티시그 지갑 주소를 생성해서 넣어야 한다.
        byte[] addressMasterNode = null;
        byte[] addressManagement = null;

        // for test!
        if(addressMasterNode == null) {
            rewards.put(block.getCoinbase(), totalReward);
            track.addBalance(block.getCoinbase(), totalReward);
        } else {
            rewards.put(block.getCoinbase(), minerReward);
            rewards.put(addressMasterNode, masternodesReward);
            rewards.put(addressManagement, managementReward);

            track.addBalance(block.getCoinbase(), minerReward);
            track.addBalance(addressMasterNode, masternodesReward);
            track.addBalance(addressManagement, managementReward);
        }
        return rewards;
    }

    @Override
    public synchronized void storeBlock(Block block, List<TransactionReceipt> receipts) {

        if (fork)
            blockStore.saveBlock(block, totalRewardPoint, false);
        else
            blockStore.saveBlock(block, totalRewardPoint, true);

        for (int i = 0; i < receipts.size(); i++) {
            transactionStore.put(new TransactionInfo(receipts.get(i), block.getHash(), i));
        }

        if (pruneManager != null) {
            pruneManager.blockCommitted(block.getHeader());
        }

        logger.debug("Block saved: number: {}, hash: {}, RP: {}",
                block.getNumber(), block.getShortHash(), totalRewardPoint);

        // TODO 아무 블럭이나 BEST 블럭이 되면 안될텐데?
        //setBestBlock(block);
        setBestBlock(blockStore.getBestBlock());

        if (logger.isDebugEnabled())
            logger.debug("block added to the blockChain: index: [{}]", block.getNumber());
        if (block.getNumber() % 100 == 0)
            logger.info("*** Last block added [ #{} ]", block.getNumber());

    }


    public boolean hasParentOnTheChain(Block block) {
        return getParent(block.getHeader()) != null;
    }

    @Override
    public List<Chain> getAltChains() {
        return altChains;
    }

    @Override
    public List<Block> getGarbage() {
        return garbage;
    }

    public TransactionStore getTransactionStore() {
        return transactionStore;
    }

    @Override
    public void setBestBlock(Block block) {
        bestBlock = block;
        repository = repository.getSnapshotTo(block.getStateRoot());
    }

    @Override
    public synchronized Block getBestBlock() {
        // the method is synchronized since the bestBlock might be
        // temporarily switched to the fork while importing non-best block
        return bestBlock;
    }

    @Override
    public synchronized void close() {
        blockStore.close();
    }

    @Override
    public BigInteger getTotalRewardPoint() {
        return totalRewardPoint;
    }

    @Override
    public synchronized void updateTotalRewardPoint(Block block) {
        //totalRewardPoint = totalRewardPoint.add(block.getRewardPointBI());
        totalRewardPoint = blockStore.getTotalRewardPointForHash(block.getParentHash()).add(block.getRewardPoint());

        logger.debug("Reward Point: updated to {}", totalRewardPoint);
    }

    @Override
    public void setTotalRewardPoint (BigInteger totalRewardPoint) {
        this.totalRewardPoint= totalRewardPoint;
    }

    /**
     * 블록들을 파일에 기록한다.
     * @param block 대상 블록
     */
    private void recordBlock(Block block) {

        if (!config.recordBlocks()) return;

        String dumpDir = config.databaseDir() + "/" + config.dumpDir();

        File dumpFile = new File(dumpDir + "/blocks-rec.dmp");
        FileWriter fw = null;
        BufferedWriter bw = null;

        try {
            dumpFile.getParentFile().mkdirs();
            if (!dumpFile.exists()) dumpFile.createNewFile();

            fw = new FileWriter(dumpFile.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);

            if (bestBlock.isGenesis()) {
                bw.write(Hex.toHexString(bestBlock.getEncoded()));
                bw.write("\n\n");
                bw.write(bestBlock.toString());
            }

            //bw.write(Hex.toHexString(block.getEncoded()));
            bw.write("\n\n");
            bw.write(block.toString());

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateBlockTotDifficulties(int startFrom) {
        // no synchronization here not to lock instance for long period
        while(true) {
            synchronized (this) {
                ((IndexedBlockStore) blockStore).updateTotalRewardPoints(startFrom);
                if (startFrom == bestBlock.getNumber()) {
                    totalRewardPoint = blockStore.getTotalRewardPointForHash(bestBlock.getHash());
                    break;
                }
                startFrom++;
            }
        }
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public void setProgramInvokeFactory(ProgramInvokeFactory factory) {
        this.programInvokeFactory = factory;
    }

    public void setExitOn(long exitOn) {
        this.exitOn = exitOn;
    }

    public void setMinerCoinbase(byte[] minerCoinbase) {
        this.minerCoinbase = minerCoinbase;
    }

    @Override
    public byte[] getMinerCoinbase() {
        return minerCoinbase;
    }

    public void setMinerExtraData(byte[] minerExtraData) {
        this.minerExtraData = minerExtraData;
    }

    public boolean isBlockExist(byte[] hash) {
        return blockStore.isBlockExist(hash);
    }

    public void setParentHeaderValidator(DependentBlockHeaderRule parentHeaderValidator) {
        this.parentHeaderValidator = parentHeaderValidator;
    }

    public void setPendingState(PendingState pendingState) {
        this.pendingState = pendingState;
    }

    public PendingState getPendingState() {
        return pendingState;
    }

    /**
     * Returns up to limit headers found with following search parameters
     * [Synchronized only in blockstore, not using any synchronized BlockchainImpl methods]
     * @param identifier        Identifier of start block, by number of by hash
     * @param skip              Number of blocks to skip between consecutive headers
     * @param limit             Maximum number of headers in return
     * @param reverse           Is search reverse or not
     * @return  {@link BlockHeader}'s list or empty list if none found
     */
    @Override
    public List<BlockHeader> getListOfHeadersStartFrom(BlockIdentifier identifier, int skip, int limit, boolean reverse) {

        // Identifying block we'll move from
        Block startBlock;
        if (identifier.getHash() != null) {
            startBlock = blockStore.getBlockByHash(identifier.getHash());
        } else {
            startBlock = blockStore.getChainBlockByNumber(identifier.getNumber());
        }

        // If nothing found or provided hash is not on main chain, return empty array
        if (startBlock == null) {
            return emptyList();
        }
        if (identifier.getHash() != null) {
            Block mainChainBlock = blockStore.getChainBlockByNumber(startBlock.getNumber());
            if (!startBlock.equals(mainChainBlock)) return emptyList();
        }

        List<BlockHeader> headers;
        if (skip == 0) {
            long bestNumber = blockStore.getBestBlock().getNumber();
            headers = getContinuousHeaders(bestNumber, startBlock.getNumber(), limit, reverse);
        } else {
            headers = getGapedHeaders(startBlock, skip, limit, reverse);
        }

        return headers;
    }

    /**
     * Finds up to limit blocks starting from blockNumber on main chain
     * @param bestNumber        Number of best block
     * @param blockNumber       Number of block to start search (included in return)
     * @param limit             Maximum number of headers in response
     * @param reverse           Order of search
     * @return  headers found by query or empty list if none
     */
    private List<BlockHeader> getContinuousHeaders(long bestNumber, long blockNumber, int limit, boolean reverse) {
        int qty = getQty(blockNumber, bestNumber, limit, reverse);

        byte[] startHash = getStartHash(blockNumber, qty, reverse);

        if (startHash == null) {
            return emptyList();
        }

        List<BlockHeader> headers = blockStore.getListHeadersEndWith(startHash, qty);

        // blocks come with falling numbers
        if (!reverse) {
            Collections.reverse(headers);
        }

        return headers;
    }

    /**
     * Gets blocks from main chain with gaps between
     * @param startBlock        Block to start from (included in return)
     * @param skip              Number of blocks skipped between every header in return
     * @param limit             Maximum number of headers in return
     * @param reverse           Order of search
     * @return  headers found by query or empty list if none
     */
    private List<BlockHeader> getGapedHeaders(Block startBlock, int skip, int limit, boolean reverse) {
        List<BlockHeader> headers = new ArrayList<>();
        headers.add(startBlock.getHeader());
        int offset = skip + 1;
        if (reverse) offset = -offset;
        long currentNumber = startBlock.getNumber();
        boolean finished = false;

        while(!finished && headers.size() < limit) {
            currentNumber += offset;
            Block nextBlock = blockStore.getChainBlockByNumber(currentNumber);
            if (nextBlock == null) {
                finished = true;
            } else {
                headers.add(nextBlock.getHeader());
            }
        }

        return headers;
    }

    private int getQty(long blockNumber, long bestNumber, int limit, boolean reverse) {
        if (reverse) {
            return blockNumber - limit + 1 < 0 ? (int) (blockNumber + 1) : limit;
        } else {
            if (blockNumber + limit - 1 > bestNumber) {
                return (int) (bestNumber - blockNumber + 1);
            } else {
                return limit;
            }
        }
    }

    private byte[] getStartHash(long blockNumber, int qty, boolean reverse) {

        long startNumber;

        if (reverse) {
            startNumber = blockNumber;
        } else {
            startNumber = blockNumber + qty - 1;
        }

        Block block = blockStore.getChainBlockByNumber(startNumber);

        if (block == null) {
            return null;
        }

        return block.getHash();
    }

    /**
     * Returns list of block bodies by block hashes, stopping on first not found block
     * [Synchronized only in blockstore, not using any synchronized BlockchainImpl methods]
     * @param hashes List of hashes
     * @return List of RLP encoded block bodies
     */
    @Override
    public List<byte[]> getListOfBodiesByHashes(List<byte[]> hashes) {
        List<byte[]> bodies = new ArrayList<>(hashes.size());

        for (byte[] hash : hashes) {
            Block block = blockStore.getBlockByHash(hash);
            if (block == null) break;
            bodies.add(block.getEncodedBody());
        }

        return bodies;
    }

    private class State {
        //        Repository savedRepo = repository;
        byte[] root = repository.getRoot();
        Block savedBest = bestBlock;
        BigInteger savedTotalRewardPoint = totalRewardPoint;
    }

    public void setPruneManager(PruneManager pruneManager) {
        this.pruneManager = pruneManager;
    }
}
