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
package org.apis.net.eth.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apis.core.Block;
import org.apis.core.Blockchain;
import org.apis.core.TransactionReceipt;
import org.apis.db.BlockStore;
import org.apis.mine.MinedBlockCache;
import org.apis.mine.MinerManager;
import org.apis.net.MessageQueue;
import org.apis.net.eth.EthVersion;
import org.apis.listener.EthereumListener;
import org.apis.config.SystemProperties;
import org.apis.net.eth.message.EthMessage;
import org.apis.net.eth.message.EthMessageCodes;
import org.apis.net.eth.message.StatusMessage;
import org.apis.core.*;
import org.apis.listener.CompositeEthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.net.eth.message.*;
import org.apis.net.message.ReasonCode;
import org.apis.net.server.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Process the messages between peers with 'eth' capability on the network<br>
 * Contains common logic to all supported versions
 * delegating version specific stuff to its descendants
 *
 */
public abstract class EthHandler extends SimpleChannelInboundHandler<EthMessage> implements Eth {

    private final static Logger logger = LoggerFactory.getLogger("net");

    protected Blockchain blockchain;

    protected SystemProperties config;

    protected CompositeEthereumListener ethereumListener;

    protected Channel channel;

    private MessageQueue msgQueue = null;

    protected EthVersion version;

    protected MinerManager minerManager;
    protected MinedBlockCache minedBlockCache;

    protected boolean peerDiscoveryMode = false;

    protected Block bestBlock;
    protected EthereumListener listener = new EthereumListenerAdapter() {
        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            bestBlock = block;
        }
    };

    boolean processTransactions = false;
    boolean processMinedBlocks = false;
    boolean processRewardPoint = false;

    protected EthHandler(EthVersion version) {
        this.version = version;
    }

    protected EthHandler(final EthVersion version, final SystemProperties config,
                         final Blockchain blockchain, final BlockStore blockStore,
                         final CompositeEthereumListener ethereumListener) {
        this.version = version;
        this.config = config;
        this.ethereumListener = ethereumListener;
        this.blockchain = blockchain;
        bestBlock = blockStore.getBestBlock();
        this.ethereumListener.addListener(listener);
        this.minerManager = MinerManager.getInstance();
        // when sync enabled we delay transactions processing until sync is complete
        processTransactions = !config.isSyncEnabled();
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, EthMessage msg) throws InterruptedException {

        if (EthMessageCodes.inRange(msg.getCommand().asByte(), version))
            logger.trace("EthHandler invoke: [{}]", msg.getCommand());

        ethereumListener.trace(String.format("EthHandler invoke: [%s]", msg.getCommand()));

        channel.getNodeStatistics().ethInbound.add();

        msgQueue.receivedMessage(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Eth handling failed", cause);
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        logger.debug("handlerRemoved: kill timers in EthHandler");
        ethereumListener.removeListener(listener);
        onShutdown();
    }

    public void activate() {
        logger.debug("ETH protocol activated");
        ethereumListener.trace("ETH protocol activated");
        sendStatus();
    }

    protected void disconnect(ReasonCode reason) {
        msgQueue.disconnect(reason);
        channel.getNodeStatistics().nodeDisconnectedLocal(reason);
    }

    protected void sendMessage(EthMessage message) {
        // TODO For test
        logger.info(message.toString());

        msgQueue.sendMessage(message);
        channel.getNodeStatistics().ethOutbound.add();
    }

    public StatusMessage getHandshakeStatusMessage() {
        return channel.getNodeStatistics().getEthLastInboundStatusMsg();
    }

    public void setMsgQueue(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }

    public void setPeerDiscoveryMode(boolean peerDiscoveryMode) {
        this.peerDiscoveryMode = peerDiscoveryMode;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public EthVersion getVersion() {
        return version;
    }

}