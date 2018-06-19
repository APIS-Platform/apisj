package org.apis.mine;

import org.apache.commons.collections4.map.LRUMap;
import org.apis.config.SystemProperties;
import org.apis.core.MinerState;
import org.apis.db.ByteArrayWrapper;
import org.apis.util.FastByteComparisons;
import org.apis.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MinerManager {

    private final Logger logger = LoggerFactory.getLogger("MinerManager");
    private final List<MinerState> minerStates = new ArrayList<>();
    private final Map<ByteArrayWrapper, Object> minerStatesMap = new LRUMap<>(100000);
    private final Object dummyObject = new Object();
    private SystemProperties config;

    private static MinerManager sMinerManager = null;

    public static MinerManager getInstance() {
        if(sMinerManager == null) {
            sMinerManager = new MinerManager();
        }

        return sMinerManager;
    }

    private MinerManager() {
        config = SystemProperties.getDefault();
    }


    public List<MinerState> getMinerStates() {
        return new ArrayList<>(minerStates);
    }

    public void addMinerState(MinerState minerState) {
        addMinerStates(Collections.singletonList(minerState));
    }

    public List<MinerState> addMinerStates(List<MinerState> minerStates) {
        int unknownMiner = 0;
        int updatedMiner = 0;

        long now = TimeUtils.getRealTimestamp();

        /* 새롭게 추가된 MinerState 들을 저장해서, 다른 노드들에 다시 전파하게 한다. */
        List<MinerState> newMiner = new ArrayList<>();
        for(MinerState minerState : minerStates) {
            if(minerState == null || minerState.getCoinbase() == null) {
                continue;
            }

            // Miner 정보가 존재하지 않으면 새로 추가한다.
            if(addNewMinerIfNotExist(minerState)) {
                unknownMiner++;

                if(addMinerStateImpl(minerState)) {
                    newMiner.add(minerState);
                }
            }

            // Miner 정보가 존재하면, 생존 시간이 업데이트 될 경우 새로 추가한다.
            else {
                if(now - minerState.getLastLived() < 60_000L) {
                    continue;
                }

                if(updateMinerStateImpl(minerState)) {
                    updatedMiner++;
                    newMiner.add(minerState);
                }
            }
        }

        logger.debug("Miner states changed : total: {}, new: {}, updated: {} valid : {} (current #of known miners : {})",
                minerStates.size(), unknownMiner, updatedMiner, newMiner, minerStatesMap.size());

        return newMiner;
    }

    private boolean addNewMinerIfNotExist(MinerState minerState) {
        return minerStatesMap.put(new ByteArrayWrapper(minerState.getCoinbase()), dummyObject) == null;
    }


    private boolean addMinerStateImpl(final MinerState minerState) {
        if(TimeUtils.getRealTimestamp() - minerState.getLastLived() > 60_000L) {
            return false;
        }

        // If the network ID values are different, do not add them to the list.
        if(minerState.getNetworkId() != config.networkId()) {
            return false;
        }

        // If the Protocol Version value is lower, it is not added to the list.
        if(minerState.getProtocolVersion() < config.defaultP2PVersion()) {
            return false;
        }

        synchronized (minerStates) {
            return minerStates.add(minerState);
        }
    }

    /**
     * 채굴자의 정보를 업데이트한다.
     */
    private  boolean updateMinerStateImpl(final MinerState minerState) {
        long now = TimeUtils.getRealTimestamp();
        if(now - minerState.getLastLived() > 5*60*1000L) {
            return false;
        }
        if(minerState.getNetworkId() != config.networkId()) {
            return false;
        }
        if(minerState.getProtocolVersion() < config.defaultP2PVersion()) {
            return false;
        }

        synchronized (minerStates) {
            minerStates.removeIf(state -> FastByteComparisons.equal(state.getCoinbase(), minerState.getCoinbase()));
            return minerStates.add(minerState);
        }
    }
}
