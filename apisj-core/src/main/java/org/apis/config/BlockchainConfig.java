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
package org.apis.config;

import org.apache.commons.lang3.tuple.Pair;
import org.apis.core.Block;
import org.apis.core.BlockHeader;
import org.apis.core.Repository;
import org.apis.core.Transaction;
import org.apis.db.BlockStore;
import org.apis.mine.MinerIfc;
import org.apis.vm.DataWord;
import org.apis.vm.GasCost;
import org.apis.vm.OpCode;
import org.apis.vm.program.Program;
import org.apis.core.*;
import org.apis.validator.BlockHeaderValidator;

import java.math.BigInteger;
import java.util.List;

/**
 * Describes constants and algorithms used for a specific blockchain at specific stage
 *
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public interface BlockchainConfig {

    /**
     * Get blockchain constants
     */
    Constants getConstants();

    /**
     * Returns the mining algorithm
     */
    MinerIfc getMineAlgorithm(SystemProperties config);

    /* *
     * Calculates the difficulty for the block depending on the parent
     */
    //BigInteger calcDifficulty(BlockHeader curBlock, BlockHeader parent);

    /* *
     * Calculates the RP(Reward Point)
     */
    //RewardPoint calcRewardPoint(byte[] coinbase, BigInteger balanceOfCoinbase, byte[] parentHash,  long blockNumber);

    //RewardPoint calcRewardPoint(byte[] coinbase, long blockNumber, Repository repository);

    /*byte[] getRewardPointSeed();

    BigInteger getRewardPointDav();*/


    /**
     * Calculates difficulty adjustment to target mean block time
     */
    BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent);

    /**
     * Calculates transaction gas fee
     */
    long getTransactionCost(Transaction tx);

    /**
     * Validates Tx signature (introduced in Homestead)
     */
    boolean acceptTransactionSignature(Transaction tx);

    boolean acceptTransactionCertificate(Transaction tx);

    /**
     * Validates transaction by the changes made by it in the repository
     * @param blockStore
     * @param curBlock The block being imported
     * @param repositoryTrack The repository track changed by transaction
     * @return null if all is fine or String validation error
     */
    String validateTransactionChanges(BlockStore blockStore, Block curBlock, Transaction tx,
                                      Repository repositoryTrack);


    /**
     * Prior to block processing performs some repository manipulations according
     * to HardFork rules.
     * This method is normally executes the logic on a specific hardfork block only
     * for other blocks it just does nothing
     *
     * 블록 처리에 앞서 HardFork 규칙에 따라서 일부 저장소를 조작합니다.
     * 이 방법은 특정 HardFork 블록에만 실행되고 다른 블록에는 적용되지 않습니다.
     */
    void hardForkTransfers(Block block, Repository repo);

    /**
     * DAO hard fork marker
     */
    byte[] getExtraData(byte[] minerExtraData, long blockNumber);

    /**
     * Fork related validators. Ensure that connected peer operates on the same fork with us
     * For example: DAO config will have validator that checks presence of extra data in specific block
     *
     * Fork 관련 검사기. peer가 우리와 동일한 포크에 연결되어있는지 확인해야한다.
     * 예를 들어, DAO 설정에는 특정 블록에 extra data가 존재하는지 확인하는 검사기가 존재
     */
    List<Pair<Long, BlockHeaderValidator>> headerValidators();

    /**
     * EVM operations costs
     */
    GasCost getGasCost();

    /**
     * Calculates available gas to be passed for callee
     * Since EIP150
     * @param op  Opcode
     * @param requestedGas amount of gas requested by the program
     * @param availableGas available gas
     * @throws Program.OutOfGasException If passed args doesn't conform to limitations
     */
    DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws Program.OutOfGasException;

    /**
     * Calculates available gas to be passed for contract constructor
     * Since EIP150
     */
    DataWord getCreateGas(DataWord availableGas);

    BigInteger getBlockGasLimit();

    /**
     * EIP161: https://github.com/ethereum/EIPs/issues/161
     */
    boolean eip161();

    /**
     * EIP155: https://github.com/ethereum/EIPs/issues/155
     */
    Integer getChainId();

    /**
     * EIP198: https://github.com/ethereum/EIPs/pull/198
     */
    boolean eip198();

    /**
     * EIP206: https://github.com/ethereum/EIPs/pull/206
     */
    boolean eip206();

    /**
     * EIP211: https://github.com/ethereum/EIPs/pull/211
     */
    boolean eip211();

    /**
     * EIP212: https://github.com/ethereum/EIPs/pull/212
     */
    boolean eip212();

    /**
     * EIP213: https://github.com/ethereum/EIPs/pull/213
     */
    boolean eip213();

    /**
     * EIP214: https://github.com/ethereum/EIPs/pull/214
     */
    boolean eip214();

    /**
     * EIP658: https://github.com/ethereum/EIPs/pull/658
     * Replaces the intermediate state root field of the receipt with the status
     */
    boolean eip658();
}
