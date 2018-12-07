package org.apis.util;

import org.apis.core.Block;
import org.apis.core.Blockchain;
import org.apis.core.Repository;
import org.apis.core.RewardPoint;
import org.apis.crypto.HashUtil;
import org.apis.db.BlockStore;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class RewardPointUtil {

    /**
     * RP 값을 계산하기 위한 seed를 생성한다.
     *
     * @param coinbase 블록 채굴자의 주소
     * @param balance 블록 채굴자의 잔고, (현재 블록 - 1000)번째에서의 잔고
     * @param parentHash 부모 블록의 해시
     * @return seed 값
     */
    public synchronized static byte[] calcSeed(byte[] coinbase, BigInteger balance, byte[] parentHash) {
        return HashUtil.sha3(HashUtil.sha3(coinbase, HashUtil.sha3(ByteUtil.bigIntegerToBytes(balance))), parentHash);
    }

    public synchronized static BigInteger calcRewardPoint(byte[] seed, BigInteger balance) {
        BigInteger seedNumber = new BigInteger(1, seed);
        BigInteger dav = seedNumber.mod(BigInteger.valueOf(27));

        return seedNumber.divide(BigInteger.valueOf(10).pow((int) (77 - dav.longValue()))).multiply(balance);
    }

    public synchronized static BigInteger calcRewardPoint (byte[] coinbase, BigInteger balance, byte[] parentHash) {
        return calcRewardPoint(calcSeed(coinbase, balance, parentHash), balance);
    }

    public synchronized static Repository getRewardPointBalanceRepo(Repository repo, Block parent, BlockStore blockchain) {
        for(int i = 0 ; i < 10 ; i++) {
            if(parent.getNumber() > 0) {
                parent = blockchain.getBlockByHash(parent.getParentHash());
            } else {
                break;
            }
        }
        return repo.getSnapshotTo(parent.getStateRoot());
    }

    public synchronized static RewardPoint genRewardPoint(Block parentBlock, byte[] coinbase, Repository repo) {
        long parentNumber = parentBlock.getNumber();

        BigInteger balance = repo.getBalance(coinbase);
        byte[] seed = calcSeed(coinbase, balance, parentBlock.getHash());
        BigInteger rp = calcRewardPoint(seed, balance);

        return new RewardPoint(parentBlock.getHash(), parentNumber, coinbase, seed, balance, rp);
    }
}
