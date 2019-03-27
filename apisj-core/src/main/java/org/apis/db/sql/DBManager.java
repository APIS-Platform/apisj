package org.apis.db.sql;

import org.apis.config.SystemProperties;
import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.core.TransactionInfo;
import org.apis.core.TransactionReceipt;
import org.apis.facade.Apis;
import org.apis.util.ByteUtil;
import org.apis.util.TimeUtils;
import org.apis.vm.DataWord;
import org.apis.vm.LogInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.DecoderException;

import java.io.File;
import java.math.BigInteger;
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
    private static int DB_VERSION = 2;
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
        String queryCreateContracts = "CREATE TABLE \"contracts\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `address` TEXT NOT NULL UNIQUE, `title` TEXT DEFAULT 'Unnamed', `mask` TEXT, `abi` TEXT, `canvas_url` TEXT, `first_tx_block_number` INTEGER, `last_synced_block` INTEGER DEFAULT 1 )";
        String queryCreateTransactions = "CREATE TABLE \"transactions\" ( `txhash` BLOB NOT NULL UNIQUE, `receiver` BLOB, `sender` BLOB, `blockUid` NUMERIC, FOREIGN KEY(`blockUid`) REFERENCES `blocks`(`uid`), PRIMARY KEY(`txhash`) )";
        String queryCreateEvents = "CREATE TABLE \"events\" ( `tx_hash` TEXT UNIQUE, `address` TEXT, `topic` TEXT )";
        String queryCreateAbis = "CREATE TABLE \"abis\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `creator` TEXT, `contract_name` TEXT, `contract_address` TEXT UNIQUE, `abi` TEXT, `created_at` INTEGER )";
        String queryCreateDBInfo = "CREATE TABLE \"db_info\" ( `uid` INTEGER, `version` INTEGER, `last_synced_block` INTEGER, PRIMARY KEY(`uid`) )";
        String queryCreateAddressGroups = "CREATE TABLE \"address_group\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `group_name` TEXT )";
        String queryCreateMyAddress = "CREATE TABLE \"myaddress\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `address` TEXT NOT NULL UNIQUE, `alias` TEXT DEFAULT 'Unnamed', `exist` INTEGER DEFAULT 0 )";
        String queryCreateConnectAddressGroups = "CREATE TABLE \"connect_address_group\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `address` TEXT NOT NULL, `group_name` TEXT NOT NULL )";
        String queryCreateRecentAddress = "CREATE TABLE \"recent_address\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `tx_hash` TEXT UNIQUE, `address` TEXT NOT NULL UNIQUE, `alias` TEXT DEFAULT 'Unnamed', `created_at` INTEGER )";
        String queryCreateBlocks = "CREATE TABLE \"blocks\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `hash` BLOB NOT NULL UNIQUE, `blockNumber` INTEGER )";
        String queryCreateTokens = "CREATE TABLE \"tokens\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `token_address` TEXT NOT NULL UNIQUE, `token_name` TEXT DEFAULT 'Unnamed', `token_symbol` TEXT NOT NULL, `decimal` INTEGER, `total_supply` TEXT )";
        String queryCreateLedgers = "CREATE TABLE \"ledgers\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `address` TEXT NOT NULL UNIQUE, `path` TEXT NOT NULL, `alias` TEXT DEFAULT 'Unnamed' )";


        String queryIndexEvent = "CREATE INDEX `eventIndex` ON `events` ( `address` )";
        String queryIndexTxReceiver = "CREATE INDEX `txReceiver` ON `transactions` ( `receiver` )";
        String queryIndexTxSender = "CREATE INDEX `txSender` ON `transactions` ( `sender` )";
        String queryIndexTxBlockUid = "CREATE INDEX `txBlockUid` ON `transactions` ( `blockUid` )";

        PreparedStatement createContracts = conn.prepareStatement(queryCreateContracts);
        createContracts.execute();
        createContracts.close();

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

        PreparedStatement createIndexTxSender = conn.prepareStatement(queryIndexTxSender);
        createIndexTxSender.execute();
        createIndexTxSender.close();

        PreparedStatement createIndexTxReceiver = conn.prepareStatement(queryIndexTxReceiver);
        createIndexTxReceiver.execute();
        createIndexTxReceiver.close();

        PreparedStatement createIndexTxBlockUid = conn.prepareStatement(queryIndexTxBlockUid);
        createIndexTxBlockUid.execute();
        createIndexTxBlockUid.close();

        PreparedStatement createIndexEvent = conn.prepareStatement(queryIndexEvent);
        createIndexEvent.execute();
        createIndexEvent.close();

        PreparedStatement createTokens = conn.prepareStatement(queryCreateTokens);
        createTokens.execute();
        createTokens.close();

        PreparedStatement createLedgers = conn.prepareStatement(queryCreateLedgers);
        createLedgers.execute();
        createLedgers.close();



        PreparedStatement state = conn.prepareStatement("insert or replace into db_info (uid, version, last_synced_block) values (1, ?, ?)");
        state.setInt(1, DB_VERSION);
        state.setInt(2, 0);
        state.execute();
        state.close();
    }

    private void update(Connection conn) throws SQLException {
        String queryCreateLedgers = "CREATE TABLE IF NOT EXISTS \"ledgers\" ( `uid` INTEGER PRIMARY KEY AUTOINCREMENT, `address` TEXT NOT NULL UNIQUE, `path` TEXT NOT NULL, `alias` TEXT DEFAULT 'Unnamed' )";

        PreparedStatement createLedgers = conn.prepareStatement(queryCreateLedgers);
        createLedgers.execute();
        createLedgers.close();

        PreparedStatement state = conn.prepareStatement("insert or replace into db_info (uid, version, last_synced_block) values (1, ?, ?)");
        state.setInt(1, DB_VERSION);
        state.setInt(2, 0);
        state.execute();
        state.close();

        logger.debug("Database Update!");
    }



    public boolean updateContractCreation(TransactionInfo txInfo) {
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

    public boolean updateContractCode(byte[] address, String title, String abi) {
        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE contracts SET title = ?, abi = ? WHERE address = ?");
            update.setString(1, title);
            update.setString(2, abi);
            update.setString(3, ByteUtil.toHexString(address));
            int updateResult = update.executeUpdate();
            if(updateResult == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO contracts (address, title, mask, abi, canvas_url) values (?, ?, ?, ?, ?)");
                state.setString(1, ByteUtil.toHexString(address));
                state.setString(2, title);
                state.setString(3, "");
                state.setString(4, abi);
                state.setString(5, "");
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


    public List<TransactionRecord> selectRecentTransactions(long rowCount, long offset) {
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
            query = "SELECT * FROM transactions ORDER BY blockUid DESC" + limit;
            state = this.connection.prepareStatement(query);
            result = null;

            result = state.executeQuery();

            while(result.next()) {
                transactions.add(new TransactionRecord(result));
            }
        } catch (SQLException | DecoderException ignored) {
        } finally {
            close(state);
            close(result);
        }

        return transactions;
    }

    private static final int TYPE_SELECT_ALL = 0;
    private static final int TYPE_SELECT_SEND = 1;
    private static final int TYPE_SELECT_RECEIVE = 2;

    public long countOfSendingTransaction(byte[] address) {
        return countOfTransaction(address, TYPE_SELECT_SEND);
    }
    public long countOfReceivingTransaction(byte[] address) {
        return countOfTransaction(address, TYPE_SELECT_RECEIVE);
    }
    public long countOfAllTransaction(byte[] address) {
        return countOfTransaction(address, TYPE_SELECT_ALL);
    }

    private long countOfTransaction(byte[] address, int type) {
        if(address == null || address.length == 0) {
            return 0;
        }

        String query;
        PreparedStatement state = null;
        ResultSet result = null;
        long countResult = 0;

        try {
            switch(type) {
                case TYPE_SELECT_SEND:
                    query = "SELECT count(*) FROM transactions WHERE sender = ?";
                    state = this.connection.prepareStatement(query);
                    state.setBytes(1, address);
                    break;
                case TYPE_SELECT_RECEIVE:
                    query = "SELECT count(*) FROM transactions WHERE receiver = ?";
                    state = this.connection.prepareStatement(query);
                    state.setBytes(1, address);
                    break;
                case TYPE_SELECT_ALL:
                default:
                    query = "SELECT count(*) FROM transactions WHERE receiver = ? OR sender = ?";
                    state = this.connection.prepareStatement(query);
                    state.setBytes(1, address);
                    state.setBytes(2, address);
                    break;
            }
            result = state.executeQuery();

            if(result.next()) {
                countResult = result.getLong(1);
            }

        } catch (SQLException | DecoderException ignored) {
        } finally {
            close(state);
            close(result);
        }

        return countResult;
    }


    public List<TransactionRecord> selectTransactions(String searchText, long rowCount, long offset) {
        long blockNumber = selectBlockUidWithBlockNum(searchText);

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
            if(searchText == null || searchText.isEmpty()) {
                query = "SELECT * FROM transactions ORDER BY blockUid DESC" + limit;
                state = this.connection.prepareStatement(query);
            } else {
                byte[] searchBytes = ByteUtil.hexStringToBytes(searchText);

                query = "SELECT * FROM transactions WHERE receiver = ? OR sender = ? OR txhash = ? OR blockUid = ? ORDER BY blockUid DESC" + limit;
                state = this.connection.prepareStatement(query);
                state.setBytes(1, searchBytes);
                state.setBytes(2, searchBytes);
                state.setBytes(3, searchBytes);
                state.setLong(4, blockNumber);
            }

            result = state.executeQuery();

            while(result.next()) {
                transactions.add(new TransactionRecord(result));
            }
        } catch (SQLException | DecoderException ignored) {
        } finally {
            close(state);
            close(result);
        }

        return transactions;
    }

    public long selectTransactionsAllCount(String searchText) {
        long count = 0;
        long blockNumber = selectBlockUidWithBlockNum(searchText);

        String query;
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            if(searchText == null || searchText.length() == 0) {
                query = "SELECT COUNT(*) as cnt FROM transactions";
                state = this.connection.prepareStatement(query);
            } else {
                byte[] searchBytes = ByteUtil.hexStringToBytes(searchText);

                query = "SELECT COUNT(*) as cnt FROM transactions WHERE receiver = ? OR sender = ? OR txhash = ? OR blockUid = ? ORDER BY blockUid DESC ";
                state = this.connection.prepareStatement(query);
                state.setBytes(1, searchBytes);
                state.setBytes(2, searchBytes);
                state.setBytes(3, searchBytes);
                state.setLong(4, blockNumber);
            }

            result = state.executeQuery();
            if(result.next()){
                count = result.getLong("cnt");
            }
        }
        catch (SQLException | DecoderException ignored) {}
        finally {
            close(state);
            close(result);
        }

        return count;
    }


    public long selectBlockUidWithBlockNum(String blockNum) {
        long blockUid = 0;

        String query;
        PreparedStatement state = null;
        ResultSet result = null;

        if (blockNum != null && !blockNum.isEmpty()) {
            try {
                query = "SELECT uid FROM blocks WHERE blockNumber = ? ORDER BY uid DESC ";
                state = this.connection.prepareStatement(query);
                state.setString(1, blockNum);

                result = state.executeQuery();
                if (result.next()) {
                    blockUid = result.getLong("uid");
                }
            } catch(SQLException e){
                e.printStackTrace();
            } finally{
                close(state);
                close(result);
            }
        }
        return blockUid;
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

    public boolean updateMyAddressExistZero(){
        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE myaddress SET `exist` = 0");
            int updateResult = update.executeUpdate();
            update.close();

            return updateResult > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateMyAddress( byte[] address, String alias, int exist ){

        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE myaddress SET `address` = ?, `alias` = ?, `exist` = ? WHERE `address` = ?");
            update.setString(1, ByteUtil.toHexString(address));
            update.setString(2, alias);
            update.setInt(3, exist);
            update.setString(4, ByteUtil.toHexString(address));
            int updateResult = update.executeUpdate();
            update.close();
            if(updateResult == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO myaddress (`address`, `alias`, `exist`) values (?, ?, ?)");
                state.setString(1, ByteUtil.toHexString(address));
                state.setString(2, alias);
                state.setInt(3, exist);
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
            state = this.connection.prepareStatement("SELECT * FROM `myaddress` ORDER BY exist ASC, LOWER(alias) ASC");
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
                    " ORDER BY exist ASC, LOWER(alias) ASC";

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

    public List<ConnectAddressGroupRecord> selectConnectAddressGroups(){
        List<ConnectAddressGroupRecord> connectAddressGroupRecord = new ArrayList<>();
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            state = this.connection.prepareStatement("SELECT * FROM `connect_address_group` ORDER BY group_name ");
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

    public List<TokenRecord> selectTokens(){
        List<TokenRecord> tokenRecord = new ArrayList<>();
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            state = this.connection.prepareStatement("SELECT * FROM `tokens` ORDER BY token_name ");
            result = state.executeQuery();

            while(result.next()) {
                tokenRecord.add(new TokenRecord(result));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }
        return tokenRecord;
    }

    public boolean updateTokens( byte[] tokenAddress, String tokenName, String tokenSymbol, long decimal, BigInteger totalSupply ){

        try {
            PreparedStatement update = this.connection.prepareStatement("UPDATE tokens SET `token_address` = ?, `token_name` = ?, `token_symbol` = ?, `decimal` = ?, `total_supply` = ? WHERE `token_address` = ?");
            update.setString(1, ByteUtil.toHexString(tokenAddress));
            update.setString(2, tokenName);
            update.setString(3, tokenSymbol);
            update.setLong(4, decimal);
            update.setString(5, ByteUtil.toHexString(totalSupply.toByteArray()));
            update.setString(6, ByteUtil.toHexString(tokenAddress));
            int updateResult = update.executeUpdate();
            update.close();
            if(updateResult == 0) {
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO tokens (`token_address`, `token_name`, `token_symbol`, `decimal`, `total_supply`) values (?, ?, ?, ?, ?)");
                state.setString(1, ByteUtil.toHexString(tokenAddress));
                state.setString(2, tokenName);
                state.setString(3, tokenSymbol);
                state.setLong(4, decimal);
                state.setString(5, ByteUtil.toHexString(totalSupply.toByteArray()));
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

    public boolean deleteToken(byte[] tokenAddress){
        try {
            PreparedStatement state = this.connection.prepareStatement("DELETE FROM `tokens` WHERE token_address = ?");
            state.setString(1, ByteUtil.toHexString(tokenAddress));
            boolean deleteResult = state.execute();
            state.close();

            return deleteResult;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<LedgerRecord> selectLedgers(){
        List<LedgerRecord> ledgerRecords = new ArrayList<>();
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            state = this.connection.prepareStatement("SELECT * FROM `ledgers`");
            result = state.executeQuery();

            while(result.next()) {
                ledgerRecords.add(new LedgerRecord(result));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }
        return ledgerRecords;
    }

    public LedgerRecord selectLedger(byte[] address) {
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            state = this.connection.prepareStatement("SELECT * FROM `ledgers` WHERE `address` = ?");
            state.setString(1, ByteUtil.toHexString(address));
            result = state.executeQuery();

            if(result.next()) {
                return new LedgerRecord(result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }

        return null;
    }

    public boolean updateLedgers( byte[] address, String path, String alias ) {
        try {
            int updateResult = 0;
            PreparedStatement update = this.connection.prepareStatement("UPDATE ledgers SET `address` = ?, `path` = ?, `alias` = ? WHERE `address` = ?");
            update.setString(1, ByteUtil.toHexString(address));
            update.setString(2, path);
            update.setString(3, alias);
            update.setString(4, ByteUtil.toHexString(address));
            updateResult = update.executeUpdate();
            update.close();
            if(updateResult == 0) {
                boolean insertResult = false;
                PreparedStatement state = this.connection.prepareStatement("INSERT INTO ledgers (`address`, `path`, `alias`) values (?, ?, ?)");
                state.setString(1, ByteUtil.toHexString(address));
                state.setString(2, path);
                state.setString(3, alias);
                insertResult = state.execute();
                state.close();
                return insertResult;
            }
            return updateResult > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteLedger(byte[] address){
        try {
            PreparedStatement state = this.connection.prepareStatement("DELETE FROM `ledgers` WHERE address = ?");
            state.setString(1, ByteUtil.toHexString(address));
            boolean deleteResult = state.execute();
            state.close();

            return deleteResult;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
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



    private boolean isExistBlock(Block block) {
        byte[] blockHash = block.getHash();
        PreparedStatement state = null;
        ResultSet result = null;

        // 같은 번호인데, 해시가 다른 블록을 조사한다.
        // 그 경우는 re-branch 된 것일 수 있으므로 기존 블록 정보와 트랜잭션 정보를 삭제한다.
        try {
            state = this.connection.prepareStatement("SELECT * FROM blocks WHERE blockNumber = ? AND hash != ?");
            state.setLong(1, block.getNumber());
            state.setBytes(2, block.getHash());
            result = state.executeQuery();

            while(result.next()) {
                deleteBlock(result.getBytes("hash"));
                deleteTxInBlock(result.getLong("uid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
            close(result);
        }


        try {
            state = this.connection.prepareStatement("SELECT * FROM blocks WHERE hash = ?");
            state.setBytes(1, blockHash);
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


    synchronized void insertBlocks(List<Block> blocks, Apis apis) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement insertBlock   = connection.prepareStatement("INSERT OR IGNORE INTO blocks (hash, blockNumber) VALUES (?, ?)");
            PreparedStatement insertTx      = connection.prepareStatement("INSERT OR REPLACE INTO transactions (txHash, receiver, sender, blockUid) VALUES (?, ?, ?, (SELECT uid FROM blocks WHERE hash = ?))");
            PreparedStatement insertEvent   = connection.prepareStatement("INSERT OR REPLACE INTO events (tx_hash, address, topic) VALUES (?, ?, ?)");
            PreparedStatement updateSync    = connection.prepareStatement("UPDATE `db_info` SET `last_synced_block` = ?");

            // block이 존재하면 패스
            try {
                long lastBlockNumber = 0;
                for(Block block : blocks) {
                    // Block Insert
                    insertBlock.setBytes(1, block.getHash());
                    insertBlock.setLong(2, block.getNumber());
                    insertBlock.addBatch();
                }
                insertBlock.executeBatch();

                for (Block block : blocks) {
                    // Transaction Insert
                    for (Transaction tx : block.getTransactionsList()) {
                        insertTx.setBytes(1, tx.getHash());            // txHash
                        insertTx.setBytes(2, tx.getReceiveAddress());  // receiver
                        insertTx.setBytes(3, tx.getSender());          // sender
                        insertTx.setBytes(4, block.getHash());
                        insertTx.addBatch();

                        TransactionReceipt txReceipt = apis.getTransactionInfo(tx.getHash()).getReceipt();
                        for(LogInfo logInfo : txReceipt.getLogInfoList()) {
                            for(DataWord topic : logInfo.getTopics()) {
                                byte[] topicBytes = topic.getData();

                                insertEvent.setBytes(1, tx.getHash());
                                insertEvent.setBytes(2, logInfo.getAddress());
                                insertEvent.setBytes(3, topicBytes);
                                insertEvent.addBatch();
                            }
                        }
                    }

                    lastBlockNumber = block.getNumber();
                }
                insertTx.executeBatch();
                insertEvent.executeBatch();

                // Sync status update
                updateSync.setLong(1, lastBlockNumber);
                updateSync.execute();

            } catch (Exception e) {
                e.printStackTrace();
                connection.rollback();
            } finally {
                connection.commit();
                connection.setAutoCommit(true);

                close(insertBlock);
                close(insertTx);
                close(insertEvent);
                close(updateSync);
            }

        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * onBlock 이벤트로 전달받은 블록들을 INSERT 하다보면
     * 더 우수한 체인의 블록을 전달받아서 같은 번호의 다른 블록이 저장될 수 있다.
     * 한 번호에 다수의 블록이 저장된 경우, 최고가 아닌 블록 정보는 삭제하도록 한다.
     */
    void trimBlocks(Apis apis) {
        PreparedStatement selectDuplicatedBlocks = null;
        PreparedStatement selectNotBestBlocks = null;
        ResultSet result = null;

        try {
            selectDuplicatedBlocks = connection.prepareStatement("SELECT blockNumber, COUNT(blockNumber) AS count FROM blocks where blockNumber > ? GROUP BY blockNumber HAVING COUNT(blockNumber) > 1");
            selectNotBestBlocks = connection.prepareStatement("SELECT uid, blockNumber, hash FROM blocks WHERE blockNumber = ? AND hash != ?");

            long startCondition = Math.max(apis.getBlockchain().getBestBlock().getNumber() - 100, 0);
            selectDuplicatedBlocks.setLong(1, startCondition);

            result = selectDuplicatedBlocks.executeQuery();

            while(result.next()) {
                long blockNumber = result.getLong(1);
                Block bestBlock = apis.getBlockchain().getBlockByNumber(blockNumber);

                selectNotBestBlocks.setLong(1, bestBlock.getNumber());
                selectNotBestBlocks.setBytes(2, bestBlock.getHash());
                ResultSet notBestResult = selectNotBestBlocks.executeQuery();

                while(notBestResult.next()) {
                    long blockUid = notBestResult.getLong(1);
                    byte[] blockHash = notBestResult.getBytes(3);

                    deleteTxInBlock(blockUid);
                    deleteBlock(blockHash);
                }

                close(notBestResult);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(selectDuplicatedBlocks);
            close(selectNotBestBlocks);
            close(result);
        }
    }

    private void deleteBlock(byte[] hash) {
        PreparedStatement state = null;
        try {
            state = this.connection.prepareStatement("DELETE FROM blocks WHERE hash = ?");
            state.setBytes(1, hash);
            state.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
        }
    }

    private void deleteTxInBlock(long blockUid) {
        PreparedStatement state = null;
        try {
            state = this.connection.prepareStatement("DELETE FROM transactions WHERE blockUid = ?");
            state.setLong(1, blockUid);
            state.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(state);
        }

    }
}
