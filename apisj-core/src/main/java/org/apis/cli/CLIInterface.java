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
package org.apis.cli;

import org.apache.commons.lang3.BooleanUtils;
import org.apis.config.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Mandeleil
 * @since 13.11.2014
 */
@Component
public class CLIInterface {

    private CLIInterface() {
    }

    private static final Logger logger = LoggerFactory.getLogger("general");

    public static void call(String[] args) {
        try {
            Map<String, Object> cliOptions = new HashMap<>();

            for (int i = 0; i < args.length; ++i) {
                String arg = args[i];

                processHelp(arg);

                processMiningStart(arg);

                processMasternodeStart(arg);

                processRpc(arg);

                // process simple option
                if (processConnectOnly(arg, cliOptions))
                    continue;

                // possible additional parameter
                if (i + 1 >= args.length)
                    continue;

                // process options with additional parameter
                if (processDbDirectory(arg, args[i + 1], cliOptions))
                    continue;
                if (processDbReset(arg, args[i + 1], cliOptions))
                    continue;
            }

            if (cliOptions.size() > 0) {
                logger.debug("Overriding config file with CLI options: {}", cliOptions);
            }

            SystemProperties.getDefault().overrideParams(cliOptions);

            processDashBoard();

        } catch (Throwable e) {
            logger.error("Error parsing command line: [{}]", e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // show help
    private static void processHelp(String arg) {
        if ("--help".equals(arg)) {
            printHelp();

            System.exit(1);
        }
    }

    private static boolean processConnectOnly(String arg, Map<String, Object> cliOptions) {
        if ("-connectOnly".equals(arg))
            return false;

        cliOptions.put(SystemProperties.PROPERTY_PEER_DISCOVERY_ENABLED, false);

        return true;
    }

    // override the db directory
    private static boolean processDbDirectory(String arg, String db, Map<String, Object> cliOptions) {
        if (!"-db".equals(arg))
            return false;

        logger.info("DB directory set to [{}]", db);

        cliOptions.put(SystemProperties.PROPERTY_DB_DIR, db);

        return true;
    }

    private static void processMiningStart(String arg) throws IOException {
        if(!"-mining".equals(arg))
            return;

        CLIStart cliStart = new CLIStart();
        cliStart.startKeystoreCheck();

        System.exit(1);
    }

    private static void processMasternodeStart(String arg) throws IOException {
        if(!"-masternode".equals(arg))
            return;

        CLIStart cliStart = new CLIStart();
        cliStart.startMasternodeSetting();

        System.exit(1);
    }

    private static void processRpc(String arg) throws IOException {
        if(!"-rpc".equals(arg))
            return;

        CLIStart cliStart = new CLIStart();
        cliStart.startRpcServerCheck();

        System.exit(1);
    }

    private static void processDashBoard() throws IOException {
        CLIStart cliStart = new CLIStart();
        cliStart.startDashBoard();
    }

    // override the connect host:port directory
    /*private static boolean processConnect(String arg, String connectStr, Map<String, Object> cliOptions) throws URISyntaxException {
        if (!arg.startsWith("-connect"))
            return false;

        logger.info("Connect URI set to [{}]", connectStr);
        URI uri = new URI(connectStr);

        if (!"enode".equals(uri.getScheme()))
            throw new RuntimeException("expecting URL in the format enode://PUBKEY@HOST:PORT");

        List<Map<String, String>> peerActiveList = Collections.singletonList(Collections.singletonMap("url", connectStr));

        cliOptions.put(SystemProperties.PROPERTY_PEER_ACTIVE, peerActiveList);

        return true;
    }*/

    // process database reset
    private static boolean processDbReset(String arg, String reset, Map<String, Object> cliOptions) {
        if (!"-reset".equals(arg))
            return false;

        Boolean resetFlag = interpret(reset);

        if (resetFlag == null) {
            throw new Error(String.format("Can't interpret DB reset arguments: %s %s", arg, reset));
        }

        logger.info("Resetting db set to [{}]", resetFlag);
        cliOptions.put(SystemProperties.PROPERTY_DB_RESET, resetFlag.toString());

        return true;
    }

    private static Boolean interpret(String arg) {
        return BooleanUtils.toBooleanObject(arg);
    }

    private static void printHelp() {

        System.out.println("--help                -- this help message ");
        System.out.println("-mining               -- to setup the path for the database directory ");
        System.out.println("-masternode           -- to setup the path for the database directory ");
        System.out.println("-rpc                  -- to setup the path for the database directory ");
        System.out.println("-reset <yes/no>       -- reset yes/no the all database ");
        System.out.println("-db <db>              -- to setup the path for the database directory ");
        System.out.println("-connectOnly <enode://pubKey@host:port>  -- like 'connect', but will not attempt to connect to other peers  ");
        System.out.println();
        System.out.println("e.g: cli -reset no -db db-1");
        System.out.println();

    }
}