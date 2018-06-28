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
import org.apis.samples.BasicSample;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Future;


public class Scrapper implements Runnable {

    @Autowired
    protected Ethereum mEthereum;

    @Autowired
    protected SystemProperties config;

    private boolean synced = false;
    private static Timer timerSubmitMinerState;

    private Logger logger;


    public static void main(String args[]) throws IOException, URISyntaxException {
        EthereumFactory.createEthereum();
    }

    private static long lastReadBlock = 1200;

    /*private static TimerTask getSyncMinerState() {
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
    }*/

    private void setupLogging() {
        logger = LoggerFactory.getLogger("Scrapper");
    }

    @PostConstruct
    private void springInit() {
        setupLogging();

        // adding the main EthereumJ callback to be notified on different kind of events
        mEthereum.addListener(mListener);

        logger.info("Listening for apis events...");

        // starting lifecycle tracking method run()
        new Thread(this, "ScrapWorkThread").start();
    }

    @Override
    public void run() {



        while(true) {
            // DB의 최신 블럭 번호를 불러온다

            // 현재 최신 블록 번호를 불러온다

            // 업데이트가 필요하면 DB에 업데이트를 한다.

        }
    }

    private EthereumListener mListener = new EthereumListenerAdapter() {

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
