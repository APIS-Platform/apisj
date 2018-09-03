package org.apis.db.sql;

import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.core.TransactionInfo;
import org.apis.core.TransactionReceipt;
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

    private boolean isSyncing = false;

    private DBManager dbManager;

    private DBSyncManager(Ethereum ethereum) {
        this.ethereum = ethereum;
        this.dbManager = DBManager.getInstance();
    }



    public void starSync() {
        if(isSyncing) {
            return;
        }
        isSyncing = true;

        List<AccountRecord> accounts = new ArrayList<>(dbManager.selectAccounts());
        List<ContractRecord> contracts = new ArrayList<>(dbManager.selectContracts());

        // 정보를 확인할 주소가 없으면 진행할 필요가 없다
        if(accounts.size() == 0) {
            isSyncing = false;
            return;
        }

        /*
         * 싱크가 진행되는 도중에 계정이나 컨트렉트가 DB에 추가될 경우, 싱크가 이루어지지 않았음에도
         * last_synced_block 값이 함께 증가하게 될 수 있다.
         * 이를 방지하기 위해서, DB에 추가될 때에는 last_synced_block 값을 0으로 설정하고
         * 싱크가 시작될 때 리스트에 포함된 경우에는, 이 값을 1로 변경한다.
         * 싱크 중간에는, 이 값이 0보다 클 때에만 싱크가 완료된 것으로 판단하도록 한다.
         */
        for(AccountRecord record : accounts) {
            dbManager.setAccountSyncStarted(record.getAddress());
        }
        for(ContractRecord record : contracts) {
            dbManager.setContractSyncStarted(record.getAddress());
        }

        long currentIndex = 1;
        while(isSyncing && dbManager.selectDBLastSyncedBlock() < ethereum.getBlockchain().getBestBlock().getNumber() - 1) {
            long lastSyncedBlock = dbManager.selectDBLastSyncedBlock();
            long currentBlockNumber = lastSyncedBlock + currentIndex;
            Block currentBlock = ethereum.getBlockchain().getBlockByNumber(currentBlockNumber);
            if(currentBlock == null) {
                break;
            }

            for(Transaction tx : currentBlock.getTransactionsList()) {
                // sender 또는 receiver 주소가 DB에 있을 때에만 정보를 추가한다.
                boolean isTarget = false;
                if(isInAccounts(accounts, tx.getSender(), currentBlock.getNumber()) || isInAccounts(accounts, tx.getReceiveAddress(), currentBlock.getNumber())) {
                    isTarget = true;
                }

                // Contract
                if(isInContracts(contracts, tx.getSender(), currentBlock.getNumber()) || isInContracts(contracts, tx.getReceiveAddress(), currentBlock.getNumber())) {
                    isTarget = true;
                }

                if(isTarget) {
                    TransactionInfo txInfo = ethereum.getTransactionInfo(tx.getHash());
                    dbManager.updateTransaction(txInfo, currentBlock);
                }
            }

            if(currentIndex > 1000) {
                dbManager.updateLastSyncedBlock(currentBlockNumber);
                currentIndex = 0;
            }
            currentIndex += 1;
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
        return list.stream().filter(item -> FastByteComparisons.equal(item.getAddress(), keyword) && item.getLastSyncedBlock() < blockNumber).collect(Collectors.toList()).size() > 0;
    }

    private boolean isInContracts(List<ContractRecord> list, byte[] keyword, long blockNumber) {
        return list.stream().filter(item -> FastByteComparisons.equal(item.getAddress(), keyword) && item.getLastSyncedBlock() < blockNumber).collect(Collectors.toList()).size() > 0;
    }
}
