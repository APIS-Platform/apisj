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

/**
 * Describes a set of configs for a specific blockchain depending on the block number
 * E.g. the main Ethereum net has at least FrontierConfig and HomesteadConfig depending on the block
 *
 * Created by Anton Nashatyrev on 25.02.2016.
 *
 * 블록 번호에 따라서 특정 블록체인의 설정들을 설명한다.
 * 예 : 메인넷은 최소한 FrontierConfig와 HomesteadConfig를 갖고 있다.
 */
public interface BlockchainNetConfig {

    /**
     * Get the config for the specific block
     * 특정 블록에 대한 설정을 가져온다
     */
    BlockchainConfig getConfigForBlock(long blockNumber);

    /**
     * Returns the constants common for all the blocks in this blockchain
     * 이 블록체인에 있는 모든 블록에 공통된 상수를 반환한다.
     */
    Constants getCommonConstants();
}
