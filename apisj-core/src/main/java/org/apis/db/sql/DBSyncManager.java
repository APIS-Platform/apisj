package org.apis.db.sql;

import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.core.TransactionInfo;
import org.apis.facade.Ethereum;
import org.apis.util.ConsoleUtil;
import org.apis.util.FastByteComparisons;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DBSyncManager {
    private static DBSyncManager sDbSyncManager = null;

    public static DBSyncManager getInstance(Ethereum ethereum) {
        if(sDbSyncManager == null) {
            sDbSyncManager = new DBSyncManager(ethereum);
        }
        return sDbSyncManager;
    }


    private final Ethereum ethereum;

    private static boolean isSyncing = false;

    private DBManager dbManager;

    private DBSyncManager(Ethereum ethereum) {
        this.ethereum = ethereum;
        this.dbManager = DBManager.getInstance();
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
            long tt = System.currentTimeMillis();

            Block currentBlock = ethereum.getBlockchain().getBlockByNumber(currentBlockNumber);

            if(currentBlock != null) {
                blocks.add(currentBlock);

                if (blocks.size() < 1000) {
                    currentBlockNumber += 1;
                    continue;
                }
            }

            if(blocks.size() == 0) {
                break;
            }

            dbManager.insertBlocks(blocks, ethereum);

            blocks.clear();
            currentBlockNumber += 1;

            ConsoleUtil.printlnPurple(currentBlockNumber + " synced : " + (System.currentTimeMillis() - tt) + "ms");
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
