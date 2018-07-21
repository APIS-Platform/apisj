package org.apis.gui.manager;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apis.config.SystemProperties;
import org.apis.core.Block;
import org.apis.core.Repository;
import org.apis.core.Transaction;
import org.apis.core.TransactionReceipt;
import org.apis.crypto.ECKey;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumFactory;
import org.apis.gui.controller.*;
import org.apis.gui.model.MainModel;
import org.apis.keystore.*;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.net.server.Channel;
import org.apis.util.ByteUtil;
import org.apis.util.TimeUtils;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AppManager {
    /* ==============================================
     *  KeyStoreManager Field : private
     * ============================================== */
    private Ethereum mEthereum;
    private Transaction tx;
    private ArrayList<KeyStoreData> keyStoreDataList = new ArrayList<KeyStoreData>();
    private ArrayList<KeyStoreDataExp> keyStoreDataExpList = new ArrayList<KeyStoreDataExp>();
    private BigInteger totalBalance = new BigInteger("0");
    private BigInteger totalMineral = new BigInteger("0");

    private boolean isSyncDone = false;

    /* ==============================================
     *  KeyStoreManager Field : public
     * ============================================== */
    public APISWalletFxGUI guiFx = new APISWalletFxGUI();

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
                Repository repository = ((Repository)mEthereum.getRepository()).getSnapshotTo(block.getStateRoot());

                // apis, mineral
                AppManager.getInstance().keystoreFileReadAll();
                BigInteger totalBalance = new BigInteger("0");
                BigInteger totalMineral = new BigInteger("0");
                for(int i=0; i<AppManager.this.keyStoreDataExpList.size(); i++){
                    BigInteger bigInteger = new BigInteger("1000000000000000000");

                    BigInteger balance = AppManager.this.mEthereum.getRepository().getBalance( Hex.decode(AppManager.this.keyStoreDataExpList.get(i).address) );
                    BigInteger mineral = AppManager.this.mEthereum.getRepository().getMineral( Hex.decode(AppManager.this.keyStoreDataExpList.get(i).address), block.getNumber() );
                    AppManager.this.keyStoreDataExpList.get(i).balance = balance.toString();
                    AppManager.this.keyStoreDataExpList.get(i).mineral = mineral.toString();

                    totalBalance = totalBalance.add(balance);
                    totalMineral = totalMineral.add(mineral);

                }

                AppManager.this.totalBalance = totalBalance;
                AppManager.this.totalMineral = totalMineral;

                // TODO : GUI 데이터 변경 - Balance
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if(AppManager.getInstance().guiFx.getMain() != null) AppManager.getInstance().guiFx.getMain().update(AppManager.this.totalBalance.toString(), AppManager.this.totalMineral.toString());
                        if(AppManager.getInstance().guiFx.getWallet() != null) AppManager.getInstance().guiFx.getWallet().update();
                        if(AppManager.getInstance().guiFx.getTransfer() != null) AppManager.getInstance().guiFx.getTransfer().update();
                    }
                });
            }

            // block number
            long myBestBlock = AppManager.this.mEthereum.getBlockchain().getBestBlock().getNumber();
            long worldBestBlock = mEthereum.getSyncStatus().getBlockBestKnown();

            //time
            long timeStemp = block.getTimestamp() * 1000; //s -> ms
            long nowStemp = TimeUtils.getRealTimestamp(); //ms

            // GUI 데이터 변경 - block, time;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    AppManager.getInstance().guiFx.getMain().syncSubMessage(myBestBlock, worldBestBlock);
                    AppManager.getInstance().guiFx.getMain().setBlock(myBestBlock, worldBestBlock);
                    AppManager.getInstance().guiFx.getMain().setTimestemp(timeStemp, nowStemp);
                }
            });
        }

        @Override
        public void onPeerDisconnect(String host, long port) {
            System.out.println("===================== [onPeerDisconnect] =====================");

            // peer number
            long peerSize = AppManager.this.mEthereum.getChannelManager().getActivePeers().size();

            // GUI 데이터 변경 - peer;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    AppManager.getInstance().guiFx.getMain().setPeer(peerSize);
                }
            });
        }

        @Override
        public void onPeerAddedToSyncPool(Channel peer) {
            System.out.println("===================== [onPeerAddedToSyncPool] =====================");

            // peer number
            int peerSize = AppManager.this.mEthereum.getChannelManager().getActivePeers().size();
            // GUI 데이터 변경 - peer;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    AppManager.getInstance().guiFx.getMain().setPeer(peerSize);
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

    public static String comma(String number) {
        double num = Double.parseDouble(number.replaceAll("[^\\d]", ""));
        DecimalFormat df = new DecimalFormat("#,##0");
        return df.format(num);
    }

    public static String addDotWidthIndex(String text){
        boolean isMinus = false;

        // data minus check
        if(text.indexOf("-") >= 0){
            isMinus = true;
            text.replace("-","");
        }

        if (text != null ){
            int size = 19 - text.length();
            for(int i=0; i<size; i++){
                text = "0"+text;
            }
            text = new StringBuffer(text).insert(text.length() - 18, ".").toString();
        }else{
            text = "0.000000000000000000";
        }

        if(isMinus){
            text = "-"+text;
        }
        return text;
    }

    // setting block timestamp
    public static String setBlockTimestamp(long lastBlockTimestamp, long nowBlockTimestamp){
        //Date nowDate = new Date();
        //long nowTimestamp = nowDate.getTime();
        long nowTimestamp = nowBlockTimestamp;
        long diffTimestamp = nowTimestamp - lastBlockTimestamp;
        long diffTime = Math.max(diffTimestamp/1000 - 10, 0); // -10 is block create time
        String text = "";

        if( diffTime >= 86400){
            //day
            text = text + diffTime / 86400 + "day";
        }else {
            // h m s
            long temp = diffTime;
            String h, m, s;
            String print_type = "s";

            // h
            temp = diffTime / 3600;
            diffTime = diffTime - temp * 3600;
            h = temp + "h ";
            if(temp > 0){
                print_type = "h";
            }

            // m
            temp = diffTime / 60;
            diffTime = diffTime - temp * 60;
            m = temp + "m ";
            if(print_type != "h" && temp > 0){
                print_type = "m";
            }

            // s
            temp = diffTime;
            s = temp + "s ";

            // print
            if(print_type == "h"){
                text = h + m + s;
            }else if(print_type == "m"){
                text = m + s;
            }else {
                text = s;
            }
        }
        return text;
    }



    /* ==============================================
     *  public method
     * ============================================== */
    public boolean isSyncDone(){return this.isSyncDone;}
    public long getBestBlock(){ return this.mEthereum.getSyncStatus().getBlockBestKnown(); }
    public long getLastBlock(){ return this.mEthereum.getBlockchain().getBestBlock().getNumber(); }
    public String getAddressWithMask(String mask){
        Repository repository = ((Repository)mEthereum.getRepository()).getSnapshotTo(mEthereum.getBlockchain().getBestBlock().getStateRoot());
        byte[] addr = repository.getAddressByMask(mask);

        if(addr != null){
            return Hex.toHexString(repository.getAddressByMask(mask));
        }else{
            return null;
        }
    }

    public ArrayList<KeyStoreData> keystoreFileReadAll(){
        ArrayList<KeyStoreData> tempKeystoreFileDataList = new ArrayList<KeyStoreData>();

        File defaultFile = KeyStoreManager.getInstance().getDefaultKeystoreDirectory();
        File[] keystoreList = defaultFile.listFiles();
        File tempFile;
        int keystoreFileCnt = 0;
        int aliasCnt = 1;
        //this.keyStoreDataList.clear();
        //this.keyStoreDataExpList.clear();

        for(int i=0; i<keystoreList.length; i++){
            tempFile = keystoreList[i];
            if(tempFile.isFile()){
                try {
                    String allText = AppManager.fileRead(tempFile);
                    KeyStoreData keyStoreData = new Gson().fromJson(allText.toString().toLowerCase(), KeyStoreData.class);
                    KeyStoreDataExp keyStoreDataExp = new Gson().fromJson(allText.toString().toLowerCase(), KeyStoreDataExp.class);
                    if(keyStoreDataExp.alias == null
                            || keyStoreDataExp.alias.equals("")){
                    }

                    tempKeystoreFileDataList.add(keyStoreData);
                    keystoreFileCnt++;

                    boolean isOverlap = false;
                    for(int k=0; k<this.keyStoreDataList.size(); k++){
                        if(this.keyStoreDataList.get(k).id.equals(keyStoreData.id)){
                            isOverlap = true;
                            break;
                        }
                    }
                    if(isOverlap == false) {
                        keyStoreDataExp.balance = "0";
                        keyStoreDataExp.mineral = "0";

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

        // keystore sync
        for(int i=0; i<this.keyStoreDataList.size(); i++){
            int count = 0;
            for(int k=0; k<tempKeystoreFileDataList.size(); k++){
                if(this.keyStoreDataList.get(i).id.equals(tempKeystoreFileDataList.get(k).id)) {

                    this.keyStoreDataList.get(i).address = tempKeystoreFileDataList.get(k).address;
                    this.keyStoreDataList.get(i).alias = tempKeystoreFileDataList.get(k).alias;
                    this.keyStoreDataExpList.get(i).address = tempKeystoreFileDataList.get(k).address;
                    this.keyStoreDataExpList.get(i).alias = tempKeystoreFileDataList.get(k).alias;

                    count++;
                }
            }

            if (count == 0) {
                this.keyStoreDataList.remove(i);
                this.keyStoreDataExpList.remove(i);
                i--;
            }

        }

        if(keystoreFileCnt == 0){
            this.keyStoreDataList.clear();
            this.keyStoreDataExpList.clear();
        }else{
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

    public void ethereumCreateTransactionsWithMask(String addr, String sGasPrice, String sGasLimit, String sMask, String sValue, String passwd){
        String json = "";
        for(int i=0; i<this.getKeystoreList().size(); i++){
            if (addr.equals(this.getKeystoreList().get(i).address)) {
                json = this.getKeystoreExpList().get(i).toString();
                break;
            }
        }

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
        BigInteger nonce = this.mEthereum.getRepository().getNonce(senderKey.getAddress());

        byte[] gasPrice = new BigInteger(sGasPrice).toByteArray();
        byte[] gasLimit = new BigInteger(sGasLimit).toByteArray();
        byte[] value = new BigInteger(sValue).toByteArray();

        Repository repo = ((Repository)mEthereum.getRepository()).getSnapshotTo(this.mEthereum.getBlockchain().getBestBlock().getStateRoot());


        byte[] reAddress = repo.getAddressByMask(sMask);
        if(reAddress == null){
            System.err.println("============ 마스크 없음 ============");
            return;
        }

        this.tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                gasPrice,
                gasLimit,
                reAddress,
                sMask,  //address mask
                value,
                new byte[0], // data - smart contract data
                this.mEthereum.getChainIdForNextBlock());

        this.tx.sign(senderKey);
    }

    public void ethereumCreateTransactions(String addr, String sGasPrice, String sGasLimit, String sToAddress, String sValue, String passwd){

        String json = "";
        for(int i=0; i<this.getKeystoreList().size(); i++){
            if (addr.equals(this.getKeystoreList().get(i).address)) {
                json = this.getKeystoreExpList().get(i).toString();
                break;
            }
        }

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

        BigInteger nonce = this.mEthereum.getRepository().getNonce(senderKey.getAddress());

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

        this.tx.sign(senderKey);
    }

    public void ethereumSendTransactions(){
        if(this.tx != null){
            this.mEthereum.submitTransaction(this.tx);
            System.err.println("Sending tx2: " + Hex.toHexString(tx.getHash()));
        }else{
        }
    }

    public boolean startMining(String walletId, String password) {
        boolean result = false;

        for(int i=0; i<this.getKeystoreList().size(); i++) {
            if(this.getKeystoreList().get(i).id.equals(walletId)){

                try {
                    byte[] privateKey = KeyStoreUtil.decryptPrivateKey(this.getKeystoreList().get(i).toString(), password);
                    SystemProperties.getDefault().setCoinbasePrivateKey(privateKey);
                    //SystemProperties.getDefault().getCoinbaseKey().getPrivKey();
                    result = true;
                    break;
                } catch (Exception e) {
                }

            }
        }

        walletId = null;
        password = null;
        return result;
    }

    /* ==============================================
     *  AppManager Getter Setter
     * ============================================== */
    public ArrayList<KeyStoreData> getKeystoreList(){ return this.keyStoreDataList; }
    public ArrayList<KeyStoreDataExp> getKeystoreExpList(){ return this.keyStoreDataExpList; }
    public String getTotalBalance(){ return this.totalBalance.toString();}
    public String getTotalMineral(){ return this.totalMineral.toString();}


    /* ==============================================
     *  AppManager Singleton
     * ============================================== */
    public class APISWalletFxGUI{
        private Stage primaryStage;
        private IntroController intro;
        private MainController main;
        private WalletController wallet;
        private TransferController transfer;

        private GridPane mainPopup1, mainPopup2;


        public APISWalletFxGUI(){}

        public void pageMoveIntro(boolean isPrevMain){
            try {
                URL fileUrl = new File("apisj-core/src/main/resources/scene/intro.fxml").toURI().toURL();
                FXMLLoader loader = new FXMLLoader(fileUrl);
                Parent root = loader.load();
                IntroController intro = (IntroController)loader.getController();
                intro.setPrevMain(isPrevMain);
                primaryStage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void pageMoveMain(){
            try {
                AppManager.getInstance().keystoreFileReadAll();

                URL fileUrl = new File("apisj-core/src/main/resources/scene/main.fxml").toURI().toURL();
                FXMLLoader loader = new FXMLLoader(fileUrl);
                Parent root = loader.load();
                //MainController intro = (MainController)loader.getController();
                primaryStage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public Object showMainPopup(String fxmlName, int zIndex){

            try {
                File file = new File("apisj-core/src/main/resources/scene/"+fxmlName);
                FXMLLoader loader = new FXMLLoader(file.toURI().toURL());
                AnchorPane popup = loader.load();
                Object controller = loader.getController();
                popup.setVisible(true);
                if(zIndex == 0){
                    this.mainPopup1.add(popup , 0 ,0 );
                    this.mainPopup1.setVisible(true);
                }else if(zIndex == 1){
                    this.mainPopup2.add(popup , 0 ,0 );
                    this.mainPopup2.setVisible(true);
                }
                return controller;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        public void hideMainPopup(int zIndex){
            if(zIndex == 0){
                this.mainPopup1.getChildren().clear();
                this.mainPopup1.setVisible(false);
            }else if(zIndex == 1){
                this.mainPopup2.getChildren().clear();
                this.mainPopup2.setVisible(false);
            }
        }

        public void setMainPopup1(GridPane popup){ this.mainPopup1 = popup; }
        public void setMainPopup2(GridPane popup){ this.mainPopup2 = popup; }

        public Stage getPrimaryStage() { return primaryStage; }
        public void setPrimaryStage(Stage primaryStage) { this.primaryStage = primaryStage; }

        public IntroController getIntro(){ return this.intro; }
        public void setIntro(IntroController intro){this.intro = intro;}

        public MainController getMain(){ return this.main; }
        public void setMain(MainController main){this.main = main;}

        public WalletController getWallet(){ return this.wallet; }
        public void setWallet(WalletController wallet){this.wallet = wallet;}

        public TransferController getTransfer(){ return this.transfer; }
        public void setTransfer(TransferController transfer){this.transfer = transfer;}
    }
}
