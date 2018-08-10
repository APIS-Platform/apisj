package org.apis.mine;

import org.apis.core.Block;
import org.apis.db.ByteArrayWrapper;
import org.apis.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

public class MinedBlockCache {

    private static MinedBlockCache sMinedBlockCache = null;
    public static MinedBlockCache getInstance() {
        if(sMinedBlockCache == null) {
            sMinedBlockCache = new MinedBlockCache();
        }

        return sMinedBlockCache;
    }


    private final Logger logger = LoggerFactory.getLogger(MinedBlockCache.class.getSimpleName());

    private final List<Block> bestMinedBlocks = new ArrayList<>();

    private final HashMap<Long, HashMap<ByteArrayWrapper, Block>> allKnownBlocks = new HashMap<>();

    private final LinkedHashMap<ByteArrayWrapper, Long> invalidBlocks = new LinkedHashMap<ByteArrayWrapper, Long>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            int maxInvalidBlockSize = 100;
            return size() > maxInvalidBlockSize;
        }
    };

    private MinedBlockCache() {}


    public List<Block> getBestMinedBlocks() {
        return new ArrayList<>(bestMinedBlocks);
    }

    /**
     * 다른 노드에서 전달받은 채굴 블럭들을 현재 저장된 블럭들과 비교한다.
     * 더 높은 RP 값을 보유한 블록일 경우에만 bestMinedBlocks로 대체한다.
     *
     * @param receivedBestBlocks bestMinedBlock과 비교하려는 블록들
     * @return true : 교체되었음 false : 기존 유지
     */
    public boolean compareMinedBlocks(List<Block> receivedBestBlocks) {

        for(int i = 0; i < receivedBestBlocks.size(); i++) {
            Block block = receivedBestBlocks.get(i);

            // 전달받은 블록들 중에 invalid 블록이 있는지 확인한다.
            if(invalidBlocks.get(new ByteArrayWrapper(block.getHash())) != null) {
                return false;
            }

            // 블록 번호가 연속되어있는지 확인한다.
            if(i > 0 && block.getNumber() - receivedBestBlocks.get(i - 1).getNumber() != 1) {
                return false;
            }

            // 블록들이 서로 연결되어있는지 확인한다.
            if(i > 0 && !FastByteComparisons.equal(block.getParentHash(), receivedBestBlocks.get(i - 1).getHash())) {
                return false;
            }
        }

        addAllBlocks(receivedBestBlocks);

        if(bestMinedBlocks.isEmpty()) {
            bestMinedBlocks.addAll(receivedBestBlocks);
            return true;
        }

        Block cachedBestBlock =  bestMinedBlocks.get(bestMinedBlocks.size() - 1);
        if(cachedBestBlock == null) {
            bestMinedBlocks.clear();
            bestMinedBlocks.addAll(receivedBestBlocks);
            return true;
        }

        Block receivedBestBlock = receivedBestBlocks.get(receivedBestBlocks.size() - 1);

        long cachedBestNumber = cachedBestBlock.getNumber();
        long receivedBlockNumber = receivedBestBlock.getNumber();

        // 최신 블록이 아니면 추가할 필요 없음
        if(receivedBlockNumber < cachedBestNumber) {
            return false;
        }

        // 동일한 블록일 경우 추가할 필요 없음
        if(cachedBestNumber == receivedBlockNumber && cachedBestBlock.getCumulativeRewardPoint().compareTo(receivedBestBlock.getCumulativeRewardPoint()) == 0) {
            return false;
        }

        int offset;
        try {
            offset = (int) (receivedBestBlocks.get(0).getNumber() - bestMinedBlocks.get(0).getNumber());
        } catch (IndexOutOfBoundsException e) {
            return false;
        }

        for (int i = 0; i < receivedBestBlocks.size() && i < bestMinedBlocks.size(); i++) {
            Block minedBlock;
            Block cachedBlock;

            if(offset >= 0) {
                if(i >= receivedBestBlocks.size() || (i + offset) >= bestMinedBlocks.size()) {
                    break;
                }
                minedBlock    = receivedBestBlocks.get(i);
                cachedBlock   = bestMinedBlocks.get(i + offset);
            } else {
                if((i - offset) >= receivedBestBlocks.size() || i >= bestMinedBlocks.size()) {
                    break;
                }
                minedBlock    = receivedBestBlocks.get(i - offset);
                cachedBlock   = bestMinedBlocks.get(i);
            }

            if (i == 0) {
                // 최소한 하나의 조상은 일치해야만 한다.
                if (!FastByteComparisons.equal(cachedBlock.getParentHash(), minedBlock.getParentHash())) {
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
        bestMinedBlocks.addAll(receivedBestBlocks);
        while(bestMinedBlocks.size() < 5 && bestMinedBlocks.size() > 0) {
            Block firstBlock = bestMinedBlocks.get(0);
            HashMap<ByteArrayWrapper, Block> blocks = allKnownBlocks.get(firstBlock.getNumber() - 1);
            if(blocks == null || blocks.isEmpty()) {
                break;
            }
            Block parentBlock = blocks.get(new ByteArrayWrapper(firstBlock.getParentHash()));
            if(parentBlock == null) {
                break;
            } else {
                bestMinedBlocks.add(0, parentBlock);
            }
        }

        /*if(!bestMinedBlocks.isEmpty()) {
            bestMinedBlocks.removeIf(block -> block.getNumber() >= receivedBestBlocks.get(0).getNumber());
        }
        if(!bestMinedBlocks.isEmpty()) {
            bestMinedBlocks.removeIf(block -> block.getNumber() <= receivedBestBlock.getNumber() - 5);
        }*/

        //bestMinedBlocks.addAll(receivedBestBlocks);

        //--LOG
        String newMiner = Hex.toHexString(receivedBestBlock.getCoinbase());
        logger.info("Cached blocks changed : Last block : {}, miner : {}..{}", receivedBlockNumber, newMiner.substring(0, 3), newMiner.substring(newMiner.length() - 3, newMiner.length()));
        return true;
    }

    private void addAllBlocks(List<Block> receivedBestBlocks) {
        if(receivedBestBlocks == null || receivedBestBlocks.isEmpty()) {
            return;
        }

        for (Block block : receivedBestBlocks) {
            long blockNumber = block.getNumber();

            HashMap<ByteArrayWrapper, Block> blocks = allKnownBlocks.get(blockNumber);
            blocks = (blocks == null ? new HashMap<>() : blocks);

            ByteArrayWrapper blockHashW = new ByteArrayWrapper(block.getHash());
            if (blocks.get(blockHashW) == null) {
                blocks.put(blockHashW, block);
                allKnownBlocks.keySet().removeIf(key -> key == blockNumber);
                allKnownBlocks.put(blockNumber, blocks);
            }
        }

        // 오래된 데이터는 삭제
        Block firstBlock = receivedBestBlocks.get(0);
        synchronized (allKnownBlocks) {
            if (!allKnownBlocks.isEmpty() && !receivedBestBlocks.isEmpty() && firstBlock != null) {
                allKnownBlocks.keySet().removeIf(key -> key < firstBlock.getNumber() - 10);
            }
        }

        /*System.out.println("----");
        for(long key : allKnownBlocks.keySet()) {
            System.out.println(key + " : " + allKnownBlocks.get(key).size() + " : " + getBestBlock(key).getShortDescr());
        }*/
    }

    /**
     * 올바르지 못한 블럭을 받았을 수 있다.
     * 검증을 통과하지 못했을 경우, 리스트에서 삭제하고, 블랙리스트에 등록시킨다.
     * @param invalidBlock invalid block
     */
    public void removeBestBlock(Block invalidBlock) {
        ByteArrayWrapper blockHashW = new ByteArrayWrapper(invalidBlock.getHash());

        invalidBlocks.keySet().removeIf(key -> key.equals(blockHashW));
        invalidBlocks.put(blockHashW, invalidBlock.getNumber());

        /*
         * bestMinedBlocks 리스트에서도 invalid block을 삭제한다.
         * 만약 invalid block이 존재하면 그 블록의 자식 블록들도 삭제해야한다.
         */
        boolean hasInvalidBlock = false;
        for(Iterator<Block> it = bestMinedBlocks.iterator(); it.hasNext();) {
            Block best = it.next();
            if(blockHashW.equals(new ByteArrayWrapper(best.getHash()))) {
                hasInvalidBlock = true;
                it.remove();
                continue;
            }

            if(hasInvalidBlock) {
                it.remove();
            }
        }

        HashMap<ByteArrayWrapper, Block> blocks = allKnownBlocks.get(invalidBlock.getNumber());
        if(blocks == null || blocks.isEmpty()) {
            return;
        }

        blocks.keySet().removeIf(key -> key.equals(blockHashW));
        allKnownBlocks.replace(invalidBlock.getNumber(), blocks);
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
}
