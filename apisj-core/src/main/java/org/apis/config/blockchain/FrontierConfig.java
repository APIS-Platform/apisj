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

import org.apis.config.Constants;
import org.apis.core.Transaction;

import java.math.BigInteger;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class FrontierConfig extends OlympicConfig {

    public static class FrontierConstants extends Constants {
        /**
         * 블록 채굴 보상  5APIS
         * 현재는 블록당 고정된 비용으로 산정되어있으나-
         * TODO APIS 상황에 맞게, 초당 비용으로 변경해야 한다.
         * (이전 블록과 발견된 블록 사이 시간 차이가 보상 금액을 결정한다)
         */
        private static final BigInteger BLOCK_REWARD = new BigInteger("5000000000000000000");

        /**
         * @return 다음 블록이 생성될 때까지 소요되는 시간, 난이도 결정에 이용된다
         * TODO POS로 동작하기 때문에, 일정 시간 이전까지는 블록을 만들 수 없도록 제한해야한다.
         */
        @Override
        public int getDURATION_LIMIT() {
            return 13;
        }

        @Override
        public BigInteger getBLOCK_REWARD() {
            return BLOCK_REWARD;
        }

        @Override
        public int getMIN_GAS_LIMIT() {
            return 5000;
        }
    };

    public FrontierConfig() {
        this(new FrontierConstants());
    }

    public FrontierConfig(Constants constants) {
        super(constants);
    }


    @Override
    public boolean acceptTransactionSignature(Transaction tx) {
        if (!super.acceptTransactionSignature(tx)) return false;
        if (tx.getSignature() == null) return false;
        return tx.getSignature().validateComponents();
    }
}
