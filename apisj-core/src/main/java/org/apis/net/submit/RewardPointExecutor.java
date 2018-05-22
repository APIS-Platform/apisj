package org.apis.net.submit;

import org.apis.core.RewardPoint;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RewardPointExecutor {
    static {
        instance = new RewardPointExecutor();
    }

    public static RewardPointExecutor instance;
    private ExecutorService executor = Executors.newFixedThreadPool(1);

    public Future<List<RewardPoint>> submitRewardPoint(RewardPointTask task) {
        return executor.submit(task);
    }
}
