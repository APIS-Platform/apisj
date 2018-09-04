package org.apis.db.sql;

import org.apis.config.SystemProperties;
import org.apis.core.*;
import org.apis.util.ByteUtil;
import org.apis.vm.LogInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    
    private static DBManager sDBDbManager;
    
    public static DBManager getInstance() {
        if(sDBDbManager == null) {
            sDBDbManager = new DBManager();
        }
        
        return sDBDbManager;
    }
    
    
    private Logger logger = LoggerFactory.getLogger("SQLiteDBManager");
    private static int DB_VERSION = 1;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:" + SystemProperties.getDefault().databaseDir() + "/storage.db";
    private boolean isOpen = false;
    
    private DBManager () {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(DB_URL);
            if(conn != null) {
                logger.debug("Connected to the database");
                DatabaseMetaData dm = conn.getMetaData();
                logger.debug("Driver name: " + dm.getDriverName());
                logger.debug("Driver version: " + dm.getDriverVersion());
                logger.debug("Product name: " + dm.getDatabaseProductName());
                logger.debug("Product version: " + dm.getDatabaseProductVersion());
                createOrUpdate(conn);
                conn.close();
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void createOrUpdate(Connection conn) throws SQLException {
        long currentVersion = selectDBVersion();

        if(currentVersion == 0) {
            create(conn);
        } else if(currentVersion < DB_VERSION) {
            update(conn);
        }
    }


    private void create(Connection conn) throws SQLException {
        String queryCreateAccounts = "CREATE TABLE \"accounts\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `address` TEXT NOT NULL UNIQUE, `title` TEXT DEFAULT 'Unnamed', `balance` TEXT, `mask` TEXT, `rewards` TEXT, `first_tx_block_number` INTEGER, `last_synced_block` INTEGER )";
        String queryCreateContracts = "CREATE TABLE \"contracts\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `address` TEXT NOT NULL UNIQUE, `title` TEXT DEFAULT 'Unnamed', `mask` TEXT, `abi` TEXT, `canvas_url` TEXT, `first_tx_block_number` INTEGER, `last_synced_block` INTEGER )";
        String queryCreateRewards = "CREATE TABLE \"rewards\" ( `address` TEXT, `recipient` TEXT, `blockHash` TEXT, `block_number` INTEGER, `type` INTEGER, `amount` TEXT, FOREIGN KEY(`address`) REFERENCES `accounts`(`address`), PRIMARY KEY(`address`) )";
        String queryCreateTransactions = "CREATE TABLE \"transactions\" ( `block_number` INTEGER, `hash` TEXT NOT NULL UNIQUE, `nonce` INTEGER, `gasPrice` TEXT, `gasLimit` INTEGER, `to` TEXT, `from` TEXT, `toMask` TEXT, `amount` TEXT, `data` TEXT, `status` INTEGER, `gasUsed` INTEGER, `mineralUsed` TEXT, `error` TEXT, `bloom` TEXT, `return` TEXT, `logs` TEXT, `contractAddress` TEXT, `blockHash` TEXT )";
        String queryCreateEvents = "CREATE TABLE \"events\" ( `address` TEXT, `tx_hash` TEXT UNIQUE, `event_name` TEXT, `event_args` TEXT, `event_json` INTEGER, FOREIGN KEY(`address`) REFERENCES `contracts`(`address`), FOREIGN KEY(`tx_hash`) REFERENCES `transactions`(`hash`) )";
        String queryCreateDBInfo = "CREATE TABLE \"db_info\" ( `uid` INTEGER, `version` INTEGER, `last_synced_block` INTEGER, PRIMARY KEY(`uid`) )";

        conn.prepareStatement(queryCreateAccounts).execute();
        conn.prepareStatement(queryCreateContracts).execute();
        conn.prepareStatement(queryCreateRewards).execute();
        conn.prepareStatement(queryCreateTransactions).execute();
        conn.prepareStatement(queryCreateEvents).execute();
        conn.prepareStatement(queryCreateDBInfo).execute();

        PreparedStatement state = conn.prepareStatement("insert or replace into db_info (uid, version, last_synced_block) values (1, ?, ?)");
        state.setInt(1, DB_VERSION);
        state.setInt(2, 0);
        state.execute();

        conn.close();
    }

    private void update(Connection conn) throws SQLException {
        conn.close();
    }

    private boolean open(boolean readonly) {
        if(!isOpen) {
            try {
                SQLiteConfig config = new SQLiteConfig();
                config.setReadOnly(readonly);
                this.connection = DriverManager.getConnection(DB_URL);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
            isOpen = true;
        }
        return true;
    }

    private boolean close() {
        if(!isOpen) {
            return true;
        }

        try {
            connection.close();
            isOpen = false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }



    public boolean updateAccount(byte[] address, String title, BigInteger balance, String mask, BigInteger rewards) {
        if(!open(false)) {
            return false;
        }

        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE accounts SET title = ?, balance = ?, mask = ?, rewards = ? WHERE address = ?");
            update.setString(1, title);
            update.setString(2, ByteUtil.toHexString(balance.toByteArray()));
            update.setString(3, mask);
            update.setString(4, ByteUtil.toHexString(rewards.toByteArray()));
            update.setString(5, ByteUtil.toHexString(address));

            if(update.executeUpdate() == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO accounts (address, title, balance, mask, rewards) values (?, ?, ?, ?, ?)");
                state.setString(1, ByteUtil.toHexString(address));
                state.setString(2, title);
                state.setString(3, ByteUtil.toHexString(balance.toByteArray()));
                state.setString(4, mask);
                state.setString(5, ByteUtil.toHexString(rewards.toByteArray()));
                return state.execute();
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return false;
    }

    public List<AccountRecord> selectAccounts() {
        List<AccountRecord> wallets = new ArrayList<>();

        if(!open(true)) {
            return wallets;
        }

        try {
            PreparedStatement state = this.connection.prepareStatement("SELECT * FROM `accounts` ORDER BY `uid` ASC");
            ResultSet result = state.executeQuery();

            while(result.next()) {
                wallets.add(new AccountRecord(result));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return wallets;
    }

    public boolean deleteAccount(byte[] address) {
        if(!open(false)) {
            return false;
        }

        try {
            PreparedStatement state = this.connection.prepareStatement("DELETE FROM accounts WHERE address = ?");
            state.setString(1, ByteUtil.toHexString(address));
            return state.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return false;
    }

    /**
     * 입력된 주소 외의 다른 주소들은 DB에서 삭제한다(정리한다).
     * Remove any addresses other than the parameters from the DB (clean up).
     *
     * @param existingAddresses DB에 유지시키려는 주소들의 목록<br/>List of addresses to keep in DB
     * @return <code>true</code> if the query execution was successful
     */
    public boolean clearAccount(List<byte[]> existingAddresses) {
        if(!open(false)) {
            return false;
        }

        try {
            StringBuilder where = new StringBuilder();
            for(int i = 0; i < existingAddresses.size(); i++) {
                if(i == 0) {
                    where = new StringBuilder("address != ?");
                } else {
                    where.append(" AND address != ?");
                }
            }

            PreparedStatement state = this.connection.prepareStatement("DELETE FROM accounts WHERE " + where.toString());
            for(int i = 0 ; i < existingAddresses.size(); i++) {
                state.setString(i + 1, ByteUtil.toHexString(existingAddresses.get(i)));
            }

            return state.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return false;
    }




    public boolean updateContract(byte[] address, String title, String mask, String abi, String canvas_url) {
        if(!open(false)) {
            return false;
        }

        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE contracts SET title = ?, mask = ?, abi = ?, canvas_url = ? WHERE address = ?");
            update.setString(1, title);
            update.setString(2, mask);
            update.setString(3, abi);
            update.setString(4, canvas_url);
            update.setString(5, ByteUtil.toHexString(address));

            if(update.executeUpdate() == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO contracts (address, title, mask, abi, canvas_url) values (?, ?, ?, ?, ?)");
                state.setString(1, ByteUtil.toHexString(address));
                state.setString(2, title);
                state.setString(3, mask);
                state.setString(4, abi);
                state.setString(5, canvas_url);
                return state.execute();
            }

            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return false;
    }

    public List<ContractRecord> selectContracts() {
        List<ContractRecord> contracts = new ArrayList<>();

        if(!open(true)) {
            return contracts;
        }

        try {
            PreparedStatement state = this.connection.prepareStatement("SELECT * FROM `contracts` ORDER BY `uid` ASC");
            ResultSet result = state.executeQuery();

            while(result.next()) {
                contracts.add(new ContractRecord(result));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return contracts;
    }

    public ContractRecord selectContract(byte[] address) {
        if(!open(true)) {
            return null;
        }

        try {
            PreparedStatement state = this.connection.prepareStatement("SELECT * FROM `contracts` WHERE `address` = ?");
            state.setString(1, ByteUtil.toHexString(address));
            ResultSet result = state.executeQuery();

            if(result.next()) {
                return new ContractRecord(result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return null;
    }

    public boolean deleteContract(byte[] address) {
        if(!open(false)) {
            return false;
        }

        try {
            PreparedStatement state = this.connection.prepareStatement("DELETE FROM contracts WHERE address = ?");
            state.setString(1, ByteUtil.toHexString(address));
            return state.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return false;
    }





    public boolean updateTransaction(Transaction tx) {
        if(!open(false)) {
            return false;
        }

        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE transactions SET `nonce` = ?, `gasPrice` = ?, `gasLimit` = ?, `to` = ?, `from` = ?, `toMask` = ?, `amount` = ?, `data` = ?, `contractAddress` = ? WHERE hash = ?");
            update.setLong(1, ByteUtil.byteArrayToLong(tx.getNonce()));
            update.setString(2, ByteUtil.toHexString(tx.getGasPrice()));
            update.setLong(3, ByteUtil.byteArrayToLong(tx.getGasLimit()));
            update.setString(4, ByteUtil.toHexString(tx.getReceiveAddress()));
            update.setString(5, ByteUtil.toHexString(tx.getSender()));
            update.setString(6, new String(tx.getReceiveMask(), Charset.forName("UTF-8")));
            update.setString(7, ByteUtil.toHexString(tx.getValue()));
            update.setString(8, ByteUtil.toHexString(tx.getData()));
            update.setString(9, ByteUtil.toHexString(tx.getContractAddress()));
            update.setString(10, ByteUtil.toHexString(tx.getHash()));

            if(update.executeUpdate() == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO transactions (`nonce`, `gasPrice`, `gasLimit`, `to`, `from`, `toMask`, `amount`, `data`, `contractAddress`, `hash`) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                state.setLong(1, ByteUtil.byteArrayToLong(tx.getNonce()));
                state.setString(2, ByteUtil.toHexString(tx.getGasPrice()));
                state.setLong(3, ByteUtil.byteArrayToLong(tx.getGasLimit()));
                state.setString(4, ByteUtil.toHexString(tx.getReceiveAddress()));
                state.setString(5, ByteUtil.toHexString(tx.getSender()));
                state.setString(6, new String(tx.getReceiveMask(), Charset.forName("UTF-8")));
                state.setString(7, ByteUtil.toHexString(tx.getValue()));
                state.setString(8, ByteUtil.toHexString(tx.getData()));
                state.setString(9, ByteUtil.toHexString(tx.getContractAddress()));
                state.setString(10, ByteUtil.toHexString(tx.getHash()));
                return state.execute();
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return false;
    }

    public boolean updateTransaction(TransactionReceipt receipt) {
        if(!open(false)) {
            return false;
        }

        StringBuilder logString = new StringBuilder();
        StringBuffer returnString = new StringBuffer();

        // Log(Event)가 존재한다면, 파싱해야한다.
        if(receipt.getLogInfoList() != null && receipt.getLogInfoList().size() > 0) {
            ContractRecord contractRecord = selectContract(receipt.getTransaction().getReceiveAddress());

            // DB에 Contract의 정보가 저장되어 있다면 파싱이 가능하다.
            if(contractRecord != null) {
                CallTransaction.Contract contract = contractRecord.getContract();

                // ABI가 저장되어있어야 한다.
                if(contract != null) {
                    for (LogInfo info : receipt.getLogInfoList()) {
                        CallTransaction.Invocation invocation = contract.parseEvent(info);
                        logString.append(invocation.toString()).append("\n");
                    }

                    try {
                        if(receipt.getExecutionResult().length >= 4) {
                            CallTransaction.Invocation result = contract.parseInvocation(receipt.getExecutionResult());
                            returnString.append(result.toString());
                        }
                    } catch (RuntimeException e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        }

        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE transactions SET `status` = ?, `gasUsed` = ?, `mineralUsed` = ?, `error` = ?, `bloom` = ?, `logs` = ?, return = ? WHERE hash = ?");
            update.setLong(1, ByteUtil.byteArrayToLong(receipt.getPostTxState()));
            update.setString(2, ByteUtil.toHexString(receipt.getGasUsed()));
            update.setString(3, ByteUtil.toHexString(receipt.getMineralUsed()));
            update.setString(4, receipt.getError());
            update.setString(5, ByteUtil.toHexString(receipt.getBloomFilter().getData()));
            update.setString(6, logString.toString());
            update.setString(7, returnString.toString());
            update.setString(8, ByteUtil.toHexString(receipt.getTransaction().getHash()));

            if(update.executeUpdate() == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO transactions (`status`, `gasUsed`, `mineralUsed`, `error`, `bloom`, `logs`, return, `hash`) values (?, ?, ?, ?, ?, ?, ?, ?)");
                state.setLong(1, ByteUtil.byteArrayToLong(receipt.getPostTxState()));
                state.setString(2, ByteUtil.toHexString(receipt.getGasUsed()));
                state.setString(3, ByteUtil.toHexString(receipt.getMineralUsed()));
                state.setString(4, receipt.getError());
                state.setString(5, ByteUtil.toHexString(receipt.getBloomFilter().getData()));
                state.setString(6, logString.toString());
                state.setString(7, returnString.toString());
                state.setString(8, ByteUtil.toHexString(receipt.getTransaction().getHash()));
                return state.execute();
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return false;
    }

    public boolean updateTransaction(TransactionInfo info, Block block) {

        TransactionReceipt receipt = info.getReceipt();
        updateTransaction(receipt);
        Transaction tx = receipt.getTransaction();
        updateTransaction(tx);

        if(!open(false)) {
            return false;
        }

        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE transactions SET `blockHash` = ?, `block_number` = ? WHERE hash = ?");
            update.setString(1, ByteUtil.toHexString(block.getHash()));
            update.setLong(2, block.getNumber());
            update.setString(3, ByteUtil.toHexString(receipt.getTransaction().getHash()));

            if(update.executeUpdate() == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO transactions (`blockHash`, `block_number`, `hash`) values (?, ?, ?)");
                state.setString(1, ByteUtil.toHexString(block.getHash()));
                state.setLong(2, block.getNumber());
                state.setString(3, ByteUtil.toHexString(receipt.getTransaction().getHash()));
                return state.execute();
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return false;
    }

    public List<TransactionRecord> selectTransactions(byte[] address) {
        return selectTransactions(address, 0, 0);
    }

    public List<TransactionRecord> selectTransactions(byte[] address, long rowCount, long offset) {
        List<TransactionRecord> transactions = new ArrayList<>();

        if(!open(true)) {
            return transactions;
        }

        String limit = "";
        if(rowCount > 0) {
            limit += " LIMIT " + rowCount;
        }
        if(offset > 0) {
            limit += " OFFSET " + offset;
        }

        try {
            String query;
            PreparedStatement state;
            if(address == null) {
                query = "SELECT * FROM `transactions` ORDER BY `block_number` DESC" + limit;
                state = this.connection.prepareStatement(query);
            } else {
                query = "SELECT * FROM `transactions` WHERE `from` = ? OR `to` = ? ORDER BY `block_number` DESC" + limit;
                state = this.connection.prepareStatement(query);
                state.setString(1, ByteUtil.toHexString(address));
                state.setString(2, ByteUtil.toHexString(address));
            }

            ResultSet result = state.executeQuery();

            while(result.next()) {
                transactions.add(new TransactionRecord(result));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return transactions;
    }

    public boolean deleteTransactionByHash(byte[] txHash) {
        if(!open(false)) {
            return false;
        }

        try {
            PreparedStatement state = this.connection.prepareStatement("DELETE FROM transactions WHERE hash = ?");
            state.setString(1, ByteUtil.toHexString(txHash));
            return state.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return false;
    }

    public boolean deleteTransactionByAddress(byte[] address) {
        if(!open(false)) {
            return false;
        }

        try {
            PreparedStatement state = this.connection.prepareStatement("DELETE FROM transactions WHERE `from` = ?");
            state.setString(1, ByteUtil.toHexString(address));
            return state.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return false;
    }






    public void updateLastSyncedBlock(long lastSyncedBlockNumber) {
        if(!open(false)) {
            return;
        }

        try {
            PreparedStatement updateDBInfo = this.connection.prepareStatement("UPDATE `db_info` SET `last_synced_block` = ?");
            updateDBInfo.setLong(1, lastSyncedBlockNumber);
            updateDBInfo.executeUpdate();

            PreparedStatement updateAccounts = this.connection.prepareStatement("UPDATE `accounts` SET `last_synced_block` = ? WHERE last_synced_block > 0");
            updateAccounts.setLong(1, lastSyncedBlockNumber);
            updateAccounts.executeUpdate();

            PreparedStatement updateContracts = this.connection.prepareStatement("UPDATE `contracts` SET `last_synced_block` = ? WHERE last_synced_block > 0");
            updateContracts.setLong(1, lastSyncedBlockNumber);
            updateContracts.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public void setAccountSyncStarted(byte[] address) {
        setSyncStarted(address, "accounts");
    }
    public void setContractSyncStarted(byte[] address) {
        setSyncStarted(address, "contracts");
    }

    private void setSyncStarted(byte[] address, String table) {
        if(!open(false)) {
            return;
        }

        try {
            PreparedStatement updateAccounts = this.connection.prepareStatement("UPDATE " + table + " SET `last_synced_block` = 1 WHERE address = ? AND last_synced_block = 0");
            updateAccounts.setString(1, ByteUtil.toHexString(address));
            updateAccounts.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }


    private DBInfoRecord selectDBInfo() {
        if(!open(true)) {
            return null;
        }

        try {
            PreparedStatement state = this.connection.prepareStatement("SELECT * FROM `db_info` WHERE uid = 1");
            ResultSet result = state.executeQuery();

            if(result.next()) {
                return new DBInfoRecord(result);
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            return null;
        } finally {
            close();
        }

        return null;
    }

    public long selectDBVersion() {
        DBInfoRecord record = selectDBInfo();
        if(record == null) {
            return 0;
        }
        return record.getVersion();
    }

    public long selectDBLastSyncedBlock() {
        if(!open(true)) {
            return 0;
        }

        try {
            PreparedStatement state = this.connection.prepareStatement("SELECT MIN(MIN(a.last_synced_block), MIN(b.last_synced_block)) from accounts a, contracts b");
            ResultSet result = state.executeQuery();

            if(result.next()) {
                long lastSyncedBlock = result.getLong(1);
                if(lastSyncedBlock > 0) {
                    return lastSyncedBlock;
                } else {
                    state = this.connection.prepareStatement("SELECT last_synced_block from db_info");
                    result = state.executeQuery();
                    if(result.next()) {
                        return result.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            return 0;
        } finally {
            close();
        }

        return 0;
    }

}
