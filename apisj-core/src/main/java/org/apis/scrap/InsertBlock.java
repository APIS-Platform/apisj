package org.apis.scrap;

import org.apis.core.Block;
import org.apis.util.BIUtil;
import org.spongycastle.util.encoders.Hex;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class InsertBlock {

    private String hash, blockNumber, parentHash, coinbase, stateRoot, txTrieHash, receiptsTrieHash, rewardPoint, cumulativeRp, gasUsed, gasLimit, mineralUsed, timestamp, extraData, rpSeed, nonce;

    InsertBlock(Block block) {
        this.hash = Hex.toHexString(block.getHash());
        this.blockNumber = String.valueOf(block.getNumber());
        this.parentHash =  Hex.toHexString(block.getParentHash());
        this.coinbase = Hex.toHexString(block.getCoinbase());
        this.stateRoot = Hex.toHexString(block.getStateRoot());
        this.txTrieHash = Hex.toHexString(block.getTxTrieRoot());
        this.receiptsTrieHash = Hex.toHexString(block.getReceiptsRoot());
        this.rewardPoint = block.getRewardPoint().toString();
        this.cumulativeRp = block.getCumulativeRewardPoint().toString();
        this.gasUsed = String.valueOf(block.getGasUsed());
        this.gasLimit = BIUtil.toBI(block.getGasLimit()).toString();
        this.mineralUsed = block.getMineralUsed().toString();
        this.timestamp = String.valueOf(block.getTimestamp());
        this.extraData = Hex.toHexString(block.getExtraData());
        this.rpSeed = Hex.toHexString(block.getMixHash());
        this.nonce = BIUtil.toBI(block.getNonce()).toString();
    }

    private String getInsertQuery() {
        return "INSERT INTO `blocks` " +
                "(`hash`, `blockNumber`, `parentHash`, `coinbase`, " +
                "`stateRoot`, `txTrieHash`, `receiptsTrieHash`, `rewardPoint`, " +
                "`cumulativeRp`, `gasUsed`, `gasLimit`, `mineralUsed`, " +
                "`timestamp`, `extraData`, `rpSeed`, `nonce`) " +
                "VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "blockNumber = VALUES(blockNumber)," +
                "parentHash = VALUES(parentHash)," +
                "coinbase = VALUES(coinbase)," +
                "stateRoot = VALUES(stateRoot)," +
                "txTrieHash = VALUES(txTrieHash)," +
                "receiptsTrieHash = VALUES(receiptsTrieHash)," +
                "rewardPoint = VALUES(rewardPoint)," +
                "cumulativeRp = VALUES(cumulativeRp)," +
                "gasUsed = VALUES(gasUsed)," +
                "gasLimit = VALUES(gasLimit)," +
                "mineralUsed = VALUES(mineralUsed)," +
                "timestamp = VALUES(timestamp)," +
                "extraData = VALUES(extraData)," +
                "rpSeed = VALUES(rpSeed)," +
                "nonce = VALUES(nonce)";
    }

    PreparedStatement getInsertState(Connection conn) throws SQLException {
        PreparedStatement preparedState = conn.prepareStatement(getInsertQuery());
        preparedState.setString(1, hash);
        preparedState.setString(2, blockNumber);
        preparedState.setString(3, parentHash);
        preparedState.setString(4, coinbase);
        preparedState.setString(5, stateRoot);
        preparedState.setString(6, txTrieHash);
        preparedState.setString(7, receiptsTrieHash);
        preparedState.setString(8, rewardPoint);
        preparedState.setString(9, cumulativeRp);
        preparedState.setString(10, gasUsed);
        preparedState.setString(11, gasLimit);
        preparedState.setString(12, mineralUsed);
        preparedState.setString(13, timestamp);
        preparedState.setString(14, extraData);
        preparedState.setString(15, rpSeed);
        preparedState.setString(16, nonce);

        return preparedState;
    }
}
