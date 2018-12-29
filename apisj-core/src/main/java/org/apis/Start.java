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
import org.apis.util.blockchain.ApisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;


public class Start {

    private static Apis mApis;

    private static boolean isRunRpc = false;
    private static boolean synced = false;
    protected static Logger logger = LoggerFactory.getLogger("start");

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

        mApis = ApisFactory.createEthereum();
        mApis.addListener(mListener);
        mApis.getBlockMiner().setMinGasPrice(ApisUtil.convert(50, ApisUtil.Unit.nAPIS));

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

    private static EthereumListener mListener = new EthereumListenerAdapter() {

        @Override
        public void onSyncDone(SyncState state) {
            synced = true;
            logger.debug(ConsoleUtil.colorBRed("\nSYNC DONE =============================================="));
        }

        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            logger.debug(ConsoleUtil.colorBBlue("\nOnBlock : %s (%.2f kB)", block.getShortDescr(), block.getEncoded().length/1000f));

            // 체인 싱크가 완료되면 SQL 서버 싱크를 시작한다.
            if(synced && isRunRpc) {
                // DB Sync Start
                DBSyncManager.getInstance(mApis).syncThreadStart();
            }
        }
    };
}
