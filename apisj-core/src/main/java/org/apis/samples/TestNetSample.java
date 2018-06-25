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
import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;
import org.apis.facade.EthereumFactory;
import org.springframework.context.annotation.Bean;

import static org.apis.crypto.HashUtil.sha3;

/**
 * This class just extends the BasicSample with the config which connect the peer to the test network
 * This class can be used as a base for free transactions testing
 * (everyone may use that 'cow' sender which has pretty enough fake coins)
 *
 * Created by Anton Nashatyrev on 10.02.2016.
 */
public class TestNetSample extends BasicSample {
    /**
     * Use that sender key to sign transactions
     */
    protected final byte[] senderPrivateKey = HashUtil.sha3("cow".getBytes());

    // sender address is derived from the private key
    protected final byte[] senderAddress = ECKey.fromPrivate(senderPrivateKey).getAddress();


    protected abstract static class TestNetConfig {
        /*private final String config =
                // Ropsten revive network configuration
                "peer.discovery.enabled = true \n" +
                "peer.listen.port = 30303 \n" +
                "peer.networkId = 3 \n" +
                // a number of public peers for this network (not all of then may be functioning)
                "peer.active = [" +
                "    {url = 'enode://6ce05930c72abc632c58e2e4324f7c7ea478cec0ed4fa2528982cf34483094e9cbc9216e7aa349691242576d552a2a56aaeae426c5303ded677ce455ba1acd9d@13.84.180.240:30303'}," +
                "    {url = 'enode://20c9ad97c081d63397d7b685a412227a40e23c8bdc6688c6f37e97cfbc22d2b4d1db1510d8f61e6a8866ad7f0e17c02b14182d37ea7c3c8b9c2683aeb6b733a1@52.169.14.227:30303'}" +
                "] \n" +
                "sync.enabled = true \n" +
                // special genesis for this test network
                "genesis = ropsten.json \n" +
                "blockchain.config.name = 'ropsten' \n" +
                "database.dir = testnetSampleDb \n" +
                "cache.flush.memory = 0";*/
        private final String config =
                // no discovery: we are connecting directly to the miner peer
                "peer.discovery.enabled = true \n" +
                        //"peer.listen.port = 44069 \n" +
                        "peer.privateKey = 3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c \n" +
                        //"peer.networkId = 10001 \n" +
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

        public abstract TestNetSample sampleBean();

        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(config.replaceAll("'", "\"")));
            return props;
        }
    }

    @Override
    public void onSyncDone() throws Exception {
        super.onSyncDone();
    }

    public static void main(String[] args) throws Exception {
        sLogger.info("Starting EthereumJ!");

        class SampleConfig extends TestNetConfig {
            @Bean
            public TestNetSample sampleBean() {
                return new TestNetSample();
            }
        }

        // Based on Config class the BasicSample would be created by Spring
        // and its springInit() method would be called as an entry point
        EthereumFactory.createEthereum(SampleConfig.class);
    }
}
