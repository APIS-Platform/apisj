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
import org.apis.contract.ContractLoader;
import org.apis.core.*;
import org.apis.crypto.ECKey;
import org.apis.db.sql.AccountRecord;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.DBSyncManager;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumFactory;
import org.apis.facade.EthereumImpl;
import org.apis.gui.controller.*;
import org.apis.keystore.*;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.net.server.Channel;
import org.apis.solidity.compiler.CompilationResult;
import org.apis.solidity.compiler.SolidityCompiler;
import org.apis.util.ByteUtil;
import org.apis.util.TimeUtils;
import org.apis.vm.program.ProgramResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AppManager {
    /* ==============================================
     *  KeyStoreManager Field : private
     * ============================================== */
    private Ethereum mEthereum;
    private ArrayList<KeyStoreData> keyStoreDataList = new ArrayList<KeyStoreData>();
    private ArrayList<KeyStoreDataExp> keyStoreDataExpList = new ArrayList<KeyStoreDataExp>();
    private BigInteger totalBalance = new BigInteger("0");
    private BigInteger totalMineral = new BigInteger("0");
    private BigInteger totalRewared = new BigInteger("0");
    private String miningWalletId = "";

    private boolean isSyncDone = false;
    private String miningAddress;

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


        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            System.out.println(String.format("===================== [onBlock %d] =====================", block.getNumber()));

            if(isSyncDone){

                Repository repository = ((Repository)mEthereum.getRepository()).getSnapshotTo(block.getStateRoot());

                // apis, mineral
                AppManager.getInstance().keystoreFileReadAll();
                BigInteger totalBalance = new BigInteger("0");
                BigInteger totalMineral = new BigInteger("0");
                BigInteger totalRewared = new BigInteger("0");
                for(int i=0; i<AppManager.this.keyStoreDataExpList.size(); i++){
                    BigInteger bigInteger = new BigInteger("1000000000000000000");
                    String address = AppManager.this.keyStoreDataExpList.get(i).address;

                    BigInteger balance = AppManager.this.mEthereum.getRepository().getBalance( Hex.decode(address) );
                    BigInteger mineral = AppManager.this.mEthereum.getRepository().getMineral( Hex.decode(address), block.getNumber() );
                    BigInteger rewared = mEthereum.getRepository().getTotalReward( Hex.decode(address) );
                    AppManager.this.keyStoreDataExpList.get(i).balance = balance.toString();
                    AppManager.this.keyStoreDataExpList.get(i).mineral = mineral.toString();

                    totalBalance = totalBalance.add(balance);
                    totalMineral = totalMineral.add(mineral);
                    totalRewared = totalRewared.add(rewared);

                }

                AppManager.this.totalBalance = totalBalance;
                AppManager.this.totalMineral = totalMineral;
                AppManager.this.totalRewared = totalRewared;

                // TODO : GUI 데이터 변경 - Balance
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if(AppManager.getInstance().guiFx.getMain() != null) AppManager.getInstance().guiFx.getMain().update(AppManager.this.totalBalance.toString(), AppManager.this.totalMineral.toString());
                        if(AppManager.getInstance().guiFx.getWallet() != null) AppManager.getInstance().guiFx.getWallet().update(AppManager.this.totalRewared.toString());
                        if(AppManager.getInstance().guiFx.getTransfer() != null) AppManager.getInstance().guiFx.getTransfer().update();
                        if(AppManager.getInstance().guiFx.getSmartContract() != null) AppManager.getInstance().guiFx.getSmartContract().update();
                        if(AppManager.getInstance().guiFx.getTransactionNative() != null) AppManager.getInstance().guiFx.getTransactionNative().update();
                    }
                });


                // DB에 저장
                KeyStoreDataExp keyStoreDataExp = null;
                for(int i=0; i<AppManager.this.keyStoreDataExpList.size(); i++){
                    keyStoreDataExp = AppManager.this.keyStoreDataExpList.get(i);
                    DBManager.getInstance().updateAccount(Hex.decode(keyStoreDataExp.address), keyStoreDataExp.alias, new BigInteger(keyStoreDataExp.balance), keyStoreDataExp.mask, new BigInteger("0"));
                }

                List<AccountRecord> dbWalletList = DBManager.getInstance().selectAccounts();
                System.out.println("dbWalletList.size() : "+dbWalletList.size());

                // DB Sync Start
                DBSyncManager.getInstance(mEthereum).syncThreadStart();

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
                    if(AppManager.getInstance().guiFx.getMain() != null) AppManager.getInstance().guiFx.getMain().syncSubMessage(myBestBlock, worldBestBlock);
                    if(AppManager.getInstance().guiFx.getMain() != null) AppManager.getInstance().guiFx.getMain().setBlock(myBestBlock, worldBestBlock);
                    if(AppManager.getInstance().guiFx.getMain() != null) AppManager.getInstance().guiFx.getMain().setTimestemp(timeStemp, nowStemp);
                }
            });
        }

        @Override
        public void onPeerDisconnect(String host, long port) {
            System.out.println("===================== [onPeerDisconnect] =====================");

            // peer number
            long peerSize = AppManager.this.mEthereum.getChannelManager().getActivePeers().size();

            // GUI 데이터 변경 - peer
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
    public static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
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
        File[] keystoreFileList = defaultFile.listFiles();
        File tempFile;
        int aliasCnt = 1;

        // keystore 폴더의 모든 내용을 읽어온다.
        for(int i=0; i<keystoreFileList.length; i++){
            tempFile = keystoreFileList[i];
            if(tempFile.isFile()){

                try {
                    // keystore 형식의 파일의 경우 그 내용을 읽어온다.
                    String allText = AppManager.fileRead(tempFile);

                    // Json형식의 데이터를 keystoreData 객체로 생성한다.
                    KeyStoreData keyStoreData = new Gson().fromJson(allText.toString(), KeyStoreData.class);
                    KeyStoreDataExp keyStoreDataExp = new Gson().fromJson(allText.toString(), KeyStoreDataExp.class);

                    // 지갑이름이 없을 경우 임의로 지갑이름을 부여한다.
                    if(keyStoreData.alias == null || keyStoreData.alias.equals("")){
                        keyStoreData.alias = "WalletAlias" + (aliasCnt++);
                        KeyStoreManager.getInstance().updateKeystoreFile(tempFile.getName(), keyStoreData.toString());
                    }
                    keyStoreDataExp.alias = keyStoreData.alias;

                    // 생성한 keystoreData객체는 비교를 위해 temp 리스트에 담아둔다.
                    tempKeystoreFileDataList.add(keyStoreData);

                    // 기존 가지고 있던 keystoreData 리스트와 새로 가져온 keystoreData를 비교하여
                    // 기존 리스트를 업데이트한다.
                    boolean isOverlap = false;
                    for(int k=0; k<this.keyStoreDataList.size(); k++){
                        if(this.keyStoreDataList.get(k).id.equals(keyStoreData.id)){
                            isOverlap = true;
                            this.keyStoreDataList.get(k).address = keyStoreData.address;
                            this.keyStoreDataList.get(k).alias = keyStoreData.alias;
                            this.keyStoreDataExpList.get(k).address = keyStoreData.address;
                            this.keyStoreDataExpList.get(k).alias = keyStoreData.alias;
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
                    this.keyStoreDataExpList.get(i).address = tempKeystoreFileDataList.get(k).address;
                    count++;
                }
            }

            if (count == 0) {
                this.keyStoreDataList.remove(i);
                this.keyStoreDataExpList.remove(i);
                i--;
            }

        }

        //sort : alias asc
        keyStoreDataList.sort(new Comparator<KeyStoreData>() {
            @Override
            public int compare(KeyStoreData item1, KeyStoreData item2) {
                return item1.alias.toLowerCase().compareTo(item2.alias.toLowerCase());
            }
        });
        keyStoreDataExpList.sort(new Comparator<KeyStoreDataExp>() {
            @Override
            public int compare(KeyStoreDataExp item1, KeyStoreDataExp item2) {
                return item1.alias.toLowerCase().compareTo(item2.alias.toLowerCase());
            }
        });

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


        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        //time
                        long timeStemp = mEthereum.getBlockchain().getBestBlock().getTimestamp() * 1000; //s -> ms
                        long nowStemp = TimeUtils.getRealTimestamp(); //ms
                        if(AppManager.getInstance().guiFx.getMain() != null) AppManager.getInstance().guiFx.getMain().setTimestemp(timeStemp, nowStemp);
                    }
                    catch (Error | Exception e) {
                    }
                }
            });
        }, 0, 1, TimeUnit.SECONDS);

    }//start

    private ECKey getSenderKey(String json, String passwd ){
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

        return senderKey;
    }

    public Transaction ethereumGenerateTransactionsWithMask(String addr, String sValue, String sGasPrice, String sGasLimit, String sMask, String passwd, byte[] data){
        String json = "";
        for(int i=0; i<this.getKeystoreList().size(); i++){
            if (addr.equals(this.getKeystoreList().get(i).address)) {
                json = this.getKeystoreExpList().get(i).toString();
                break;
            }
        }

        ECKey senderKey = getSenderKey(json, passwd);
        passwd = null;

        BigInteger nonce = this.mEthereum.getRepository().getNonce(senderKey.getAddress());

        byte[] gasPrice = new BigInteger(sGasPrice).toByteArray();
        byte[] gasLimit = new BigInteger(sGasLimit).toByteArray();
        byte[] value = new BigInteger(sValue).toByteArray();

        Repository repo = ((Repository)mEthereum.getRepository()).getSnapshotTo(this.mEthereum.getBlockchain().getBestBlock().getStateRoot());


        byte[] reAddress = repo.getAddressByMask(sMask);
        if(reAddress == null){
            System.err.println("============ 마스크 없음 ============");
            return null;
        }

        Transaction tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                gasPrice,
                gasLimit,
                reAddress,
                sMask,  //address mask
                value,
                data, // data - smart contract data
                this.mEthereum.getChainIdForNextBlock());

        tx.sign(senderKey);

        return tx;
    }

    public Transaction ethereumGenerateTransaction(String addr, String sValue, String sGasPrice, String sGasLimit, byte[] toAddress, String passwd, byte[] data){
        sValue = (sValue != null &&  sValue.length() > 0) ? sValue : "0";
        sGasPrice = (sGasPrice != null &&  sGasPrice.length() > 0) ? sGasPrice : "0";
        sGasLimit = (sGasLimit != null &&  sGasLimit.length() > 0) ? sGasLimit : "0";

        String json = "";
        for(int i=0; i<this.getKeystoreList().size(); i++){
            if (addr.equals(this.getKeystoreList().get(i).address)) {
                json = this.getKeystoreExpList().get(i).toString();
                break;
            }
        }

        ECKey senderKey = getSenderKey(json, passwd);
        passwd = null;

        BigInteger nonce = this.mEthereum.getRepository().getNonce(senderKey.getAddress());
        System.out.println("nonce : "+nonce.toString());
        byte[] gasPrice = new BigInteger(sGasPrice).toByteArray();
        byte[] gasLimit = new BigInteger(sGasLimit).toByteArray();
        byte[] value = new BigInteger(sValue).toByteArray();

        Transaction tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce), // none
                gasPrice,   //price
                gasLimit,   //gasLimit
                toAddress,  //reciveAddress
                value,  //value
                data, // data - smart contract data
                this.mEthereum.getChainIdForNextBlock());

        tx.sign(senderKey);

        return tx;
    }

    public void ethereumSendTransactions(Transaction tx){
        if(tx != null){
            this.mEthereum.submitTransaction(tx);
            System.err.println("Sending tx2: " + Hex.toHexString(tx.getHash()));
        }else{
        }
    }

    public long getPreGasUsed(byte[] sender, byte[] contractAddress, byte[] data){
        return ((ContractLoader.ContractRunEstimate) ContractLoader.preRunContract((EthereumImpl)this.mEthereum, sender, contractAddress, data)).getGasUsed();
    }
    public long getPreGasUsed(String abi, byte[] sender, byte[] contractAddress, String functionName, Object ... args) {
        return ((ContractLoader.ContractRunEstimate) ContractLoader.preRunContract((EthereumImpl)this.mEthereum, abi, sender, contractAddress, functionName, args)).getGasUsed();
    }

    public byte[] getGasUsed(String txHash){
        TransactionInfo txInfo = ((BlockchainImpl) this.mEthereum.getBlockchain()).getTransactionInfo(Hex.decode(txHash));
        TransactionReceipt txReceipt = txInfo.getReceipt();
        byte[] gasUsed = txReceipt.getGasUsed();
        if(gasUsed != null){
            return gasUsed;
        }
        return new byte[0];
    }

    // 스마트 컨트렉트 컴파일
    public String ethereumSmartContractStartToCompile(String stringContract){
        if(stringContract == null || stringContract.length() == 0){
            return "null";
        }

        String message = null;
        try {
            SolidityCompiler.Result result = SolidityCompiler.getInstance().compileSrc(stringContract.getBytes(), true, true,
                    SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);

            if (result.isFailed()) {
                message = result.errors;
            }else{
                message = result.output;
                CompilationResult res = CompilationResult.parse(message);
                if(res.getContracts().isEmpty()){
                    message = "Compilation filed, no contracts returned";
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return message;
    }

    public Object[] callConstantFunction(String contractAddress, CallTransaction.Function function){
        ProgramResult r = this.mEthereum.callConstantFunction(contractAddress, function);
        Object[] ret = function.decodeResult(r.getHReturn());
        return ret;
    }

    public Object[] callConstantFunction(String contractAddress, CallTransaction.Function function, Object ... args){
        System.out.println("args : "+args.length);
        for(int i=0; i<args.length ; i++){
            System.out.println(" args : "+args[i]);
        }
        ProgramResult r = this.mEthereum.callConstantFunction(contractAddress, function, args);
        Object[] ret = function.decodeResult(r.getHReturn());
        return ret;
    }

    public boolean startMining(String walletId, String password) {
        boolean result = false;
        this.miningAddress = null;
        for(int i=0; i<this.getKeystoreList().size(); i++) {
            if(this.getKeystoreList().get(i).id.equals(walletId)){

                try {
                    byte[] privateKey = KeyStoreUtil.decryptPrivateKey(this.getKeystoreList().get(i).toString(), password);
                    SystemProperties.getDefault().setCoinbasePrivateKey(privateKey);
                    //SystemProperties.getDefault().getCoinbaseKey().getPrivKey();
                    result = true;

                    this.miningAddress = this.getKeystoreList().get(i).address;
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
    public void setMiningWalletId(String miningWalletId){this.miningWalletId = miningWalletId;}
    public String getMiningWalletId(){return this.miningWalletId;}

    /* ==============================================
     *  AppManager Singleton
     * ============================================== */
    public class APISWalletFxGUI{
        private Stage primaryStage;
        private IntroController intro;
        private MainController main;
        private WalletController wallet;
        private TransferController transfer;
        private SmartContractController smartContract;
        private TransactionController transaction;
        private TransactionNativeController transactionNative;
        private AddressMaskingController addressMasking;


        private GridPane mainPopup0, mainPopup1, mainPopup2;


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
                if(zIndex == -1) {
                    this.mainPopup0.getChildren().clear();
                    this.mainPopup0.add(popup , 0 ,0 );
                    this.mainPopup0.setVisible(true);
                } else if(zIndex == 0){
                    this.mainPopup1.getChildren().clear();
                    this.mainPopup1.add(popup , 0 ,0 );
                    this.mainPopup1.setVisible(true);
                }else if(zIndex == 1){
                    this.mainPopup2.getChildren().clear();
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
            if(zIndex == -1) {
                this.mainPopup0.getChildren().clear();
                this.mainPopup0.setVisible(false);
            } else if(zIndex == 0){
                this.mainPopup1.getChildren().clear();
                this.mainPopup1.setVisible(false);
            }else if(zIndex == 1){
                this.mainPopup2.getChildren().clear();
                this.mainPopup2.setVisible(false);
            }
        }

        public void setMainPopup0(GridPane popup){ this.mainPopup0 = popup; }
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

        public SmartContractController getSmartContract() { return smartContract; }
        public void setSmartContract(SmartContractController smartContract) { this.smartContract = smartContract; }

        public TransactionController getTransaction() { return transaction; }
        public void setTransaction(TransactionController transaction) { this.transaction = transaction; }

        public TransactionNativeController getTransactionNative() { return transactionNative; }
        public void setTransactionNative(TransactionNativeController transactionNative) { this.transactionNative = transactionNative; }

        public AddressMaskingController getAddressMasking() { return addressMasking; }
        public void setAddressMasking(AddressMaskingController addressMasking) { this.addressMasking = addressMasking; }


    }
}
