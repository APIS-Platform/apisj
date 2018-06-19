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
import org.apis.net.server.Channel;
import org.apis.net.server.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Daniel
 * @since 23.05.2018
 */
public class MinedBlockTask implements Callable<List<Block>> {

    private static final Logger logger = LoggerFactory.getLogger("minedBlockTask");

    private final List<Block> minedBlocks;
    private final ChannelManager channelManager;
    private final Channel receivedFrom;

    public MinedBlockTask(List<Block> minedBlocks, ChannelManager channelManager) {
        this(minedBlocks, channelManager, null);
    }

    public MinedBlockTask(List<Block> minedBlocks, ChannelManager channelManager, Channel receivedFrom) {
        this.minedBlocks = minedBlocks;
        this.channelManager = channelManager;
        this.receivedFrom = receivedFrom;
    }

    @Override
    public List<Block> call() throws Exception {

        try {
            logger.debug("Submit MinedBlocks : {}", minedBlocks.toString());
            channelManager.sendMinedBlocks(minedBlocks, receivedFrom);
            return minedBlocks;

        } catch (Throwable th) {
            logger.warn("Exception caught: {}", th);
        }
        return null;
    }
}
