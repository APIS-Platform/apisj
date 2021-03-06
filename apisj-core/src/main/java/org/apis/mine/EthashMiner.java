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
package org.apis.mine;

import com.google.common.util.concurrent.ListenableFuture;
import org.apis.core.Block;
import org.apis.core.BlockHeader;
import org.apis.config.SystemProperties;

/**
 * The adapter of Ethash for MinerIfc
 *
 * Created by Anton Nashatyrev on 26.02.2016.
 */
public class EthashMiner implements MinerIfc {

    SystemProperties config;

    public EthashMiner(SystemProperties config) {
        this.config = config;
    }

    @Override
    public ListenableFuture<MiningResult> mine(Block block) {
        return Ethash.getForBlock(config, block.getNumber()).mine(block);
    }

    @Override
    public boolean validate(BlockHeader blockHeader) {
        return Ethash.getForBlock(config, blockHeader.getNumber()).validate(blockHeader);
    }
}
