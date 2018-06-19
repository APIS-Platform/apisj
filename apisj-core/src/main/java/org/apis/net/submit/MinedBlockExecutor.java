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
package org.apis.net.submit;

import org.apis.core.Block;
import org.apis.core.BlockHeader;
import org.apis.core.MinerState;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Daniel
 * @since 15.06.2018
 */
public class MinedBlockExecutor {

    static {
        instance = new MinedBlockExecutor();
    }

    public static MinedBlockExecutor instance;
    private ExecutorService executor = Executors.newFixedThreadPool(1);

    public Future<List<Block>> submitMinedBlock(MinedBlockTask task) {
        return executor.submit(task);
    }
}
