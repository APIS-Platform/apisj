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

public class OsirisPreBalanceConfig extends AbstractConfig {

    private static final BigInteger SECP256K1N_HALF = Constants.getSECP256K1N().divide(BigInteger.valueOf(2));

    public static class OsirisConstants extends Constants {

        OsirisConstants() {}

        @Override
        public long getINIT_MINERAL_APPLY_BLOCK() {
            return 0;
        }

        // 마스터노드를 활성화하지 않을 예정..
        @Override
        public long getMASTERNODE_EARLYBIRD_PERIOD() {
            return Long.MAX_VALUE;
        }

        @Override
        public long getMASTERNODE_PERIOD() {
            return Long.MAX_VALUE;
        }

        @Override
        public long getMASTERNODE_REWARD_PERIOD() {
            return Long.MAX_VALUE;
        }


        /**
         * @return 다음 블록이 생성될 때까지 소요되는 시간
         */
        @Override
        public int getDURATION_LIMIT() {
            return 8;
        }

        @Override
        public long getBLOCK_TIME() {
            return 1;
        }

        @Override
        public long getBLOCK_TIME_MS() {
            return this.getBLOCK_TIME()*1000;
        }

        // 1000 APIS
        @Override
        public BigInteger getINIT_BALANCE() {
            return new BigInteger("10000000000000000000000");
        }

        /**
         * 트랜잭션이 존재할 때에만 블록을 생성하도록 한다
         * @return false
         */
        @Override
        public boolean isBlockGenerateWithoutTx() {
            return false;
        }

        /**
         * 테스트넷에서 발행량은 0
         */
        @Override
        public BigInteger getBLOCK_REWARD(long blockNumber) {
            return BigInteger.ZERO;
        }

        @Override
        public int getMIN_GAS_LIMIT() {
            return 50_000_000;
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

    public OsirisPreBalanceConfig() {
        this(new OsirisConstants());
    }

    public OsirisPreBalanceConfig(Constants constants) {
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
