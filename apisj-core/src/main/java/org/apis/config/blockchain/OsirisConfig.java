package org.apis.config.blockchain;

import org.apache.commons.lang3.ArrayUtils;
import org.apis.config.Constants;
import org.apis.config.SystemProperties;
import org.apis.core.BlockHeader;
import org.apis.core.Transaction;
import org.apis.util.Utils;
import org.apis.vm.DataWord;
import org.apis.vm.OpCode;
import org.apis.vm.program.Program;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Frontier, Homestead, Eip150, Eip160 적용했음
 * Created by Daniel on 18.03.2018.
 */
public class OsirisConfig extends AbstractConfig {

    private static final BigInteger SECP256K1N_HALF = Constants.getSECP256K1N().divide(BigInteger.valueOf(2));

    public static class FrontierConstants extends Constants {
        /**
         * 1차년도 블록 채굴 보상  392 APIS
         * 초기 발행량(9,520,000,000)의 13%(1,237,600,000)를 1년동안의 블록 수(3,153,600)로 나눈 양
         */
        private static final BigInteger INIT_BLOCK_REWARD = new BigInteger("392000000000000000000");
        private static final long BLOCKS_IN_YEAR = 3_153_600L;

        private BigInteger[] blockRewards = new BigInteger[100];
        private long[] blockNumbers = new long[100];


        FrontierConstants() {
            // 해마다 변경되는 블록 보상을 미리 계산한다.
            for(int i = 0; i < 100; i++) {
                if(i == 0) {
                    blockNumbers[i] = 0;
                    blockRewards[i] = INIT_BLOCK_REWARD;
                } else {
                    blockNumbers[i] = BLOCKS_IN_YEAR * i;
                    blockRewards[i] = blockRewards[i - 1].multiply(BigInteger.valueOf(8863)).divide(BigInteger.valueOf(10000));   // 88.63%
                }
            }
        }



        /**
         * @return 다음 블록이 생성될 때까지 소요되는 시간, 난이도 결정에 이용된다
         * TODO POS로 동작하기 때문에, 일정 시간 이전까지는 블록을 만들 수 없도록 제한해야한다.
         */
        @Override
        public int getDURATION_LIMIT() {
            return 10;
        }

        /**
         * 메인넷 시작 후 1년동안의 신규 발행량은 초기 발행량(9520000000 APIS)의 13%
         * 이후 매 1년마다 11.37% 감소한 양을 발행한다.
         * 1년차 : 1,237,600,000 APIS
         * 2년차 : 1,096,884,880 APIS ...
         *
         * 10초마다 블록이 생성되므로 1년 동안 3,153,600 블록마다 발행량이 감소한다.
         * @return 현재 블록에서의 보상
         */
        @Override
        public BigInteger getBLOCK_REWARD(long blockNumber) {

            for(int i = 0 ; i < blockNumbers.length ; i++) {
                if(blockNumber < blockNumbers[i]) {
                    return blockRewards[i - 1];
                }
            }

            return BigInteger.ZERO;
        }

        @Override
        public int getMIN_GAS_LIMIT() {
            return 50000;
        }

        @Override
        public boolean createEmptyContractOnOOG() {
            return false;
        }

        @Override
        public boolean hasDelegateCallOpcode() {
            return true;
        }
    };

    public OsirisConfig() {
        this(new FrontierConstants());
    }

    public OsirisConfig(Constants constants) {
        super(constants);
    }

    @Override
    public Constants getConstants() {
        return super.getConstants();
    }

    @Override
    public BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent) {
        return null;
    }


    @Override
    public long getTransactionCost(Transaction tx) {
        long nonZeroes = tx.nonZeroDataBytes();
        long zeroVals  = ArrayUtils.getLength(tx.getData()) - nonZeroes;

        return (tx.isContractCreation() ? getGasCost().getTRANSACTION_CREATE_CONTRACT() : getGasCost().getTRANSACTION())
                + zeroVals * getGasCost().getTX_ZERO_DATA() + nonZeroes * getGasCost().getTX_NO_ZERO_DATA();
    }

    @Override
    public boolean acceptTransactionSignature(Transaction tx) {
        if (tx.getSignature() == null) {
            return false;
        }

        // Restoring old logic. Making this through inheritance stinks too much
        if (!tx.getSignature().validateComponents() ||
                tx.getSignature().s.compareTo(SECP256K1N_HALF) > 0) {
            return false;
        }

        // 서명에서 chainId를 확인할 수 없다면 APIS의 트랜잭션 형식이 아니다. (초기 이더리움 형식)
        if(tx.getChainId() == null) {
            return false;
        }

        return  Objects.equals(getChainId(), tx.getChainId());
    }

    public boolean acceptTransactionCertificate(Transaction tx) {
        if (tx.getCertificate() == null) {
            return false;
        }

        // Restoring old logic. Making this through inheritance stinks too much
        return tx.getCertificate().validateComponents() &&
                tx.getCertificate().s.compareTo(SECP256K1N_HALF) <= 0;
    }

    @Override
    public BigInteger getBlockGasLimit() {
        return BigInteger.valueOf(getGasCost().getBLOCK_GAS_LIMIT());
    }

    @Override
    public DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws Program.OutOfGasException {
        DataWord maxAllowed = Utils.allButOne64th(availableGas);
        return requestedGas.compareTo(maxAllowed) > 0 ? maxAllowed : requestedGas;
    }

    @Override
    public DataWord getCreateGas(DataWord availableGas) {
        return Utils.allButOne64th(availableGas);
    }

    @Override
    public Integer getChainId() {
        /*
         * 이더리움에서는 EIP-155에서 특정 블록부터는 chainId가 트랜잭션에 명시되도록 하였으나
         * APIS는 최초부터 chainId를 적용하고 있으며, networkId 값이 chainId가 되도록 한다.
         */
        return SystemProperties.getDefault().networkId();
    }
}
