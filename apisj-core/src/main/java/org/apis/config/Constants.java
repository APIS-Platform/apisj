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

import org.apis.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * Describes different constants specific for a blockchain
 *
 * Created by Anton Nashatyrev on 25.02.2016.
 *
 */
public class Constants {
    private static final int MAXIMUM_EXTRA_DATA_SIZE = 32;
    private static final int MIN_GAS_LIMIT = 2000000;
    private static final int GAS_LIMIT_BOUND_DIVISOR = 1024;
    private static final BigInteger MINIMUM_DIFFICULTY = BigInteger.valueOf(131072);
    private static final BigInteger DIFFICULTY_BOUND_DIVISOR = BigInteger.valueOf(2048);
    private static final int EXP_DIFFICULTY_PERIOD = 100000;

    private static final BigInteger REWARD_PORTION_MINER = BigInteger.valueOf(4500);
    private static final BigInteger REWARD_PORTION_MASTERNODES = BigInteger.valueOf(4500);
    private static final BigInteger REWARD_PORTION_DENOMINATOR = BigInteger.valueOf(10000);

    private static final int UNCLE_GENERATION_LIMIT = 7;
    private static final int UNCLE_LIST_LIMIT = 2;

    private static final BigInteger MASTERNODE_GENERAL_BALANCE = BigInteger.valueOf(50_000L).multiply(BigInteger.TEN.pow(18));
    private static final BigInteger MASTERNODE_MAJOR_BALANCE = BigInteger.valueOf(200_000L).multiply(BigInteger.TEN.pow(18));
    private static final BigInteger MASTERNODE_PRIVATE_BALANCE = BigInteger.valueOf(500_000L).multiply(BigInteger.TEN.pow(18));

    private static final long MASTERNODE_GENERAL_LIMIT = 4_000L;
    private static final long MASTERNODE_MAJOR_LIMIT = 3_000L;
    private static final long MASTERNODE_PRIVATE_LIMIT = 2_000L;

    /** 마스터노드는 마지막 업데이트 이후, 이 블록 번호가 초과하기 전에 다시 업데이트해야만 상태가 유지될 수 있다. */
    private static final long MASTERNODE_UPDATING_LIMIT = 10_000L;

    //TODO 테스트를 위해서 10으로 설정.. 차후 37,037 로 수정 예정
    //private static final long MASTERNODE_REWARD_PERIOD = 37_037L;
    private static final long MASTERNODE_REWARD_PERIOD = 10;

    /** 마스터노드가 초기화되는 주기 */
    private static final long MASTERNODE_RESET_PERIOD = 90L;//777_777L; TODO 테스트후 원복할 것

    private static final long BLOCK_MINING_BREAK = 3;

    private static final long BLOCK_TIME_MS = 10_000L;

    private static final byte[] FOUNDATION_STORAGE = Hex.decode("1000000000000000000000000000000000037448");
    private static final byte[] ADDRESS_MASKING = Hex.decode("1000000000000000000000000000000000037449");
    private static final byte[] SMART_CONTRACT_CODE_CHANGER = Hex.decode("1000000000000000000000000000000000037450");
    private static final byte[] SMART_CONTRACT_CODE_FREEZER = Hex.decode("1000000000000000000000000000000000037451");
    private static final byte[] PROOF_OF_KNOWLEDGE = Hex.decode("1000000000000000000000000000000000037452");
    private static final byte[] BUY_MINERAL = Hex.decode("1000000000000000000000000000000000037453");

    private static final byte[] MASTERNODE_PLATFORM = Hex.decode("1000000000000000000000000000000000037454");
    private static final byte[] MASTERNODE_STORAGE = Hex.decode("7777777777777777777777777777777777777770");
    private static final byte[] MASTERNODE_EARLY_GENERAL = Hex.decode("7777777777777777777777777777777777777771");
    private static final byte[] MASTERNODE_EARLY_MAJOR   = Hex.decode("7777777777777777777777777777777777777772");
    private static final byte[] MASTERNODE_EARLY_PRIVATE = Hex.decode("7777777777777777777777777777777777777773");
    private static final byte[] MASTERNODE_GENERAL       = Hex.decode("7777777777777777777777777777777777777774");
    private static final byte[] MASTERNODE_MAJOR         = Hex.decode("7777777777777777777777777777777777777775");
    private static final byte[] MASTERNODE_PRIVATE       = Hex.decode("7777777777777777777777777777777777777776");
    private static final byte[] MASTERNODE_LATE_GENERAL  = Hex.decode("7777777777777777777777777777777777777777");
    private static final byte[] MASTERNODE_LATE_MAJOR    = Hex.decode("7777777777777777777777777777777777777778");
    private static final byte[] MASTERNODE_LATE_PRIVATE  = Hex.decode("7777777777777777777777777777777777777779");
    private static final byte[] MASTERNODE_EARLY_RUN_GENERAL  = Hex.decode("777777777777777777777777777777777777777a");
    private static final byte[] MASTERNODE_EARLY_RUN_MAJOR    = Hex.decode("777777777777777777777777777777777777777b");
    private static final byte[] MASTERNODE_EARLY_RUN_PRIVATE  = Hex.decode("777777777777777777777777777777777777777c");



    private static final int BEST_NUMBER_DIFF_LIMIT = 100;

    private static final BigInteger BLOCK_REWARD = new BigInteger("392000000000000000000"); // 392 APIS

    private static final BigInteger SECP256K1N = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);

    public int getDURATION_LIMIT() {
        return 10;
    }

    public BigInteger getInitialNonce() {
        return BigInteger.ZERO;
    }

    public int getMAXIMUM_EXTRA_DATA_SIZE() {
        return MAXIMUM_EXTRA_DATA_SIZE;
    }

    public int getMIN_GAS_LIMIT() {
        return MIN_GAS_LIMIT;
    }

    public int getGAS_LIMIT_BOUND_DIVISOR() {
        return GAS_LIMIT_BOUND_DIVISOR;
    }

    public BigInteger getMINIMUM_DIFFICULTY() {
        return MINIMUM_DIFFICULTY;
    }

    public BigInteger getDIFFICULTY_BOUND_DIVISOR() {
        return DIFFICULTY_BOUND_DIVISOR;
    }

    public int getEXP_DIFFICULTY_PERIOD() {
        return EXP_DIFFICULTY_PERIOD;
    }

    public int getUNCLE_GENERATION_LIMIT() {
        return UNCLE_GENERATION_LIMIT;
    }

    public int getUNCLE_LIST_LIMIT() {
        return UNCLE_LIST_LIMIT;
    }

    public int getBEST_NUMBER_DIFF_LIMIT() {
        return BEST_NUMBER_DIFF_LIMIT;
    }

    public BigInteger getBLOCK_REWARD(long blockNumber) {
        return BLOCK_REWARD;
    }

    public BigInteger getBLOCK_REWARD() {
        return BLOCK_REWARD;
    }

    public int getMAX_CONTRACT_SZIE() {
        //return Integer.MAX_VALUE;
        return 0x9000;
    }

    /**
     * @return 전체 블록 보상 중에서 채굴자가 가져가는 수익 비율을 반환한다.
     */
    public BigInteger getREWARD_PORTION_MINER() {
        return REWARD_PORTION_MINER;
    }

    /**
     * @return 전체 블록 보상 중에서 마스터노드가 가져가는 수익 비율을 반환한다.
     */
    public BigInteger getREWARD_PORTION_MASTERNODES() {
        return REWARD_PORTION_MASTERNODES;
    }

    /**
     * @return 블록 보상 비율을 퍼센트로 적용하기 위한 분모 값
     */
    public BigInteger getREWARD_PORTION_DENOMINATOR() {
        return REWARD_PORTION_DENOMINATOR;
    }

    public BigInteger getMASTERNODE_BALANCE_GENERAL() { return MASTERNODE_GENERAL_BALANCE; };
    public BigInteger getMASTERNODE_BALANCE_MAJOR() { return MASTERNODE_MAJOR_BALANCE; };
    public BigInteger getMASTERNODE_BALANCE_PRIVATE() { return MASTERNODE_PRIVATE_BALANCE; };

    public long getMASTERNODE_LIMIT_GENERAL() {return MASTERNODE_GENERAL_LIMIT; }
    public long getMASTERNODE_LIMIT_MAJOR() {return MASTERNODE_MAJOR_LIMIT; }
    public long getMASTERNODE_LIMIT_PRIVATE() {return MASTERNODE_PRIVATE_LIMIT; }
    public long getMASTERNODE_LIMIT_TOTAL() {return MASTERNODE_GENERAL_LIMIT + MASTERNODE_MAJOR_LIMIT + MASTERNODE_PRIVATE_LIMIT; }
    public long getMASTERNODE_REWARD_PERIOD() { return MASTERNODE_REWARD_PERIOD; }

    /**
     * 입력된 블록 번호가 마스터노드 보상을 지급하는 블록번호인지 확인한다.
     * @param blockNumber 검증하려는 블록 번호
     * @return TRUE 보상을 지급하는 블록이 맞을 경우
     */
    public boolean isMasternodeRewardTime(long blockNumber) {
        if(blockNumber < getBLOCKS_PER_DAY()) {
            return false;
        }

        return (blockNumber - getBLOCKS_PER_DAY()) % MASTERNODE_REWARD_PERIOD == MASTERNODE_REWARD_PERIOD - 1;
    }

    public long getBLOCK_MINING_BREAK() { return BLOCK_MINING_BREAK; }

    public long getMASTERNODE_LIMIT(BigInteger balance) {
        if(balance.compareTo(MASTERNODE_GENERAL_BALANCE) == 0) {
            return getMASTERNODE_LIMIT_GENERAL();
        } else if(balance.compareTo(MASTERNODE_MAJOR_BALANCE) == 0) {
            return getMASTERNODE_LIMIT_MAJOR();
        } else if(balance.compareTo(MASTERNODE_PRIVATE_BALANCE) == 0) {
            return getMASTERNODE_LIMIT_PRIVATE();
        } else {
            return 0;
        }
    }

    public long getBLOCK_TIME_MS() { return BLOCK_TIME_MS; }

    //public long getBLOCKS_PER_DAY() { return 24*60*60*1000/BLOCK_TIME_MS; }   // TODO 테스트후 원복할 것
    public long getBLOCKS_PER_DAY() { return 15; }

    public byte[] getFOUNDATION_STORAGE() { return FOUNDATION_STORAGE; }

    public byte[] getADDRESS_MASKING_ADDRESS() { return ADDRESS_MASKING; }

    public byte[] getSMART_CONTRACT_CODE_CHANGER() { return SMART_CONTRACT_CODE_CHANGER; }

    public byte[] getSMART_CONTRACT_CODE_FREEZER() { return SMART_CONTRACT_CODE_FREEZER; }

    public byte[] getPROOF_OF_KNOWLEDGE() { return PROOF_OF_KNOWLEDGE; }

    public byte[] getBUY_MINERAL() { return BUY_MINERAL; }

    public long getMASTERNODE_UPDATING_LIMIT() { return MASTERNODE_UPDATING_LIMIT; }

    public byte[] getMASTERNODE_PLATFORM() { return MASTERNODE_PLATFORM; };
    public byte[] getMASTERNODE_EARLY_GENERAL() { return MASTERNODE_EARLY_GENERAL; };
    public byte[] getMASTERNODE_EARLY_MAJOR() { return MASTERNODE_EARLY_MAJOR; };
    public byte[] getMASTERNODE_EARLY_PRIVATE() { return MASTERNODE_EARLY_PRIVATE; };
    public byte[] getMASTERNODE_EARLY_RUN_GENERAL() { return MASTERNODE_EARLY_RUN_GENERAL; };
    public byte[] getMASTERNODE_EARLY_RUN_MAJOR() { return MASTERNODE_EARLY_RUN_MAJOR; };
    public byte[] getMASTERNODE_EARLY_RUN_PRIVATE() { return MASTERNODE_EARLY_RUN_PRIVATE; };
    public byte[] getMASTERNODE_GENERAL() { return MASTERNODE_GENERAL; };
    public byte[] getMASTERNODE_MAJOR() { return MASTERNODE_MAJOR; };
    public byte[] getMASTERNODE_PRIVATE() { return MASTERNODE_PRIVATE; };
    public byte[] getMASTERNODE_LATE_GENERAL() { return MASTERNODE_LATE_GENERAL; };
    public byte[] getMASTERNODE_LATE_MAJOR() { return MASTERNODE_LATE_MAJOR; };
    public byte[] getMASTERNODE_LATE_PRIVATE() { return MASTERNODE_LATE_PRIVATE; };
    public byte[] getMASTERNODE_STORAGE() { return MASTERNODE_STORAGE; };
    public long getMASTERNODE_PERIOD() { return MASTERNODE_RESET_PERIOD; };


    /**
     * Introduced in the Homestead release
     */
    public boolean createEmptyContractOnOOG() {
        return true;
    }

    /**
     * New DELEGATECALL opcode introduced in the Homestead release. Before Homestead this opcode should generate
     * exception
     */
    public boolean hasDelegateCallOpcode() {return false; }

    /**
     * Introduced in the Homestead release
     */
    public static BigInteger getSECP256K1N() {
        return SECP256K1N;
    }
}
