package org.apis.config.net;

import org.apis.config.blockchain.OsirisPreBalanceConfig;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class PreBalanceConfig extends BaseNetConfig {
    public PreBalanceConfig() {
        add(0, new OsirisPreBalanceConfig());
    }
}
