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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apis.cli.CLIInterface;
import org.apis.config.SystemProperties;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumFactory;
import org.apis.mine.Ethash;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Roman Mandeleil
 * @since 14.11.2014
 */
public class Start {

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
            new Ethash(config, Long.parseLong(System.getProperty("ethash.blockNumber"))).getFullDataset();
            // DAG file has been created, lets exit
            System.exit(0);
        } else {
            Ethereum ethereum = EthereumFactory.createEthereum();

            if (actionBlocksLoader) {
                ethereum.getBlockLoader().loadBlocks();
            }
        }
    }

}
