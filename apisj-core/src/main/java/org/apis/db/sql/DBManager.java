package org.apis.db.sql;

import org.apis.config.SystemProperties;
import org.apis.core.TransactionReceipt;
import org.apis.util.ByteUtil;
import org.apis.vm.LogInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;

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
        String query = "SELECT * FROM `db_info` WHERE `version` > 0";
        PreparedStatement prep;
        try {
            prep = conn.prepareStatement(query);
            ResultSet row = prep.executeQuery();
            if(row.next()) {
                if(row.getInt("version") < DB_VERSION) {
                    update(conn);
                }
            }
        } catch (SQLException e) {
            create(conn);
        }
    }

    private void create(Connection conn) throws SQLException {
        String queryCreateAccounts = "CREATE TABLE \"accounts\" ( `address` TEXT NOT NULL UNIQUE, `title` TEXT DEFAULT 'Unnamed', `balance` TEXT, `mask` TEXT, `rewards` TEXT, `first_tx_block_number` INTEGER, PRIMARY KEY(`address`) )";
        String queryCreateContracts = "CREATE TABLE \"contracts\" ( `address` TEXT NOT NULL UNIQUE, `creator` TEXT, `title` TEXT DEFAULT 'Unnamed', `balance` TEXT, `mask` TEXT, `abi` TEXT, `canvas_url` TEXT, `first_tx_block_number` INTEGER, PRIMARY KEY(`address`) )";
        String queryCreateRewards = "CREATE TABLE \"rewards\" ( `address` TEXT, `recipient` TEXT, `block_hash` TEXT, `block_number` INTEGER, `type` INTEGER, `amount` TEXT, FOREIGN KEY(`address`) REFERENCES `accounts`(`address`), PRIMARY KEY(`address`) )";
        String queryCreateTransactions = "CREATE TABLE \"transactions\" ( `block_number` INTEGER, `hash` TEXT NOT NULL UNIQUE, `nonce` INTEGER, `gasPrice` TEXT, `gasLimit` INTEGER, `to` TEXT, `from` TEXT, `toMask` TEXT, `amount` TEXT, `data` TEXT, `status` INTEGER, `gasUsed` INTEGER, `mineralUsed` TEXT, `error` TEXT, `bloom` TEXT, `logs` TEXT, `block_hash` TEXT )";
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
    }

    private void update(Connection conn) {
        String queryDeleteAccounts = "DROP TABLE IF EXISTS `accounts`";

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
            PreparedStatement state = this.connection.prepareStatement("INSERT OR REPLACE INTO accounts (address, title, balance, mask, rewards) values (?, ?, ?, ?, ?)");
            state.setString(1, ByteUtil.toHexString(address));
            state.setString(2, title);
            state.setString(3, ByteUtil.toHexString(balance.toByteArray()));
            state.setString(4, mask);
            state.setString(5, ByteUtil.toHexString(rewards.toByteArray()));
            return state.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<AccountWallet> selectAccounts() {
        if(!open(true)) {
            return null;
        }

        List<AccountWallet> wallets = new ArrayList<>();

        try {
            PreparedStatement state = this.connection.prepareStatement("SELECT * FROM `accounts` ORDER BY `balance` DESC");
            ResultSet result = state.executeQuery();

            while(result.next()) {
                wallets.add(new AccountWallet(result));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wallets;
    }
}
