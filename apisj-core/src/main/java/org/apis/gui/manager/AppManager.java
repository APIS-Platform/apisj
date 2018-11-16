package org.apis.gui.manager;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import org.apis.config.Constants;
import org.apis.config.SystemProperties;
import org.apis.contract.ContractLoader;
import org.apis.core.*;
import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;
import org.apis.db.sql.*;
import org.apis.db.sql.DBManager;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumFactory;
import org.apis.facade.EthereumImpl;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.*;
import org.apis.gui.controller.addressmasking.AddressMaskingController;
import org.apis.gui.controller.smartcontrect.SmartContractController;
import org.apis.gui.controller.transaction.TransactionNativeController;
import org.apis.gui.controller.transfer.TransferController;
import org.apis.gui.controller.wallet.WalletController;
import org.apis.gui.model.TokenModel;
import org.apis.keystore.*;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.net.server.Channel;
import org.apis.solidity.compiler.CompilationResult;
import org.apis.solidity.compiler.SolidityCompiler;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.KnowledgeKeyUtil;
import org.apis.util.TimeUtils;
import org.apis.vm.LogInfo;
import org.apis.vm.program.InternalTransaction;
import org.apis.vm.program.ProgramResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AppManager {
    /* ==============================================
     *  KeyStoreManager Field : private
     * ============================================== */
    private Ethereum mEthereum;
    private ArrayList<KeyStoreData> keyStoreDataList = new ArrayList<>();
    private ArrayList<KeyStoreDataExp> keyStoreDataExpList = new ArrayList<>();
    private int peerSize = 0;
    private long myBestBlock = 0;
    private long worldBestBlock = 0;
    private String miningWalletId = "";
    private String masterNodeWalletId = "";

    private boolean isSyncDone = false;
    private String miningAddress;
    private SimpleStringProperty searchToken = new SimpleStringProperty();
    private AudioClip coinSount = new AudioClip(getClass().getClassLoader().getResource("coin.wav").toString());
    private CallTransaction.Contract tokenContract = null;
    private ArrayList<TokenModel> tokens = new ArrayList<>();

    private Map<String, BigInteger> totalValue = new HashMap<>();

    /* ==============================================
     *  KeyStoreManager Field : public
     * ============================================== */
    public Constants constants = SystemProperties.getDefault().getBlockchainConfig().getCommonConstants();
    public APISWalletFxGUI guiFx = new APISWalletFxGUI();
    //public static final String TOKEN_ABI = ContractLoader.readABI(ContractLoader.CONTRACT_ERC20);

    private EthereumListener mListener = new EthereumListenerAdapter() {

        @Override
        public void onSyncDone(SyncState state) {
            System.out.println("===================== [onSyncDone] =====================");
            isSyncDone = true;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    if(AppManager.getInstance().guiFx.getMain() != null){
                        AppManager.getInstance().guiFx.getMain().exitSyncPopup();
                        AppManager.getInstance().guiFx.getMain().succesSync();

                        // keystore 폴더의 파일들을 불러들여 변동 사항을 확인하고, balance, mineral, mask, rewards 등의 값을 최신화한다.
                        BigInteger totalBalance = BigInteger.ZERO;
                        BigInteger totalMineral = BigInteger.ZERO;
                        BigInteger totalReward = BigInteger.ZERO;
                        AppManager.getInstance().keystoreFileReadAll();
                        for(int i=0; i<AppManager.this.keyStoreDataExpList.size(); i++){
                            KeyStoreDataExp keyExp = AppManager.this.keyStoreDataExpList.get(i);
                            BigInteger balance  = keyExp.balance;
                            BigInteger mineral  = keyExp.mineral;
                            BigInteger reward   = keyExp.rewards;

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

                        AppManager.this.setTotalBalance(totalBalance);
                        AppManager.this.setTotalMineral(totalMineral);
                        AppManager.this.setTotalReward(totalReward);

                        if(AppManager.getInstance().guiFx.getWallet() != null) {
                            AppManager.getInstance().guiFx.getWallet().update();
                        }
                    }
                }
            });
        }

        long lastOnBLockTime = 0;
        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            System.out.println(String.format("===================== [onBlock %d] =====================", block.getNumber()));

            // DB Sync Start
            DBSyncManager.getInstance(mEthereum).syncThreadStart();

            // constants
            constants = SystemProperties.getDefault().getBlockchainConfig().getConfigForBlock(block.getNumber()).getConstants();

            if(isSyncDone){

                // keystore 폴더의 파일들을 불러들여 변동 사항을 확인하고, balance, mineral, mask, rewards 등의 값을 최신화한다.
                BigInteger totalBalance = BigInteger.ZERO;
                BigInteger totalMineral = BigInteger.ZERO;
                BigInteger totalReward = BigInteger.ZERO;
                BigInteger totalTokenValue = BigInteger.ZERO;
                AppManager.getInstance().keystoreFileReadAll();
                for(int i=0; i<AppManager.this.keyStoreDataExpList.size(); i++){
                    KeyStoreDataExp keyExp = AppManager.this.keyStoreDataExpList.get(i);
                    BigInteger balance  = keyExp.balance;
                    BigInteger mineral  = keyExp.mineral;
                    BigInteger reward   = keyExp.rewards;

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

                AppManager.this.setTotalBalance(totalBalance);
                AppManager.this.setTotalMineral(totalMineral);
                AppManager.this.setTotalReward(totalReward);

                // 디플리오한 컨트랙트 있는지 체크하여 내부 DB에 저장
                for (Transaction tx : mEthereum.getBlockchain().getBestBlock().getTransactionsList()) {
                    TransactionInfo txInfo = ((BlockchainImpl) mEthereum.getBlockchain()).getTransactionInfo(tx.getHash());
                    DBManager.getInstance().updateContractCreation(txInfo);
                }

                // Reward 받을 시 동전소리 재생
                if(AppManager.this.getTotalReward().toString().equals(totalReward.toString())){
                }else{
                    if("true".equals(getGeneralPropertiesData("reward_sound"))){
                        coinSount.play();
                    }
                }

                for(TokenModel token : AppManager.getInstance().getTokens()){
                    if(!token.getTokenAddress().equals("-1") && !token.getTokenAddress().equals("-2")) {
                        totalTokenValue = BigInteger.ZERO;
                        for (int i = 0; i < AppManager.this.keyStoreDataExpList.size(); i++) {
                            totalTokenValue = totalTokenValue.add(AppManager.this.getTokenValue(token.getTokenAddress(), AppManager.this.keyStoreDataExpList.get(i).address));
                        }
                        AppManager.this.setTotalTokenValue(token.getTokenAddress(), totalTokenValue);
                    }
                }
            }

            // block number
            AppManager.this.myBestBlock = AppManager.this.mEthereum.getBlockchain().getBestBlock().getNumber();
            AppManager.this.worldBestBlock = mEthereum.getSyncStatus().getBlockBestKnown();


            //sync
            if(AppManager.this.isSyncDone){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (AppManager.getInstance().guiFx.getMain() != null) {

                            MainController.MainTab selectedIndex = AppManager.getInstance().guiFx.getMain().getSelectedIndex();
                            AppManager.getInstance().guiFx.getMain().update();

                            switch (selectedIndex) {
                                case WALLET:
                                    if (AppManager.getInstance().guiFx.getWallet() != null) {
                                        AppManager.getInstance().guiFx.getWallet().update();
                                    }
                                    break;
                                case TRANSFER:
                                    if (AppManager.getInstance().guiFx.getTransfer() != null) {
                                        AppManager.getInstance().guiFx.getTransfer().update();
                                    }
                                    break;
                                case SMART_CONTRECT:
                                    if (AppManager.getInstance().guiFx.getSmartContract() != null)
                                        AppManager.getInstance().guiFx.getSmartContract().update();
                                    break;
                                case ADDRESS_MASKING:
                                    if (AppManager.getInstance().guiFx.getAddressMasking() != null) {
                                        AppManager.getInstance().guiFx.getAddressMasking().update();
                                    }
                                    break;
                                case TRANSACTION:
                                    if (AppManager.getInstance().guiFx.getTransactionNative() != null) {
                                        AppManager.getInstance().guiFx.getTransactionNative().update();
                                    }
                                    break;
                            }

                        }
                    }
                });
            }

        }

        @Override
        public void onPeerDisconnect(String host, long port) {
            System.out.println("===================== [onPeerDisconnect] =====================");

            // peer number
            AppManager.this.peerSize = AppManager.this.mEthereum.getChannelManager().getActivePeers().size();

        }

        @Override
        public void onPeerAddedToSyncPool(Channel peer) {
            System.out.println("===================== [onPeerAddedToSyncPool] =====================");

            // peer number
            AppManager.this.peerSize = AppManager.this.mEthereum.getChannelManager().getActivePeers().size();

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
        return allText.toString().replaceAll("Crypto", "crypto");
    }

    public static String comma(String number) {
        double num = Double.parseDouble(number.replaceAll("[^\\d]", ""));
        DecimalFormat df = new DecimalFormat("#,##0");
        return df.format(num);
    }

    public static String commaSpace(String number) {
        return comma(number).replaceAll(","," ");
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
    public long getBlockTimeLong(long block_number) {
        return this.mEthereum.getBlockchain().getBlockByNumber(block_number).getTimestamp();
    }
    public String getBlockTimeToString(long block_number) {
        long blockTime = getBlockTimeLong(block_number)*1000;
        return setBlockTimestamp(blockTime,TimeUtils.getRealTimestamp()) ;
    }
    public BigInteger getTxNonce(String address){
        return ((Repository)mEthereum.getRepository()).getNonce(Hex.decode(address));
    }
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
        if(mEthereum == null || address == null || address.length() != 40){
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

    public long getContractCreateNonce(byte[] addr, byte[] contractAddress){
        long maxNonce = Long.parseLong(mEthereum.getRepository().getNonce(addr).toString());
        for(long nonce = maxNonce; nonce >=0 ; nonce-- ) {
            if(FastByteComparisons.equal(contractAddress, HashUtil.calcNewAddr(addr, ByteUtil.longToBytes(nonce)))){
                return nonce;
            }
        }
        return 0;
    }

    public void initTokens(){
        this.tokens.clear();
        TokenModel apis = new TokenModel();
        apis.setTokenName("APIS");
        apis.setTokenSymbol("APIS");
        apis.setTokenAddress("-1");
        tokens.add(apis);

        TokenModel mineral = new TokenModel();
        mineral.setTokenName("MINERAL");
        mineral.setTokenSymbol("MNR");
        mineral.setTokenAddress("-2");
        tokens.add(mineral);

        List<TokenRecord> list = DBManager.getInstance().selectTokens();
        for(TokenRecord record : list){
            TokenModel model = new TokenModel();
            model.setTokenName(record.getTokenName());
            model.setTokenSymbol(record.getTokenSymbol());
            model.setTokenAddress(ByteUtil.toHexString(record.getTokenAddress()));
            tokens.add(model);
        }
    }

    private CallTransaction.Contract getTokenContract() {
        if(tokenContract == null) {
            tokenContract = new CallTransaction.Contract(ContractLoader.readABI(ContractLoader.CONTRACT_ERC20));
        }
        return tokenContract;
    }

    private CallTransaction.Function getTokenFunction(String functionName) {
        return getTokenContract().getByName(functionName);
    }

    public List<TokenModel> getTokens(){
        return this.tokens;
    }

    public String getTokenName(String tokenAddress){
        if(tokenAddress == null || tokenAddress.length() == 0){
            return "";
        }else if(tokenAddress.equals("-1")){
            return "APIS";
        }else if(tokenAddress.equals("-2")){
            return "MINERAL";
        }
        return (String)AppManager.getInstance().callConstantFunction(tokenAddress, getTokenFunction("name"))[0];
    }

    public String getTokenSymbol(String tokenAddress){
        if(tokenAddress == null || tokenAddress.length() == 0){
            return "";
        }
        return (String)AppManager.getInstance().callConstantFunction(tokenAddress, getTokenFunction("symbol"))[0];
    }

    public BigInteger getTokenTotalSupply(String tokenAddress){
        if(tokenAddress == null || tokenAddress.length() == 0){
            return BigInteger.ZERO;
        }
        return new BigInteger(""+AppManager.getInstance().callConstantFunction(tokenAddress, getTokenFunction("totalSupply"))[0].toString());
    }

    public long getTokenDecimals(String tokenAddress){
        if(tokenAddress == null || tokenAddress.length() == 0){
            return 0;
        }
        return Long.parseLong(AppManager.getInstance().callConstantFunction(tokenAddress, getTokenFunction("decimals"))[0].toString());
    }

    public BigInteger getTokenValue(String tokenAddress, String address){
        if(tokenAddress == null || tokenAddress.length() == 0){
            return BigInteger.ZERO;
        }
        return new BigInteger(""+AppManager.getInstance().callConstantFunction(tokenAddress, getTokenFunction("balanceOf"), Hex.decode(address))[0].toString());
    }

    public BigInteger getTokenTotalValue(String tokenAddress) {
        BigInteger totalValue = new BigInteger("0");
        for(int i=0; i<keyStoreDataExpList.size(); i++){
            BigInteger tokenValue = getTokenValue(tokenAddress, keyStoreDataExpList.get(i).address);
            totalValue = totalValue.add(tokenValue);
        }
        return totalValue;
    }

    public Image getTokenIcon(String tokenAddress) {
        if(tokenAddress == null || tokenAddress.length() == 0){
            return null;
        }

        if(tokenAddress.equals("-1")){
            return ImageManager.apisIcon;
        }else if(tokenAddress.equals("-2")){
            return ImageManager.mineraIcon;
        }else{
            return ImageManager.getIdenticons(tokenAddress);
        }
    }

    public void tokenSendTransfer(String addr, String sValue, String sGasPrice, String sGasLimit, String tokenAddress, byte[] password, byte[] knowledgeKey, Object[] args){
        byte[] toAddress = Hex.decode(tokenAddress);
        byte[] functionCallBytes = getTokenSendTransferData(args);
        Transaction tx = AppManager.getInstance().ethereumGenerateTransaction(addr, sValue, sGasPrice, sGasLimit, toAddress, functionCallBytes,  password, knowledgeKey);
        AppManager.getInstance().ethereumSendTransactions(tx);
    }

    public byte[] getTokenSendTransferData(Object[] args){
        CallTransaction.Function setter = getTokenFunction("transfer");
        byte[] functionCallBytes = setter.encode(args);
        return functionCallBytes;
    }

    public void setSearchToken(String searchToken){
        this.searchToken.set(searchToken);
    }
    public SimpleStringProperty getSearchToken(){
        return this.searchToken;
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
                keyExp.balance = mEthereum.getRepository().getBalance(Hex.decode(keyExp.address));
                keyExp.mineral = mEthereum.getRepository().getMineral(Hex.decode(keyExp.address), mEthereum.getBlockchain().getBestBlock().getNumber());
                keyExp.rewards = mEthereum.getRepository().getTotalReward(Hex.decode(keyExp.address));
                keyExp.isUsedProofkey = isUsedProofKey(Hex.decode(keyExp.address));
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

        // token 불러오기
        initTokens();

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(AppManager.getInstance().guiFx.getMain() != null) AppManager.getInstance().guiFx.getMain().syncSubMessage(myBestBlock, worldBestBlock);

                        //main - time
                        long timeStemp = mEthereum.getBlockchain().getBestBlock().getTimestamp() * 1000; //s -> ms
                        long nowStemp = TimeUtils.getRealTimestamp(); //ms
                        if(AppManager.getInstance().guiFx.getMain() != null) AppManager.getInstance().guiFx.getMain().setTimestemp(timeStemp, nowStemp);

                        //main - peer
                        if(AppManager.getInstance().guiFx.getMain() != null) AppManager.getInstance().guiFx.getMain().setPeer(peerSize);

                        //main - block
                        if(AppManager.getInstance().guiFx.getMain() != null) AppManager.getInstance().guiFx.getMain().setBlock(myBestBlock, worldBestBlock);

                    }
                    catch (Error | Exception e) {
                    }
                }
            });
        }, 0, 1, TimeUnit.SECONDS);


    }//start


    public BigInteger getBalance(String address){
        return mEthereum.getRepository().getBalance(Hex.decode(address));
    }
    public BigInteger getMineral(String address){
        return mEthereum.getRepository().getMineral(Hex.decode(address), mEthereum.getBlockchain().getBestBlock().getNumber());
    }

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

    public long getPreGasUsed(byte[] sender, byte[] contractAddress, byte[] data)  {
        if(this.mEthereum != null) {
            ContractLoader.ContractRunEstimate contractRunEstimate = (ContractLoader.ContractRunEstimate) ContractLoader.preRunContract((EthereumImpl) this.mEthereum, sender, contractAddress, data);
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

    public byte[] getContractCreationCode(String sender, String contractSource, String contractName) {
        return ContractLoader.getContractCreationCode(this.mEthereum, this.mEthereum.getBlockchain().getBestBlock(), Hex.decode(sender), contractSource, contractName);
    }

    public ArrayList<Object[]> getTokenTransfer(String txHash) {
            ArrayList<Object[]> tokenTransferList = new  ArrayList<Object[]>();
            TransactionInfo txInfo = ((BlockchainImpl)this.mEthereum.getBlockchain()).getTransactionInfo(Hex.decode(txHash));
            TransactionReceipt txReceipt = txInfo.getReceipt();

            if (txReceipt == null) {
                return tokenTransferList;
            }
            Transaction tx = txReceipt.getTransaction();
            if (tx == null || tx.getReceiveAddress() == null || !txReceipt.isSuccessful()) {
                return tokenTransferList;
            }

            CallTransaction.Contract contract = new CallTransaction.Contract(ContractLoader.readABI(ContractLoader.CONTRACT_ERC20));
            List<LogInfo> events = txReceipt.getLogInfoList();
            for (LogInfo loginfo : events) {
                try {
                    CallTransaction.Invocation event = contract.parseEvent(loginfo);
                    String eventName = event.function.name;
                    if (eventName.toLowerCase().equals("transfer")) {
                        tokenTransferList.add(event.args);
                    }
                } catch(Exception e) {
                }
            }

        return tokenTransferList;
    }

    public List<LogInfo> getEventData(String txHash) {
        TransactionInfo txInfo = ((BlockchainImpl)this.mEthereum.getBlockchain()).getTransactionInfo(Hex.decode(txHash));
        TransactionReceipt txReceipt = txInfo.getReceipt();

        if(txReceipt == null) { return null; }
        Transaction tx = txReceipt.getTransaction();
        if(tx == null || !txReceipt.isSuccessful()) { return null; }

        List<LogInfo> events = txReceipt.getLogInfoList();
        if(events == null || events.size() == 0) {
            return null;
        }

        return events;
    }

    public List<InternalTransaction> getInternalTransactions(String txHash) {
        TransactionInfo txInfo = ((BlockchainImpl)this.mEthereum.getBlockchain()).getTransactionInfo(Hex.decode(txHash));
        TransactionReceipt txReceipt = txInfo.getReceipt();

        if(txReceipt == null) { return null; }
        Transaction tx = txReceipt.getTransaction();
        if(tx == null || !txReceipt.isSuccessful()) { return null; }

        List<InternalTransaction> internalTransactions = txReceipt.getInternalTransactionList();
        if(internalTransactions == null || internalTransactions.size() == 0) { return null; }

        return internalTransactions;
    }

    public ContractLoader.ContractRunEstimate ethereumPreRunTransaction(Transaction tx){
        return ContractLoader.preRunTransaction(this.mEthereum, tx);
    }
    public Transaction ethereumGenerateTransactionsWithMask(String addr, String sValue, String sGasPrice, String sGasLimit, String sMask, byte[] data, byte[] passwd, byte[] knowledgeKey){
        String json = "";
        for(int i=0; i<this.getKeystoreList().size(); i++){
            if (addr.equals(this.getKeystoreList().get(i).address)) {
                json = this.getKeystoreExpList().get(i).toString();
                break;
            }
        }

        ECKey senderKey = getSenderKey(json, new String(passwd));
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
        if(knowledgeKey != null && knowledgeKey.length > 0){
            tx.authorize(new String(knowledgeKey)); //2차비밀번호가 있을 경우 한번 더 호출
        }
        return tx;
    }

    public Transaction ethereumGenerateTransaction(String addr, String sValue, String sGasPrice, String sGasLimit, byte[] toAddress, byte[] data, byte[] passwd, byte[] knowledgeKey){
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

        ECKey senderKey = getSenderKey(json, new String(passwd));

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
        if(knowledgeKey != null && knowledgeKey.length > 0){
            tx.authorize(new String(knowledgeKey)); //2차비밀번호가 있을 경우 한번 더 호출
        }
        return tx;
    }

    public void ethereumSendTransactions(Transaction tx){
        if(tx != null){
            this.mEthereum.submitTransaction(tx);
        }else{
        }
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
                    AppManager.saveGeneralProperties("mining_address", this.miningAddress);

                    break;
                } catch (Exception e) {
                }

            }
        }

        walletId = null;
        password = null;
        return result;
    }

    public boolean stopMining(){
        this.miningAddress = null;
        this.miningWalletId = null;
        AppManager.saveGeneralProperties("mining_address", this.miningAddress);
        SystemProperties.getDefault().setCoinbasePrivateKey(null);
        return true;
    }

    public byte[] getKnowledgeKey(String knowledgeCode){
        return KnowledgeKeyUtil.getKnowledgeKey(knowledgeCode).getAddress();
    }

    public byte[] getProofKey(byte[] addr){
        Repository data = ((Repository)mEthereum.getRepository());
        return data.getProofKey(addr);
    }

    public boolean isUsedProofKey(byte[] addr){
        return !FastByteComparisons.equal(getProofKey(addr), HashUtil.EMPTY_DATA_HASH);
    }

    public static void settingTextField(TextField textField){
        if(textField.getText().length() == 0){
            textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-background-color", "#ffffff").toString());
        }else{
            textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-background-color", "#f2f2f2").toString());
        }

        textField.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-border-color", "#2b2b2b").toString());
            }
        });

        textField.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(textField.isFocused() == true){
                    textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-border-color", "#2b2b2b").toString());
                }else{
                    textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-border-color", "#d8d8d8").toString());
                }
            }
        });

        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // focus in
                if(newValue){
                    textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-border-color", "#2b2b2b").toString());
                    textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-background-color", "#ffffff").toString());
                }else{
                    textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-border-color", "#d8d8d8").toString());
                    if(textField.getText().length() == 0){
                        textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-background-color", "#ffffff").toString());
                    }else{
                        textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-background-color", "#f2f2f2").toString());
                    }
                }
            }
        });
    }

    /* ==============================================
     *  AppManager Getter Setter
     * ============================================== */
    public ArrayList<KeyStoreData> getKeystoreList(){ return this.keyStoreDataList; }
    public ArrayList<KeyStoreDataExp> getKeystoreExpList(){ return this.keyStoreDataExpList; }
    public void setTotalBalance(BigInteger value){ this.totalValue.put("-1", value); }
    public BigInteger getTotalApis(){ return checkZeroBigInterger(this.totalValue.get("-1"));}
    public void setTotalMineral(BigInteger value){ this.totalValue.put("-2", value); }
    public BigInteger getTotalMineral(){ return checkZeroBigInterger(this.totalValue.get("-2"));}
    public void setTotalReward(BigInteger value){ this.totalValue.put("-3", value); }
    public BigInteger getTotalReward(){ return checkZeroBigInterger(this.totalValue.get("-3"));}
    public void setTotalTokenValue(String tokenAddress, BigInteger value){ this.totalValue.put(tokenAddress, value); }
    public BigInteger getTotalTokenValue(String tokenAddress){ return checkZeroBigInterger(this.totalValue.get(tokenAddress)); }
    private BigInteger checkZeroBigInterger(BigInteger value){
        if(value == null){
            return BigInteger.ZERO;
        }
        return  value;
    }


    public void setMiningWalletId(String miningWalletId){this.miningWalletId = miningWalletId;}
    public String getMiningWalletId(){return this.miningWalletId;}
    public void setMasterNodeWalletId(String masterNodeWalletId){this.masterNodeWalletId = masterNodeWalletId;}
    public String getMasterNodeWalletId(){return this.masterNodeWalletId;}




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
        prop.clear();

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
        prop.setProperty(key, (value != null) ? value : "");
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
        prop.clear();

        try {
            InputStream input = new FileInputStream("config/general.properties");
            prop.load(input);
            input.close();
        } catch (IOException e) {
            prop.setProperty("in_system_log", "false");
            prop.setProperty("enable_event_log", "false");
            prop.setProperty("mining_address","");
            prop.setProperty("masternode_address","");
            prop.setProperty("language","eng");
            prop.setProperty("footer_total_unit","APIS");
            prop.setProperty("reward_sound","false");
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
        prop.setProperty(key, (value != null) ? value : "");
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
        prop.clear();

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
        prop.setProperty(key, (value != null) ? value : "");
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

    public TransactionRecord initTransactionRecord(TransactionRecord record) {
        return record.init(mEthereum);
    }

    public static void copyClipboard(String text){
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
}
