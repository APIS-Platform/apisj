package org.apis;

import org.apis.cli.CLIInterface;
import org.apis.cli.CLIStart;
import org.apis.config.SystemProperties;
import org.apis.core.Block;
import org.apis.core.TransactionReceipt;
import org.apis.db.sql.DBSyncManager;
import org.apis.facade.Apis;
import org.apis.facade.ApisFactory;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.rpc.RPCServerManager;
import org.apis.util.ConsoleUtil;
import org.apis.util.TimeUtils;
import org.apis.util.blockchain.ApisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Start {

    private static Apis mApis;

    private static boolean isRunRpc = false;
    private static boolean synced = false;
    protected static Logger logger = LoggerFactory.getLogger("start");
    /**
     * 마지막으로 블록을 전달 받은 이후, 일정 시간이 지나도 싱크가 안된다면 프로그램을 재시작하고자 한다.
     * 이를 위해 마지막으로 블록을 전달받은 시간을 저장한다.
     */
    private static long timeLastBlockReceived = 0;

    private static long timeLastProgramClosed = 0;

    /**
     * 블록 싱크가 멈춘 후, 프로그램을 재시작하기까지 대기하는 시간
     */
    private static final long TIME_CLOSE_WAIT = 3*60*1_000L;

    private static final long TIME_RESTART_WAIT = 30*1_000L;

    private static boolean isClosed = false;



    public static void main(String args[]) throws IOException {
        new CLIStart();
        CLIInterface.call(args);

        final SystemProperties config = SystemProperties.getDefault();
        if(config == null) {
            System.out.println("Failed to load config");
            System.exit(0);
        }


        final boolean actionBlocksLoader = !config.blocksLoader().equals("");

        if (actionBlocksLoader) {
            config.setSyncEnabled(false);
            config.setDiscoveryEnabled(false);
        }

        startAPIS();

        if (actionBlocksLoader) {
            mApis.getBlockLoader().loadBlocks();
        }

        // start server
        try {
            RPCServerManager rpcServerManager = RPCServerManager.getInstance(mApis);
            if(rpcServerManager.isAvailable()) {
                rpcServerManager.startServer();
                isRunRpc = true;
            }
        } catch (IOException e) {
            logger.error(ConsoleUtil.colorRed("The RPC server can not be started."));
            System.exit(0);
        }
    }

    private static void startAPIS() {
        mApis = ApisFactory.createEthereum();
        mApis.addListener(mListener);
        mApis.getBlockMiner().setMinGasPrice(ApisUtil.convert(50, ApisUtil.Unit.nAPIS));
    }

    private static EthereumListener mListener = new EthereumListenerAdapter() {

        @Override
        public void onSyncDone(SyncState state) {
            synced = true;
            logger.debug(ConsoleUtil.colorBRed("\nSYNC DONE =============================================="));

            /*
             * 싱크가 완료된 이후, 현재의 싱크 상태를 확인해서 프로그램 재시작 여부를 판단한다.
             */
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                long now = TimeUtils.getRealTimestamp();

                // 싱크가 지연된 경우 프로그램을 종료한다.
                if(!isClosed && now - timeLastBlockReceived >= TIME_CLOSE_WAIT) {
                    timeLastProgramClosed = now;
                    isClosed = true;
                    mApis.close();
                }

                // 프로그램 종료 후 일정 시간이 경과하면 프로그램을 시작시킨다.
                if(isClosed && timeLastProgramClosed > 0 && now - timeLastProgramClosed >= TIME_RESTART_WAIT) {
                    startAPIS();
                    isClosed = false;
                }

            }, 30, 1, TimeUnit.SECONDS);
        }

        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            logger.debug(ConsoleUtil.colorBBlue("\nOnBlock : %s (%.2f kB)", block.getShortDescr(), block.getEncoded().length/1000f));
            timeLastBlockReceived = TimeUtils.getRealTimestamp();

            // 체인 싱크가 완료되면 SQL 서버 싱크를 시작한다.
            if(synced && isRunRpc) {
                // DB Sync Start
                DBSyncManager.getInstance(mApis).syncThreadStart();
            }
        }
    };


}
