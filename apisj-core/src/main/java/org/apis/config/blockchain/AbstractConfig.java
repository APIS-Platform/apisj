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
package org.apis.config.blockchain;

import org.apache.commons.lang3.tuple.Pair;
import org.apis.config.BlockchainConfig;
import org.apis.config.BlockchainNetConfig;
import org.apis.config.Constants;
import org.apis.config.SystemProperties;
import org.apis.core.Block;
import org.apis.core.BlockHeader;
import org.apis.core.Repository;
import org.apis.core.Transaction;
import org.apis.crypto.HashUtil;
import org.apis.db.BlockStore;
import org.apis.mine.EthashMiner;
import org.apis.mine.MinerIfc;
import org.apis.util.BIUtil;
import org.apis.vm.DataWord;
import org.apis.vm.GasCost;
import org.apis.vm.OpCode;
import org.apis.vm.program.Program;
import org.apis.core.*;
import org.apis.validator.BlockHeaderValidator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * BlockchainForkConfig is also implemented by this class - its (mostly testing) purpose to represent
 * the specific config for all blocks on the chain (kinda constant config).
 *
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public abstract class AbstractConfig implements BlockchainConfig, BlockchainNetConfig {
    private static final GasCost GAS_COST = new GasCost();

    protected Constants constants;
    protected MinerIfc miner;
    private List<Pair<Long, BlockHeaderValidator>> headerValidators = new ArrayList<>();

    public AbstractConfig() {
        this(new Constants());
    }

    public AbstractConfig(Constants constants) {
        this.constants = constants;
    }

    @Override
    public Constants getConstants() {
        return constants;
    }

    @Override
    public BlockchainConfig getConfigForBlock(long blockHeader) {
        return this;
    }

    @Override
    public Constants getCommonConstants() {
        return getConstants();
    }

    @Override
    public MinerIfc getMineAlgorithm(SystemProperties config) {
        if (miner == null) miner = new EthashMiner(config);
        return miner;
    }

    /*@Override
    public BigInteger calcDifficulty(BlockHeader curBlock, BlockHeader parent) {
        BigInteger pd = parent.getDifficultyBI();
        BigInteger quotient = pd.divide(getConstants().getDIFFICULTY_BOUND_DIVISOR());

        BigInteger sign = getCalcDifficultyMultiplier(curBlock, parent);

        BigInteger fromParent = pd.add(quotient.multiply(sign));
        BigInteger difficulty = BIUtil.max(getConstants().getMINIMUM_DIFFICULTY(), fromParent);

        int explosion = getExplosion(curBlock, parent);

        // 20만 블록 이후
        if (explosion >= 0) {
            difficulty = BIUtil.max(getConstants().getMINIMUM_DIFFICULTY(), difficulty.add(BigInteger.ONE.shiftLeft(explosion)));
        }

        // 20만 블록 이전
        return difficulty;
    }*/

    /**
     * POS 방식으로 채굴자를 선정하기 위한 RP 값을 계산한다.
     *
     * @param coinbase address of miner
     * @param balanceOfCoinbase balance of coinbase
     * @param parent block header of parent
     * @return Reward Point
     */
    @Override
    public BigInteger calcRewardPoint(byte[] coinbase, BigInteger balanceOfCoinbase, BlockHeader parent) {

        byte[] seedBytes = HashUtil.sha3(HashUtil.sha3(coinbase, balanceOfCoinbase.toByteArray()), parent.getHash());
        BigInteger seedNumber = new BigInteger(seedBytes);

        long loggedBalance = (long) Math.log(balanceOfCoinbase.divide(BigInteger.valueOf((long) Math.pow(10, 18))).doubleValue());

        return seedNumber.multiply(BigInteger.valueOf(loggedBalance));
    }


    @Override
    public boolean acceptTransactionSignature(Transaction tx) {
        return Objects.equals(tx.getChainId(), getChainId());
    }

    @Override
    public String validateTransactionChanges(BlockStore blockStore, Block curBlock, Transaction tx,
                                             Repository repository) {
        return null;
    }

    @Override
    public void hardForkTransfers(Block block, Repository repo) {}

    @Override
    public byte[] getExtraData(byte[] minerExtraData, long blockNumber) {
        return minerExtraData;
    }

    @Override
    public List<Pair<Long, BlockHeaderValidator>> headerValidators() {
        return headerValidators;
    }


    @Override
    public GasCost getGasCost() {
        return GAS_COST;
    }

    @Override
    public DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws Program.OutOfGasException {
        if (requestedGas.compareTo(availableGas) > 0) {
            throw Program.Exception.notEnoughOpGas(op, requestedGas, availableGas);
        }
        return requestedGas.clone();
    }

    @Override
    public DataWord getCreateGas(DataWord availableGas) {
        return availableGas;
    }

    @Override
    public boolean eip161() {
        return false;
    }

    @Override
    public Integer getChainId() {
        return 1;
    }

    @Override
    public boolean eip198() {
        return false;
    }

    @Override
    public boolean eip206() {
        return false;
    }

    @Override
    public boolean eip211() {
        return false;
    }

    @Override
    public boolean eip212() {
        return false;
    }

    @Override
    public boolean eip213() {
        return false;
    }

    @Override
    public boolean eip214() {
        return false;
    }

    @Override
    public boolean eip658() {
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
