package org.apis.db.sql;

import org.apis.core.Block;
import org.apis.facade.Apis;
import org.apis.util.ConsoleUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DBSyncManager {
    private static DBSyncManager sDbSyncManager = null;

    public static DBSyncManager getInstance(Apis apis) {
        if(sDbSyncManager == null) {
            sDbSyncManager = new DBSyncManager(apis);
        }
        return sDbSyncManager;
    }


    private Apis apis;

    private static boolean isSyncing = false;

    private DBManager dbManager;

    private DBSyncManager(Apis apis) {
        this.apis = apis;
        this.dbManager = DBManager.getInstance();
    }

    public void setApis(Apis apis) {
        if(apis != null) {
            this.apis = apis;
        }
    }


    // UI가 멈추는 현상을 방지하기 위해 스레드를 추가했음
    public void syncThreadStart() {
        if(isSyncing) {
            return;
        }

        new Thread(() -> {
            if(!isSyncing) {
                startSync();
            }
        }).start();
    }

    private synchronized void  startSync() {
        if(isSyncing) {
            return;
        }
        isSyncing = true;

        long currentBlockNumber = dbManager.selectDBLastSyncedBlock();
        List<Block> blocks = new ArrayList<>();


        while(isSyncing) {
            long tt = TimeUtils.getRealTimestamp();

            if(currentBlockNumber <= apis.getBlockchain().getBestBlock().getNumber()) {
                Block currentBlock = apis.getBlockchain().getBlockByNumber(currentBlockNumber);

                if (currentBlock != null) {
                    blocks.add(currentBlock);

                    if (blocks.size() < 1000) {
                        currentBlockNumber += 1;
                        continue;
                    }
                }
            }

            if(blocks.size() == 0) {
                break;
            }

            dbManager.insertBlocks(blocks, apis);

            // 중복된 블록을 정리한다.
            dbManager.trimBlocks(apis);

            ConsoleUtil.printlnYellow("The last block inserted into the SQL database : %d (%d ms)", blocks.get(blocks.size() - 1).getNumber(), (TimeUtils.getRealTimestamp() - tt));

            blocks.clear();
            currentBlockNumber += 1;
        }

        isSyncing = false;
    }

    public void stopSync() {
        if(!isSyncing) {
            return;
        }
        isSyncing = false;
    }


    private boolean isInAccounts(List<AccountRecord> list, byte[] keyword, long blockNumber) {
        if(keyword == null) {
            return false;
        }
        return list.stream().filter(item -> FastByteComparisons.equal(item.getAddress(), keyword) && item.getLastSyncedBlock() < blockNumber).collect(Collectors.toList()).size() > 0;
    }

    private boolean isInContracts(List<ContractRecord> list, byte[] keyword, long blockNumber) {
        if(keyword == null) {
            return false;
        }
        return list.stream().filter(item -> FastByteComparisons.equal(item.getAddress(), keyword) && item.getLastSyncedBlock() < blockNumber).collect(Collectors.toList()).size() > 0;
    }
}
