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
package org.apis.run;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.apis.cli.CLIInterface;
import org.apis.config.SystemProperties;
import org.apis.core.*;
import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;
import org.apis.facade.Blockchain;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumFactory;
import org.apis.json.BlockData;
import org.apis.json.TransactionReceiptData;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.net.server.Channel;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.TimeUtils;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Future;

/**
 * @author Roman Mandeleil
 * @since 14.11.2014
 */
public class Scrapper {

    private static Ethereum mEthereum;

    private static boolean synced = false;
    private static Timer timerSubmitMinerState;


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

            if (actionBlocksLoader) {
                mEthereum.getBlockLoader().loadBlocks();
            }
        }

        timerSubmitMinerState = new Timer();
        timerSubmitMinerState.schedule(getSyncMinerState(), 30L*1000L, 100L);
    }

    private static long lastReadBlock = 1200;

    private static TimerTask getSyncMinerState() {
        return new TimerTask() {
            @Override
            public void run() {
                Blockchain blockchain = mEthereum.getBlockchain();

                Block block = blockchain.getBlockByNumber(lastReadBlock + 1);

                if(block == null || block.getNumber() == blockchain.getBestBlock().getNumber()) {
                    return;
                }

                //List<TransactionReceipt> blockReceipts = new ArrayList<>();
                List<TransactionReceiptData> transactionReceipts = new ArrayList<>();

                for (Transaction transaction : block.getTransactionsList()) {
                    TransactionInfo transactionInfo = mEthereum.getTransactionInfo(transaction.getHash());
                    if (transactionInfo == null) break;

                    //blockReceipts.add(transactionInfo.getReceipt());
                    transactionReceipts.add(new TransactionReceiptData(block, transactionInfo.getReceipt()));
                };

                BlockData blockData = new BlockData(block);

                String blockDataJson = new Gson().toJson(blockData);
                String transactionDataJson = new Gson().toJson(transactionReceipts);


                Future<HttpResponse<String>> future = Unirest.post("http://35.197.153.64:54632/updateBlock.php")
                    .field("block", blockDataJson)
                    .field("txs", transactionDataJson)
                    .asStringAsync(new Callback<String>() {
                        @Override
                        public void completed(HttpResponse<String> response) {
                            System.out.println("RESPONSE:" + response.getBody());
                        }

                        @Override
                        public void failed(UnirestException e) {
                            System.out.println("ERROR:" + e.getMessage());
                        }

                        @Override
                        public void cancelled() {

                        }
                    });

                lastReadBlock += 1;
            }
        };
    }


    private static EthereumListener mListener = new EthereumListenerAdapter() {

        @Override
        public void onSyncDone(SyncState state) {
            synced = true;
            System.out.println("SYNC DONEDONEDONE");
        }

        /**
         *  블록들을 전달받았으면 다른 노드들에게 현재의 RP를 전파해야한다.
         */
        @Override
        public synchronized void onBlock(Block block, List<TransactionReceipt> receipts) {
            System.out.println( block.getNumber() + "th : \t" + block.getShortHash() + "  *******");
        }
    };

}
