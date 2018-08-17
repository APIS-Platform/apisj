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
package org.apis.core;

import org.apache.commons.lang3.tuple.Pair;
import org.apis.db.BlockStore;
import org.apis.db.ContractDetails;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.blockchain.ApisUtil;
import org.apis.vm.*;
import org.apis.vm.program.Program;
import org.apis.vm.program.ProgramResult;
import org.apis.vm.program.invoke.ProgramInvoke;
import org.apis.vm.program.invoke.ProgramInvokeFactory;
import org.apis.config.BlockchainConfig;
import org.apis.config.CommonConfig;
import org.apis.config.SystemProperties;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.util.ByteArraySet;
import org.apis.vm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;

import static org.apache.commons.lang3.ArrayUtils.getLength;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apis.util.BIUtil.*;
import static org.apis.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.apis.util.ByteUtil.toHexString;
import static org.apis.vm.VMUtils.saveProgramTraceFile;
import static org.apis.vm.VMUtils.zipAndEncode;

/**
 * @author Roman Mandeleil
 * @since 19.12.2014
 */
public class TransactionExecutor {

    private static final Logger logger = LoggerFactory.getLogger("TxExecute");
    private static final Logger stateLogger = LoggerFactory.getLogger("state");

    SystemProperties config;
    private CommonConfig commonConfig;
    private BlockchainConfig blockchainConfig;

    private Transaction tx;
    private Repository track;
    private Repository cacheTrack;
    private BlockStore blockStore;
    private final long gasUsedInTheBlock;
    private final BigInteger mineralUsedInTheBlock;
    private boolean readyToExecute = false;
    private String execError;

    private ProgramInvokeFactory programInvokeFactory;
    private byte[] coinbase;

    private TransactionReceipt receipt;
    private ProgramResult result = new ProgramResult();
    private Block currentBlock;

    private final EthereumListener listener;

    private VM vm;
    private Program program;

    private BigInteger m_endGas = BigInteger.ZERO;
    private BigInteger m_usedMineral = BigInteger.ZERO;
    private long basicTxCost = 0;
    private List<LogInfo> logs = null;

    private ByteArraySet touchedAccounts = new ByteArraySet();

    private boolean localCall = false;

    public TransactionExecutor(Transaction tx, byte[] coinbase, Repository track, BlockStore blockStore, ProgramInvokeFactory programInvokeFactory, Block currentBlock) {

        this(tx, coinbase, track, blockStore, programInvokeFactory, currentBlock, new EthereumListenerAdapter(), 0, BigInteger.ZERO);
    }

    public TransactionExecutor(Transaction tx, byte[] coinbase, Repository track, BlockStore blockStore,
                               ProgramInvokeFactory programInvokeFactory, Block currentBlock,
                               EthereumListener listener, long gasUsedInTheBlock, BigInteger mineralUsedInTheBlock) {

        this.tx = tx;
        this.coinbase = coinbase;
        this.track = track;
        this.cacheTrack = track.startTracking();
        this.blockStore = blockStore;
        this.programInvokeFactory = programInvokeFactory;
        this.currentBlock = currentBlock;
        this.listener = listener;
        this.gasUsedInTheBlock = gasUsedInTheBlock;
        this.mineralUsedInTheBlock = mineralUsedInTheBlock;
        this.m_endGas = toBI(tx.getGasLimit());
        withCommonConfig(CommonConfig.getDefault());
    }

    public TransactionExecutor withCommonConfig(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
        this.config = commonConfig.systemProperties();
        this.blockchainConfig = config.getBlockchainConfig().getConfigForBlock(currentBlock.getNumber());
        return this;
    }

    private void execError(String err) {
        logger.warn(err);
        execError = err;
    }

    /**
     * Do all the basic validation, if the executor
     * will be ready to scrap the transaction at the end
     * set readyToExecute = true
     *
     * 기본적은 검증을 실행한다.
     * 만약 트랜잭션을 실행할 준비가 되면 함수의 마지막에서
     * readyToExecute = true
     * 가 설정된다.
     */
    public void init() {
        basicTxCost = tx.transactionCost(config.getBlockchainConfig(), currentBlock);   // 21000

        if (localCall) {
            readyToExecute = true;
            return;
        }

        // 트랜잭션을 실행하기 전에, Address masking이 제대로 지정되어있는지 확인한다.
        if(tx.getReceiveMask() != null) {
            String receiverMask = new String(tx.getReceiveMask(), Charset.forName("UTF-8"));
            if(!receiverMask.isEmpty()) {
                byte[] receiverAddressByMask = track.getAddressByMask(receiverMask);
                if (!FastByteComparisons.equal(tx.getReceiveAddress(), receiverAddressByMask)) {
                    execError(String.format("Receiver address does not match with masked address: (Addr)%s : (Masked Addr)%s : (Mask)%s", Hex.toHexString(tx.getReceiveAddress()), Hex.toHexString(receiverAddressByMask), receiverMask));
                    return;
                }
            }
        }

        // 받는 사람이 마스터노드 일 경우, APIS가 송금되면 안된다.
        if(tx.getReceiveAddress() != null) {
            BigInteger value = ByteUtil.bytesToBigInteger(tx.getValue());
            if (track.getMnStartBlock(tx.getReceiveAddress()) > 0 && value.compareTo(BigInteger.ZERO) > 0) {
                execError(String.format("Can not send APIS to MasterNode. (Value) %sAPIS", ApisUtil.readableApis(value)));
                return;
            }
        }


        BigInteger txGasLimit = new BigInteger(1, tx.getGasLimit());
        BigInteger curBlockGasLimit = new BigInteger(1, currentBlock.getGasLimit());

        /*
         * 트랜잭션을 실행하기 전에, 블록에 담길 수 있는 트랜잭션의 gas 제한을 확인한다.
         * 이번 트랜잭션이 실행되서 블록의 가스 제한을 넘게 되면 실행하지 않는다.
         */
        boolean cumulativeGasReached = txGasLimit.add(BigInteger.valueOf(gasUsedInTheBlock)).compareTo(curBlockGasLimit) > 0;
        if (cumulativeGasReached) {
            execError(String.format("Too much gas used in this block: Require: %s Got: %s nonce: %s", curBlockGasLimit.longValue() - gasUsedInTheBlock, toBI(tx.getGasLimit()).longValue(), toBI(tx.getNonce()).longValue()));
            return;
        }

        /*
         * 트랜잭션의 가스 제한이 기본적인 가스 가격보다 낮으면 실행하지 않는다
         */
        if (txGasLimit.compareTo(BigInteger.valueOf(basicTxCost)) < 0) {
            execError(String.format("Not enough gas for transaction execution: Require: %s Got: %s", basicTxCost, txGasLimit));
            return;
        }

        /*
         * 보낸 주소의 nonce 값과 트랜잭션에 기록된 nonce 값이 다르면 실행하지 않는다
         */
        BigInteger reqNonce = track.getNonce(tx.getSender());
        BigInteger txNonce = toBI(tx.getNonce());
        if (isNotEqual(reqNonce, txNonce)) {
            execError(String.format("Invalid nonce: required: %s , tx.nonce: %s", reqNonce, txNonce));
            return;
        }


        /*
         * 최대 가스 사용량이 보낸 주소의 잔고를 넘어설 경우 실행하지 않는다
         */
        BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(txGasLimit);
        BigInteger miBalance = track.getMineral(tx.getSender(), currentBlock.getNumber());

        // 실제로 지불해야하는 가스 값은, tx 가스 값과 mineral 보유량의 차이로 결정
        // 만약 tx gas < mineral 이라면 지불할 가스 값은 0
        BigInteger gasCost = BigInteger.ZERO;
        if(miBalance.compareTo(txGasCost) < 0) {
            gasCost = txGasCost.add(miBalance.negate());
        }

        BigInteger totalCost = toBI(tx.getValue()).add(gasCost);
        BigInteger senderBalance = track.getBalance(tx.getSender());

        if (!isCovers(senderBalance, totalCost)) {
            execError(String.format("Not enough cash: Require: %s, Sender cash: %s", totalCost, senderBalance));
            return;
        }

        /*
         * 트랜잭션의 서명이 유효하지 않을경우 실행하지 않는다
         */
        if (!blockchainConfig.acceptTransactionSignature(tx)) {
            execError("Transaction signature not accepted: " + tx.getSignature());
            return;
        }

        readyToExecute = true;
    }

    public void execute() {
        // 기본 검증을 통과하지 못했으면 종료
        if (!readyToExecute) return;

        if (!localCall) {
            track.increaseNonce(tx.getSender());

            BigInteger txGasLimit = toBI(tx.getGasLimit());
            BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(txGasLimit);

            BigInteger miBalance = track.getMineral(tx.getSender(), currentBlock.getNumber());
            BigInteger gasCost;
            BigInteger paidMineral;

            // 미네랄 보유량이 가스총량보다 작으면, 가스 총량에서 총 미네랄을 제외하고, 모든 미네랄을 사용한다.
            if(miBalance.compareTo(txGasCost) < 0) {
                gasCost = txGasCost.add(miBalance.negate());
                paidMineral = miBalance;
            }
            // 미네랄 보유량이 가스 총량 이상이면, 가스 총량만큼만 미네랄을 사용한다.
            else {
                gasCost = BigInteger.ZERO;
                paidMineral = txGasCost;
            }

            m_usedMineral = paidMineral;

            track.addBalance(tx.getSender(), gasCost.negate());
            track.setMineral(tx.getSender(), miBalance.subtract(paidMineral), currentBlock.getNumber());

            //if (logger.isInfoEnabled())
            //    logger.info("Paying: txGasCost: [{}], gasPrice: [{}], gasLimit: [{}]", txGasCost, toBI(tx.getGasPrice()), txGasLimit);

            //logger.info("Paying: txGasCost: [{}], gasPrice: [{}], paidMineral[{}], gasLimit: [{}]", txGasCost, toBI(tx.getGasPrice()), miBalance, txGasLimit);
        }

        if (tx.isContractCreation()) {
            create();
        } else {
            call();
        }
    }

    // m_endGas 초기 값은 gas limit
    private void call() {
        if (!readyToExecute) return;

        byte[] targetAddress = tx.getReceiveAddress();
        PrecompiledContracts.PrecompiledContract precompiledContract = PrecompiledContracts.getContractForAddress(new DataWord(targetAddress), blockchainConfig);

        if (precompiledContract != null) {
            long requiredGas = precompiledContract.getGasForData(tx.getData());

            BigInteger spendingGas = BigInteger.valueOf(requiredGas).add(BigInteger.valueOf(basicTxCost));

            if (!localCall && m_endGas.compareTo(spendingGas) < 0) {
                // no refund
                // no endowment
                execError("Out of Gas calling precompiled contract 0x" + Hex.toHexString(targetAddress) +
                        ", required: " + spendingGas + ", left: " + m_endGas);
                m_endGas = BigInteger.ZERO;
                return;
            } else {

                m_endGas = m_endGas.subtract(spendingGas);

                // FIXME: save return for vm trace
                Pair<Boolean, byte[]> out = precompiledContract.execute(tx.getData());

                if (!out.getLeft()) {
                    execError("Error executing precompiled contract 0x" + Hex.toHexString(targetAddress));
                    m_endGas = BigInteger.ZERO;
                    return;
                }
            }

        } else {

            byte[] code = track.getCode(targetAddress);
            if (isEmpty(code)) {
                m_endGas = m_endGas.subtract(BigInteger.valueOf(basicTxCost));
                result.spendGas(basicTxCost);
            } else {
                ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(tx, currentBlock, cacheTrack, blockStore);

                programInvoke.getMinGasPrice();
                this.vm = new VM(config);
                this.program = new Program(track.getCodeHash(targetAddress), code, programInvoke, tx, config).withCommonConfig(commonConfig);
            }
        }

        BigInteger endowment = toBI(tx.getValue());
        transfer(cacheTrack, tx.getSender(), targetAddress, endowment);

        touchedAccounts.add(targetAddress);
    }

    private void create() {
        byte[] newContractAddress = tx.getContractAddress();

        AccountState existingAddr = cacheTrack.getAccountState(newContractAddress);
        if (existingAddr != null && existingAddr.isContractExist(blockchainConfig)) {
            execError("Trying to create a contract with existing contract address: 0x" + Hex.toHexString(newContractAddress));
            m_endGas = BigInteger.ZERO;
            return;
        }

        //In case of hashing collisions (for TCK tests only), check for any balance before createAccount()
        BigInteger oldBalance = track.getBalance(newContractAddress);
        cacheTrack.createAccount(newContractAddress);
        cacheTrack.addBalance(newContractAddress, oldBalance);
        if (blockchainConfig.eip161()) {
            cacheTrack.increaseNonce(newContractAddress);
        }

        if (isEmpty(tx.getData())) {
            m_endGas = m_endGas.subtract(BigInteger.valueOf(basicTxCost));
            result.spendGas(basicTxCost);
        } else {
            ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(tx, currentBlock, cacheTrack, blockStore);

            this.vm = new VM(config);
            this.program = new Program(tx.getData(), programInvoke, tx, config).withCommonConfig(commonConfig);

            // reset storage if the contract with the same address already exists
            // TCK test case only - normally this is near-impossible situation in the real network
            // TODO make via Trie.clear() without keyset
//            ContractDetails contractDetails = program.getStorage().getContractDetails(newContractAddress);
//            for (DataWord key : contractDetails.getStorageKeys()) {
//                program.storageSave(key, DataWord.ZERO);
//            }
        }

        BigInteger endowment = toBI(tx.getValue());
        transfer(cacheTrack, tx.getSender(), newContractAddress, endowment);

        touchedAccounts.add(newContractAddress);
    }

    public void go() {
        if (!readyToExecute) return;

        try {
            if (vm != null) {
                // Charge basic cost of the transaction
                program.spendGas(tx.transactionCost(config.getBlockchainConfig(), currentBlock), "TRANSACTION COST");

                if (config.playVM())
                    vm.play(program);

                result = program.getResult();
                m_endGas = toBI(tx.getGasLimit()).subtract(toBI(program.getResult().getGasUsed()));

                if (tx.isContractCreation() && !result.isRevert()) {
                    int returnDataGasValue = getLength(program.getResult().getHReturn()) * blockchainConfig.getGasCost().getCREATE_DATA();

                    if (m_endGas.compareTo(BigInteger.valueOf(returnDataGasValue)) < 0) {
                        // Not enough gas to return contract code
                        if (!blockchainConfig.getConstants().createEmptyContractOnOOG()) {
                            program.setRuntimeFailure(Program.Exception.notEnoughSpendingGas("No gas to return just created contract", returnDataGasValue, program));
                            result = program.getResult();
                        }
                        result.setHReturn(EMPTY_BYTE_ARRAY);
                    } else if (getLength(result.getHReturn()) > blockchainConfig.getConstants().getMAX_CONTRACT_SZIE()) {
                        // Contract size too large
                        program.setRuntimeFailure(Program.Exception.notEnoughSpendingGas("Contract size too large: " + getLength(result.getHReturn()), returnDataGasValue, program));
                        result = program.getResult();
                        result.setHReturn(EMPTY_BYTE_ARRAY);
                    } else {
                        // Contract successfully created
                        m_endGas = m_endGas.subtract(BigInteger.valueOf(returnDataGasValue));
                        cacheTrack.saveCode(tx.getContractAddress(), result.getHReturn());
                    }
                }

                String err = config.getBlockchainConfig().getConfigForBlock(currentBlock.getNumber()).validateTransactionChanges(blockStore, currentBlock, tx, null);
                if (err != null) {
                    program.setRuntimeFailure(new RuntimeException("Transaction changes validation failed: " + err));
                }

                if (result.getException() != null || result.isRevert()) {
                    result.getDeleteAccounts().clear();
                    result.getLogInfoList().clear();
                    result.resetFutureRefund();
                    rollback();

                    if (result.getException() != null) {
                        throw result.getException();
                    } else {
                        execError("REVERT opcode executed");
                    }
                } else {
                    touchedAccounts.addAll(result.getTouchedAccounts());
                    cacheTrack.commit();
                }

            } else {
                cacheTrack.commit();
            }

        } catch (Throwable e) {

            // TODO: catch whatever they will throw on you !!!
//            https://github.com/ethereum/cpp-ethereum/blob/develop/libethereum/Executive.cpp#L241
            rollback();
            m_endGas = BigInteger.ZERO;
            execError(e.getMessage());
        }
    }

    private void rollback() {

        cacheTrack.rollback();

        // remove touched account
        touchedAccounts.remove(tx.isContractCreation() ? tx.getContractAddress() : tx.getReceiveAddress());
    }

    public TransactionExecutionSummary finalization() {
        if (!readyToExecute) return null;

        TransactionExecutionSummary.Builder summaryBuilder = TransactionExecutionSummary.builderFor(tx)
                .gasLeftover(m_endGas)
                .logs(result.getLogInfoList())
                .result(result.getHReturn());

        if (result != null) {
            // Accumulate refunds for suicides
            result.addFutureRefund(result.getDeleteAccounts().size() * config.getBlockchainConfig().
                    getConfigForBlock(currentBlock.getNumber()).getGasCost().getSUICIDE_REFUND());
            long gasRefund = Math.min(result.getFutureRefund(), getGasUsed() / 2);      // 성공적으로 트랜잭션 실행이 종료되면 20,000gas가 환불되나 전체 소모 gas의 1/2만큼까지만 환불 가능
            byte[] addr = tx.isContractCreation() ? tx.getContractAddress() : tx.getReceiveAddress();
            m_endGas = m_endGas.add(BigInteger.valueOf(gasRefund));

            /*
             * 가스 대신 소모된 미네랄의 양을 책정한다.
             * 만약 수수료보다 많은 미네랄이 소모된 경우, 초과된 미네랄은 반환해야한다.
             */
            BigInteger fee = toBI(tx.getGasLimit()).subtract(m_endGas).multiply(toBI(tx.getGasPrice()));
            BigInteger mineralUsed;
            BigInteger mineralRefund;

            if(fee.compareTo(m_usedMineral) > 0) {
                mineralUsed = m_usedMineral;
                mineralRefund = BigInteger.ZERO;
            } else {
                mineralUsed = fee;
                mineralRefund = m_usedMineral.subtract(fee);
            }

            summaryBuilder
                    .gasUsed(toBI(result.getGasUsed()))
                    .gasRefund(toBI(gasRefund))
                    .mineralUsed(mineralUsed)
                    .mineralRefund(mineralRefund)
                    .deletedAccounts(result.getDeleteAccounts())
                    .internalTransactions(result.getInternalTransactions());

            ContractDetails contractDetails = track.getContractDetails(addr);
            if (contractDetails != null) {
                // TODO
//                summaryBuilder.storageDiff(track.getContractDetails(addr).getStorage());
//
//                if (program != null) {
//                    summaryBuilder.touchedStorage(contractDetails.getStorage(), program.getStorageDiff());
//                }
            }

            if (result.getException() != null) {
                summaryBuilder.markAsFailed();
            }
        }

        TransactionExecutionSummary summary = summaryBuilder.build();


        /* 보낸 주소로 수수료 잔액을 반환한다.
         * 이 때, 미네랄이 사용된 만큼 추가적으로 반환하고
         * 만약 미네랄이 최종적으로 사용된 수수료보다 클 경우, 수수료만큼만 미네랄을 사용하고 남는 부분은 미네랄 잔고로 반환한다. */
        BigInteger refundBalance;
        if(summary.getFee().compareTo(m_usedMineral) > 0) {
            refundBalance = summary.getFee().subtract(m_usedMineral);
        } else {
            refundBalance = BigInteger.ZERO;
        }
        track.addBalance(tx.getSender(), refundBalance);
        track.addMineral(tx.getSender(), summary.getMineralRefund(), currentBlock.getNumber());
        //logger.debug("Pay total refund to sender: [{}], refund val: [{}] (MNR in refund : [{}])", Hex.toHexString(tx.getSender()), refundBalance, summary.getMineralUsed());

        /* 채굴자에게 수수료를 전송한다.
         * 단, 미네랄로 지불된 수수료는 제외한다. */
        //track.addBalance(coinbase, summary.getFee().subtract(summary.getMineralUsed()));
        touchedAccounts.add(coinbase);
        //logger.debug("Pay fees to miner: [{}], feesEarned: [{}]", Hex.toHexString(coinbase), summary.getFee());

        if (result != null) {
            logs = result.getLogInfoList();
            // Traverse list of suicides
            for (DataWord address : result.getDeleteAccounts()) {
                track.delete(address.getLast20Bytes());
            }
        }

        if (blockchainConfig.eip161()) {
            for (byte[] acctAddr : touchedAccounts) {
                AccountState state = track.getAccountState(acctAddr);
                if (state != null && state.isEmpty()) {
                    track.delete(acctAddr);
                }
            }
        }


        if(listener != null) {
            listener.onTransactionExecuted(summary);
        }

        if (config.vmTrace() && program != null && result != null) {
            String trace = program.getTrace()
                    .result(result.getHReturn())
                    .error(result.getException())
                    .toString();


            if (config.vmTraceCompressed()) {
                trace = VMUtils.zipAndEncode(trace);
            }

            String txHash = toHexString(tx.getHash());
            VMUtils.saveProgramTraceFile(config, txHash, trace);
            if(listener != null) {
                listener.onVMTraceCreated(txHash, trace);
            }
        }

        return summary;
    }

    public TransactionExecutor setLocalCall(boolean localCall) {
        this.localCall = localCall;
        return this;
    }


    public TransactionReceipt getReceipt() {
        if (receipt == null) {
            receipt = new TransactionReceipt();
            long totalGasUsed = gasUsedInTheBlock + getGasUsed();
            receipt.setCumulativeGas(totalGasUsed);
            BigInteger totalMineralUsed = mineralUsedInTheBlock.add(getMineralUsed());
            receipt.setCumulativeMineral(totalMineralUsed);
            receipt.setTransaction(tx);
            receipt.setLogInfoList(getVMLogs());
            receipt.setGasUsed(getGasUsed());
            receipt.setMineralUsed(getMineralUsed());
            receipt.setExecutionResult(getResult().getHReturn());
            receipt.setError(execError);
//            receipt.setPostTxState(track.getRoot()); // TODO later when RepositoryTrack.getRoot() is implemented
        }
        return receipt;
    }

    public List<LogInfo> getVMLogs() {
        return logs;
    }

    public ProgramResult getResult() {
        return result;
    }

    public long getGasUsed() {
        return toBI(tx.getGasLimit()).subtract(m_endGas).longValue();
    }

    public BigInteger getMineralUsed() {
        BigInteger fee = toBI(tx.getGasLimit()).subtract(m_endGas).multiply(toBI(tx.getGasPrice()));
        BigInteger mineralUsed;

        if(fee.compareTo(m_usedMineral) < 0) {
            mineralUsed = fee;
        } else {
            mineralUsed = m_usedMineral;
        }

        return mineralUsed;
    }
}
