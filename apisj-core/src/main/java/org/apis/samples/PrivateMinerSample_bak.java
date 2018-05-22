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
package org.apis.samples;

import com.typesafe.config.ConfigFactory;
import org.apis.config.SystemProperties;
import org.apis.core.*;
import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;
import org.apis.facade.EthereumFactory;
import org.apis.keystore.KeyStoreUtil;
import org.apis.listener.EthereumListener;
import org.apis.mine.Ethash;
import org.apis.mine.MinerListener;
import org.apis.net.eth.message.StatusMessage;
import org.apis.net.message.Message;
import org.apis.net.p2p.HelloMessage;
import org.apis.net.rlpx.Node;
import org.apis.net.server.Channel;
import org.apis.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Bean;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

/**
 * The sample creates a small private net with two peers: one is the miner, another is a regular peer
 * which is directly connected to the miner peer and starts submitting transactions which are then
 * included to blocks by the miner.
 *
 * Another concept demonstrated by this sample is the ability to run two independently configured
 * EthereumJ peers in a single JVM. For this two Spring ApplicationContext's are created which
 * are mostly differed by the configuration supplied
 *
 * Created by Anton Nashatyrev on 05.02.2016.
 */
public class PrivateMinerSample_bak {

    /**
     * Spring configuration class for the Miner peer
     */
    private static class MinerConfig {

        private final String config =
                // no need for discovery in that small network
                "peer.discovery.enabled = true \n" +
                        "peer.listen.port = 30335 \n" +
                        "peer.privateKey = 6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec \n" +
                        "peer.networkId = 10001 \n" +
                        // actively connecting to the miner
                        "peer.active = [" +
                        //"    { url = 'enode://26ba1aadaf59d7607ad7f437146927d79e80312f026cfa635c6b2ccf2c5d3521f5812ca2beb3b295b14f97110e6448c1c7ff68f14c5328d43a3c62b44143e9b1@localhost:30335' }" +
                        "    { url = 'enode://b3054aae3eafbfb3ba00111d930e048f3ce38b44291e00c0d8f3e2c2880abadfeb83e816368aea82f2df381236a101d3c29ddedde1191ae479b55ff4fe933da4@45.76.214.57:44069' }" +
                        "] \n" +
                        /*"peer.listen.port = 30335 \n" +
                        // need to have different nodeId's for the peers
                        "peer.privateKey = 6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec \n" +
                        // our private net ID
                        "peer.networkId = 555 \n" +*/
                        // we have no peers to sync with
                        "sync.enabled = true \n" +
                        // genesis with a lower initial difficulty and some predefined known funded accounts
                        //"genesis = sample-genesis.json \n" +
                        "genesis = apis-test.json \n" +
                        // two peers need to have separate database dirs
                        "database.dir = sampleDB-3 \n" +
                        // when more than 1 miner exist on the network extraData helps to identify the block creator
                        "mine.extraDataHex = abcccccccccccccccccc \n" +
                        "mine.cpuMineThreads = 4 \n" +
                        "cache.flush.blocks = 1";

        @Bean
        public MinerNode node() {
            return new MinerNode();
        }

        /**
         * Instead of supplying properties via config file for the peer
         * we are substituting the corresponding bean which returns required
         * config for this instance.
         */
        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(config.replaceAll("'", "\"")));
            return props;
        }
    }

    /**
     * Miner bean, which just start a miner upon creation and prints miner events
     */
    static class MinerNode extends BasicSample implements MinerListener{
        public MinerNode() {
            // peers need different loggers
            super("sampleMiner");
        }

        // overriding run() method since we don't need to wait for any discovery,
        // networking or sync events
        @Override
        public void run() {
            if (config.isMineFullDataset()) {
                logger.info("Generating Full Dataset (may take up to 10 min if not cached)...");
                // calling this just for indication of the dataset generation
                // basically this is not required
                Ethash ethash = Ethash.getForBlock(config, ethereum.getBlockchain().getBestBlock().getNumber());
                //ethash.getFullDataset();
                logger.info("Full dataset generated (loaded).");
            }
            ethereum.getBlockMiner().addListener(this);
            ethereum.getBlockMiner().startMining();
        }

        @Override
        public void miningStarted() {
            logger.info("Miner started");
        }

        @Override
        public void miningStopped() {
            logger.info("Miner stopped");
        }

        @Override
        public void blockMiningStarted(Block block) {
            logger.info("Start mining block: " + block.getShortDescr());
        }

        @Override
        public void blockMined(Block block) {
            logger.info("Block mined! : \n" + block);
            //ethereum.submitRewardPoint(new RewardPoint(block.getNumber(), config.getMinerCoinbase(), config.getBlockchainConfig().getConfigForBlock(block.getNumber()).calcRewardPoint(config.getMinerCoinbase(), ethereum.getSnapshotTo(block.getStateRoot()).getBalance(config.getMinerCoinbase()), block.getHeader())));
        }

        @Override
        public void blockMiningCanceled(Block block) {
            logger.info("Cancel mining block: " + block.getShortDescr());
        }
    }

    /**
     * Spring configuration class for the Regular peer
     */
    private static class RegularConfig {

        
        private final String config =
                // no discovery: we are connecting directly to the miner peer
                "peer.discovery.enabled = true \n" +
                        "peer.listen.port = 44069 \n" +
                        "peer.privateKey = 3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c \n" +
                        "peer.networkId = 10001 \n" +
                        // actively connecting to the miner
                        //"peer.active = [" +
                        //"    { url = 'enode://26ba1aadaf59d7607ad7f437146927d79e80312f026cfa635c6b2ccf2c5d3521f5812ca2beb3b295b14f97110e6448c1c7ff68f14c5328d43a3c62b44143e9b1@localhost:30335' }" +
                        //"    { url = 'enode://6316e29db407e52f077c7f44b273112cc44f6bf62f0880e0a60d24f13e77b32028939570ef4d7e77e64a67b5202085961b9a9a862463f5fada6b245d6c70bd89@149.28.32.239:44069' }" +
                        //"    { url = 'enode://0dcb1dc28941e67a5f460712743f2a936e879aa168027de99df081ab29fb2e657bd5642698f5195a82e69b8103dd4759c412c7dab9d24f05e7d0e08126c190f6@45.76.214.57:44069' }" +
                        //"] \n" +
                        "sync.enabled = true \n" +
                        // all peers in the same network need to use the same genesis block
                        //"genesis = sample-genesis.json \n" +
                        "genesis = apis-test.json \n" +
                        // two peers need to have separate database dirs
                        "database.dir = sampleDB-4 \n" +
                        "mine.extraDataHex = abcdefabcedf \n" +
                        "mine.coinbase = 0000000000000000000000000000000000000000\n";

        @Bean
        public RegularNode node() {
            return new RegularNode();
        }

        /**
         * Instead of supplying properties via config file for the peer
         * we are substituting the corresponding bean which returns required
         * config for this instance.
         */
        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(config.replaceAll("'", "\"")));
            return props;
        }


    }

    /**
     * Spring configuration class for the Regular peer
     */
    private static class RegularConfig2 {


        private final String config =
                // no discovery: we are connecting directly to the miner peer
                "peer.discovery.enabled = false \n" +
                        "peer.listen.port = 30337 \n" +
                        "peer.privateKey = 3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c \n" +
                        "peer.networkId = 555 \n" +
                        // actively connecting to the miner
                        "peer.active = [" +
                        "    { url = 'enode://26ba1aadaf59d7607ad7f437146927d79e80312f026cfa635c6b2ccf2c5d3521f5812ca2beb3b295b14f97110e6448c1c7ff68f14c5328d43a3c62b44143e9b1@localhost:30335' }" +
                        "] \n" +
                        "sync.enabled = true \n" +
                        // all peers in the same network need to use the same genesis block
                        //"genesis = sample-genesis.json \n" +
                        "genesis = apis-test.json \n" +
                        // two peers need to have separate database dirs
                        "database.dir = sampleDB-5 \n";

        @Bean
        public RegularNode2 node() {
            return new RegularNode2();
        }

        /**
         * Instead of supplying properties via config file for the peer
         * we are substituting the corresponding bean which returns required
         * config for this instance.
         */
        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(config.replaceAll("'", "\"")));
            return props;
        }
    }

    /**
     * The second node in the network which connects to the miner
     * waits for the sync and starts submitting transactions.
     * Those transactions should be included into mined blocks and the peer
     * should receive those blocks back
     */
    static class RegularNode extends BasicSample {
        public RegularNode() {
            // peers need different loggers
            super("ApisNode2");
        }


        @Override
        public void onSyncDone() {
            new Thread(() -> {
                try {
                    generateTransactions();
                } catch (Exception e) {
                    logger.error("Error generating tx: ", e);
                }
            }).start();
        }






        /**
         * Generate one simple value transfer transaction each 7 seconds.
         * Thus blocks will include one, several and none transactions
         */
        private void generateTransactions() throws Exception{
            logger.info("Start generating transactions...");

            // the sender which some coins from the genesis
            ECKey senderKey = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));
            //byte[] receiverAddr = Hex.decode("5db10750e8caff27f906b41c71b3471057dd2004");

            logger.info(Hex.encode(senderKey.getAddress()).toString());
            String aa = Hex.encode(senderKey.getAddress()).toString();

            for (int i = ethereum.getRepository().getNonce(senderKey.getAddress()).intValue(), j = 0; j < 20000; i++, j++) {
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

                    /*
                     * For generate private key & public key sample project
                     */
                    byte[] privateKey = SecureRandom.getInstanceStrong().generateSeed(32);
                    String address = ECKey.fromPrivate(privateKey).toString();

                    KeyStoreUtil.getEncryptKeyStore(privateKey, "12a3adf@@");



                    byte[] receiverAddr = ECKey.fromPrivate(HashUtil.sha3(temp.toString().getBytes())).getAddress();

                    byte[] nonce = ByteUtil.intToBytesNoLeadZeroes(i);
                    if(nonce.length == 0) {
                        nonce = new byte[]{0};
                    }
                    Transaction txs = new Transaction(nonce,
                            ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L), ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                            receiverAddr, new byte[]{77}, new byte[0], ethereum.getChainIdForNextBlock());
                    txs.sign(senderKey);
                    //logger.info("<== Submitting tx: " + txs);
                    ethereum.submitTransaction(txs);
                }
                Thread.sleep(9000);
            }
        }

    }


    static class RegularNode2 extends BasicSample {
        public RegularNode2() {
            // peers need different loggers
            super("ZZZZZZZZ2");
        }


    }

    /**
     *  Creating two EthereumJ instances with different config classes
     */
    public static void main(String[] args) throws Exception {
        if (Runtime.getRuntime().maxMemory() < (1250L << 20)) {
            MinerNode.sLogger.error("Not enough JVM heap (" + (Runtime.getRuntime().maxMemory() >> 20) + "Mb) to generate DAG for mining (DAG requires min 1G). For this sample it is recommended to set -Xmx2G JVM option");
            return;
        }

        BasicSample.sLogger.info("Starting APIS miner instance!");
        //EthereumFactory.createEthereum(MinerConfig.class);

        BasicSample.sLogger.info("Starting APIS regular instance!");
        EthereumFactory.createEthereum(RegularConfig.class);

        /*BasicSample.sLogger.info("Starting APIS2 regular instance!");
        EthereumFactory.createEthereum(RegularConfig.class);*/
    }
}