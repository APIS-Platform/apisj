package org.apis.db.sql;

import org.apis.config.SystemProperties;
import org.apis.core.*;
import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.apis.util.TimeUtils;
import org.apis.vm.LogInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;

import java.io.File;
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
            File keystore = new File(SystemProperties.getDefault().databaseDir());
            if(!keystore.exists()) {
                keystore.mkdirs();
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            if(connection != null) {
                logger.debug("Connected to the database");
                DatabaseMetaData dm = connection.getMetaData();
                logger.debug("Driver name: " + dm.getDriverName());
                logger.debug("Driver version: " + dm.getDriverVersion());
                logger.debug("Product name: " + dm.getDatabaseProductName());
                logger.debug("Product version: " + dm.getDatabaseProductVersion());
                createOrUpdate(connection);
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
        String queryCreateAccounts = "CREATE TABLE \"accounts\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `address` TEXT NOT NULL UNIQUE, `title` TEXT DEFAULT 'Unnamed', `balance` TEXT, `mask` TEXT, `rewards` TEXT, `first_tx_block_number` INTEGER, `last_synced_block` INTEGER DEFAULT 1 )";
        String queryCreateContracts = "CREATE TABLE \"contracts\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `address` TEXT NOT NULL UNIQUE, `title` TEXT DEFAULT 'Unnamed', `mask` TEXT, `abi` TEXT, `canvas_url` TEXT, `first_tx_block_number` INTEGER, `last_synced_block` INTEGER DEFAULT 1 )";
        String queryCreateRewards = "CREATE TABLE \"rewards\" ( `address` TEXT, `type` INTEGER, `amount` TEXT, `blockIndex` INTEGER, FOREIGN KEY(`blockIndex`) REFERENCES `blocks`(`uid`), PRIMARY KEY(`address`) )";
        String queryCreateTransactions = "CREATE TABLE \"transactions\" ( `txhash` TEXT NOT NULL UNIQUE, `receiver` TEXT, `sender` TEXT, `blockUid` NUMERIC, PRIMARY KEY(`txhash`), FOREIGN KEY(`blockUid`) REFERENCES `blocks`(`uid`) )";
        String queryCreateEvents = "CREATE TABLE \"events\" ( `address` TEXT, `tx_hash` TEXT UNIQUE, `event_raw` TEXT, `event_text` TEXT )";
        String queryCreateAbis = "CREATE TABLE \"abis\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `creator` TEXT, `contract_name` TEXT, `contract_address` TEXT UNIQUE, `abi` TEXT, `created_at` INTEGER )";
        String queryCreateDBInfo = "CREATE TABLE \"db_info\" ( `uid` INTEGER, `version` INTEGER, `last_synced_block` INTEGER, PRIMARY KEY(`uid`) )";
        String queryCreateAddressGroups = "CREATE TABLE \"address_group\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `group_name` TEXT )";
        String queryCreateMyAddress = "CREATE TABLE \"myaddress\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `address` TEXT NOT NULL UNIQUE, `alias` TEXT DEFAULT 'Unnamed' )";
        String queryCreateConnectAddressGroups = "CREATE TABLE \"connect_address_group\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `address` TEXT NOT NULL, `group_name` TEXT NOT NULL)";
        String queryCreateRecentAddress = "CREATE TABLE \"recent_address\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `tx_hash` TEXT UNIQUE, `address` TEXT NOT NULL UNIQUE, `alias` TEXT DEFAULT 'Unnamed', `created_at` INTEGER )";
        String queryCreateBlocks = "CREATE TABLE \"blocks\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `hash` TEXT NOT NULL UNIQUE, `blockNumber` INTEGER )";
        String queryIndexTransaction = "CREATE INDEX `txIndex` ON `transactions` ( `receiver`, `sender` )";
        String queryIndexBlock = "CREATE UNIQUE INDEX `blockIndex` ON `blocks` ( `hash` )";
        String queryIndexReward = "CREATE INDEX `rewardIndex` ON `rewards` ( `address`, `blockIndex` )";
        String queryIndexEvent = "CREATE INDEX `eventIndex` ON `events` ( `address`, `tx_hash` )";

        PreparedStatement createAccounts = conn.prepareStatement(queryCreateAccounts);
        createAccounts.execute();
        createAccounts.close();

        PreparedStatement createContracts = conn.prepareStatement(queryCreateContracts);
        createContracts.execute();
        createContracts.close();

        PreparedStatement createRewards = conn.prepareStatement(queryCreateRewards);
        createRewards.execute();
        createRewards.close();

        PreparedStatement createTransactions = conn.prepareStatement(queryCreateTransactions);
        createTransactions.execute();
        createTransactions.close();

        PreparedStatement createEvents = conn.prepareStatement(queryCreateEvents);
        createEvents.execute();
        createEvents.close();

        PreparedStatement createDBInfo = conn.prepareStatement(queryCreateDBInfo);
        createDBInfo.execute();
        createDBInfo.close();

        PreparedStatement createAbis = conn.prepareStatement(queryCreateAbis);
        createAbis.execute();
        createAbis.close();

        PreparedStatement createAddressGroups = conn.prepareStatement(queryCreateAddressGroups);
        createAddressGroups.execute();
        createAddressGroups.close();

        PreparedStatement createMyAddress = conn.prepareStatement(queryCreateMyAddress);
        createMyAddress.execute();
        createMyAddress.close();

        PreparedStatement createConnectAddressGroups = conn.prepareStatement(queryCreateConnectAddressGroups);
        createConnectAddressGroups.execute();
        createConnectAddressGroups.close();

        PreparedStatement createConnectRecentAddress = conn.prepareStatement(queryCreateRecentAddress);
        createConnectRecentAddress.execute();
        createConnectRecentAddress.close();

        PreparedStatement createBlocks = conn.prepareStatement(queryCreateBlocks);
        createBlocks.execute();
        createBlocks.close();

        PreparedStatement createIndexBlocks = conn.prepareStatement(queryIndexBlock);
        createIndexBlocks.execute();
        createIndexBlocks.close();

        PreparedStatement createIndexTx = conn.prepareStatement(queryIndexTransaction);
        createIndexTx.execute();
        createIndexTx.close();

        PreparedStatement createIndexReward = conn.prepareStatement(queryIndexReward);
        createIndexReward.execute();
        createIndexReward.close();

        PreparedStatement createIndexEvent = conn.prepareStatement(queryIndexEvent);
        createIndexEvent.execute();
        createIndexEvent.close();



        PreparedStatement state = conn.prepareStatement("insert or replace into db_info (uid, version, last_synced_block) values (1, ?, ?)");
        state.setInt(1, DB_VERSION);
        state.setInt(2, 0);
        state.execute();
        state.close();
    }

    private void update(Connection conn) throws SQLException {
        logger.debug("Database Update!");
    }




    public boolean updateAccount(byte[] address, String title, BigInteger balance, String mask, BigInteger rewards) {

        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE accounts SET title = ?, balance = ?, mask = ?, rewards = ? WHERE address = ?");
            update.setString(1, title);
            update.setString(2, ByteUtil.toHexString(balance.toByteArray()));
            update.setString(3, mask);
            update.setString(4, ByteUtil.toHexString(rewards.toByteArray()));
            update.setString(5, ByteUtil.toHexString(address));
            int updateResult = update.executeUpdate();
            if(updateResult == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO accounts (address, title, balance, mask, rewards) values (?, ?, ?, ?, ?)");
                state.setString(1, ByteUtil.toHexString(address));
                state.setString(2, title);
                state.setString(3, ByteUtil.toHexString(balance.toByteArray()));
                state.setString(4, mask);
                state.setString(5, ByteUtil.toHexString(rewards.toByteArray()));
                boolean insertResult = state.execute();
                state.close();

                return insertResult;
            }

            return updateResult > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    List<AccountRecord> selectAccounts() {
        List<AccountRecord> wallets = new ArrayList<>();
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            state = this.connection.prepareStatement("SELECT * FROM accounts ORDER BY uid ASC");
            result = state.executeQuery();

            while (result.next()) {
                wallets.add(new AccountRecord(result));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }

        return wallets;
    }

    public boolean deleteAccount(byte[] address) {

        try {
            PreparedStatement state = this.connection.prepareStatement("DELETE FROM accounts WHERE address = ?");
            state.setString(1, ByteUtil.toHexString(address));
            boolean deleteResult = state.execute();
            state.close();
            return deleteResult;
        } catch (SQLException e) {
            e.printStackTrace();
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

            boolean clearResult = state.execute();
            state.close();
            return clearResult;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    boolean updateContractCreation(TransactionInfo txInfo) {
        //abis 테이블에 컨트렉트가 존재하는지 확인한다.
        TransactionReceipt receipt = txInfo.getReceipt();
        Transaction tx = receipt.getTransaction();

        if(tx.getContractAddress() == null) {
            return false;
        }
        AbiRecord abiRecord = selectAbi(tx.getContractAddress());
        if(abiRecord == null) {
            return false;
        }
        updateContract(tx.getContractAddress(), abiRecord.getContractName(), "", abiRecord.getAbi(), null);
        deleteAbi(tx.getContractAddress());
        return true;
    }


    public boolean updateContract(byte[] address, String title, String mask, String abi, String canvas_url) {

        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE contracts SET title = ?, mask = ?, abi = ?, canvas_url = ? WHERE address = ?");
            update.setString(1, title);
            update.setString(2, mask);
            update.setString(3, abi);
            update.setString(4, canvas_url);
            update.setString(5, ByteUtil.toHexString(address));
            int updateResult = update.executeUpdate();
            if(updateResult == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO contracts (address, title, mask, abi, canvas_url) values (?, ?, ?, ?, ?)");
                state.setString(1, ByteUtil.toHexString(address));
                state.setString(2, title);
                state.setString(3, mask);
                state.setString(4, abi);
                state.setString(5, canvas_url);
                boolean insertResult = state.execute();
                state.close();
                return insertResult;
            }

            return updateResult > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<ContractRecord> selectContracts() {
        List<ContractRecord> contracts = new ArrayList<>();
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            state = this.connection.prepareStatement("SELECT * FROM `contracts` ORDER BY uid ASC");
            result = state.executeQuery();

            while(result.next()) {
                contracts.add(new ContractRecord(result));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }

        return contracts;
    }

    private void close(PreparedStatement state) {
        try {
            if(state != null) {
                state.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void close(ResultSet result) {
        try {
            if(result != null) {
                result.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ContractRecord selectContract(byte[] address) {
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            state = this.connection.prepareStatement("SELECT * FROM `contracts` WHERE `address` = ?");
            state.setString(1, ByteUtil.toHexString(address));
            result = state.executeQuery();

            if(result.next()) {
                return new ContractRecord(result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }

        return null;
    }

    public boolean deleteContract(byte[] address) {
       try {
            PreparedStatement state = this.connection.prepareStatement("DELETE FROM contracts WHERE address = ?");
            state.setString(1, ByteUtil.toHexString(address));
            boolean deleteResult = state.execute();
            state.close();
            return deleteResult;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    public boolean updateAbi(byte[] creator, byte[] contractAddress, String abi, String contractName) {

        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE abis SET creator = ?, contract_address = ?, abi = ?, created_at = ?, contract_name = ?  WHERE contract_address = ?");
            update.setString(1, ByteUtil.toHexString(creator));
            update.setString(2, ByteUtil.toHexString(contractAddress));
            update.setString(3, abi);
            update.setLong(4, TimeUtils.getRealTimestamp());
            update.setString(5, contractName);
            update.setString(6, ByteUtil.toHexString(contractAddress));
            int updateResult = update.executeUpdate();
            if(updateResult == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO abis (creator, contract_address, abi, created_at, contract_name) values (?, ?, ?, ?, ?)");
                state.setString(1, ByteUtil.toHexString(creator));
                state.setString(2, ByteUtil.toHexString(contractAddress));
                state.setString(3, abi);
                state.setLong(4, TimeUtils.getRealTimestamp());
                state.setString(5, contractName);
                boolean insertResult = state.execute();
                state.close();
                return insertResult;
            }

            return updateResult > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<AbiRecord> selectAbis() {
        List<AbiRecord> contracts = new ArrayList<>();
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            state = this.connection.prepareStatement("SELECT * FROM `abis` ORDER BY uid ASC");
            result = state.executeQuery();

            while(result.next()) {
                contracts.add(new AbiRecord(result));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }

        return contracts;
    }

    public AbiRecord selectAbi(byte[] contractAddress) {
        PreparedStatement state = null;
        ResultSet result = null;
        try {
            state = this.connection.prepareStatement("SELECT * FROM abis WHERE contract_address = ?");
            state.setString(1, ByteUtil.toHexString(contractAddress));
            result = state.executeQuery();

            if(result.next()) {
                return new AbiRecord(result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }

        return null;
    }

    public boolean deleteAbi(byte[] contractAddress) {
        try {
            PreparedStatement state = this.connection.prepareStatement("DELETE FROM abis WHERE contract_address = ?");
            state.setString(1, ByteUtil.toHexString(contractAddress));
            boolean deleteResult = state.execute();
            state.close();
            return deleteResult;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }




    public boolean updateTransaction(Transaction tx) {
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
            int updateResult = update.executeUpdate();
            update.close();

            if(updateResult == 0) {
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
                boolean insertResult = state.execute();
                state.close();
                return insertResult;
            }
            return updateResult > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateTransaction(TransactionReceipt receipt) {

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
            int updateResult = update.executeUpdate();
            update.close();

            if(updateResult == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO transactions (`status`, `gasUsed`, `mineralUsed`, `error`, `bloom`, `logs`, return, `hash`) values (?, ?, ?, ?, ?, ?, ?, ?)");
                state.setLong(1, ByteUtil.byteArrayToLong(receipt.getPostTxState()));
                state.setString(2, ByteUtil.toHexString(receipt.getGasUsed()));
                state.setString(3, ByteUtil.toHexString(receipt.getMineralUsed()));
                state.setString(4, receipt.getError());
                state.setString(5, ByteUtil.toHexString(receipt.getBloomFilter().getData()));
                state.setString(6, logString.toString());
                state.setString(7, returnString.toString());
                state.setString(8, ByteUtil.toHexString(receipt.getTransaction().getHash()));
                boolean insertResult = state.execute();
                state.close();
                return insertResult;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateTransaction(TransactionInfo info, Block block) {

        TransactionReceipt receipt = info.getReceipt();
        updateTransaction(receipt);
        Transaction tx = receipt.getTransaction();
        updateTransaction(tx);

        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE transactions SET `blockHash` = ?, `block_number` = ? WHERE hash = ?");
            update.setString(1, ByteUtil.toHexString(block.getHash()));
            update.setLong(2, block.getNumber());
            update.setString(3, ByteUtil.toHexString(receipt.getTransaction().getHash()));
            int updateResult = update.executeUpdate();
            update.close();

            if(updateResult == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO transactions (`blockHash`, `block_number`, `hash`) values (?, ?, ?)");
                state.setString(1, ByteUtil.toHexString(block.getHash()));
                state.setLong(2, block.getNumber());
                state.setString(3, ByteUtil.toHexString(receipt.getTransaction().getHash()));
                boolean insertResult = state.execute();
                state.close();
                return insertResult;
            }
            return updateResult > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<TransactionRecord> selectTransactions(byte[] address) {
        return selectTransactions(address, 0, 0);
    }

    public List<TransactionRecord> selectTransactions(byte[] address, long rowCount, long offset) {
        List<TransactionRecord> transactions = new ArrayList<>();

        String limit = "";
        if(rowCount > 0) {
            limit += " LIMIT " + rowCount;
        }
        if(offset > 0) {
            limit += " OFFSET " + offset;
        }

        String query;
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            if(address == null) {
                query = "SELECT * FROM transactions ORDER BY blockUid DESC" + limit;
                state = this.connection.prepareStatement(query);
            } else {
                query = "SELECT * FROM transactions WHERE receiver = ? OR sender = ? ORDER BY blockUid DESC" + limit;
                state = this.connection.prepareStatement(query);
                state.setString(1, ByteUtil.toHexString(address));
                state.setString(2, ByteUtil.toHexString(address));
            }

            result = state.executeQuery();

            while(result.next()) {
                transactions.add(new TransactionRecord(result));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }

        return transactions;
    }


    public boolean deleteTransactionByHash(byte[] txHash) {

        try {
            PreparedStatement state = this.connection.prepareStatement("DELETE FROM transactions WHERE hash = ?");
            state.setString(1, ByteUtil.toHexString(txHash));
            return state.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteTransactionByAddress(byte[] address) {

        try {
            PreparedStatement state = this.connection.prepareStatement("DELETE FROM transactions WHERE `from` = ?");
            state.setString(1, ByteUtil.toHexString(address));
            boolean deleteResult = state.execute();
            state.close();
            return deleteResult;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    public boolean updateAddressGroup( String groupName ){
        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE address_group SET group_name = ?  WHERE group_name = ?");
            update.setString(1, groupName);
            update.setString(2, groupName);
            int updateResult = update.executeUpdate();
            if(updateResult == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO address_group (group_name) values (?)");
                state.setString(1, groupName);
                boolean insertResult = state.execute();
                state.close();
                return insertResult;
            }

            return updateResult > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<AddressGroupRecord> selectAddressGroups() {
        List<AddressGroupRecord> addressGroup = new ArrayList<>();
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            state = this.connection.prepareStatement("SELECT * FROM `address_group` ORDER BY group_name ASC");
            result = state.executeQuery();

            while(result.next()) {
                addressGroup.add(new AddressGroupRecord(result));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }

        return addressGroup;
    }

    public boolean deleteAddressGroup(String groupName) {

        try {
            PreparedStatement state = this.connection.prepareStatement("DELETE FROM `address_group` WHERE group_name = ?");
            state.setString(1, groupName);
            boolean deleteResult = state.execute();
            state.close();

            deleteConnectAddressGroup(groupName);

            return deleteResult;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateMyAddress( byte[] address, String alias ){

        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE myaddress SET `address` = ?, `alias` = ? WHERE `address` = ?");
            update.setString(1, ByteUtil.toHexString(address));
            update.setString(2, alias);
            update.setString(3, ByteUtil.toHexString(address));
            int updateResult = update.executeUpdate();
            update.close();
            if(updateResult == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO myaddress (`address`, `alias`) values (?, ?)");
                state.setString(1, ByteUtil.toHexString(address));
                state.setString(2, alias);
                boolean insertResult = state.execute();
                state.close();
                return insertResult;
            }
            return updateResult > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<MyAddressRecord> selectMyAddress() {
        List<MyAddressRecord> myAddress = new ArrayList<>();
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            state = this.connection.prepareStatement("SELECT * FROM `myaddress` ORDER BY alias ASC");
            result = state.executeQuery();

            while(result.next()) {
                myAddress.add(new MyAddressRecord(result));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }
        return myAddress;
    }
    public List<MyAddressRecord> selectMyAddressSearch(String search) {
        if(search == null || search.length() == 0){
            return selectMyAddress();
        }

        List<MyAddressRecord> myAddress = new ArrayList<>();
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            String query = "" +
                    "SELECT * FROM `myaddress` " +
                    "WHERE address IN ( " +
                    "   SELECT m.address FROM `myaddress` AS m " +
                    "   LEFT JOIN `connect_address_group` AS c ON m.address = c.address " +
                    "   WHERE m.address LIKE ? OR m.alias LIKE ? OR c.group_name LIKE ? " +
                    " ) " +
                    " ORDER BY alias ASC";

            state = this.connection.prepareStatement(query);
            state.setString(1, "%"+search+"%");
            state.setString(2, "%"+search+"%");
            state.setString(3, "%"+search+"%");
            result = state.executeQuery();

            while(result.next()) {
                myAddress.add(new MyAddressRecord(result));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }
        return myAddress;
    }

    public boolean deleteMyAddress(byte[] address) {

        try {
            PreparedStatement state = this.connection.prepareStatement("DELETE FROM `myaddress` WHERE address = ?");
            state.setString(1, ByteUtil.toHexString(address));
            boolean deleteResult = state.execute();
            state.close();

            deleteConnectAddressGroup(address);

            return deleteResult;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateConnectAddressGroup( byte[] address, String groupName ){

        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE connect_address_group SET `address` = ?, `group_name` = ? WHERE `address` = ? AND group_name = ?");
            update.setString(1, ByteUtil.toHexString(address));
            update.setString(2, groupName);
            int updateResult = update.executeUpdate();
            update.close();
            if(updateResult == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO connect_address_group (`address`, `group_name`) values (?, ?)");
                state.setString(1, ByteUtil.toHexString(address));
                state.setString(2, groupName);
                boolean insertResult = state.execute();
                state.close();
                return insertResult;
            }
            return updateResult > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<ConnectAddressGroupRecord> selectConnectAddressGroup(byte[] address) {
        return selectConnectAddressGroup(address, null);
    }
    public List<ConnectAddressGroupRecord> selectConnectAddressGroup(String groupName) {
        return selectConnectAddressGroup(null, groupName);
    }
    private List<ConnectAddressGroupRecord> selectConnectAddressGroup(byte[] address, String groupName) {
        List<ConnectAddressGroupRecord> connectAddressGroupRecord = new ArrayList<>();
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            if(address != null && groupName != null){
                state = this.connection.prepareStatement("SELECT * FROM `connect_address_group` WHERE address = ? AND group_name = ? ORDER BY group_name ASC");
                state.setString(1, ByteUtil.toHexString(address));
                state.setString(2, groupName);
            }else if(address != null){
                state = this.connection.prepareStatement("SELECT * FROM `connect_address_group` WHERE address = ? ORDER BY group_name ASC");
                state.setString(1, ByteUtil.toHexString(address));
            }else{
                state = this.connection.prepareStatement("SELECT * FROM `connect_address_group` WHERE group_name = ? ORDER BY group_name ASC");
                state.setString(1, groupName);
            }

            result = state.executeQuery();
            while(result.next()) {
                connectAddressGroupRecord.add(new ConnectAddressGroupRecord(result));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }

        return connectAddressGroupRecord;
    }

    public boolean deleteConnectAddressGroup(byte[] address) {
        return deleteConnectAddressGroup(address, null);
    }
    public boolean deleteConnectAddressGroup(String groupName) {
        return deleteConnectAddressGroup(null, groupName);
    }
    private boolean deleteConnectAddressGroup(byte[] address, String groupName) {
        try {
            PreparedStatement state;
            if(address != null && groupName != null){
                state = this.connection.prepareStatement("DELETE FROM `connect_address_group` WHERE address = ? AND group_name = ? ");
                state.setString(1, ByteUtil.toHexString(address));
                state.setString(2, groupName);
            }else if(address != null){
                state = this.connection.prepareStatement("DELETE FROM `connect_address_group` WHERE address = ? ");
                state.setString(1, ByteUtil.toHexString(address));
            }else{
                state = this.connection.prepareStatement("DELETE FROM `connect_address_group` WHERE group_name = ? ");
                state.setString(1, groupName);
            }
            boolean deleteResult = state.execute();
            state.close();
            return deleteResult;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateRecentAddress( byte[] txHash, byte[] address, String alias ){

        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE recent_address SET `tx_hash` = ?, `address` = ?, `alias` = ?, `created_at` = ? WHERE `address` = ?");
            update.setString(1, ByteUtil.toHexString(txHash));
            update.setString(2, ByteUtil.toHexString(address));
            update.setString(3, alias);
            update.setLong(4, TimeUtils.getRealTimestamp());
            update.setString(5, ByteUtil.toHexString(address));
            int updateResult = update.executeUpdate();
            update.close();
            if(updateResult == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO recent_address (`tx_hash`, `address`, `alias`, `created_at`) values (?, ?, ?, ?)");
                state.setString(1, ByteUtil.toHexString(txHash));
                state.setString(2, ByteUtil.toHexString(address));
                state.setString(3, alias);
                state.setLong(4, TimeUtils.getRealTimestamp());
                boolean insertResult = state.execute();
                state.close();
                return insertResult;
            }
            return updateResult > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<RecentAddressRecord> selectRecentAddress() {
        List<RecentAddressRecord> recentAddress = new ArrayList<>();
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            state = this.connection.prepareStatement("SELECT * FROM `recent_address` ORDER BY created_at DESC LIMIT 0, 10");
            result = state.executeQuery();

            while(result.next()) {
                recentAddress.add(new RecentAddressRecord(result));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }
        return recentAddress;
    }


    synchronized void updateLastSyncedBlock(long lastSyncedBlockNumber) {

        try {
            PreparedStatement updateDBInfo = this.connection.prepareStatement("UPDATE `db_info` SET `last_synced_block` = ?");
            updateDBInfo.setLong(1, lastSyncedBlockNumber);
            updateDBInfo.executeUpdate();
            updateDBInfo.close();

            PreparedStatement updateAccounts = this.connection.prepareStatement("UPDATE `accounts` SET `last_synced_block` = ? WHERE last_synced_block > 1");
            updateAccounts.setLong(1, lastSyncedBlockNumber);
            updateAccounts.executeUpdate();
            updateAccounts.close();

            PreparedStatement updateContracts = this.connection.prepareStatement("UPDATE `contracts` SET `last_synced_block` = ? WHERE last_synced_block > 1");
            updateContracts.setLong(1, lastSyncedBlockNumber);
            updateContracts.executeUpdate();
            updateContracts.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setAccountSyncStarted(byte[] address) {
        setSyncStarted(address, "accounts");
    }
    public void setContractSyncStarted(byte[] address) {
        setSyncStarted(address, "contracts");
    }

    private void setSyncStarted(byte[] address, String table) {

        try {
            PreparedStatement updateAccounts = this.connection.prepareStatement("UPDATE " + table + " SET `last_synced_block` = 2 WHERE address = ? AND last_synced_block = 1");
            updateAccounts.setString(1, ByteUtil.toHexString(address));
            updateAccounts.executeUpdate();

            updateAccounts.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private DBInfoRecord selectDBInfo() {
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            state = this.connection.prepareStatement("SELECT * FROM `db_info` WHERE uid = 1");
            result = state.executeQuery();

            if(result.next()) {
                return new DBInfoRecord(result);
            }
        } catch (SQLException e) {
            return null;
            //e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }

        return null;
    }

    private long selectDBVersion() {
        DBInfoRecord record = selectDBInfo();
        if(record == null) {
            return 0;
        }
        return record.getVersion();
    }

    long selectDBLastSyncedBlock() {
        PreparedStatement state = null;
        ResultSet result = null;
        try {
            long lastSyncedBlock = 0;

            state = this.connection.prepareStatement("SELECT last_synced_block from db_info");
            result = state.executeQuery();
            if(result.next()) {
                lastSyncedBlock = result.getLong(1);
            }

            return lastSyncedBlock;
        } catch (SQLException e) {
            return 0;
        } finally {
            close(state);
            close(result);
        }
    }



    private boolean isExistBlock(byte[] blockHash) {
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            state = this.connection.prepareStatement("SELECT * FROM blocks WHERE hash = ?");
            state.setString(1, ByteUtil.toHexString(blockHash));
            result = state.executeQuery();

            if(result.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }

        return false;
    }


    /*void insertBlock(Block block) {
        long tt = System.currentTimeMillis();
        try {
            // block이 존재하면 패스
            if(isExistBlock(block.getHash())) {
                return;
            }
            ConsoleUtil.printlnGreen("B1 : " + (System.currentTimeMillis() - tt) + "ms");

            connection.setAutoCommit(false);
            ConsoleUtil.printlnGreen("B2 : " + (System.currentTimeMillis() - tt) + "ms");
            //Statement state = connection.createStatement();
            PreparedStatement insertBlock = connection.prepareStatement("INSERT INTO blocks (hash, blockNumber) VALUES (?, ?)");
            PreparedStatement insertTx = connection.prepareStatement("INSERT INTO transactions (txHash, receiver, sender, blockUid) values (?, ?, ?, ?)");
            PreparedStatement updateSync = connection.prepareStatement("UPDATE `db_info` SET `last_synced_block` = ?");

            ResultSet blockInsertResult = null;
            ConsoleUtil.printlnGreen("B3 : " + (System.currentTimeMillis() - tt) + "ms");
            try {
                // Block Insert
                long blockUid = 0;
                String blockHash = ByteUtil.toHexString(block.getHash());
                insertBlock.setString(1, blockHash);
                insertBlock.setLong(2, block.getNumber());
                insertBlock.execute();
                ConsoleUtil.printlnGreen("B4 : " + (System.currentTimeMillis() - tt) + "ms");
                blockInsertResult = insertBlock.getGeneratedKeys();
                if(blockInsertResult.next()) {
                    blockUid = blockInsertResult.getLong(1);
                }
                ConsoleUtil.printlnGreen("B5 : " + (System.currentTimeMillis() - tt) + "ms");
                // Transaction Insert
                for(Transaction tx : block.getTransactionsList()) {
                    insertTx.setString  (1, ByteUtil.toHexString(tx.getHash()));            // txHash
                    insertTx.setString  (2, ByteUtil.toHexString(tx.getReceiveAddress()));  // receiver
                    insertTx.setString  (3, ByteUtil.toHexString(tx.getSender()));          // sender
                    insertTx.setLong    (4, blockUid);
                    insertTx.addBatch();
                }
                insertTx.executeBatch();
                ConsoleUtil.printlnGreen("B6 : " + (System.currentTimeMillis() - tt) + "ms");
                // Sync status update
                updateSync.setLong(1, blockUid);
                updateSync.execute();
                ConsoleUtil.printlnGreen("B7 : " + (System.currentTimeMillis() - tt) + "ms");
            } catch (Exception e) {
                connection.rollback();
            } finally {
                connection.commit();
                ConsoleUtil.printlnYellow("C1 : " + (System.currentTimeMillis() - tt) + "ms");
                connection.setAutoCommit(true);
                ConsoleUtil.printlnYellow("C2 : " + (System.currentTimeMillis() - tt) + "ms");

                close(insertBlock);
                ConsoleUtil.printlnYellow("C3 : " + (System.currentTimeMillis() - tt) + "ms");
                close(blockInsertResult);
                ConsoleUtil.printlnYellow("C4 : " + (System.currentTimeMillis() - tt) + "ms");
                close(insertTx);
                ConsoleUtil.printlnYellow("C5 : " + (System.currentTimeMillis() - tt) + "ms");
                close(updateSync);
                ConsoleUtil.printlnYellow("C6 : " + (System.currentTimeMillis() - tt) + "ms");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ConsoleUtil.printlnGreen("B8 : " + (System.currentTimeMillis() - tt) + "ms");
    }*/


    void insertBlocks(List<Block> blocks) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement insertBlock = connection.prepareStatement("INSERT INTO blocks (hash, blockNumber) VALUES (?, ?)");
            PreparedStatement insertTx = connection.prepareStatement("INSERT INTO transactions (txHash, receiver, sender, blockUid) values (?, ?, ?, ?)");
            PreparedStatement updateSync = connection.prepareStatement("UPDATE `db_info` SET `last_synced_block` = ?");
            ResultSet blockInsertResult = null;

            // block이 존재하면 패스
            try {
                for (Block block : blocks) {
                    if (isExistBlock(block.getHash())) {
                        continue;
                    }

                    // Block Insert
                    long blockUid = 0;
                    String blockHash = ByteUtil.toHexString(block.getHash());
                    insertBlock.setString(1, blockHash);
                    insertBlock.setLong(2, block.getNumber());
                    insertBlock.execute();

                    blockInsertResult = insertBlock.getGeneratedKeys();
                    if (blockInsertResult.next()) {
                        blockUid = blockInsertResult.getLong(1);
                    }

                    // Transaction Insert
                    for (Transaction tx : block.getTransactionsList()) {
                        insertTx.setString(1, ByteUtil.toHexString(tx.getHash()));            // txHash
                        insertTx.setString(2, ByteUtil.toHexString(tx.getReceiveAddress()));  // receiver
                        insertTx.setString(3, ByteUtil.toHexString(tx.getSender()));          // sender
                        insertTx.setLong(4, blockUid);
                        insertTx.addBatch();
                    }

                    // Sync status update
                    updateSync.setLong(1, blockUid);
                    updateSync.execute();
                }
                insertTx.executeBatch();

            } catch (Exception e) {
                connection.rollback();
            } finally {
                connection.commit();
                connection.setAutoCommit(true);

                close(insertBlock);
                close(blockInsertResult);
                close(insertTx);
                close(updateSync);
            }

        } catch(SQLException e){
            e.printStackTrace();
        }
    }

}
