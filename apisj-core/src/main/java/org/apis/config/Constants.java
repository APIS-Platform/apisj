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
import org.apis.util.blockchain.ApisUtil;

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

    private static final BigInteger MASTERNODE_GENERAL_COLLATERAL = BigInteger.valueOf(50_000L).multiply(BigInteger.TEN.pow(18));
    private static final BigInteger MASTERNODE_MAJOR_COLLATERAL = BigInteger.valueOf(200_000L).multiply(BigInteger.TEN.pow(18));
    private static final BigInteger MASTERNODE_PRIVATE_COLLATERAL = BigInteger.valueOf(500_000L).multiply(BigInteger.TEN.pow(18));

    private static final long MASTERNODE_GENERAL_LIMIT = 4_000L;
    private static final long MASTERNODE_MAJOR_LIMIT = 3_000L;
    private static final long MASTERNODE_PRIVATE_LIMIT = 2_000L;

    /** 마스터노드는 마지막 업데이트 이후, 이 블록 번호가 초과하기 전에 다시 업데이트해야만 상태가 유지될 수 있다. */
    private static final long MASTERNODE_UPDATING_LIMIT = 10_000L;

    private static final long BLOCK_TIME_MS = 8_000L;

    private static final BigInteger GENESIS_APIS = ApisUtil.convert(9_520_000_000L, ApisUtil.Unit.APIS);

    /*
     * 블록타임이 8초 일 경우 : 6일마다 이자 지급 : 64_800 Blocks
     * TODO 테스트 종료 후 원상 복귀할 것
     */
    private static final long MASTERNODE_REWARD_PERIOD = 50L;

    /** 마스터노드가 초기화되는 주기 */
    /*
     * 블록타임이 8초 일 경우 : 90일마다 초기화 : 972_000 Blocks
     * TODO 테스트 종료 후 원상 복귀할 것
     */
    private static final long MASTERNODE_RESET_PERIOD = 400L;

    /*
     * 블록타임이 8초일 경우 : 1일 동안 진행 : 10_800 Blocks (24*60*60*1000/BLOCK_TIME_MS)
     * TODO 테스트 종료 후 원상 복귀할 것
     */
    private static final long MASTERNODE_EARLYBIRD_PERIOD = 100L;


    private static final long CONTINUOUS_MINING_LIMIT = 3;



    private static final byte[] FOUNDATION_STORAGE = ByteUtil.hexStringToBytes("3affc7a364d5c725ce33be33aff9673a7dfeab64");
    private static final byte[] ADDRESS_MASKING = ByteUtil.hexStringToBytes("8aae7a8b8a34ce5a9f386ea5eed33071b3729371");
    private static final byte[] SMART_CONTRACT_CODE_CHANGER = ByteUtil.hexStringToBytes("02f5f9019447efc5ec4a19c0731481e68fc674aa");
    private static final byte[] SMART_CONTRACT_CODE_FREEZER = ByteUtil.hexStringToBytes("766810d7e2c69fe619b771d0a388eb40770feaf0");
    private static final byte[] PROOF_OF_KNOWLEDGE = ByteUtil.hexStringToBytes("17660edd98a8044f87c572e5f211cc365fc2bd04");
    private static final byte[] BUY_MINERAL = ByteUtil.hexStringToBytes("becf13e64aebf8b0637be18bcea7a892fe48e498");

    private static final byte[] MASTERNODE_PLATFORM = ByteUtil.hexStringToBytes("866962b19d403a712f2c6bca390f9f295ba2dfe9");
    
    private static final byte[] MASTERNODE_STORAGE = ByteUtil.hexStringToBytes("7777777777777777777777777777777777777770");
    private static final byte[] MASTERNODE_GENERAL_BASE_EARLY = ByteUtil.hexStringToBytes("7777777777777777777777777777777777777771");
    private static final byte[] MASTERNODE_MAJOR_BASE_EARLY = ByteUtil.hexStringToBytes("7777777777777777777777777777777777777772");
    private static final byte[] MASTERNODE_PRIVATE_BASE_EARLY = ByteUtil.hexStringToBytes("7777777777777777777777777777777777777773");
    private static final byte[] MASTERNODE_GENERAL_BASE_NORMAL = ByteUtil.hexStringToBytes("7777777777777777777777777777777777777774");
    private static final byte[] MASTERNODE_MAJOR_BASE_NORMAL = ByteUtil.hexStringToBytes("7777777777777777777777777777777777777775");
    private static final byte[] MASTERNODE_PRIVATE_BASE_NORMAL = ByteUtil.hexStringToBytes("7777777777777777777777777777777777777776");
    private static final byte[] MASTERNODE_GENERAL_BASE_LATE = ByteUtil.hexStringToBytes("7777777777777777777777777777777777777777");
    private static final byte[] MASTERNODE_MAJOR_BASE_LATE = ByteUtil.hexStringToBytes("7777777777777777777777777777777777777778");
    private static final byte[] MASTERNODE_PRIVATE_BASE_LATE = ByteUtil.hexStringToBytes("7777777777777777777777777777777777777779");
    private static final byte[] MASTERNODE_GENERAL_BASE_EARLY_RUN = ByteUtil.hexStringToBytes("777777777777777777777777777777777777777a");
    private static final byte[] MASTERNODE_MAJOR_BASE_EARLY_RUN = ByteUtil.hexStringToBytes("777777777777777777777777777777777777777b");
    private static final byte[] MASTERNODE_PRIVATE_BASE_EARLY_RUN = ByteUtil.hexStringToBytes("777777777777777777777777777777777777777c");



    private static final int BEST_NUMBER_DIFF_LIMIT = 100;

    private static final BigInteger BLOCK_REWARD = new BigInteger("314000000000000000000"); // 314 APIS

    private static final BigInteger SECP256K1N = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);

    public int getDURATION_LIMIT() {
        return 8;
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

    public BigInteger getGENESIS_APIS() {
        return GENESIS_APIS;
    }

    public BigInteger getTotalAPIS(long blockNumber) {
        return GENESIS_APIS;
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

    public BigInteger getMASTERNODE_BALANCE_GENERAL() { return MASTERNODE_GENERAL_COLLATERAL; }
    public BigInteger getMASTERNODE_BALANCE_MAJOR() { return MASTERNODE_MAJOR_COLLATERAL; }
    public BigInteger getMASTERNODE_BALANCE_PRIVATE() { return MASTERNODE_PRIVATE_COLLATERAL; }

    public long getMASTERNODE_LIMIT_GENERAL() {return MASTERNODE_GENERAL_LIMIT; }
    public long getMASTERNODE_LIMIT_MAJOR() {return MASTERNODE_MAJOR_LIMIT; }
    public long getMASTERNODE_LIMIT_PRIVATE() {return MASTERNODE_PRIVATE_LIMIT; }
    public long getMASTERNODE_LIMIT_TOTAL() {return MASTERNODE_GENERAL_LIMIT + MASTERNODE_MAJOR_LIMIT + MASTERNODE_PRIVATE_LIMIT; }
    public long getMASTERNODE_REWARD_PERIOD() { return MASTERNODE_REWARD_PERIOD; }
    public long getMASTERNODE_EARLYBIRD_PERIOD() {return MASTERNODE_EARLYBIRD_PERIOD; }

    /**
     * 입력된 블록 번호가 마스터노드 보상을 지급하는 블록번호인지 확인한다.
     * @param blockNumber 검증하려는 블록 번호
     * @return TRUE 보상을 지급하는 블록이 맞을 경우
     */
    public boolean isMasternodeRewardBlock(long blockNumber) {
        if(blockNumber < MASTERNODE_EARLYBIRD_PERIOD) {
            return false;
        }

        return (blockNumber - MASTERNODE_EARLYBIRD_PERIOD) % getMASTERNODE_REWARD_PERIOD() == getMASTERNODE_REWARD_PERIOD() - 1;
    }

    public long getCONTINUOUS_MINING_LIMIT() { return CONTINUOUS_MINING_LIMIT; }

    public long getMASTERNODE_LIMIT(BigInteger collateral) {
        if(collateral.compareTo(MASTERNODE_GENERAL_COLLATERAL) == 0) {
            return getMASTERNODE_LIMIT_GENERAL();
        } else if(collateral.compareTo(MASTERNODE_MAJOR_COLLATERAL) == 0) {
            return getMASTERNODE_LIMIT_MAJOR();
        } else if(collateral.compareTo(MASTERNODE_PRIVATE_COLLATERAL) == 0) {
            return getMASTERNODE_LIMIT_PRIVATE();
        } else {
            return 0;
        }
    }

    public long getBLOCK_TIME_MS() { return BLOCK_TIME_MS; }

    public long getBLOCK_TIME() { return getBLOCK_TIME_MS()/1_000L; }

    public long getBLOCKS_PER_DAY() { return 24*60*60*1000/BLOCK_TIME_MS; }


    public byte[] getFOUNDATION_STORAGE() { return FOUNDATION_STORAGE; }

    public byte[] getADDRESS_MASKING_ADDRESS() { return ADDRESS_MASKING; }

    public byte[] getSMART_CONTRACT_CODE_CHANGER() { return SMART_CONTRACT_CODE_CHANGER; }

    public byte[] getSMART_CONTRACT_CODE_FREEZER() { return SMART_CONTRACT_CODE_FREEZER; }

    public byte[] getPROOF_OF_KNOWLEDGE() { return PROOF_OF_KNOWLEDGE; }

    public byte[] getBUY_MINERAL() { return BUY_MINERAL; }

    public long getMASTERNODE_UPDATING_LIMIT() { return MASTERNODE_UPDATING_LIMIT; }

    public byte[] getMASTERNODE_PLATFORM_CONTRACT() { return MASTERNODE_PLATFORM; }

    public byte[] getMASTERNODE_GENERAL_BASE_EARLY() { return MASTERNODE_GENERAL_BASE_EARLY; }
    public byte[] getMASTERNODE_GENERAL_BASE_EARLY_RUN() { return MASTERNODE_GENERAL_BASE_EARLY_RUN; }
    public byte[] getMASTERNODE_GENERAL_BASE_NORMAL() { return MASTERNODE_GENERAL_BASE_NORMAL; }
    public byte[] getMASTERNODE_GENERAL_BASE_LATE() { return MASTERNODE_GENERAL_BASE_LATE; }

    public byte[] getMASTERNODE_MAJOR_BASE_EARLY() { return MASTERNODE_MAJOR_BASE_EARLY; }
    public byte[] getMASTERNODE_MAJOR_BASE_EARLY_RUN() { return MASTERNODE_MAJOR_BASE_EARLY_RUN; }
    public byte[] getMASTERNODE_MAJOR_BASE_NORMAL() { return MASTERNODE_MAJOR_BASE_NORMAL; }
    public byte[] getMASTERNODE_MAJOR_BASE_LATE() { return MASTERNODE_MAJOR_BASE_LATE; }

    public byte[] getMASTERNODE_PRIVATE_BASE_EARLY() { return MASTERNODE_PRIVATE_BASE_EARLY; }
    public byte[] getMASTERNODE_PRIVATE_BASE_EARLY_RUN() { return MASTERNODE_PRIVATE_BASE_EARLY_RUN; }
    public byte[] getMASTERNODE_PRIVATE_BASE_NORMAL() { return MASTERNODE_PRIVATE_BASE_NORMAL; }
    public byte[] getMASTERNODE_PRIVATE_BASE_LATE() { return MASTERNODE_PRIVATE_BASE_LATE; }

    public byte[] getMASTERNODE_STORAGE() { return MASTERNODE_STORAGE; }

    public long getMASTERNODE_PERIOD() { return MASTERNODE_RESET_PERIOD; }

    public byte[] getMASTERNODE_BASE_EARLY(BigInteger collateral) {
        return getMASTERNODE_BASE(MASTERNODE_TYPE_EARLY, collateral);
    }

    public byte[] getMASTERNODE_BASE_EARLY_RUN(BigInteger collateral) {
        return getMASTERNODE_BASE(MASTERNODE_TYPE_EARLY_RUN, collateral);
    }

    public byte[] getMASTERNODE_BASE_NORMAL(BigInteger collateral) {
        return getMASTERNODE_BASE(MASTERNODE_TYPE_NORMAL, collateral);
    }

    public byte[] getMASTERNODE_BASE_LATE(BigInteger collateral) {
        return getMASTERNODE_BASE(MASTERNODE_TYPE_LATE, collateral);
    }

    private static final int MASTERNODE_TYPE_EARLY = 1;
    private static final int MASTERNODE_TYPE_EARLY_RUN = 2;
    private static final int MASTERNODE_TYPE_NORMAL = 3;
    private static final int MASTERNODE_TYPE_LATE = 4;

    private byte[] getMASTERNODE_BASE(final int type, BigInteger collateral) {
        byte[] baseGeneral;
        byte[] baseMajor;
        byte[] basePrivate;

        switch(type) {
            case MASTERNODE_TYPE_EARLY:
                baseGeneral = MASTERNODE_GENERAL_BASE_EARLY;
                baseMajor = MASTERNODE_MAJOR_BASE_EARLY;
                basePrivate = MASTERNODE_PRIVATE_BASE_EARLY;
                break;
            case MASTERNODE_TYPE_EARLY_RUN:
                baseGeneral = MASTERNODE_GENERAL_BASE_EARLY_RUN;
                baseMajor = MASTERNODE_MAJOR_BASE_EARLY_RUN;
                basePrivate = MASTERNODE_PRIVATE_BASE_EARLY_RUN;
                break;
            case MASTERNODE_TYPE_NORMAL:
                baseGeneral = MASTERNODE_GENERAL_BASE_NORMAL;
                baseMajor = MASTERNODE_MAJOR_BASE_NORMAL;
                basePrivate = MASTERNODE_PRIVATE_BASE_NORMAL;
                break;
            case MASTERNODE_TYPE_LATE:
                baseGeneral = MASTERNODE_GENERAL_BASE_LATE;
                baseMajor = MASTERNODE_MAJOR_BASE_LATE;
                basePrivate = MASTERNODE_PRIVATE_BASE_LATE;
                break;
            default:
                return null;
        }

        if(collateral.compareTo(MASTERNODE_GENERAL_COLLATERAL) == 0) {
            return baseGeneral;
        }
        else if(collateral.compareTo(MASTERNODE_MAJOR_COLLATERAL) == 0) {
            return baseMajor;
        }
        else if(collateral.compareTo(MASTERNODE_PRIVATE_COLLATERAL) == 0) {
            return basePrivate;
        }
        return null;
    }

    /**
     * 입력된 블록 번호가 마스터노드 Normal 등급의 신청이 이루어지는 기간에 포함되는지 확인한다.
     * @param blockNumber 확인하려는 블록 번호
     * @return TRUE : Normal 참여 가능할 경우
     */
    public boolean isMasternodeNormalPeriod(long blockNumber) {
        long blockNumberInRound = blockNumber % getMASTERNODE_PERIOD();
        long day_1 = getMASTERNODE_EARLYBIRD_PERIOD();
        long day_2 = getMASTERNODE_EARLYBIRD_PERIOD()*2;

        return blockNumberInRound >= day_1 && blockNumberInRound < day_2;
    }



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
