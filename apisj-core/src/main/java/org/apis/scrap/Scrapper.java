package org.apis.scrap;

import org.apache.http.conn.util.InetAddressUtils;
import org.apis.config.SystemProperties;
import org.apis.core.*;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumFactory;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.util.BIUtil;
import org.apis.util.ConsoleUtil;
import org.apis.vm.program.InternalTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.*;
import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;


public class Scrapper implements Runnable {

    @Autowired
    protected static Ethereum mEthereum;

    @Autowired
    protected SystemProperties config;

    private boolean synced = false;
    private static Timer timerSubmitMinerState;

    private Logger logger;
    private static Connection conn;


    public static void main(String args[]) {
        String host;
        String port;

        try {
            // host 주소를 입력받는다
            boolean isValidHost;
            do {
                host = ConsoleUtil.readLine("Input database(MySQL) host (Default:localhost) : ");
                if (host.isEmpty()) {
                    host = "localhost";
                    break;
                } else if(host.equalsIgnoreCase("localhost")) {
                    break;
                }
                isValidHost = InetAddressUtils.isIPv4Address(host);
            } while (!isValidHost);

            println("Host : " + host + "\n");

            // port 번호를 입력받는다
            boolean isValidPort;
            do {
                port = ConsoleUtil.readLine("Input database port (Default:3306): ");
                if(port.isEmpty()) {
                    port = "3306";
                    break;
                }
                try {
                    int portInt = Integer.parseUnsignedInt(port);
                    isValidPort = (portInt > 0 && portInt < 65536);
                } catch(NumberFormatException e) {
                    isValidPort = false;
                }
            } while (!isValidPort);

            String user = ConsoleUtil.readLine("Input database user : ");
            char[] password = ConsoleUtil.readPassword("Input database password : ");

            String url = "jdbc:mysql://" + host + ":" + port;
            println(url);

            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, new String(password));
        } catch (IOException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }


        mEthereum = EthereumFactory.createEthereum();
        mEthereum.addListener(mListener);
    }

    private static void println(String str) {
        System.out.println(str);
    }

    private void setupLogging() {
        logger = LoggerFactory.getLogger("Scrapper");
    }

    @PostConstruct
    private void springInit() {
        setupLogging();

        // adding the main EthereumJ callback to be notified on different kind of events
        mEthereum.addListener(mListener);

        logger.info("Listening for apis events...");

        // starting lifecycle tracking method scrap()
        new Thread(this, "ScrapWorkThread").start();
    }

    @Override
    public void run() {

    }

    private static EthereumListener mListener = new EthereumListenerAdapter() {

        @Override
        public void onSyncDone(SyncState state) {
            //synced = true;
            System.out.println("SYNC DONE. Blocks upload start");

            // 업로드를 시작할 블록 번호를 확인한다.
            try {
                conn.setCatalog("explorer");
                Statement stmt = conn.createStatement();

                String query = "SELECT `blockNumber` FROM `blocks` ORDER BY `blockNumber` DESC LIMIT 1";
                ResultSet result = stmt.executeQuery(query);

                long lastBlockNumber = 0;
                if(result.next()) {
                    lastBlockNumber = Math.max(0, result.getLong(1) - 1);
                }
                stmt.close();
                result.close();

                while(true) {
                    if(lastBlockNumber >= mEthereum.getBlockchain().getBestBlock().getNumber()) {
                        Thread.sleep(1_000L);
                        continue;
                    }

                    lastBlockNumber += 1;
                    Block block = mEthereum.getBlockchain().getBlockByNumber(lastBlockNumber);

                    InsertBlock insertBlock = new InsertBlock(block);
                    PreparedStatement preparedStatement = insertBlock.getInsertState(conn);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();

                    // 각 블록의 트랜잭션들을 기록한다.
                    for (Transaction tx : block.getTransactionsList()) {
                        TransactionInfo transactionInfo = mEthereum.getTransactionInfo(tx.getHash());
                        if (transactionInfo == null) break;

                        TransactionReceipt receipt = transactionInfo.getReceipt();

                        PreparedStatement txState = new InsertTx(tx, block.getHash()).getInsertState(conn);
                        txState.executeUpdate();
                        txState.close();


                        PreparedStatement receiptState = new InsertTxReceipt(receipt, tx.getHash()).getInsertState(conn);
                        receiptState.executeUpdate();
                        receiptState.close();

                        updateAccount(tx.getSender());
                        updateAccount(tx.getReceiveAddress());
                    };
                }
            } catch (SQLException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         *  블록들을 전달받았으면 다른 노드들에게 현재의 RP를 전파해야한다.
         */
        @Override
        public synchronized void onBlock(Block block, List<TransactionReceipt> receipts) {
            System.out.println( block.getNumber() + "th : \t" + block.getShortHash() + "  *******");
        }

        @Override
        public void onTransactionExecuted(TransactionExecutionSummary summary) {
            List<InternalTransaction> internalTxs = summary.getInternalTransactions();

            for(InternalTransaction internalTx : internalTxs) {
                PreparedStatement receiptState;
                try {
                    receiptState = new InsertInternalTx(internalTx).getInsertState(conn);
                    receiptState.executeUpdate();
                    receiptState.close();

                    updateAccount(internalTx.getSender());
                    updateAccount(internalTx.getReceiveAddress());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private static void updateAccount(byte[] address) throws SQLException {
        BigInteger balance = mEthereum.getRepository().getBalance(address);

        PreparedStatement state = new InsertAccount(address, balance).getInsertState(conn);
        state.executeUpdate();
        state.close();
    }

}
