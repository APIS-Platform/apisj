package org.apis;

import org.apis.cli.CLIStart;
import org.apis.config.Constants;
import org.apis.config.SystemProperties;
import org.apis.core.Block;
import org.apis.core.Repository;
import org.apis.core.TransactionReceipt;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumFactory;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.rpc.RPCServerManager;
import org.apis.util.ConsoleUtil;
import org.apis.util.blockchain.ApisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;


public class Start {

    private static Ethereum mEthereum;

    private static boolean synced = false;
    protected static Logger logger = LoggerFactory.getLogger("start");

    public static void main(String args[]) throws Exception {
        CLIStart cliStart = new CLIStart();

        cliStart.startKeystoreCheck();

        cliStart.startRpcServerCheck();



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

        mEthereum = EthereumFactory.createEthereum();
        mEthereum.addListener(mListener);
        mEthereum.getBlockMiner().setMinGasPrice(ApisUtil.convert(50, ApisUtil.Unit.nAPIS));

        if (actionBlocksLoader) {
            mEthereum.getBlockLoader().loadBlocks();
        }

        // start server
        try {
            RPCServerManager rpcServerManager = RPCServerManager.getInstance(mEthereum);
            if(rpcServerManager.isAvailable()) {
                rpcServerManager.startServer();
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
            logger.debug(ConsoleUtil.colorBRed("SYNC DONE =============================================="));
        }

        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            logger.debug(ConsoleUtil.colorBBlue("OnBlock : %s (%.2f kB)", block.getShortDescr(), block.getEncoded().length/1000f));

            Constants constants = Objects.requireNonNull(SystemProperties.getDefault()).getBlockchainConfig().getConfigForBlock(block.getNumber()).getConstants();

            logger.debug(ConsoleUtil.colorYellow("MASTERNODE REWARD : " + ApisUtil.readableApis(mEthereum.getRepository().getBalance(constants.getMASTERNODE_STORAGE()))));

            List<byte[]> generalEarlyRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_EARLY());
            List<byte[]> generalEarlyRunRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_EARLY_RUN());
            List<byte[]> generalRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_NORMAL());
            List<byte[]> generalLateRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_GENERAL_BASE_LATE());

            List<byte[]> majorEarlyRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_EARLY());
            List<byte[]> majorEarlyRunRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_EARLY_RUN());
            List<byte[]> majorRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_NORMAL());
            List<byte[]> majorLateRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_MAJOR_BASE_LATE());

            List<byte[]> privateEarlyRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_EARLY());
            List<byte[]> privateEarlyRunRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_EARLY_RUN());
            List<byte[]> privateRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_NORMAL());
            List<byte[]> privateLateRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_PRIVATE_BASE_LATE());

            logger.debug(ConsoleUtil.colorYellow("REPO EARLY  G:%d\t M:%d\t P:%d", generalEarlyRepo.size(), majorEarlyRepo.size(), privateEarlyRepo.size()));
            logger.debug(ConsoleUtil.colorYellow("REPO EARUN  G:%d\t M:%d\t P:%d", generalEarlyRunRepo.size(), majorEarlyRunRepo.size(), privateEarlyRunRepo.size()));
            logger.debug(ConsoleUtil.colorYellow("REPO NORMA  G:%d\t M:%d\t P:%d", generalRepo.size(), majorRepo.size(), privateRepo.size()));
            logger.debug(ConsoleUtil.colorYellow("REPO LATE   G:%d\t M:%d\t P:%d", generalLateRepo.size(), majorLateRepo.size(), privateLateRepo.size()));

            generalEarlyRunRepo.addAll(generalRepo);
            generalEarlyRunRepo.addAll(generalLateRepo);
            majorEarlyRunRepo.addAll(majorRepo);
            majorEarlyRunRepo.addAll(majorLateRepo);
            privateEarlyRunRepo.addAll(privateRepo);
            privateEarlyRunRepo.addAll(privateLateRepo);

            logger.debug(ConsoleUtil.colorGreen("REPO ALL    G:%d\t M:%d\t P:%d", generalEarlyRunRepo.size(), majorEarlyRunRepo.size(), privateEarlyRunRepo.size()));

            if(constants.isMasternodeRewardBlock(block.getNumber())) {
                logger.debug(ConsoleUtil.colorCyan("BLOCK G:%d M:%d P:%d", block.getMnGeneralList().size(), block.getMnMajorList().size(), block.getMnPrivateList().size()));
            }
        }
    };
}
