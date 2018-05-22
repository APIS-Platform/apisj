package org.apis.core;

import org.apis.db.BlockStore;
import org.apis.util.FastByteComparisons;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

/**
 * 블록이 생성되면 각자 연결된 노드에게 Reward Point 값을 전달한다.
 * 이 때 최신의 블록 번호에 대해 전달받은 가장 큰 RP 값을 MAX_SIZE_CACHE 갯수 만큼 저장하는데 이용된다.
 */
public class RewardPointCache {
    private static final int MAX_SIZE_CACHE = 10;
    private static RewardPointCache sRewardPointCache = null;


    //private ArrayList<RewardPoint> ;
    private HashMap<Long, Map<BigInteger, RewardPoint>> mRewardPointList;

    public static RewardPointCache getInstance() {
        if(sRewardPointCache == null) {
            sRewardPointCache = new RewardPointCache();
        }

        return sRewardPointCache;
    }

    private RewardPointCache() {
        mRewardPointList = new HashMap<>();
    }


    /**
     * 다른 노드에게서 전달받은 RP 값을 저장한다.
     */
    public synchronized void insertUpdate(List<RewardPoint> rewardPoints, BlockStore blockStore) {
        // 채굴자의 블록 업데이트가 다 되지 않았으면, 네트워크 정보들도 접수하지 않는다
        if(rewardPoints == null || rewardPoints.size() == 0) {
            return;
        }

        Block worldBlock = blockStore.getBlockByHash(rewardPoints.get(0).getParentBlockHash());
        long parentNumber = worldBlock.getNumber();

        Map<BigInteger, RewardPoint> rewardPointList = mRewardPointList.get(parentNumber);
        if(rewardPointList == null) {
            rewardPointList = new HashMap<>();
        }

        // 새로 받은 블럭의 RP 값이 기존 리스트에 존재하는 블럭들의 RP 값 보다 클 경우, 리스트를 초기화한다
        // RP 값이 높은 블럭에 대해서만 채굴이 진행될 수 있도록 하기 위해서.
        if(rewardPointList.size() > 0) {
            BigInteger worldBlockRP = worldBlock.getRewardPoint();
            BigInteger cacheBlockRP = BigInteger.ZERO;

            if(rewardPointList.keySet().iterator().hasNext()) {
                BigInteger bi = rewardPointList.keySet().iterator().next();
                cacheBlockRP = blockStore.getBlockByHash(rewardPointList.get(bi).getParentBlockHash()).getRewardPoint();
            }

            // 전달받은 리스트 내 블록의 RP 값이 캐쉬에 보관된 블럭보다 클 경우, 캐쉬를 비운다
            if(worldBlockRP.compareTo(cacheBlockRP) > 0) {
                clear(parentNumber);
            }
        }


        for(RewardPoint rp : rewardPoints) {
            rewardPointList.put(rp.getRP(), rp);
        }
        Map<BigInteger, RewardPoint> sorted = new TreeMap<>(new DescRpOrder());
        sorted.putAll(rewardPointList);

        // For log -----------------------------
        for(BigInteger bi : sorted.keySet()) {
            RewardPoint rp = sorted.get(bi);
            String coinbaseStr = Hex.toHexString(rp.getCoinbase());
            System.out.println("[RP-Cache] " + rp.getParentBlockNumber() + "th - " + coinbaseStr.substring(0, 2) + ".." + coinbaseStr.substring(coinbaseStr.length() - 2, coinbaseStr.length()) + " : " + sorted.get(bi).toString());
        } // -----------------------------------

        while(sorted.size() > MAX_SIZE_CACHE) {
            sorted.remove(((TreeMap<BigInteger, RewardPoint>) sorted).lastKey());
        }
        rewardPointList.clear();
        rewardPointList.putAll(sorted);



        if(mRewardPointList.get(parentNumber) == null) {
            mRewardPointList.put(parentNumber, rewardPointList);
        } else {
            mRewardPointList.replace(parentNumber, rewardPointList);
        }


        Map<Long, Map<BigInteger, RewardPoint>> sortedAll = new TreeMap<>(new DescBlockNumberOrder());
        sortedAll.putAll(mRewardPointList);

        if(sortedAll.size() > MAX_SIZE_CACHE) {
            sortedAll.remove(((TreeMap<Long, Map<BigInteger,RewardPoint>>) sortedAll).lastKey());
        }
        mRewardPointList.clear();
        mRewardPointList.putAll(sortedAll);
    }

    public void clear(long blockNumber) {
        if(mRewardPointList.get(blockNumber) == null) {
            mRewardPointList.put(blockNumber, new HashMap<>());
        } else {
            mRewardPointList.get(blockNumber).clear();
        }
    }

    public RewardPoint getRewardPoint(long parentNumber, int index) {
        Map<BigInteger, RewardPoint> rewardPointList = mRewardPointList.get(parentNumber);

        if(rewardPointList == null || rewardPointList.size() == 0) {
            return null;
        }

        Map<BigInteger, RewardPoint> sortedRewardPointList = new TreeMap<>(new DescRpOrder());
        sortedRewardPointList.putAll(rewardPointList);

        int key = 0;
        for(BigInteger bi : sortedRewardPointList.keySet()) {
            if(key == index) {
                return sortedRewardPointList.get(bi);
            }
            key += 1;
        }

        return null;
    }

    public List<RewardPoint> getRewardPointList(long parentNumber) {
        Map<BigInteger, RewardPoint> rewardPointList = mRewardPointList.get(parentNumber);

        if(rewardPointList == null || rewardPointList.size() == 0) {
            return null;
        }

        Map<BigInteger, RewardPoint> sortedRewardPointList = new TreeMap<>(new DescRpOrder());
        sortedRewardPointList.putAll(rewardPointList);

        List<RewardPoint> list = new ArrayList<>();

        for(BigInteger bi : sortedRewardPointList.keySet()) {
            list.add(sortedRewardPointList.get(bi));
        }

        return list;
    }

    public BigInteger getTotalRP(Block parentBlock) {
        BigInteger totalRP = BigInteger.ZERO;

        Map<BigInteger, RewardPoint> rewardPointList = mRewardPointList.get(parentBlock.getNumber());
        if(rewardPointList == null || rewardPointList.size() == 0) {
            return totalRP;
        }

        for(BigInteger bi : rewardPointList.keySet()) {
            RewardPoint rp = rewardPointList.get(bi);

            if(FastByteComparisons.equal(rp.getParentBlockHash(), parentBlock.getHash())) {
                totalRP = totalRP.add(bi);
            }
        }

        return totalRP;
    }


    /**
     * 입력된 블록 높이에서, 존재하는 해더 해쉬 리스트를 반환한다.
     * @param parentNumber 리스트를 추출하려는 블로 높이
     * @return 해쉬 리스트
     */
    public byte[] getBlockHash(long parentNumber) {
        byte[] hash = new byte[]{};

        if(mRewardPointList == null || mRewardPointList.size() == 0 || mRewardPointList.get(parentNumber) == null || mRewardPointList.get(parentNumber).size() == 0) {
            return hash;
        }

        Map<BigInteger, RewardPoint> rewardPointList = mRewardPointList.get(parentNumber);

        if(rewardPointList.keySet().iterator().hasNext()) {
            BigInteger bi = rewardPointList.keySet().iterator().next();
            hash = rewardPointList.get(bi).getParentBlockHash();
        }

        return hash;
    }

    public int sizeOfRewardPoint(long parentNumber) {
        if(mRewardPointList == null || mRewardPointList.get(parentNumber) == null) {
            return 0;
        }

        return mRewardPointList.get(parentNumber).size();
    }



    static class DescRpOrder implements Comparator<BigInteger> {

        @Override
        public int compare(BigInteger o1, BigInteger o2) {
            return o2.compareTo(o1);
        }
    }

    static class DescBlockNumberOrder implements Comparator<Long> {
        @Override
        public int compare(Long o1, Long o2) {
            return o2.compareTo(o1);
        }
    }
}
