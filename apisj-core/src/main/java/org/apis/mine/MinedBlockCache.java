package org.apis.mine;

import org.apis.core.Block;
import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

public class MinedBlockCache {

    private final Logger logger = LoggerFactory.getLogger("MinedBlockCache");
    private final List<Block> bestMinedBlocks = new ArrayList<>();

    private final HashMap<Long, HashMap<BigInteger, Block>> allMinedBlocks = new HashMap<>();

    private final LinkedHashMap<BigInteger, Long> invalidBlocks = new LinkedHashMap<BigInteger, Long>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            int maxInvalidBlockSize = 100;
            return size() > maxInvalidBlockSize;
        }
    };

    private static MinedBlockCache sMinedBlockCache = null;

    public static MinedBlockCache getInstance() {
        if(sMinedBlockCache == null) {
            sMinedBlockCache = new MinedBlockCache();
        }

        return sMinedBlockCache;
    }

    private MinedBlockCache() {}


    public List<Block> getBestMinedBlocks() {
        return new ArrayList<>(bestMinedBlocks);
    }

    /**
     * 다른 노드에서 전달받은 채굴 블럭들을 현재 저장된 블럭들과 비교한다.
     * 더 높은 RP 값을 보유한 블록일 경우에만 bestMinedBlocks로 대체한다.
     *
     * @param minedBlocks bestMinedBlock과 비교하려는 블록들
     * @return true : 교체되었음 false : 기존 유지
     */
    public boolean compareMinedBlocks(List<Block> minedBlocks) {
        // 전달받은 블록들 중에 invalid 블록이 있는지 확인한다.
        for(Block block : minedBlocks) {
            if(invalidBlocks.get(ByteUtil.bytesToBigInteger(block.getHash())) != null) {
                return false;
            }
        }

        addAllBlocks(minedBlocks);

        if(bestMinedBlocks.isEmpty()) {
            bestMinedBlocks.addAll(minedBlocks);
            return true;
        }

        Block cachedBestBlock =  bestMinedBlocks.get(bestMinedBlocks.size() - 1);
        if(cachedBestBlock == null) {
            bestMinedBlocks.clear();
            bestMinedBlocks.addAll(minedBlocks);
            return true;
        }

        Block minedBestBlock = minedBlocks.get(minedBlocks.size() - 1);

        long cachedBestNumber = cachedBestBlock.getNumber();
        long minedBlockNumber = minedBestBlock.getNumber();

        // 최신 블록이 아니면 추가할 필요 없음
        if(minedBlockNumber < cachedBestNumber) {
            return false;
        }

        // 동일한 블록일 경우 추가할 필요 없음
        if(cachedBestNumber == minedBlockNumber && cachedBestBlock.getCumulativeRewardPoint().compareTo(minedBestBlock.getCumulativeRewardPoint()) == 0) {
            return false;
        }

        int offset = (int) (minedBlocks.get(0).getNumber() - bestMinedBlocks.get(0).getNumber());

        for (int i = 0; i < minedBlocks.size() && i < bestMinedBlocks.size(); i++) {
            Block minedBlock;
            Block cachedBlock;

            if(offset >= 0) {
                if(i >= minedBlocks.size() || (i + offset) >= bestMinedBlocks.size()) {
                    break;
                }
                minedBlock    = minedBlocks.get(i);
                cachedBlock   = bestMinedBlocks.get(i + offset);
            } else {
                if((i - offset) >= minedBlocks.size() || i >= bestMinedBlocks.size()) {
                    break;
                }
                minedBlock    = minedBlocks.get(i - offset);
                cachedBlock   = bestMinedBlocks.get(i);
            }

            if (i == 0) {
                // 최소한 하나의 조상은 일치해야만 한다.
                if (!FastByteComparisons.equal(cachedBlock.getHash(), minedBlock.getHash())) {
                    return false;
                }
            }

            if (cachedBlock.getNumber() != minedBlock.getNumber()) {
                return false;
            }

            BigInteger cachedRP = cachedBlock.getCumulativeRewardPoint();
            BigInteger minedRP = minedBlock.getCumulativeRewardPoint();

            if (cachedRP.compareTo(minedRP) > 0) {
                return false;
            }
        }


        bestMinedBlocks.clear();
        bestMinedBlocks.addAll(minedBlocks);

        //--LOG
        String newMiner = Hex.toHexString(minedBestBlock.getCoinbase());
        logger.info("Cached blocks changed : Last block : {}, miner : {}..{}", minedBlockNumber, newMiner.substring(0, 3), newMiner.substring(newMiner.length() - 3, newMiner.length()));
        return true;
    }

    private void addAllBlocks(List<Block> minedBlocks) {
        if(minedBlocks == null || minedBlocks.isEmpty()) {
            return;
        }

        for(Block block : minedBlocks) {
            HashMap<BigInteger, Block> blocks = allMinedBlocks.get(block.getNumber());
            blocks = (blocks == null ? new HashMap<>() : blocks);

            BigInteger hashBI = ByteUtil.bytesToBigInteger(block.getHash());
            if(blocks.get(hashBI) == null) {
                blocks.put(hashBI, block);
                allMinedBlocks.put(block.getNumber(), blocks);
            }
        }

        // 오래된 데이터는 삭제
        allMinedBlocks.keySet().removeIf(key -> key < minedBlocks.get(0).getNumber());

        /*System.out.println("----");
        for(long key : allMinedBlocks.keySet()) {
            System.out.println(key + " : " + allMinedBlocks.get(key).size() + " : " + getBestBlock(key).getShortDescr());
        }*/
    }

    public Block getBestBlock(long blockNumber) {
        HashMap<BigInteger, Block> blocks = allMinedBlocks.get(blockNumber);

        if(blocks == null || blocks.isEmpty()) {
            return null;
        }

        BigInteger maxHash = BigInteger.ZERO;
        BigInteger maxRP = BigInteger.ZERO;

        for(BigInteger hash : blocks.keySet()) {
            if(invalidBlocks.get(hash) != null) {
                continue;
            }
            BigInteger blockRP = blocks.get(hash).getCumulativeRewardPoint();
            if(blockRP.compareTo(maxRP) > 0) {
                maxHash = hash;
                maxRP = blockRP;
            }
        }

        if(maxHash == null) {
            return null;
        } else {
            return blocks.get(maxHash);
        }
    }

    /**
     * 올바르지 못한 블럭을 받았을 수 있다.
     * 검증을 통과하지 못했을 경우, 리스트에서 삭제하고, 블랙리스트에 등록시킨다.
     * @param invalidBlock invalid block
     */
    public void removeBestBlock(Block invalidBlock) {
        BigInteger hashBi = ByteUtil.bytesToBigInteger(invalidBlock.getHash());

        invalidBlocks.keySet().removeIf(key -> key.equals(hashBi));
        invalidBlocks.put(hashBi, invalidBlock.getNumber());

        /*
         * bestMinedBlocks 리스트에서도 invalid block을 삭제한다.
         * 만약 invalid block이 존재하면 그 블록의 자식 블록들도 삭제해야한다.
         */
        boolean hasInvalidBlock = false;
        for(Iterator<Block> it = bestMinedBlocks.iterator(); it.hasNext();) {
            Block best = it.next();
            if(hashBi.equals(BIUtil.toBI(best.hashCode()))) {
                hasInvalidBlock = true;
                it.remove();
                continue;
            }

            if(hasInvalidBlock) {
                it.remove();
            }
        }

        HashMap<BigInteger, Block> blocks = allMinedBlocks.get(invalidBlock.getNumber());
        if(blocks == null || blocks.isEmpty()) {
            return;
        }

        blocks.keySet().removeIf(key -> key.equals(hashBi));
        allMinedBlocks.replace(invalidBlock.getNumber(), blocks);
    }


    public List<Block> getCachedBlocks() {
        return bestMinedBlocks;
    }

    long getBestBlockNumber() {
        if(bestMinedBlocks.isEmpty()) {
            return 0;
        }

        return getBestBlock().getNumber();
    }

    public Block getBestBlock() {
        if(bestMinedBlocks.isEmpty()) {
            return null;
        }

        return bestMinedBlocks.get(bestMinedBlocks.size() - 1);
    }

    long getBestBlockTimestamp() {
        if(bestMinedBlocks.isEmpty()) {
            return 0;
        }

        return bestMinedBlocks.get(bestMinedBlocks.size() - 1).getTimestamp();
    }
}
