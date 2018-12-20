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
import org.apis.config.BlockchainConfig;
import org.apis.config.CommonConfig;
import org.apis.config.Constants;
import org.apis.config.SystemProperties;
import org.apis.contract.ContractLoader;
import org.apis.contract.Wink;
import org.apis.crypto.HashUtil;
import org.apis.db.BlockStore;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.util.ByteArraySet;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.Utils;
import org.apis.util.blockchain.ApisUtil;
import org.apis.vm.*;
import org.apis.vm.program.InternalTransaction;
import org.apis.vm.program.Program;
import org.apis.vm.program.ProgramResult;
import org.apis.vm.program.invoke.ProgramInvoke;
import org.apis.vm.program.invoke.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;

import static java.util.Arrays.copyOfRange;
import static org.apache.commons.lang3.ArrayUtils.getLength;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apis.crypto.HashUtil.EMPTY_DATA_HASH;
import static org.apis.util.BIUtil.*;
import static org.apis.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.apis.util.ByteUtil.toHexString;

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
    private BigInteger initDepositAPIS = BigInteger.ZERO;
    private BigInteger initDepositMNR = BigInteger.ZERO;
    private BigInteger finalUsedMNR = BigInteger.ZERO;
    private long basicTxCost = 0;
    private List<LogInfo> logs = null;
    private List<InternalTransaction> internalTxs = null;

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
        BigInteger totalCost = toBI(tx.getValue()).add(txGasCost);

        BigInteger senderMNR = track.getMineral(tx.getSender(), currentBlock.getNumber());
        if(txGasCost.compareTo(senderMNR) < 0) {
            senderMNR = txGasCost;
        }
        BigInteger senderBalance = track.getBalance(tx.getSender());

        if (!isCovers(senderBalance.add(senderMNR), totalCost)) {
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

        /*
         * 마스터노드 등록과 관련된 트랜잭션은 2FA 확인을 하지 않는다
         * 그 외의 트랜잭션은 2FA가 설정되어있는지, 일치하는지 등을 확인한다.
         *
         * If this transaction is to update the masternode state, omit 2FA.
         * Otherwise, 2FA verification is performed.
         */
        if(!isMnUpdateTx(senderBalance)) {
            if(execError != null && !execError.isEmpty()) {
                return;
            }

            byte[] proofCode = track.getProofKey(tx.getSender());
            if (proofCode != null && !FastByteComparisons.equal(proofCode, EMPTY_DATA_HASH)) {
                if (!blockchainConfig.acceptTransactionCertificate(tx)) {
                    execError("Transaction certificate not accepted: " + tx.getCertificate());
                    return;
                }

                if (!FastByteComparisons.equal(proofCode, tx.getProofCode())) {
                    execError("Transaction 2-step verification code does not match.");
                    return;
                }
            }
        } else {
            if(currentBlock.getNumber() < config.getBlockchainConfig().getConfigForBlock(currentBlock.getNumber()).getConstants().getMASTERNODE_EARLYBIRD_PERIOD()) {
                execError("The masternode join has not yet opened.");
                return;
            }
        }

        // 컨트렉트 업데이트 주소의 경우, APIS를 송금받을 수 없다.
        if(tx.getReceiveAddress() != null) {
            if (FastByteComparisons.equal(tx.getReceiveAddress(), blockchainConfig.getConstants().getSMART_CONTRACT_CODE_CHANGER()) && toBI(tx.getValue()).compareTo(BigInteger.ZERO) > 0) {
                execError("The 'SmartContract Code Updater' account can not receive APIS.");
                return;
            }
        }


        readyToExecute = true;
    }

    /**
     * Checks whether the this transaction is a masternode state update transaction.
     *
     * @param balance Sender's balance
     * @return True if this transaction is masternode state update transaction
     */
    private boolean isMnUpdateTx(BigInteger balance) {
        if(tx.getSender() == null || tx.getReceiveAddress() == null) {
            return false;
        }

        // 마스터노드를 등록하는 트랜잭션은 보내는 주소와 받는 주소가 일치한다.
        // The sender and receiver must have the same address.
        if(!FastByteComparisons.equal(tx.getSender(), tx.getReceiveAddress())) {
            return false;
        }

        // 송금액은 0이어야 한다.
        // The transfer amount should be zero.
        if(toBI(tx.getValue()).compareTo(BigInteger.ZERO) > 0) {
            return false;
        }

        // 보내는 주소의 잔고가 마스터노드 기준 금액과 일치하는지 확인한다.
        // The balance must be equal to the masternode's reference amount. (General: 50,000 | Major: 200,000 | Private: 500,000)
        Constants constants = config.getBlockchainConfig().getConfigForBlock(currentBlock.getNumber()).getConstants();
        if(constants.getMASTERNODE_LIMIT(balance) == 0) {
            return false;
        }

        // 마스터노드를 운영하려면, 2FA 인증을 등록해야만 한다.
        if(track.getProofKey(tx.getSender()) == null || FastByteComparisons.equal(track.getProofKey(tx.getSender()), EMPTY_DATA_HASH)) {
            execError("To become a masternode, an knowledge key must be registered.");
            return false;
        }

        /*
         * 트랜잭션의 data를 확인한다.
         * 1. sender가 마스터노드가 아닌 경우, tx.data는 주소 형식이어야 한다. (이자 수령 주소 등록)
         * 2. sender가 마스터노드로 등록되어 있는 경우, tx.data는 null(유지) 또는 주소 형식(변경)이어야 한다.
         * 3. Sender 주소와 이자 수령 주소는 같을 수 없다.
         * 4. 다른 마스터노드를 이자 수령 주소로 등록할 수 없다.
         *
         * Check the data of the transaction.
         * 1. If the sender is not the masternode, the tx.data must be in address format. (To register a recipient)
         * 2. If the sender is the master, the data should be null (retainer of recipients) or address format (change of recipient).
         * 3. The sender and the recipient can not be the same.
         * 4. The sender can not register another masternode's address as a recipient address.
         */
        byte[] recipient = track.getMnRecipient(tx.getSender());
        byte[] txData = tx.getData();
        if(recipient == null || FastByteComparisons.equal(recipient, HashUtil.EMPTY_DATA_HASH)) {
            if(!Utils.isValidAddress(txData)) {
                execError("tx.data(recipient) must be in address format");
                return false;
            }
        } else {
            if(txData != null && !Utils.isValidAddress(txData)) {
                execError("tx.data(recipient) must be in address format");
                return false;
            }
        }

        // Sender 주소와 이자 수령 주소는 같을 수 없다.
        // The masternode's address and the recipient's address can not be the same.
        if(txData != null && FastByteComparisons.equal(tx.getSender(), txData)) {
            execError("The masternode's address and the recipient's address can not be the same.");
            return false;
        }

        // 다른 마스터노드를 이자 수령 주소로 등록할 수 없다.
        // Do not register another masternode's address as a recipient address.
        AccountState otherMN = track.getAccountState(txData);
        if(txData != null && otherMN != null && otherMN.getMnStartBlock().compareTo(BigInteger.ZERO) > 0) {
            execError("You can not register another masternode's address as a recipient address.");
            return false;
        }

        return true;
    }


    public void execute() {
        // 기본 검증을 통과하지 못했으면 종료
        if (!readyToExecute) return;

        if (!localCall) {
            track.increaseNonce(tx.getSender());

            BigInteger txGasLimit = toBI(tx.getGasLimit());
            BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(txGasLimit);


            BigInteger senderMNR = track.getMineral(tx.getSender(), currentBlock.getNumber());
            BigInteger gasCost;

            // 미네랄 보유량이 가스총량보다 작으면, 가스 총량에서 총 미네랄을 제외하고, 모든 미네랄을 사용한다.
            if(senderMNR.compareTo(txGasCost) < 0) {
                gasCost = txGasCost.subtract(senderMNR);
                m_usedMineral = senderMNR;
            }
            // 미네랄 보유량이 가스 총량 이상이면, 가스 총량만큼만 미네랄을 사용한다.
            else {
                gasCost = BigInteger.ZERO;
                m_usedMineral = txGasCost;
            }

            track.addBalance(tx.getSender(), gasCost.negate());
            track.setMineral(tx.getSender(), senderMNR.subtract(m_usedMineral), currentBlock.getNumber());
            initDepositAPIS = gasCost;
            initDepositMNR = m_usedMineral;

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
            // Smart-Contract Code Updater
            if(FastByteComparisons.equal(targetAddress, blockchainConfig.getConstants().getSMART_CONTRACT_CODE_CHANGER()) && currentBlock.getRewardPoint().compareTo(BigInteger.ZERO) > 0) {
                byte[] data = tx.getData();
                byte[] targetContractAddress = copyOfRange(data, 0, 20);
                byte[] targetNonceBytes = copyOfRange(data, 20, 28);
                byte[] updateCode = copyOfRange(data, 28, data.length);

                /*
                 * 컨트렉트 코드를 업데이트하는 것은 일부 상황에서는 필요치 않을 수 있다.
                 * - 멀티 시그 지갑 컨트렉트의 자산을 컨트렉트 생성자가 코드를 변경해 자산을 탈취할 수 있음
                 * - ICO를 통해 투자한 자산이 악의적인 운영에 의해 탈취될 수 있음
                 * - 컨트렉트 생성자의 프라이빗 키가 해킹되어 해커에 의해 컨트렉트 자산을 탈취, 또는 데이터 조작될 수 있음
                 * - 그 외에도 컨트렉트 사용자들의 기대에 어긋나는 업데이트가 발생할 수 있음
                 *
                 * 이러한 상황을 원치 않을 경우, 컨트렉트 코드를 얼려서 향후 업데이트를 강제적으로 금지시킬 수 있다.
                 * 얼려진 컨트렉트는 다시는 업데이트가 불가하므로 신중한 결정이 요구된다.
                 *
                 * 컨트렉트를 얼리기 위해서는
                 * 해당 컨트렉트에서 1000000000000000000000000000000000037451 (freezer@apis) 주소로 call 함수를 호출해야 한다.
                 * resources/contract/ContractFreezer.sol 내의 Tester 컨트렉트에 예제 구현되어 있음.
                 */
                boolean isFrozen = ContractLoader.isContractFrozen(cacheTrack, blockStore, currentBlock, blockchainConfig, toHexString(targetContractAddress));
                if(isFrozen) {
                    String err = "The target contract is already frozen. Your code can not be changed.";
                    result.setException(new ContractCodeFrozenException(err));
                    execError(err);
                }

                else {
                    long targetNonce = ByteUtil.byteArrayToLong(targetNonceBytes);

                    byte[] contractAddress = HashUtil.calcNewAddr(tx.getSender(), ByteUtil.longToBytes(targetNonce));

                    if (FastByteComparisons.equal(contractAddress, targetContractAddress)) {
                        cacheTrack.saveCode(targetContractAddress, updateCode);
                    } else {
                        String err = "Target contract creator and the transaction sender does not match.";
                        result.setException(new ContractCreatorNotMatchException(err));
                        execError(err);
                    }
                }

                m_endGas = m_endGas.subtract(BigInteger.valueOf(basicTxCost));
                result.spendGas(basicTxCost);
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
                    // 컨트렉트 생성 결과 return 값이 존재하는 경우, 그에 대한 가스 값
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

            if(e instanceof Program.OutOfGasException) {
                execError("Not enough gas");
            } else if(e instanceof Program.IllegalOperationException) {
                execError("Reverted");
            } else {
                execError(e.getMessage() + result);
            }
        }
    }

    private void rollback() {
        cacheTrack.rollback();

        // remove touched account
        touchedAccounts.remove(tx.isContractCreation() ? tx.getContractAddress() : tx.getReceiveAddress());
    }





    public TransactionExecutionSummary finalization() {
        if (!readyToExecute) return null;

        BigInteger usedWinkerMNR = BigInteger.ZERO;
        BigInteger contractMNR = BigInteger.ZERO;

        TransactionExecutionSummary.Builder summaryBuilder = TransactionExecutionSummary.builderFor(tx)
                .gasLeftover(m_endGas)
                .logs(result.getLogInfoList())
                .result(result.getHReturn());

        if (result != null) {
            // Accumulate refunds for suicides
            // SUICIDE로 인한 수수료 반환을 적용한다.
            result.addFutureRefund(result.getDeleteAccounts().size() * config.getBlockchainConfig().getConfigForBlock(currentBlock.getNumber()).getGasCost().getSUICIDE_REFUND());
            long gasRefund = Math.min(result.getFutureRefund(), getGasUsed() / 2);
            // byte[] addr = tx.isContractCreation() ? tx.getContractAddress() : tx.getReceiveAddress();
            m_endGas = m_endGas.add(BigInteger.valueOf(gasRefund));

            /*
             * 가스 대신 소모된 미네랄의 양을 책정한다.
             * 만약 수수료보다 많은 미네랄이 소모된 경우, 초과된 미네랄은 반환해야한다.
             */
            BigInteger fee = toBI(tx.getGasLimit()).subtract(m_endGas).multiply(toBI(tx.getGasPrice()));
            BigInteger mineralUsed;
            BigInteger mineralRefund;

            /*
             * m_usedMineral 값은 execute 내에서 최초 트랜잭션 실행 시, 최대한 사용 가능한 만큼 사용한 상태이다.
             * 그런데 실제로 사용된 수수료가 초기에 사용한 미네랄의 양보다 크다면,
             * 모든 미네랄은 수수료에 포함되므로 반환할 금액이 없고
             *
             * 사용된 미네랄이 수수료 이상이라면, 남은 미네랄을 sender에게 반환해야 한다.
             */
            if(fee.compareTo(m_usedMineral) > 0) {
                mineralUsed = m_usedMineral;
                mineralRefund = BigInteger.ZERO;
            } else {
                mineralUsed = fee;
                mineralRefund = m_usedMineral.subtract(fee);
            }

            // 인터널 트랜잭션의 대상자에 마스터노드는 포함될 수 없다
            List<InternalTransaction> itxs = result.getInternalTransactions();
            for(InternalTransaction itx : itxs) {
                byte[] to = itx.getReceiveAddress();
                AccountState toState = track.getAccountState(to);
                if(toState.getMnStartBalance().compareTo(BigInteger.ZERO) > 0) {
                    String err = "Can not designate a receiver as a masternode in an internal transaction.";
                    result.setException(new NotEnoughMineralException(err));
                    break;
                }
            }


            /*
             * 실행된 컨트렉트에 윙크 이벤트가 존재하는지 확인한다.
             * 윙크 이벤트가 존재할 경우, 수수료를 부담해줘야 한다.
             */
            boolean hasWink = false;
            Wink wink;
            for(LogInfo log : result.getLogInfoList()) {
                wink = ContractLoader.parseWink(log);

                if(wink != null &&
                        wink.getBeneficiary() != null &&
                        wink.getWinker() != null &&
                        FastByteComparisons.equal(tx.getSender(), wink.getBeneficiary()) &&
                        FastByteComparisons.equal(tx.getReceiveAddress(), wink.getWinker())) {
                    hasWink = true;
                    break;
                }
            }

            if(hasWink) {
                // 컨트렉트의 미네랄 양을 가져온다
                contractMNR = track.getMineral(tx.getReceiveAddress(), currentBlock.getNumber());

                if(contractMNR.compareTo(fee) >= 0) {
                    usedWinkerMNR = fee;
                } else {
                    // 오류를 발생시킨다
                    String err = "There are not enough MNR in the contract.";
                    result.setException(new NotEnoughMineralException(err));
                    execError(err);
                }
            }

            finalUsedMNR = usedWinkerMNR.compareTo(BigInteger.ZERO) > 0 ? usedWinkerMNR : mineralUsed;

            summaryBuilder
                    .gasUsed(toBI(result.getGasUsed()))
                    .gasRefund(toBI(gasRefund))
                    .mineralUsed(finalUsedMNR)
                    .mineralRefund(usedWinkerMNR.compareTo(BigInteger.ZERO) > 0 ? mineralUsed : mineralRefund)
                    .mineralWinked(usedWinkerMNR)
                    .deletedAccounts(result.getDeleteAccounts())
                    .internalTransactions(result.getInternalTransactions());

            /*ContractDetails contractDetails = track.getContractDetails(addr);
            if (contractDetails != null) {
//                summaryBuilder.storageDiff(track.getContractDetails(addr).getStorage());
//
//                if (program != null) {
//                    summaryBuilder.touchedStorage(contractDetails.getStorage(), program.getStorageDiff());
//                }
            }*/

            if (result.getException() != null) {
                summaryBuilder.markAsFailed();
            }
        }

        TransactionExecutionSummary summary = summaryBuilder.build();



        /* 보낸 주소로 수수료 잔액을 반환한다.
         * 이 때, 미네랄이 사용된 만큼 추가적으로 반환하고
         * 만약 미네랄이 최종적으로 사용된 수수료보다 클 경우, 수수료만큼만 미네랄을 사용하고 남는 부분은 미네랄 잔고로 반환한다. */
        BigInteger refundBalance;
        BigInteger refundMineral;
        if(tx.getReceiveAddress() != null && summary.getMineralWinked().compareTo(BigInteger.ZERO) > 0) {
            refundBalance = initDepositAPIS;
            refundMineral = initDepositMNR;

            track.setMineral(tx.getReceiveAddress(), contractMNR.subtract(summary.getMineralWinked()), currentBlock.getNumber());
        } else {
            refundBalance = initDepositAPIS.add(summary.getMineralUsed()).subtract(summary.getFee());
            refundMineral = summary.getMineralRefund();
        }

        track.addBalance(tx.getSender(), refundBalance);
        track.addMineral(tx.getSender(), refundMineral, currentBlock.getNumber());



        //logger.debug("Pay total refund to sender: [{}], refund val: [{}] (MNR in refund : [{}])", Hex.toHexString(tx.getSender()), refundBalance, summary.getMineralUsed());

        /* 채굴자에게 수수료를 전송한다.
         * 단, 미네랄로 지불된 수수료는 제외한다. */
        //track.addBalance(coinbase, summary.getFee().subtract(summary.getMineralUsed()));
        touchedAccounts.add(coinbase);
        //logger.debug("Pay fees to miner: [{}], feesEarned: [{}]", Hex.toHexString(coinbase), summary.getFee());

        if (result != null) {
            logs = result.getLogInfoList();
            internalTxs = result.getInternalTransactions();
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
            receipt.setInternalTransactionList(internalTxs);
            receipt.setGasUsed(getGasUsed());
            receipt.setMineralUsed(getMineralUsed());
            receipt.setExecutionResult(getResult().getHReturn());
            receipt.setError(execError);
            receipt.setTxStatus(receipt.isSuccessful());
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
        return finalUsedMNR;
    }

    private static class ContractCodeFrozenException extends RuntimeException {
        ContractCodeFrozenException(String message) {
            super(message);
        }
    }

    private static class ContractCreatorNotMatchException extends RuntimeException {
        ContractCreatorNotMatchException(String message) {
            super(message);
        }
    }

    private static class NotEnoughMineralException extends RuntimeException {
        NotEnoughMineralException(String message) {
            super(message);
        }
    }
}
