package org.apis.gui.manager;

import com.google.gson.Gson;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.apis.config.SystemProperties;
import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.core.TransactionReceipt;
import org.apis.crypto.ECKey;
import org.apis.db.RepositoryImpl;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumFactory;
import org.apis.gui.view.APISWalletGUI;
import org.apis.keystore.*;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.net.eth.handler.Eth;
import org.apis.net.eth.message.StatusMessage;
import org.apis.net.server.Channel;
import org.apis.net.swarm.Key;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.TimeUtils;
import org.spongycastle.crypto.ec.ECNewPublicKeyTransform;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppManager {
    /* ==============================================
     *  KeyStoreManager Field : private
     * ============================================== */
    private Ethereum mEthereum;
    private APISWalletGUI gui;
    private Transaction tx;
    private ArrayList<KeyStoreData> keyStoreDataList = new ArrayList<KeyStoreData>();
    private ArrayList<KeyStoreDataExp> keyStoreDataExpList = new ArrayList<KeyStoreDataExp>();
    private BigInteger totalBalance = new BigInteger("0");
    private BigInteger totalMineral = new BigInteger("0");

    private boolean isSyncDone = false;

    private EthereumListener mListener = new EthereumListenerAdapter() {

        boolean isStartGenerateTx = false;

        @Override
        public void onSyncDone(SyncState state) {
            System.out.println("===================== [onSyncDone] =====================");
            isSyncDone = true;
        }

        /**
         *  블록들을 전달받았으면 다른 노드들에게 현재의 RP를 전파해야한다.
         */
        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            System.out.println("===================== [onBlock] =====================");

            if(isSyncDone){

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        // apis, mineral
                        AppManager.getInstance().keystoreFileReadAll();
                        BigInteger totalBalance = new BigInteger("0");
                        BigInteger totalMineral = new BigInteger("0");
                        for(int i=0; i<AppManager.this.keyStoreDataExpList.size(); i++){
                            BigInteger bigInteger = new BigInteger("1000000000000000000");

                            BigInteger balance = AppManager.this.mEthereum.getRepository().getBalance( Hex.decode(AppManager.this.keyStoreDataExpList.get(i).address) );
                            BigInteger mineral = mEthereum.getRepository().getMineral( Hex.decode(AppManager.this.keyStoreDataExpList.get(i).address), block.getNumber() );
                            AppManager.this.keyStoreDataExpList.get(i).balance = balance.toString();
                            AppManager.this.keyStoreDataExpList.get(i).mineral = mineral.toString();


                            totalBalance = totalBalance.add(balance);
                            totalMineral = totalMineral.add(mineral);
                        }

                        AppManager.this.totalBalance = totalBalance;
                        AppManager.this.totalMineral = totalMineral;

                        AppManager.this.gui.getWebEngine().executeScript("loadWalletList();");
                        AppManager.this.gui.getWebEngine().executeScript("setFooterTotalBalance('"+AppManager.this.totalBalance.toString()+"');");
                        AppManager.this.gui.getWebEngine().executeScript("setTotalBalance('"+AppManager.this.totalBalance.toString()+"');");
                        AppManager.this.gui.getWebEngine().executeScript("setTotalMineral('"+AppManager.this.totalMineral.toString()+"');");
                    }
                });
            }

            // block number
            long myBestBlock = AppManager.this.mEthereum.getBlockchain().getBestBlock().getNumber();
            long worldBestBlock = mEthereum.getSyncStatus().getBlockBestKnown();

            //time
            long timeStemp = block.getTimestamp() * 1000; //s -> ms
            long nowStemp = TimeUtils.getRealTimestamp(); //ms

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    AppManager.this.gui.getWebEngine().executeScript("setFooterBlockNumber('"+myBestBlock+"', '"+worldBestBlock+"')");
                    AppManager.this.gui.getWebEngine().executeScript("setFooterBlockTimestamp('"+timeStemp+"', '"+nowStemp+"')");
                }
            });
        }

        @Override
        public void onPeerDisconnect(String host, long port) {
            System.out.println("===================== [onPeerDisconnect] =====================");

            // peer number
            int peerSize = AppManager.this.mEthereum.getChannelManager().getActivePeers().size();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    AppManager.this.gui.getWebEngine().executeScript("setPeerNumber('"+peerSize+"')");
                }
            });
        }

        @Override
        public void onPeerAddedToSyncPool(Channel peer) {
            System.out.println("===================== [onPeerAddedToSyncPool] =====================");

            // peer number
            int peerSize = AppManager.this.mEthereum.getChannelManager().getActivePeers().size();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    AppManager.this.gui.getWebEngine().executeScript("setPeerNumber('"+peerSize+"')");
                }
            });
        }
    };

    /* ==============================================
     *  AppManager Singleton
     * ============================================== */
    private AppManager () {}
    private static class Singleton {
        private static final AppManager instance = new AppManager();
    }
    public static AppManager getInstance () {
        return Singleton.instance;
    }


    /* ==============================================
     *  public static method
     * ============================================== */
    public static String fileRead(File selectFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(selectFile));

        StringBuffer allText = new StringBuffer();
        String sCurrentLine;
        allText = new StringBuffer();

        while ((sCurrentLine = br.readLine()) != null) {
            sCurrentLine = sCurrentLine.replaceAll(" ", "");
            allText.append(sCurrentLine.trim());
        }
        return allText.toString();
    }


    /* ==============================================
     *  public method
     * ============================================== */
    public ArrayList<KeyStoreData> keystoreFileReadAll(){
        File defaultFile = KeyStoreManager.getInstance().getDefaultKeystoreDirectory();
        File[] keystoreList = defaultFile.listFiles();
        File tempFile;

        //this.keyStoreDataList.clear();
        //this.keyStoreDataExpList.clear();

        for(int i=0; i<keystoreList.length; i++){
            tempFile = keystoreList[i];
            if(tempFile.isFile()){
                try {
                    String allText = AppManager.fileRead(tempFile);
                    KeyStoreData keyStoreData = new Gson().fromJson(allText.toString().toLowerCase(), KeyStoreData.class);
                    KeyStoreDataExp keyStoreDataExp = new Gson().fromJson(allText.toString().toLowerCase(), KeyStoreDataExp.class);

                    boolean isOverlap = false;
                    for(int k=0; k<this.keyStoreDataList.size(); k++){
                        if(this.keyStoreDataList.get(k).id.equals(keyStoreData.id)){
                            isOverlap = true;
                            break;
                        }
                    }
                    if(isOverlap == false) {
                        keyStoreDataExp.balance = "0.000000000000000000";
                        keyStoreDataExp.mineral = "0.000000000000000000";

                        this.keyStoreDataList.add(keyStoreData);
                        this.keyStoreDataExpList.add(keyStoreDataExp);
                    }

                }catch (com.google.gson.JsonSyntaxException e) {
                    System.out.println("keystore 형식이 아닙니다 (FileName : "+tempFile.getName()+")");
                }catch (IOException e){
                    System.out.println("file read failed (FileName : "+tempFile.getName()+")");
                }
            }
        }
        return this.keyStoreDataList;
    }
    public void start(){

        final SystemProperties config = SystemProperties.getDefault();
        // Coinbase를 생성하기 위해 선택하도록 해야한다.
        // keystore 폴더가 존재하는지, 파일들이 존재하는지 확인한다.
        String keystoreDir = config.keystoreDir();

        final boolean actionBlocksLoader = !config.blocksLoader().equals("");

        if (actionBlocksLoader) {
            config.setSyncEnabled(false);
            config.setDiscoveryEnabled(false);
        }else{
        }

        mEthereum = EthereumFactory.createEthereum();
        mEthereum.addListener(mListener);

        if (actionBlocksLoader) {
            mEthereum.getBlockLoader().loadBlocks();
        }else{
        }


    }//start

    public void ethereumCreateTransactions(String addr, String sGasPrice, String sGasLimit, String sToAddress, String sValue, String passwd){

        String json = "";
        for(int i=0; i<this.getKeystoreList().size(); i++){
            if (addr.equals(this.getKeystoreList().get(i).address)) {
                json = this.getKeystoreExpList().get(i).toString();
                break;
            }
        }

        //ECKey senderKey = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));
        ECKey senderKey = null;
        try {
            String decryptPrivateKey = Hex.toHexString(KeyStoreUtil.decryptPrivateKey(json, passwd));
            senderKey = ECKey.fromPrivate(Hex.decode(decryptPrivateKey));
            passwd = null;
        } catch (KeystoreVersionException e) {
            System.out.println("KeystoreVersionException : ");
            e.printStackTrace();
        } catch (NotSupportKdfException e) {
            System.out.println("NotSupportKdfException : ");
            e.printStackTrace();
        } catch (NotSupportCipherException e) {
            System.out.println("NotSupportCipherException : ");
            e.printStackTrace();
        } catch (InvalidPasswordException e) {
            System.out.println("InvalidPasswordException : ");
            e.printStackTrace();
        }
        System.out.println("senderKey.getAddress() : "+Hex.toHexString(senderKey.getAddress()));
        BigInteger nonce = this.mEthereum.getRepository().getNonce(senderKey.getAddress());

        //BigInteger nonce = this.mEthereum.getRepository().getNonce(Hex.decode(addr));
        System.out.println("nonce : "+nonce.toString());

        byte[] gasPrice = new BigInteger(sGasPrice).toByteArray();
        byte[] gasLimit = new BigInteger(sGasLimit).toByteArray();
        byte[] toAddress = Hex.decode(sToAddress);
        byte[] value = new BigInteger(sValue).toByteArray();

        this.tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                gasPrice,
                gasLimit,
                toAddress,
                value,
                new byte[0], // data - smart contract data
                this.mEthereum.getChainIdForNextBlock());

        //서명
        //ECKey senderKey = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));

        this.tx.sign(senderKey);
    }

    public void ethereumSendTransactions(){
        if(this.tx != null){
            this.mEthereum.submitTransaction(this.tx);
            System.err.println("Sending tx2: " + Hex.toHexString(tx.getHash()));
        }else{
        }
    }

    /* ==============================================
     *  AppManager Setter
     * ============================================== */
    public void setApisWalletGUI(APISWalletGUI gui){this.gui = gui;}

    /* ==============================================
     *  AppManager Getter
     * ============================================== */
    public ArrayList<KeyStoreData> getKeystoreList(){ return this.keyStoreDataList; }
    public ArrayList<KeyStoreDataExp> getKeystoreExpList(){ return this.keyStoreDataExpList; }
    public String getTotalBalance(){ return this.totalBalance.toString();}
    public String getTotalMineral(){ return this.totalMineral.toString();}

}
