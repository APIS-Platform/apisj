package org.apis.net.submit;

import org.apis.core.RewardPoint;
import org.apis.net.server.Channel;
import org.apis.net.server.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

public class RewardPointTask implements Callable<List<RewardPoint>> {
    private static final Logger logger = LoggerFactory.getLogger("net");

    private final List<RewardPoint> rps;
    private final ChannelManager channelManager;
    private final Channel receivedFrom;

    public RewardPointTask(List<RewardPoint> rps, ChannelManager channelManager) {
        this(rps, channelManager, null);
    }

    public RewardPointTask(List<RewardPoint> rps, ChannelManager channelManager, Channel receivedFrom) {
        this.rps = rps;
        this.channelManager = channelManager;
        this.receivedFrom = receivedFrom;
    }

    @Override
    public List<RewardPoint> call() throws Exception {

        try {
            logger.info("submit RP count : {}", rps.size());
            channelManager.sendRewardPoint(rps, receivedFrom);
            return rps;
        } catch (Throwable th) {
            logger.warn("Exception caught : {}", th);
        }
        return null;
    }
}
