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

import ch.qos.logback.core.spi.LifeCycle;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apis.cli.CLIInterface;
import org.apis.config.BlockchainConfig;
import org.apis.config.SystemProperties;
import org.apis.core.*;
import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumFactory;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.mine.Ethash;
import org.apis.net.eth.message.StatusMessage;
import org.apis.net.message.Message;
import org.apis.net.p2p.HelloMessage;
import org.apis.net.rlpx.Node;
import org.apis.net.server.Channel;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.RewardPointUtil;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.Lifecycle;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Roman Mandeleil
 * @since 14.11.2014
 */
public class Start {

    private static Ethereum mEthereum;

    private static boolean synced = false;


    public static void main(String args[]) throws IOException, URISyntaxException {
        CLIInterface.call(args);

        final SystemProperties config = SystemProperties.getDefault();
        final boolean actionBlocksLoader = !config.blocksLoader().equals("");
        final boolean actionGenerateDag = !StringUtils.isEmpty(System.getProperty("ethash.blockNumber"));

        if (actionBlocksLoader || actionGenerateDag) {
            config.setSyncEnabled(false);
            config.setDiscoveryEnabled(false);
        }


        if (actionGenerateDag) {
            //new Ethash(config, Long.parseLong(System.getProperty("ethash.blockNumber"))).getFullDataset();
            // DAG file has been created, lets exit
            System.exit(0);
        } else {
            mEthereum = EthereumFactory.createEthereum();
            mEthereum.addListener(mListener);

            //mEthereum.getBlockMiner().startMining();      //TODO for test

            if (actionBlocksLoader) {
                mEthereum.getBlockLoader().loadBlocks();
            }
        }
    }

    private static EthereumListener mListener = new EthereumListenerAdapter() {

        @Override
        public void onSyncDone(SyncState state) {
            synced = true;
            System.out.println("SYND DONEDONEDONE");
            System.out.println("SYND DONEDONEDONE");
            System.out.println("SYND DONEDONEDONE");

            /*try {
                generateTransactions();
            } catch (Exception e) {
                e.printStackTrace();
            }*/
        }

        long blockCount = 0;

        /**
         *  블록들을 전달받았으면 다른 노드들에게 현재의 RP를 전파해야한다.
         */
        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            if(!FastByteComparisons.equal(block.getCoinbase(), SystemProperties.getDefault().getMinerCoinbase())) {
                System.out.println("OnBlock : " + block.getNumber());
                System.out.println(block.toString());
            }

            blockCount += 1;

            if(blockCount > 100 && synced) {
                ((SmartLifecycle)mEthereum).stop(() -> {
                    mEthereum = EthereumFactory.createEthereum();
                    mEthereum.addListener(mListener);
                    blockCount = 0;
                });
            }

            if (synced) {
                /*SystemProperties config = SystemProperties.getDefault();

                RewardPoint minerRP = RewardPointUtil.genRewardPoint(block, config.getMinerCoinbase(), mEthereum.getBlockchain().getBlockStore(), (Repository)mEthereum);

                List<RewardPoint> rpList = new ArrayList<>();
                rpList.add(minerRP);

                mEthereum.submitRewardPoints(rpList);*/
            }
        }

        @Override
        public void onPeerAddedToSyncPool(Channel peer) {
            //System.out.println();

            //mEthereum.getBlockMiner().startMining();      //TODO for test
        }
    };

    private static void generateTransactions() throws Exception{
        //logger.info("Start generating transactions...");

        // the sender which some coins from the genesis
        ECKey senderKey = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));
        //byte[] receiverAddr = Hex.decode("5db10750e8caff27f906b41c71b3471057dd2004");


        for (int i = mEthereum.getRepository().getNonce(senderKey.getAddress()).intValue(), j = 0; j < 20000; i++, j++) {
            {
                StringBuffer temp = new StringBuffer();
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

                byte[] nonce = ByteUtil.intToBytesNoLeadZeroes(i);
                if(nonce.length == 0) {
                    nonce = new byte[]{0};
                }
                Transaction txs = new Transaction(nonce,
                        ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L), ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                        receiverAddr, new byte[]{77}, new byte[0], mEthereum.getChainIdForNextBlock());
                txs.sign(senderKey);
                //logger.info("<== Submitting tx: " + txs);
                mEthereum.submitTransaction(txs);
            }
            Random rand = new Random();

            Thread.sleep(rand.nextInt(100)*100L);
        }
    }
}
