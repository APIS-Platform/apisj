package org.apis.config.net;

import org.apis.config.blockchain.OsirisTestConfig;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class TestNetConfig extends BaseNetConfig {
    public TestNetConfig() {
        add(0, new OsirisTestConfig());
        //add(1_150_000, new HomesteadConfig());
    }
}
