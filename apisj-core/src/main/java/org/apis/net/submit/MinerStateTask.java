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

import org.apis.core.MinerState;
import org.apis.core.Transaction;
import org.apis.net.server.Channel;
import org.apis.net.server.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Daniel
 * @since 23.05.2018
 */
public class MinerStateTask implements Callable<List<MinerState>> {

    private static final Logger logger = LoggerFactory.getLogger("net");

    private final List<MinerState> minerStates;
    private final ChannelManager channelManager;
    private final Channel receivedFrom;

    public MinerStateTask(MinerState minerStates, ChannelManager channelManager) {
        this(Collections.singletonList(minerStates), channelManager);
    }

    public MinerStateTask(List<MinerState> minerStates, ChannelManager channelManager) {
        this(minerStates, channelManager, null);
    }

    public MinerStateTask(List<MinerState> minerStates, ChannelManager channelManager, Channel receivedFrom) {
        this.minerStates = minerStates;
        this.channelManager = channelManager;
        this.receivedFrom = receivedFrom;
    }

    @Override
    public List<MinerState> call() throws Exception {

        try {
            logger.info("submit MinerStates: {}", minerStates.toString());
            channelManager.sendMinerState(minerStates, receivedFrom);
            return minerStates;

        } catch (Throwable th) {
            logger.warn("Exception caught: {}", th);
        }
        return null;
    }
}
