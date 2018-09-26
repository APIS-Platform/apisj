package org.apis.gui.manager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apis.config.SystemProperties;
import org.apis.contract.ContractLoader;
import org.apis.core.*;
import org.apis.crypto.ECKey;
import org.apis.db.sql.*;
import org.apis.db.sql.DBManager;
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
import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.apis.util.TimeUtils;
import org.apis.vm.program.ProgramResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AppManager {
    /* ==============================================
     *  KeyStoreManager Field : private
     * ============================================== */
    private Ethereum mEthereum;
    private ArrayList<KeyStoreData> keyStoreDataList = new ArrayList<>();
    private ArrayList<KeyStoreDataExp> keyStoreDataExpList = new ArrayList<>();
    private BigInteger totalBalance = BigInteger.ZERO;
    private BigInteger totalMineral = BigInteger.ZERO;
    private BigInteger totalReward = BigInteger.ZERO;
    private String miningWalletId = "";

    private boolean isSyncDone = false;
    private String miningAddress;

    /* ==============================================
     *  KeyStoreManager Field : public
     * ============================================== */
    public APISWalletFxGUI guiFx = new APISWalletFxGUI();

    private EthereumListener mListener = new EthereumListenerAdapter() {

        @Override
        public void onSyncDone(SyncState state) {
            System.out.println("===================== [onSyncDone] =====================");
            isSyncDone = true;
        }

        long lastOnBLockTime = 0;
        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            System.out.println(String.format("===================== [onBlock %d] =====================", block.getNumber()));

            BigInteger totalBalance = BigInteger.ZERO;
            BigInteger totalMineral = BigInteger.ZERO;
            BigInteger totalReward = BigInteger.ZERO;

            if(isSyncDone){
                // onBlock 콜백이 연달아서 호출될 경우, 10초 이내의 재 호출은 무시하도록 한다.
                if(System.currentTimeMillis() - lastOnBLockTime < 10_000L) {
                    return;
                }
                lastOnBLockTime = System.currentTimeMillis();

                // keystore 폴더의 파일들을 불러들여 변동 사항을 확인하고, balance, mineral, mask, rewards 등의 값을 최신화한다.
                AppManager.getInstance().keystoreFileReadAll();
                for(int i=0; i<AppManager.this.keyStoreDataExpList.size(); i++){
                    KeyStoreDataExp keyExp = AppManager.this.keyStoreDataExpList.get(i);
                    BigInteger balance  = new BigInteger(keyExp.balance);
                    BigInteger mineral  = new BigInteger(keyExp.mineral);
                    BigInteger reward   = new BigInteger(keyExp.rewards);

                    totalBalance = totalBalance.add(balance);
                    totalMineral = totalMineral.add(mineral);
                    totalReward = totalReward.add(reward);

                    // DB에 저장
                    Task<Void> task = new Task<Void>() {
                        @Override
                        protected Void call() {
                            DBManager.getInstance().updateAccount(Hex.decode(keyExp.address), keyExp.alias, balance, keyExp.mask, reward);
                            return null;
                        }
                    };
                    Thread thread = new Thread(task);
                    thread.setDaemon(true);
                    thread.start();
                }

                AppManager.this.totalBalance = totalBalance;
                AppManager.this.totalMineral = totalMineral;
                AppManager.this.totalReward = totalReward;

                // TODO : GUI 데이터 변경 - Balance
                Platform.runLater(() -> {
                    if(AppManager.getInstance().guiFx.getMain() != null) AppManager.getInstance().guiFx.getMain().update(AppManager.this.totalBalance.toString(), AppManager.this.totalMineral.toString());
                    if(AppManager.getInstance().guiFx.getWallet() != null) AppManager.getInstance().guiFx.getWallet().update(AppManager.this.totalReward.toString());
                    if(AppManager.getInstance().guiFx.getTransfer() != null) AppManager.getInstance().guiFx.getTransfer().update();
                    if(AppManager.getInstance().guiFx.getSmartContract() != null) AppManager.getInstance().guiFx.getSmartContract().update();
                    if(AppManager.getInstance().guiFx.getTransactionNative() != null) AppManager.getInstance().guiFx.getTransactionNative().update();
                });

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
                    if(AppManager.getInstance().guiFx.getMain() != null){
                        AppManager.getInstance().guiFx.getMain().setPeer(peerSize);
                    }
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
                    if(AppManager.getInstance().guiFx.getMain() != null){
                        AppManager.getInstance().guiFx.getMain().setPeer(peerSize);
                    }
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
        br.close();
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
    public String getMaskWithAddress(String address){
        if(mEthereum == null || address == null){
            return null;
        }
        Repository repository = ((Repository)mEthereum.getRepository()).getSnapshotTo(mEthereum.getBlockchain().getBestBlock().getStateRoot());
        String mask = repository.getMaskByAddress(Hex.decode(address));

        if(mask != null){
            return mask;
        }else{
            return null;
        }
    }

    public String getAliasWithAddress(String address){
        for(int i=0; i<getKeystoreExpList().size(); i++){
            if(getKeystoreExpList().get(i).address.equals(address)){
                return getKeystoreExpList().get(i).alias;
            }
        }
        return null;
    }

    public ArrayList<KeyStoreData> keystoreFileReadAll(){
        org.apis.keystore.KeyStoreManager keyStoreManager = org.apis.keystore.KeyStoreManager.getInstance();
        List<KeyStoreData> keys = keyStoreManager.loadKeyStoreFiles();

        for(KeyStoreData key : keys) {
            boolean isExist = false;
            for(KeyStoreData listKey : keyStoreDataList) {
                if(key.id.equalsIgnoreCase(listKey.id)) {
                    isExist = true;
                    break;
                }
            }

            if(!isExist) {
                keyStoreDataList.add(key);
                keyStoreDataExpList.add(new KeyStoreDataExp(key));
            }
        }

        // KeyStore 파일이 존재하지 않는 경우, 목록에서 제거
        List<String> removeIds = new ArrayList<>();
        for(KeyStoreData listKey : keyStoreDataList) {
            boolean isExist = false;
            for(KeyStoreData key : keys) {
                if(key.id.equalsIgnoreCase(listKey.id)) {
                    isExist = true;
                    break;
                }
            }

            if(!isExist) {
                removeIds.add(listKey.id);
            }
        }

        for(String id : removeIds) {
            keyStoreDataList.removeIf(key -> key.id.equalsIgnoreCase(id));
            keyStoreDataExpList.removeIf(key -> key.id.equalsIgnoreCase(id));
        }

        // 목록에 있는 데이터들의 값을 갱신한다.
        if(mEthereum != null) {
            for (KeyStoreDataExp keyExp : keyStoreDataExpList) {
                keyExp.mask = getMaskWithAddress(keyExp.address);
                keyExp.balance = mEthereum.getRepository().getBalance(Hex.decode(keyExp.address)).toString();
                keyExp.mineral = mEthereum.getRepository().getMineral(Hex.decode(keyExp.address), mEthereum.getBlockchain().getBestBlock().getNumber()).toString();
                keyExp.rewards = mEthereum.getRepository().getTotalReward(Hex.decode(keyExp.address)).toString();
            }
        }

        //sort : alias asc
        keyStoreDataList.sort(Comparator.comparing(item -> item.alias.toLowerCase()));
        keyStoreDataExpList.sort(Comparator.comparing(item -> item.alias.toLowerCase()));

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
        } catch (NotSupportKdfException e) {
            System.out.println("NotSupportKdfException : ");
        } catch (NotSupportCipherException e) {
            System.out.println("NotSupportCipherException : ");
        } catch (InvalidPasswordException e) {
            System.out.println("InvalidPasswordException : ");
        }

        return senderKey;
    }

    public Transaction ethereumGenerateTransactionsWithMask(String addr, String sValue, String sGasPrice, String sGasLimit, String sMask, byte[] data, String passwd){
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

    public Transaction ethereumGenerateTransaction(String addr, String sValue, String sGasPrice, String sGasLimit, byte[] toAddress, byte[] data, String passwd){
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
        }else{
        }
    }

    public long getPreGasUsed(String abi, byte[] sender, byte[] contractAddress, BigInteger value, String functionName, Object ... args) {
        if(this.mEthereum != null) {
            ContractLoader.ContractRunEstimate contractRunEstimate = (ContractLoader.ContractRunEstimate) ContractLoader.preRunContract((EthereumImpl) this.mEthereum, abi, sender, contractAddress, value, functionName, args);
            if (contractRunEstimate != null) {
                if(contractRunEstimate.isSuccess()){
                    return contractRunEstimate.getGasUsed();
                }else{
                   return -1;
                }
            } else {
                return -1;
            }
        }else {
            return -1;
        }
    }
    public long getPreGasCreateContract(byte[] sender, String contractSource, String contractName, Object ... args){
        if(this.mEthereum != null) {
            Block callBlock = this.mEthereum.getBlockchain().getBestBlock();
            ContractLoader.ContractRunEstimate contractRunEstimate = (ContractLoader.ContractRunEstimate) ContractLoader.preCreateContract((EthereumImpl) this.mEthereum, callBlock, sender, contractSource, contractName, args);
            if(contractRunEstimate != null) {
                if(contractRunEstimate.isSuccess()){
                    return contractRunEstimate.getGasUsed();
                }else{
                    return -1;
                }
            }else{
                return -1;
            }
        }else{
            return -1;
        }
    }

    public byte[] getGasUsed(String txHash){
        try {
            TransactionInfo txInfo = ((BlockchainImpl) this.mEthereum.getBlockchain()).getTransactionInfo(Hex.decode(txHash));
            TransactionReceipt txReceipt = txInfo.getReceipt();
            byte[] gasUsed = txReceipt.getGasUsed();
            if (gasUsed != null) {
                return gasUsed;
            }
        }catch (NullPointerException ex){
        }
        return new byte[0];
    }

    // 스마트 컨트렉트 컴파일
    public String ethereumSmartContractStartToCompile(String stringContract){
        if(stringContract == null || stringContract.length() == 0){
            return "";
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

    //마스터노드 실행
    public boolean ethereumMasternode(String keyStore, String password, byte[] recipientAddr){
        try {
            byte[] privateKey = KeyStoreUtil.decryptPrivateKey(keyStore, password);
            SystemProperties.getDefault().setMasternodePrivateKey(privateKey);
            SystemProperties.getDefault().setMasternodeRecipient(recipientAddr);

            password = null;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Object[] callConstantFunction(String contractAddress, CallTransaction.Function function){
        ProgramResult r = this.mEthereum.callConstantFunction(contractAddress, function);
        Object[] ret = function.decodeResult(r.getHReturn());
        return ret;
    }

    public Object[] callConstantFunction(String contractAddress, CallTransaction.Function function, Object ... args){
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
                    result = true;

                    this.miningAddress = this.getKeystoreList().get(i).address;

                    // 파일로 저장
                    Properties prop = AppManager.getGeneralProperties();
                    prop.setProperty("mining_address", this.miningAddress);
                    AppManager.saveGeneralProperties();

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
        private TransactionNativeController transactionNative;
        private AddressMaskingController addressMasking;

        public APISWalletFxGUI(){}

        public void pageMoveIntro(boolean isPrevMain){
            try {
                URL fileUrl = getClass().getClassLoader().getResource("scene/intro.fxml");
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

                URL fileUrl = getClass().getClassLoader().getResource("scene/main.fxml");
                FXMLLoader loader = new FXMLLoader(fileUrl);
                Parent root = loader.load();
                //MainController intro = (MainController)loader.getController();
                primaryStage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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

        public TransactionNativeController getTransactionNative() { return transactionNative; }
        public void setTransactionNative(TransactionNativeController transactionNative) { this.transactionNative = transactionNative; }

        public AddressMaskingController getAddressMasking() { return addressMasking; }
        public void setAddressMasking(AddressMaskingController addressMasking) { this.addressMasking = addressMasking; }


    }



    /* ==============================================
     *  Save / Load File Data
     * ============================================== */
    private static Properties prop;
    private static void createProperties(){
        prop = new Properties() {
            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<>(super.keySet()));
            }
        };
    }

    public static String getRPCPropertiesData(String key){ return getRPCProperties().getProperty(key); }
    public static Properties getRPCProperties(){
        if(prop == null) {
            createProperties();
        }

        try {
            InputStream input = new FileInputStream("config/rpc.properties");
            prop.load(input);
            input.close();
        } catch (IOException e) {
            prop.setProperty("port", String.valueOf(new Random().nextInt(10000) + 40000));  // TODO 리스닝 포트는 제외하도록 수정해야함
            prop.setProperty("id", ByteUtil.toHexString(SecureRandom.getSeed(16)));
            prop.setProperty("password", ByteUtil.toHexString(SecureRandom.getSeed(16)));
            prop.setProperty("max_connections", String.valueOf(1));
            prop.setProperty("allow_ip", "127.0.0.1");

            try {
                OutputStream output = new FileOutputStream("config/rpc.properties");
                prop.store(output, null);
                output.close();
            }catch (IOException err){
                err.printStackTrace();
            }
        }
        return prop;
    }

    public static void saveRPCProperties(String key, String value){
        Properties prop = getRPCProperties();
        prop.setProperty(key, value);
        saveRPCProperties();
    }
    public static void saveRPCProperties(){
        try {
            OutputStream output = new FileOutputStream("config/rpc.properties");
            prop.store(output, null);
            output.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    public static String getGeneralPropertiesData(String key){ return getGeneralProperties().getProperty(key); }
    public static Properties getGeneralProperties(){
        if(prop == null) {
            createProperties();
        }

        try {
            InputStream input = new FileInputStream("config/general.properties");
            prop.load(input);
            input.close();
        } catch (IOException e) {
            prop.setProperty("in_system_log", "false");
            prop.setProperty("enable_event_log", "false");
            prop.setProperty("mining_address","");
            prop.setProperty("language","eng");
            prop.setProperty("footer_total_unit","APIS");
            try {
                OutputStream output = new FileOutputStream("config/general.properties");
                prop.store(output, null);
                output.close();
            }catch (IOException err){
                err.printStackTrace();
            }
        }

        return prop;
    }

    public static void saveGeneralProperties(String key, String value){
        Properties prop = getGeneralProperties();
        prop.setProperty(key, value);
        saveGeneralProperties();
    }
    public static void saveGeneralProperties(){
        try {
            OutputStream output = new FileOutputStream("config/general.properties");
            prop.store(output, null);
            output.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String getWindowPropertiesData(String key){ return getWindowProperties().getProperty(key); }
    public static Properties getWindowProperties(){
        if(prop == null) {
            createProperties();
        }

        try {
            InputStream input = new FileInputStream("config/window.properties");
            prop.load(input);
            input.close();
        } catch (IOException e) {
            prop.setProperty("minimize_to_tray", "false");
            try {
                OutputStream output = new FileOutputStream("config/window.properties");
                prop.store(output, null);
                output.close();
            }catch (IOException err){
                err.printStackTrace();
            }
        }

        return prop;
    }
    public static void saveWindowProperties(String key, String value){
        Properties prop = getWindowProperties();
        prop.setProperty(key, value);
        saveWindowProperties();
    }
    public static void saveWindowProperties(){
        try {
            OutputStream output = new FileOutputStream("config/window.properties");
            prop.store(output, null);
            output.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
