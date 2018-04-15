package org.apis.net.submit;

import org.apis.core.RewardPoint;
import org.apis.net.server.Channel;
import org.apis.net.server.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class RewardPointTask implements Callable<RewardPoint> {
    private static final Logger logger = LoggerFactory.getLogger("net");

    private final RewardPoint rp;
    private final ChannelManager channelManager;
    private final Channel receivedFrom;

    public RewardPointTask(RewardPoint rp, ChannelManager channelManager) {
        this(rp, channelManager, null);
    }

    public RewardPointTask(RewardPoint rp, ChannelManager channelManager, Channel receivedFrom) {
        this.rp = rp;
        this.channelManager = channelManager;
        this.receivedFrom = receivedFrom;
    }

    @Override
    public RewardPoint call() throws Exception {

        try {
            logger.info("submit RP: {}", rp.toString());
            channelManager.sendRewardPoint(rp, receivedFrom);
            return rp;
        } catch (Throwable th) {
            logger.warn("Exception caught : {}", th);
        }
        return null;
    }
}
