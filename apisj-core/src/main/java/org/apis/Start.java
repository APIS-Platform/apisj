/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.apis;

import org.apis.cli.CLIStart;
import org.apis.config.Constants;
import org.apis.config.SystemProperties;
import org.apis.core.Block;
import org.apis.core.Repository;
import org.apis.core.Transaction;
import org.apis.core.TransactionReceipt;
import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;
import org.apis.db.ByteArrayWrapper;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumFactory;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.rpc.RPCWebSocketServer;
import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.apis.util.blockchain.ApisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.*;


public class Start {

    private static Ethereum mEthereum;

    private static boolean synced = false;
    protected static Logger logger = LoggerFactory.getLogger("start");

    static private Map<ByteArrayWrapper, TransactionReceipt> txWaiters =
            Collections.synchronizedMap(new HashMap<>());


    public static void main(String args[]) throws IOException, URISyntaxException {
        //CLIInterface.call(args);

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


        Properties prop = new Properties() {
            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<>(super.keySet()));
            }
        };

        // start server
        try {
            InputStream input = new FileInputStream("config/rpc.properties");
            prop.load(input);

            int port =  Integer.parseInt(prop.getProperty("port"));
            String id = prop.getProperty("id");
            char[] pw = prop.getProperty("password").toCharArray();
            boolean use = Boolean.parseBoolean(prop.getProperty("use_rpc"));
            int allowMaxIP = Integer.parseInt(prop.getProperty("max_connections"));
            char[] allowIP = prop.getProperty("allow_ip").toCharArray();

            if (use) {
                RPCWebSocketServer rpcServer = new RPCWebSocketServer(port, id, pw, mEthereum);
                rpcServer.setIPConnections(allowIP, allowMaxIP);
                rpcServer.start();
            }
        } catch (IOException e) {
            System.exit(0);
        }
    }

    private static EthereumListener mListener = new EthereumListenerAdapter() {

        boolean isStartGenerateTx = false;

        @Override
        public void onSyncDone(SyncState state) {
            synced = true;
            logger.debug(ConsoleUtil.colorBRed("SYNC DONE =============================================="));
        }

        long blockCount = 0;

        /**
         *  블록들을 전달받았으면 다른 노드들에게 현재의 RP를 전파해야한다.
         */
        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            logger.debug(ConsoleUtil.colorBBlue("OnBlock : " + block.getShortDescr()));

            Constants constants = Objects.requireNonNull(SystemProperties.getDefault()).getBlockchainConfig().getConfigForBlock(block.getNumber()).getConstants();

            logger.debug(ConsoleUtil.colorYellow("MASTERNODE REWARD : " + ApisUtil.readableApis(mEthereum.getRepository().getBalance(constants.getMASTERNODE_STORAGE()))));

            List<byte[]> generalEarlyRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_EARLY_GENERAL());
            List<byte[]> generalRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_GENERAL());
            List<byte[]> generalLateRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_LATE_GENERAL());

            List<byte[]> majorEarlyRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_EARLY_MAJOR());
            List<byte[]> majorRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_MAJOR());
            List<byte[]> majorLateRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_LATE_MAJOR());

            List<byte[]> privateEarlyRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_EARLY_PRIVATE());
            List<byte[]> privateRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_PRIVATE());
            List<byte[]> privateLateRepo = ((Repository)mEthereum.getRepository()).getMasterNodeList(constants.getMASTERNODE_LATE_PRIVATE());

            logger.debug(ConsoleUtil.colorYellow("REPO EARLY  G:%d\t M:%d\t P:%d", generalEarlyRepo.size(), majorEarlyRepo.size(), privateEarlyRepo.size()));
            logger.debug(ConsoleUtil.colorYellow("REPO NORMA  G:%d\t M:%d\t P:%d", generalRepo.size(), majorRepo.size(), privateRepo.size()));
            logger.debug(ConsoleUtil.colorYellow("REPO LATE   G:%d\t M:%d\t P:%d", generalLateRepo.size(), majorLateRepo.size(), privateLateRepo.size()));

            generalEarlyRepo.addAll(generalRepo);
            generalEarlyRepo.addAll(generalLateRepo);
            majorEarlyRepo.addAll(majorRepo);
            majorEarlyRepo.addAll(majorLateRepo);
            privateEarlyRepo.addAll(privateRepo);
            privateEarlyRepo.addAll(privateLateRepo);
            logger.debug(ConsoleUtil.colorYellow("REPO ALL    G:%d\t M:%d\t P:%d", generalEarlyRepo.size(), majorEarlyRepo.size(), privateEarlyRepo.size()));

            //if(block.getMnReward().compareTo(BigInteger.ZERO) > 0) {
            if(constants.isMasternodeRewardTime(block.getNumber())) {
                logger.debug(ConsoleUtil.colorCyan("BLOCK G:%d M:%d P:%d", block.getMnGeneralList().size(), block.getMnMajorList().size(), block.getMnPrivateList().size()));
            }




            SecureRandom rnd = new SecureRandom();

            if(synced && block.getNumber() > 10) {
                //generateTransactions(rnd.nextInt(77));
            }
        }
    };

    private static int lastNonce = 0;

    private static void generateTransactions(int num) {

        ECKey senderKey = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));

        if(lastNonce == 0) {
            lastNonce = mEthereum.getRepository().getNonce(senderKey.getAddress()).intValue();
        }

        //num = Math.max(100, num);
        for (int i = lastNonce, j = 0; j < num; i++, j++, lastNonce++) {
            {
                StringBuilder temp = new StringBuilder();
                Random rnd = new Random();
                for (int k = 0; k < 20; k++) {
                    int rIndex = rnd.nextInt(3);
                    switch (rIndex) {
                        case 0:
                            // a-z
                            temp.append((char) ((int) (rnd.nextInt(26)) + 97));
                            break;
                        case 1:
                            // A-Z
                            temp.append((char) ((int) (rnd.nextInt(26)) + 65));
                            break;
                        case 2:
                            // 0-9
                            temp.append((rnd.nextInt(10)));
                            break;
                    }
                }

                byte[] receiverAddr = ECKey.fromPrivate(HashUtil.sha3(temp.toString().getBytes())).getAddress();
                receiverAddr = Hex.decode("66a99a95246aa66237514f8aa03e2386351cf432");  //PARIS
                //receiverAddr = Hex.decode("026f7929da07156036b295bf256e12fac8f947d0");  //AMSTERDAM

                byte[] nonce = ByteUtil.intToBytesNoLeadZeroes(i);
                if (nonce.length == 0) {
                    nonce = new byte[]{0};
                }
                SecureRandom seed = new SecureRandom();
                byte[] value = seed.generateSeed(6);
                Transaction txs = new Transaction(nonce,
                        ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L), ByteUtil.longToBytesNoLeadZeroes(500000),
                        receiverAddr, BIUtil.toBI(value).toByteArray(), new byte[0], mEthereum.getChainIdForNextBlock());
                txs.sign(senderKey);
                //logger.info("<== Submitting tx: " + txs);
                mEthereum.submitTransaction(txs);
            }
        }
    }

    private static void BuyMineral(BigInteger aapis) {

        ECKey senderKey = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));

        if(lastNonce == 0) {
            lastNonce = mEthereum.getRepository().getNonce(senderKey.getAddress()).intValue();
        }

        assert(SystemProperties.getDefault() != null);
        byte[] receiverAddr = SystemProperties.getDefault().getBlockchainConfig().getCommonConstants().getBUY_MINERAL();

        byte[] nonce = ByteUtil.intToBytesNoLeadZeroes(lastNonce);
        if (nonce.length == 0) {
            nonce = new byte[]{0};
        }

        Transaction txs = new Transaction(nonce,
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L), ByteUtil.longToBytesNoLeadZeroes(1000000),
                receiverAddr, aapis.toByteArray(), new byte[0], mEthereum.getChainIdForNextBlock());
        txs.sign(senderKey);

        ConsoleUtil.printlnGreen("<== Submitting tx: " + txs);
        mEthereum.submitTransaction(txs);
    }
}
