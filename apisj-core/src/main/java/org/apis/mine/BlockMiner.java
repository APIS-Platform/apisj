package org.apis.mine;

import org.apis.config.Constants;
import org.apis.config.SystemProperties;
import org.apis.contract.ContractLoader;
import org.apis.core.*;
import org.apis.crypto.ECKey;
import org.apis.db.BlockStore;
import org.apis.facade.Apis;
import org.apis.facade.ApisImpl;
import org.apis.listener.CompositeEthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
public class BlockMiner {
    private static final Logger logger = LoggerFactory.getLogger("mine");

    private Blockchain blockchain;

    private BlockStore blockStore;

    @Autowired
    private Apis apis;

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
                boolean miningStarted = checkMiningReady();
                if(!miningStarted) {
                    updateMinedBlocks();
                }
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
        MinedBlockCache cache = MinedBlockCache.getInstance();
        List<Block> receivedBlocks = cache.getBestMinedBlocks();

        for (Block block : receivedBlocks) {
            if(block != null) {
                if(blockStore.getBlockByHash(block.getHash()) == null) {
                    if(isSyncDone) {
                        ((ApisImpl) apis).addNewMinedBlock(block);
                    }
                    else {
                        blockchain.tryToConnect(block);
                    }
                }
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


        Block bestBlock = blockchain.getBestBlock();
        Constants constants = config.getBlockchainConfig().getConfigForBlock(bestBlock.getNumber()).getConstants();

        // 아직 마스터노드 참여가 시작되지 않은 경우는 생략한다.
        if(bestBlock.getNumber() < constants.getMASTERNODE_EARLYBIRD_PERIOD()) {
            return;
        }

        // It prevents checking multiple times in the same block number.
        if(bestBlock.getNumber() - lastMnCheckedBlock == 0) {
            return;
        }
        long blockNumber = bestBlock.getNumber();
        lastMnCheckedBlock = blockNumber;


        // Get the state of the master node.
        Repository mnRepo = ((Repository) apis.getRepository()).getSnapshotTo(bestBlock.getStateRoot());
        AccountState mnState = mnRepo.getAccountState(mnKey.getAddress());

        /*
         * 잔고가 마스터노드 기준 금액과 동일해야한다.
         * The balance should be equal to the master node reference amount.
         */
        BigInteger mnBalance = mnRepo.getBalance(mnKey.getAddress());
        if (constants.getMASTERNODE_LIMIT(mnBalance) == 0) {
            return;
        }



        /* ---------------------------------------------------------------------------------------------------
         * 상태 값의 MnStartBalance 값이 0일 경우, 마스터노드가 아니라는 뜻이다.
         * If the MnStartBalance value of the AccountStatus  is 0, it means that this address is not masternode.
         */
        if(mnState.getMnStartBalance().compareTo(BigInteger.ZERO) == 0) {
            // 마스터노드 갯수를 확인한다. 여유가 있을 경우 시도한다.
            long limit = constants.getMASTERNODE_LIMIT(mnBalance);
            List<byte[]> mns = new ArrayList<>();
            mns.addAll(mnRepo.getMasterNodeList(constants.getMASTERNODE_BASE_EARLY_RUN(mnBalance)));
            mns.addAll(mnRepo.getMasterNodeList(constants.getMASTERNODE_BASE_NORMAL(mnBalance)));
            mns.addAll(mnRepo.getMasterNodeList(constants.getMASTERNODE_BASE_LATE(mnBalance)));

            if(mns.size() < limit) {
                sendMasternodeTransaction(mnKey, blockNumber);
            }
        }
        /*
         * 현재 마스터노드가 업데이트 대상 목록에 존재하는지 확인하고 존재한다면 업데이트를 실시한다.
         * Check whether the current masternode exists in the updating target list, and update if it exists.
         */
        else {
            List<byte[]> updatingList = mnRepo.getNodeListToCheckExpiration(blockNumber);
            for(byte[] mn : updatingList) {
                if(FastByteComparisons.equal(mn, mnKey.getAddress())) {
                    updateMano(mnKey, blockNumber);
                    return;
                }
            }

            // 또는 너무 오래 된 경우 업데이트를 한다.
            long masternodeLastUpdate = mnRepo.getMnLastBlock(mnKey.getAddress());
            long randomNum = ThreadLocalRandom.current().nextLong(5000, 7000);

            if(blockNumber - masternodeLastUpdate > randomNum) {
                updateMano(mnKey, blockNumber);
            }
        }
    }

    /**
     * 마스터노드를 등록하거나 상태를 업데이트하는 트랜잭션을 전송한다.
     * @param mnKey 마스터노드의 계정
     * @param bestNumber best block
     */
    private void sendMasternodeTransaction(ECKey mnKey, long bestNumber) {

        Repository repo = ((Repository)apis.getRepository()).getSnapshotTo(apis.getBlockchain().getBlockByNumber(bestNumber).getStateRoot());
        BigInteger nonce = repo.getNonce(mnKey.getAddress());
        BigInteger gasPrice = config.getMasternodeGasPrice();
        BigInteger gasLimit = BigInteger.valueOf(220_000L);
        long value = 0L;

        BigInteger nodeMineral = repo.getMineral(mnKey.getAddress(), bestNumber);
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
                apis.getChainIdForNextBlock());
        tx.sign(mnKey);

        logger.debug(ConsoleUtil.colorBBlue("Submit Masternode Update TX : {}"), tx.toString());

        apis.submitTransaction(tx);
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


    private long loggedBlockNumber = 0;
    private long nothingToMining = 0;


    // 마지막으로 기존 값에서 변경되어 확인된 best block의 번호
    private long lastBestNumber = 0;
    // 마지막으로 best block의 번호를 확인한 시간
    private long lastBestCheckedTime = 0;

    /**
     * 매 9초로 끝나는 시간마다 블록을 생성할 준비가 되었는지(RP 값이 적당한지) 확인한다.
     */
    private synchronized boolean checkMiningReady() {
        // Check whether the mining function is activated.
        // Coinbase key must be set because the block must include the miner's signature.
        if(!config.minerStart() || config.getCoinbaseKey() == null || config.getMinerCoinbase() == null) {
            return false;
        }

        final long now = TimeUtils.getRealTimestamp();

        Block bestBlock = blockchain.getBestBlock();
        Block bestPendingBlock = ((PendingStateImpl) pendingState).getBestBlock();
        if(bestBlock == null || bestPendingBlock == null) {
            logger.debug("The parent block is not prepared.");
            return false;
        }

        if(bestBlock.getNumber() > lastBestNumber) {
            lastBestNumber = bestBlock.getNumber();
            lastBestCheckedTime = now;
        }

        if(bestBlock.isGenesis()) {
            if(!isGeneratingBlock) {
                isSyncDone = true;
                restartMining();
                return true;
            } else {
                return false;
            }
        }

        Constants constants = config.getBlockchainConfig().getConfigForBlock(bestBlock.getNumber()).getConstants();

        /*
         * 마스터노드는 마이닝을 실행할 수 없도록 한다.
         * 블록을 생성하더라도 다른 노드에 의해 블록이 거절되고, 블랙리스트에 등록된다.
         */
        Repository bestRepo = ((Repository) apis.getRepository()).getSnapshotTo(bestBlock.getStateRoot());
        AccountState minerState = bestRepo.getAccountState(config.getMinerCoinbase());
        if(minerState == null) {
            printMiningMessage("No account for coinbase address.", bestBlock.getNumber());
            return false;
        }
        BigInteger minerMasternodeStartBlock = minerState.getMnStartBlock();
        if (minerMasternodeStartBlock.compareTo(BigInteger.ZERO) > 0) {
            printMiningMessage("The master node can not perform mining.", bestBlock.getNumber());
            return false;
        }


        /*
         * 직전 n블록 내에 내 블록이 존재할 경우 블록을 생성하지 않도록 한다.
         * 30 블록 이내에는 초기 세팅 중이므로 충분한 수의 노드가 존재하지 않을 수 있으므로 예외로 한다.
         */
        //List<ByteArrayWrapper> recentMiners = new ArrayList<>();
        if(bestBlock.getNumber() > 30) {
            long continuousMiningLimit = constants.getCONTINUOUS_MINING_LIMIT();
            Block parentBlock = blockchain.getBlockByHash(bestBlock.getHash());
            for (int i = 0; i < continuousMiningLimit; i++) {
                //recentMiners.add(new ByteArrayWrapper(parentBlock.getCoinbase()));

                if (FastByteComparisons.equal(parentBlock.getCoinbase(), config.getMinerCoinbase())) {
                    printMiningMessage(String.format("You have created #%d block. If there is a block created by coinbase within %d blocks, skip mining.", parentBlock.getNumber(), continuousMiningLimit), bestBlock.getNumber());
                    return false;
                }
                parentBlock = blockchain.getBlockByHash(parentBlock.getParentHash());
            }
        }


        /*
         * 만약 블록 채굴이 오랫동안 실행되지 않는다면 다시 블록 생성을 시도하도록 한다.
         * 마지막으로 전달받은 블록으로부터 시간이 많이 경과했을 때 블록을 생성한다.
         * 싱크가 완료되지 않은 상태에서도, 시간이 오래 경과하면 블록을 생성하도록 한다.
         */
        long blockTime = constants.getBLOCK_TIME();
        long diff = (now - lastBestCheckedTime)/1_000L;
        if(diff > blockTime*2 && diff % blockTime == 1) {
            if(!isGeneratingBlock) {
                printMiningMessage("There is no block response and will create a new block.", bestBlock.getNumber());
                restartMining();
                return true;
            } else {
                return false;
            }
        }



        // Blocks can only be created if synchronization is complete.
        if(!isSyncDone) {
            printMiningMessage("Syncing is not complete yet.", bestBlock.getNumber());
            return false;
        }

        // If miner's balance is zero, mining will not proceed.
        if(apis.getRepository().getBalance(config.getMinerCoinbase()).compareTo(BigInteger.ONE) < 0) {
            printMiningMessage("Miner's balance is zero.", bestBlock.getNumber());
            return false;
        }


        if(now - bestPendingBlock.getTimestamp()*1_000L < constants.getBLOCK_TIME_MS()) {
            //printMiningMessage("It is too early to create a block.", bestBlock.getNumber());
            return false;
        }

        // 이미 같은 부모를 이용해서 블록을 만들었으면, 더 블록을 생성하지 않는다
        if(lastMinedParentBlockHash != null && FastByteComparisons.equal(bestBlock.getHash(), lastMinedParentBlockHash)) {
            logger.debug("You have already created a block .", bestBlock.getNumber());
            return false;
        }


        // 다른 네트워크에서 전달받은 블록들과 높이 차이가 크면 블록을 생성하면 안된다.
        if(bestBlock.getNumber() < MinedBlockCache.getInstance().getBestBlockNumber() - 2) {
            printMiningMessage("Synchronization is slow.", bestBlock.getNumber());
            return false;
        }

        // 연결된 노드가 없는 경우, 블록을 생성하지 않도록 한다.
        if(blockchain.getBestBlock().getNumber() > 100 && apis.getChannelManager().getActivePeers().isEmpty()) {
            printMiningMessage("Blocks can not be created because there are no connected nodes.", bestBlock.getNumber());
            return false;
        }


        if(!isGeneratingBlock) {
            restartMining();
            return true;
        }

        nothingToMining += 1;
        if(nothingToMining > constants.getBLOCK_TIME()) {
            isGeneratingBlock = false;
        }
        return false;
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

        //-------------------------------------------------------------------
        // 마스터노드 보상 블록인 경우에, 트랜잭션을 탑재하지 않아야 하므로..
        if(config.getBlockchainConfig().getCommonConstants().isMasternodeRewardBlock(bestPendingState.getNumber() + 1)) {
            return blockchain.createNewBlock(bestPendingState, new ArrayList<>());
        }

        //-------------------------------------------------------------------
        // 일반 블록에는 트랜잭션이 같이 탑재되어야 한다.
        else {

            List<Transaction> pendingTxs = getAllPendingTransactions();
            List<Transaction> newPendingTxs = new ArrayList<>();

            for (Transaction tx : pendingTxs) {
                newPendingTxs.add(new Transaction(tx.getEncoded()));
            }

            boolean hasInvalidTx = true;
            Block newBlock = blockchain.createNewBlock(bestPendingState, newPendingTxs);

            if (newBlock == null) {
                return null;
            }

            while (hasInvalidTx) {
                BlockSummary summary = apis.replayBlock(newBlock);

                hasInvalidTx = false;
                for (int i = 0; i < summary.getReceipts().size(); i++) {
                    final TransactionReceipt receipt = summary.getReceipts().get(i);
                    final TransactionExecutionSummary txSummary = summary.getSummaries().get(i);

                    // TransactionExecutor 내에서 init() 함수에서 실행되지 못한 경우 (Too much gas used in this block 등)
                    if (txSummary == null) {
                        newPendingTxs.removeIf(tx -> FastByteComparisons.equal(tx.getHash(), receipt.getTransaction().getHash()));
                        hasInvalidTx = true;
                    }
                }

                if (hasInvalidTx) {
                    // 블록 내에 실행되지 않은 트랜잭션이 포함되어 있으므로 제거되었다.
                    logger.debug("Mining block has been removed because it contains unexecuted transactions.");

                    // 제거된 블록을 새롭게 생성
                    newBlock = blockchain.createNewBlock(bestPendingState, newPendingTxs);
                }
            }

            return newBlock;
        }
    }


    private synchronized void restartMining() {
        isGeneratingBlock = true;
        cancelCurrentBlock();

        // 부모가 genesis일 경우, 컨트렉트들을 생성한다.
        if(blockchain.getBestBlock().isGenesis()) {
            ContractLoader.initFoundationContracts(apis);
        } else if(blockchain.getBestBlock().getNumber() == 2) {
            ContractLoader.initAddressMaskingContracts(apis);
        } else if(blockchain.getBestBlock().getNumber() == 3) {
            ContractLoader.initBuyMineralContract(apis);
        } else if(blockchain.getBestBlock().getNumber() == 4) {
            ContractLoader.initEarlyBirdManagerContracts(apis);
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
            ImportResult importResult = ((ApisImpl) apis).addNewMinedBlock(newBlock);
            logger.debug("Mined block import result is " + importResult);
        }


        List<Block> minedBlocks = new ArrayList<>();
        minedBlocks.add(newBlock);
        Block parentBlock = newBlock;
        long parentTimestamp = parentBlock.getTimestamp();
        for(int i = 0; i < 4 && parentBlock.getNumber() > 1; i++) {
            parentBlock = blockchain.getBlockByHash(parentBlock.getParentHash());
            minedBlocks.add(0, parentBlock);
            if(i == 1) {
                parentTimestamp = parentBlock.getTimestamp();
            }
        }

        // 새로운 정보가 더 좋을 경우, 블록을 전파한다.
        if(MinedBlockCache.getInstance().compareMinedBlocks(minedBlocks) || newBlock.getTimestamp() - parentTimestamp > 20) {
            Block parent = blockStore.getBlockByHash(newBlock.getParentHash());

            // 너무 오랜 시간이 지났으면, 일단 채굴한 블럭을 체인에 연결한다.
            if(now - parent.getTimestamp() > config.getBlockchainConfig().getConfigForBlock(newBlock.getNumber()).getConstants().getBLOCK_TIME()*2) {
                ((ApisImpl) apis).addNewMinedBlock(newBlock);
            }

            apis.submitMinedBlock(minedBlocks);
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
