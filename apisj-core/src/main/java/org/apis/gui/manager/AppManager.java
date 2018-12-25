package org.apis.gui.manager;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apis.config.Constants;
import org.apis.config.SystemProperties;
import org.apis.contract.ContractLoader;
import org.apis.contract.EstimateTransaction;
import org.apis.contract.EstimateTransactionResult;
import org.apis.core.*;
import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;
import org.apis.db.sql.DBManager;
import org.apis.db.sql.DBSyncManager;
import org.apis.db.sql.TokenRecord;
import org.apis.db.sql.TransactionRecord;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumFactory;
import org.apis.facade.EthereumImpl;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.IntroController;
import org.apis.gui.controller.MainController;
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
import org.apis.rpc.RPCServerManager;
import org.apis.solidity.compiler.CompilationResult;
import org.apis.solidity.compiler.SolidityCompiler;
import org.apis.util.*;
import org.apis.vm.LogInfo;
import org.apis.vm.program.InternalTransaction;
import org.apis.vm.program.ProgramResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AppManager {
    public static final String REGISTER_DOMAIN_URL = "https://goo.gl/forms/oytS76KKssTcosND3";

    /* ==============================================
     *  KeyStoreManager Field : private
     * ============================================== */
    private Ethereum mEthereum;
    private ArrayList<KeyStoreData> keyStoreDataList = new ArrayList<>();
    private ArrayList<KeyStoreDataExp> keyStoreDataExpList = new ArrayList<>();
    private int peerSize = 0;
    private long myBestBlock = 0;
    private long worldBestBlock = 0;
    private String miningWalletAddress = "";

    private boolean isSyncDone = false;
    private String miningAddress, masternodeAddress, recipientAddress, masternodeState;
    private SimpleStringProperty searchToken = new SimpleStringProperty();
    private AudioClip coinSount = new AudioClip(getClass().getClassLoader().getResource("coin.wav").toString());
    private CallTransaction.Contract tokenContract = null;
    private ArrayList<TokenModel> tokens = new ArrayList<>();

    // totalBalance
    private Map<String, BigInteger> totalValue = new HashMap<>();


    /* ==============================================
     *  KeyStoreManager Field : public
     * ============================================== */
    // File Read
    public File openFileReader(){
        File selectFile = null;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(SystemProperties.getDefault().keystoreDir()));
        selectFile = fileChooser.showOpenDialog(AppManager.getInstance().guiFx.getPrimaryStage());

        return selectFile;
    }

    // Directory Read
    public String openDirectoryReader(){
        String result = null;
        File selectFile = null;

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        selectFile = directoryChooser.showDialog(AppManager.getInstance().guiFx.getPrimaryStage());

        if(selectFile != null){
            result = selectFile.getPath();
        }

        return result;
    }


    public Constants constants = SystemProperties.getDefault().getBlockchainConfig().getCommonConstants();
    public APISWalletFxGUI guiFx = new APISWalletFxGUI();
    //public static final String TOKEN_ABI = ContractLoader.readABI(ContractLoader.CONTRACT_ERC20);

    private EthereumListener mListener = new EthereumListenerAdapter() {

        @Override
        public void onSyncDone(SyncState state) {
            System.out.println("===================== [onSyncDone] =====================");
            isSyncDone = true;

            // start rpc server
            AppManager.getInstance().startRPC();

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
                        for (KeyStoreDataExp keyExp : AppManager.this.keyStoreDataExpList) {
                            BigInteger balance = keyExp.balance;
                            BigInteger mineral = keyExp.mineral;
                            BigInteger reward = keyExp.rewards;

                            totalBalance = totalBalance.add(balance);
                            totalMineral = totalMineral.add(mineral);
                            totalReward = totalReward.add(reward);
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

                    BigInteger balance = keyExp.balance;
                    BigInteger mineral = keyExp.mineral;
                    BigInteger reward = keyExp.rewards;

                    totalBalance = totalBalance.add(balance);
                    totalMineral = totalMineral.add(mineral);
                    totalReward = totalReward.add(reward);
                }

                AppManager.this.setTotalBalance(totalBalance);
                AppManager.this.setTotalMineral(totalMineral);

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
                AppManager.this.setTotalReward(totalReward);

                for(TokenModel token : AppManager.getInstance().getTokens()){
                    if(!token.getTokenAddress().equals("-1") && !token.getTokenAddress().equals("-2")) {
                        totalTokenValue = BigInteger.ZERO;
                        for (int i = 0; i < AppManager.this.keyStoreDataExpList.size(); i++) {
                            totalTokenValue = totalTokenValue.add(AppManager.this.getTokenValue(token.getTokenAddress(), AppManager.this.keyStoreDataExpList.get(i).address));
                        }
                        AppManager.this.setTotalTokenValue(token.getTokenAddress(), totalTokenValue);
                    }
                }

                // Check & Change Masternode State
                if(AppManager.this.keyStoreDataExpList != null && AppManager.this.keyStoreDataExpList.size() != 0) {
                    for(int i=0; i<AppManager.this.keyStoreDataExpList.size(); i++){
                        KeyStoreDataExp keyExp = AppManager.this.keyStoreDataExpList.get(i);
                        String address = keyExp.address;

                        if (AppManager.getGeneralPropertiesData("masternode_state").equals(Integer.toString(MnState.EMPTY_MASTERNODE.num))) {
                            if (isMasterNode(address)) {
                                if(address.equals(AppManager.getGeneralPropertiesData("masternode_address"))) {
                                    AppManager.saveGeneralProperties("masternode_state", Integer.toString(MnState.MASTERNODE.num));
                                }
                            }

                        } else if (AppManager.getGeneralPropertiesData("masternode_state").equals(Integer.toString(MnState.REQUEST_MASTERNODE.num))) {
                            if (isMasterNode(address)) {
                                if(address.equals(AppManager.getGeneralPropertiesData("masternode_address"))) {
                                    AppManager.saveGeneralProperties("masternode_state", Integer.toString(MnState.MASTERNODE.num));
                                }
                            }

                        } else if (AppManager.getGeneralPropertiesData("masternode_state").equals(Integer.toString(MnState.MASTERNODE.num))) {
                            if (!isMasterNode(address)) {

                            }

                        } else if (AppManager.getGeneralPropertiesData("masternode_state").equals(Integer.toString(MnState.CANCEL_MASTERNODE.num))) {
                            if (!isMasterNode(address)) {
                                if(address.equals(AppManager.getGeneralPropertiesData("masternode_address"))) {
                                    cancelMasternode();
                                }
                            }
                        }
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

    // setting block timestamp
    public static String setBlockTimestamp(long lastBlockTimestamp, long nowBlockTimestamp){
        //Date nowDate = new Date();
        //long nowTimestamp = nowDate.getTime();
        long nowTimestamp = nowBlockTimestamp;
        long diffTimestamp = nowTimestamp - lastBlockTimestamp;
        long diffTime = Math.max(diffTimestamp/1000 - 0, 0);
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
        if(mEthereum == null || address == null || !AddressUtil.isAddress(address)){
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

    public long getDomainIndex(String domainName){
        String abi = ContractLoader.readABI(ContractLoader.CONTRACT_ADDRESS_MASKING);
        byte[] addressMaskingAddress = AppManager.getInstance().constants.getADDRESS_MASKING_ADDRESS();
        CallTransaction.Contract contract = new CallTransaction.Contract(abi);
        CallTransaction.Function functionDomainCount = contract.getByName("domainCount");
        CallTransaction.Function functionGetDomainInfo = contract.getByName("getDomainInfo"); //[2]:domainName, [5]needApproval, [6]isOpened

        Object[] values = AppManager.getInstance().callConstantFunction(ByteUtil.toHexString(addressMaskingAddress), functionDomainCount);
        long count = Long.parseLong(""+values[0]);

        Object[] args = new Object[1];
        for(int i=0; i<count; i++){
            args[0] = i;
            values = AppManager.getInstance().callConstantFunction(ByteUtil.toHexString(addressMaskingAddress), functionGetDomainInfo, args);
            if((""+values[2]).equals(domainName)){
                return i;
            }
        }
        return -1;
    }

    public String getAliasWithAddress(String address){
        for(int i=0; i<getKeystoreExpList().size(); i++){
            if(getKeystoreExpList().get(i).address.equals(address)){
                return getKeystoreExpList().get(i).alias;
            }
        }
        return "(Not Found)";
    }

    public long getContractCreateNonce(byte[] addr, byte[] contractAddress){
        long maxNonce = Long.parseLong(mEthereum.getRepository().getNonce(addr).toString());
        for(long nonce = maxNonce; nonce >=0 ; nonce-- ) {
            if(FastByteComparisons.equal(contractAddress, HashUtil.calcNewAddr(addr, ByteUtil.longToBytes(nonce)))){
                return nonce;
            }
        }
        return -1;
    }

    public void loadDBTokens(){
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

    public boolean startRPC(){
        // start server
        try {
            RPCServerManager rpcServerManager = RPCServerManager.getInstance(mEthereum);
            rpcServerManager.loadProperties();
            if(rpcServerManager.isAvailable()) {
                rpcServerManager.startServer();
                System.out.println("START RPC SERVER ======== >>");
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public boolean stopRPC(){
        // stop server
        try {
            RPCServerManager rpcServerManager = RPCServerManager.getInstance(mEthereum);
            rpcServerManager.stopServer();
            System.out.println("<< ======== STOP RPC SERVER");
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public void openBrowserRegisterDomain(){
        try {
            Desktop.getDesktop().browse(new URI(REGISTER_DOMAIN_URL));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public boolean isMasterNode(String address){
        return ((Repository)mEthereum.getRepository()).getMnStartBlock(ByteUtil.hexStringToBytes(address)) > 0;
    }

    public boolean isMining(String address) {
        if(SystemProperties.getDefault().getCoinbaseKey() == null) {
            return false;
        }
        return ByteUtil.toHexString(SystemProperties.getDefault().getCoinbaseKey().getAddress()).equals(address);
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
        if(tokenAddress == null){
            return "";
        }else if(tokenAddress.equals("-1")){
            return "APIS";
        }else if(tokenAddress.equals("-2")){
            return "MINERAL";
        }else if(tokenAddress.length() < 40){
            return "";
        }
        return (String)AppManager.getInstance().callConstantFunction(tokenAddress, getTokenFunction("name"))[0];
    }

    public String getTokenNameDB(String tokenAddress){
        if(tokenAddress == null){
            return "";
        }else if(tokenAddress.equals("-1")){
            return "APIS";
        }else if(tokenAddress.equals("-2")){
            return "MINERAL";
        }else if( tokenAddress.length() < 40){
            return "";
        }

        List<TokenRecord> tokenRecordList = DBManager.getInstance().selectTokens();
        for(int i=0; i<tokenRecordList.size(); i++){
            if(tokenRecordList.get(i).getTokenAddress().equals(tokenAddress)){
                return tokenRecordList.get(i).getTokenName();
            }
        }

        return getTokenName(tokenAddress);
    }

    public String getTokenSymbol(String tokenAddress){
        if(tokenAddress == null ){
            return "";
        }
        if(tokenAddress.equals("-1")){
            return "APIS";
        }else if(tokenAddress.equals("-2")){
            return "MNR";
        }else if(tokenAddress.length() < 40){
            return "";
        }
        return (String)AppManager.getInstance().callConstantFunction(tokenAddress, getTokenFunction("symbol"))[0];
    }

    public BigInteger getTokenTotalSupply(String tokenAddress){
        if(tokenAddress == null || tokenAddress.length() < 40){
            return BigInteger.ZERO;
        }
        return new BigInteger(""+AppManager.getInstance().callConstantFunction(tokenAddress, getTokenFunction("totalSupply"))[0].toString());
    }

    public long getTokenDecimals(String tokenAddress){
        if(tokenAddress == null || tokenAddress.length() < 40){
            return 0;
        }
        return Long.parseLong(AppManager.getInstance().callConstantFunction(tokenAddress, getTokenFunction("decimals"))[0].toString());
    }

    public BigInteger getTokenValue(String tokenAddress, String address){
        if(tokenAddress == null || tokenAddress.length() < 40){
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
            return ImageManager.icCrcleNone;
        }
        tokenAddress = tokenAddress.replace("0x", "");
        if(tokenAddress.equals("-1")){
            return ImageManager.apisIcon;
        }else if(tokenAddress.equals("-2")){
            return ImageManager.mineraIcon;
        }else if(AddressUtil.isAddress(tokenAddress)){
            return getTokenImage(tokenAddress);
        }else{
            return ImageManager.icCrcleNone;
        }
    }

    private HashMap<String, Image> tokenIconWithUrl = new HashMap<>();
    private Image getTokenImage(String tokenAddress){
        Image image = null;
        if(tokenAddress != null && tokenAddress.length() > 0){
            String url = AppManager.getInstance().callConstantFunction(tokenAddress, getTokenFunction("iconUrl"))[0].toString();
            if( isValidURL(url) ){
                image = tokenIconWithUrl.get(url);
                if(image == null){
                    image = new Image(url);
                    tokenIconWithUrl.put(url, image);
                }
            }else{
                image = ImageManager.getIdenticons(tokenAddress);
            }
        }
        return image;
    }
    private boolean isValidURL(String urlStr) {
        try {
            URL url = new URL(urlStr);
            return true;
        }
        catch (MalformedURLException e) {
            return false;
        }
    }

    public void tokenSendTransfer(String addr, String sValue, String sGasPrice, String sGasLimit, String tokenAddress, char[] password, char[] knowledgeKey, Object[] args){
        byte[] toAddress = Hex.decode(tokenAddress);
        byte[] functionCallBytes = getTokenSendTransferData(args);
        Transaction tx = AppManager.getInstance().generateTransaction(addr, sValue, sGasPrice, sGasLimit, toAddress, new byte[0], functionCallBytes,  password, knowledgeKey);
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
        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance();
        List<KeyStoreData> keys = keyStoreManager.loadKeyStoreFiles();

        for(KeyStoreData key : keys){
            boolean isExist = false;
            for(int i = 0; i<keyStoreDataList.size(); i++) {
                if(key.address.equalsIgnoreCase(keyStoreDataList.get(i).address)) {
                    isExist = true;

                    // alias update
                    keyStoreDataList.get(i).alias = key.alias;
                    keyStoreDataExpList.get(i).alias = key.alias;

                    // password update
                    keyStoreDataList.get(i).crypto = key.crypto;
                    keyStoreDataExpList.get(i).crypto = key.crypto;
                    break;
                }
            }

            if(!isExist) {
                keyStoreDataList.add(key);
                keyStoreDataExpList.add(new KeyStoreDataExp(key));
            }
        }

        // KeyStore 파일이 존재하지 않는 경우, 목록에서 제거
        List<byte[]> removeAddressList = new ArrayList<>();
        for(KeyStoreData listKey : keyStoreDataList) {
            boolean isExist = false;
            for(KeyStoreData key : keys) {
                if(key.address.equalsIgnoreCase(listKey.address)) {
                    isExist = true;
                    break;
                }
            }

            if(!isExist) {
                removeAddressList.add(Hex.decode(listKey.address));
            }
        }

        for(byte[] address : removeAddressList) {
            keyStoreDataList.removeIf(key -> key.address.equalsIgnoreCase(Hex.toHexString(address)));
            keyStoreDataExpList.removeIf(key -> key.address.equalsIgnoreCase(Hex.toHexString(address)));
        }

        // 목록에 있는 데이터들의 값을 갱신한다.
        if(mEthereum != null) {
            KeyStoreDataExp keyExp = null;
            for(int i=0; i<keyStoreDataExpList.size(); i++){
                keyExp = keyStoreDataExpList.get(i);
                keyExp.mask = getMaskWithAddress(keyExp.address);
                keyExp.balance = mEthereum.getRepository().getBalance(Hex.decode(keyExp.address));
                keyExp.mineral = mEthereum.getRepository().getMineral(Hex.decode(keyExp.address), mEthereum.getBlockchain().getBestBlock().getNumber());
                keyExp.rewards = mEthereum.getRepository().getTotalReward(Hex.decode(keyExp.address));
                keyExp.isUsedProofkey = isUsedProofKey(Hex.decode(keyExp.address));
            }
        }

        //sort : alias asc
        if(keyStoreDataList.size() > 1) {
            try {
                keyStoreDataList.sort(Comparator.comparing(item -> item.alias.toLowerCase()));
                keyStoreDataExpList.sort(Comparator.comparing(item -> item.alias.toLowerCase()));
            }catch (Exception e){
                // sort error
            }
        }

        return this.keyStoreDataList;
    }

    public boolean isFrozen(String address) {
        String abi = ContractLoader.readABI(ContractLoader.CONTRACT_CODE_FREEZER);
        CallTransaction.Contract contract = new CallTransaction.Contract(abi);
        CallTransaction.Function functionIsFrozen = contract.getByName("isFrozen");
        byte[] codeFreezer = AppManager.getInstance().constants.getSMART_CONTRACT_CODE_FREEZER();

        // 데이터 불러오기
        Object[] result = AppManager.getInstance().callConstantFunction(ByteUtil.toHexString(codeFreezer), contract.getByName(functionIsFrozen.name), address);
        Boolean isFrozen = Boolean.parseBoolean(result[0].toString());

        return isFrozen;
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
        loadDBTokens();

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
            BigInteger value = BigInteger.ZERO;

            EstimateTransaction estimator = EstimateTransaction.getInstance((EthereumImpl)mEthereum);
            EstimateTransactionResult estimateResult = estimator.estimate(sender, contractAddress, value, data);

            if(estimateResult == null) {
                return -1;
            }

            if(estimateResult.isSuccess()) {
                return estimateResult.getGasUsed();
            } else {
                return -1;
            }
        }else {
            return -1;
        }

    }

    public long getPreGasUsedWithNonce(byte[] sender, byte[] contractAddress, byte[] data)  {
        if(this.mEthereum != null) {
            BigInteger value = BigInteger.ZERO;
            long nonce = this.mEthereum.getRepository().getNonce(sender).longValue();
            System.out.println("nonce : "+nonce);
            EstimateTransaction estimator = EstimateTransaction.getInstance((EthereumImpl)mEthereum);
            EstimateTransactionResult estimateResult = estimator.estimate(sender, contractAddress, nonce, value, data);

            if(estimateResult == null) {
                return -1;
            }

            if(estimateResult.isSuccess()) {
                return estimateResult.getGasUsed();
            } else {
                return -1;
            }
        }else {
            return -1;
        }

    }

    public long getPreGasUsed(String abi, byte[] sender, byte[] contractAddress, BigInteger value, String functionName, Object ... args) {
        if(this.mEthereum != null) {
            EstimateTransaction estimator = EstimateTransaction.getInstance((EthereumImpl)mEthereum);
            EstimateTransactionResult estimateResult = estimator.estimate(abi, sender, contractAddress, value, functionName, args);

            if(estimateResult == null) {
                return -1;
            }

            if(estimateResult.isSuccess()) {
                return estimateResult.getGasUsed();
            } else {
                return -1;
            }
        }else {
            return -1;
        }
    }
    public long getPreGasCreateContract(byte[] sender, long nonce, String contractSource, String contractName, Object ... args){
        if(this.mEthereum != null) {
            EstimateTransaction estimator = EstimateTransaction.getInstance((EthereumImpl)mEthereum);
            EstimateTransactionResult estimateResult = estimator.estimateDeploy(sender, nonce, contractSource, contractName, args);

            if(estimateResult == null) {
                return -1;
            }

            if(estimateResult.isSuccess()) {
                return estimateResult.getGasUsed();
            } else {
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

    public byte[] getContractCreationCode(String sender, long nonce, String contractSource, String contractName) {
        EstimateTransaction estimator = EstimateTransaction.getInstance((EthereumImpl)mEthereum);
        EstimateTransactionResult estimateResult = estimator.estimateDeploy(ByteUtil.hexStringToBytes(sender), nonce, contractSource, contractName);

        return estimateResult.getDeployBytes();
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

    public EstimateTransactionResult estimateTransaction(Transaction tx){
        return EstimateTransaction.getInstance((EthereumImpl)mEthereum).estimate(tx);
    }
    public Transaction ethereumGenerateTransactionsWithMask(String addr, String sValue, String sGasPrice, String sGasLimit, String sMask, byte[] data, char[] passwd, char[] knowledgeKey){
        String json = "";
        for(int i=0; i<this.getKeystoreList().size(); i++){
            if (addr.equals(this.getKeystoreList().get(i).address)) {
                json = this.getKeystoreExpList().get(i).toString();
                break;
            }
        }

        ECKey senderKey = getSenderKey(json, String.valueOf(passwd));
        BigInteger nonce = this.mEthereum.getPendingState().getNonce(senderKey.getAddress());

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
            tx.authorize(String.valueOf(knowledgeKey)); //2차비밀번호가 있을 경우 한번 더 호출
        }
        return tx;
    }

    public Transaction ethereumGenerateTransaction(BigInteger nonce, String addr, String sValue, String sGasPrice, String sGasLimit, byte[] toAddress, byte[] toMask, byte[] data, char[] passwd, char[] knowledgeKey){
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

        ECKey senderKey = getSenderKey(json, String.valueOf(passwd));

        byte[] gasPrice = new BigInteger(sGasPrice).toByteArray();
        byte[] gasLimit = new BigInteger(sGasLimit).toByteArray();
        byte[] value = new BigInteger(sValue).toByteArray();

        Transaction tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce), // none
                gasPrice,   //price
                gasLimit,   //gasLimit
                toAddress,  //reciveAddress
                toMask, //mask
                value,  //value
                data, // data - smart contract data
                this.mEthereum.getChainIdForNextBlock());

        // For raw transaction byte code
        //System.out.println("@@@@@@@@@@@@@@@@@@@@@@@" + ByteUtil.toHexString(tx.getHash()));

        tx.sign(senderKey);
        if(knowledgeKey != null && knowledgeKey.length > 0){
            tx.authorize(String.valueOf(knowledgeKey)); //2차비밀번호가 있을 경우 한번 더 호출
        }
        return tx;
    }

    public Transaction generateTransaction(String addr, String sValue, String sGasPrice, String sGasLimit, byte[] toAddress, byte[] toMask, byte[] data, char[] passwd, char[] knowledgeKey){
        BigInteger nonce = this.mEthereum.getPendingState().getNonce(Hex.decode(addr));
        return ethereumGenerateTransaction(nonce, addr, sValue, sGasPrice, sGasLimit, toAddress, toMask, data, passwd, knowledgeKey);
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
        this.masternodeState = Integer.toString(MnState.EMPTY_MASTERNODE.num);
        this.masternodeAddress = null;
        this.recipientAddress = null;
        try {
            KeyStoreData keyStoreData = new Gson().fromJson(keyStore.toLowerCase(), KeyStoreData.class);
            byte[] privateKey = KeyStoreUtil.decryptPrivateKey(keyStore, password);
            SystemProperties.getDefault().setMasternodePrivateKey(privateKey);
            SystemProperties.getDefault().setMasternodeRecipient(recipientAddr);
            this.masternodeState = Integer.toString(MnState.REQUEST_MASTERNODE.num);
            this.masternodeAddress = keyStoreData.address;
            this.recipientAddress = ByteUtil.toHexString(recipientAddr);

            AppManager.saveGeneralProperties("masternode_state", this.masternodeState);
            AppManager.saveGeneralProperties("masternode_address", this.masternodeAddress);
            AppManager.saveGeneralProperties("recipient_address", this.recipientAddress);

            password = null;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void cancelMasternode() {
        this.masternodeState = Integer.toString(MnState.EMPTY_MASTERNODE.num);
        this.masternodeAddress = null;
        this.recipientAddress = null;
        AppManager.saveGeneralProperties("masternode_state", this.masternodeState);
        AppManager.saveGeneralProperties("masternode_address", this.masternodeAddress);
        AppManager.saveGeneralProperties("recipient_address", this.recipientAddress);
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

    public boolean startMining(byte[] address, char[] password) {
        boolean result = false;
        this.miningAddress = null;
        for(int i=0; i<this.getKeystoreList().size(); i++) {
            if(this.getKeystoreList().get(i).address.equals(ByteUtil.toHexString(address))){

                try {
                    byte[] privateKey = KeyStoreUtil.decryptPrivateKey(this.getKeystoreList().get(i).toString(), String.valueOf(password));
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

        return result;
    }

    public boolean stopMining(){
        this.miningAddress = null;
        this.miningWalletAddress = null;
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

    public static void settingNodeStyle(Node node){
        node.setStyle(new JavaFXStyle(node.getStyle()).add("-fx-border-color", "#d8d8d8").toString());
        node.setStyle(new JavaFXStyle(node.getStyle()).add("-fx-background-color", "#f8f8fb").toString());

        node.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                node.setStyle(new JavaFXStyle(node.getStyle()).add("-fx-border-color", "#2b2b2b").toString());
            }
        });

        node.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(node.isFocused()){
                    node.setStyle(new JavaFXStyle(node.getStyle()).add("-fx-border-color", "#2b2b2b").toString());
                }else{
                    node.setStyle(new JavaFXStyle(node.getStyle()).add("-fx-border-color", "#d8d8d8").toString());
                }
            }
        });

        node.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // focus in
                if(newValue){
                    node.setStyle(new JavaFXStyle(node.getStyle()).add("-fx-border-color", "#2b2b2b").toString());
                    node.setStyle(new JavaFXStyle(node.getStyle()).add("-fx-background-color", "#ffffff").toString());
                }else{
                    node.setStyle(new JavaFXStyle(node.getStyle()).add("-fx-border-color", "#d8d8d8").toString());
                    node.setStyle(new JavaFXStyle(node.getStyle()).add("-fx-background-color", "#f8f8fb").toString());
                }
            }
        });
    }

    public static void settingTextFieldLineStyle(TextField textField){

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
                }else{
                    textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-border-color", "#d8d8d8").toString());
                }
            }
        });
    }

    public static void settingTextFieldStyle(TextField textField){

        if(textField.getText().length() == 0){
            textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-background-color", "#ffffff").toString());
        }else{
            textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-background-color", "#f8f8fb").toString());
        }

        settingTextFieldLineStyle(textField);

        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                if(textField.getText() == null || textField.getText().length() == 0){
                    textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-background-color", "#ffffff").toString());
                }else{
                    if(textField.isFocused()){
                        textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-background-color", "#ffffff").toString());
                    }else{
                        textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-background-color", "#f8f8fb").toString());
                    }
                }
            }
        });

        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

                // focus in
                if(newValue){
                    textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-background-color", "#ffffff").toString());
                }else{
                    if(textField.getText() == null || textField.getText().length() == 0){
                        textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-background-color", "#ffffff").toString());
                    }else{
                        textField.setStyle(new JavaFXStyle(textField.getStyle()).add("-fx-background-color", "#f8f8fb").toString());
                    }
                }
            }
        });
    }

    public static void settingIdenticonStyle(ImageView icon){
        Rectangle clip = new Rectangle( icon.getFitWidth()-0.5, icon.getFitHeight()-0.5 );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        icon.setClip(clip);
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


    public void setMiningWalletAddress(String miningWalletAddress){this.miningWalletAddress = miningWalletAddress;}
    public String getMiningWalletAddress(){return this.miningWalletAddress;}


    /* ==============================================
     *  AppManager Singleton
     * ============================================== */
    public class APISWalletFxGUI{
        private Stage primaryStage;
        private Stage loadingStage;
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

        public void setLoadingStage(Stage loadingStage){this.loadingStage = loadingStage;}
        public void hideLoadingStage(){
            this.loadingStage.hide();
        }

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
            if(prop.getProperty("port") == null) { prop.setProperty("port", String.valueOf(new Random().nextInt(10000) + 40000)); }
            if(prop.getProperty("id") == null) { prop.setProperty("id", ByteUtil.toHexString(SecureRandom.getSeed(16))); }
            if(prop.getProperty("password") == null) { prop.setProperty("password", ByteUtil.toHexString(SecureRandom.getSeed(16))); }
            if(prop.getProperty("max_connections") == null) { prop.setProperty("max_connections", String.valueOf(5)); }
            if(prop.getProperty("allow_ip") == null) { prop.setProperty("allow_ip", "127.0.0.1"); }
            if(prop.getProperty("use_rpc") == null) { prop.setProperty("use_rpc", "false"); }
            input.close();

        } catch (IOException e) {
            prop.setProperty("port", String.valueOf(new Random().nextInt(10000) + 40000));  // TODO 리스닝 포트는 제외하도록 수정해야함
            prop.setProperty("id", ByteUtil.toHexString(SecureRandom.getSeed(16)));
            prop.setProperty("password", ByteUtil.toHexString(SecureRandom.getSeed(16)));
            prop.setProperty("max_connections", String.valueOf(5));
            prop.setProperty("allow_ip", "127.0.0.1");
            prop.setProperty("use_rpc", "false");

            try {
                File config = new File("config");
                if(!config.exists()) {
                    config.mkdirs();
                }
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
            File config = new File("config");
            if(!config.exists()) {
                config.mkdirs();
            }
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
            if (prop.getProperty("in_system_log") == null) { prop.setProperty("in_system_log", "false"); }
            if (prop.getProperty("enable_event_log") == null) { prop.setProperty("enable_event_log", "false"); }
            if (prop.getProperty("masternode_state") == null) { prop.setProperty("masternode_state", Integer.toString(MnState.EMPTY_MASTERNODE.num)); }
            if (prop.getProperty("masternode_address") == null) { prop.setProperty("masternode_address", ""); }
            if (prop.getProperty("recipient_address") == null) { prop.setProperty("recipient_address", ""); }
            if (prop.getProperty("mining_address") == null) { prop.setProperty("mining_address", ""); }
            if (prop.getProperty("language") == null) { prop.setProperty("language", "eng"); }
            if (prop.getProperty("footer_total_unit") == null) { prop.setProperty("footer_total_unit", "APIS"); }
            if (prop.getProperty("reward_sound") == null) { prop.setProperty("reward_sound", "false"); }
            input.close();

        } catch (IOException e) {
            prop.setProperty("in_system_log", "false");
            prop.setProperty("enable_event_log", "false");
            prop.setProperty("masternode_state", Integer.toString(MnState.EMPTY_MASTERNODE.num));
            prop.setProperty("masternode_address", "");
            prop.setProperty("recipient_address", "");
            prop.setProperty("mining_address","");
            prop.setProperty("language","eng");
            prop.setProperty("footer_total_unit","APIS");
            prop.setProperty("reward_sound","false");
            File config = new File("config");
            if(!config.exists()) {
                config.mkdirs();
            }
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
            File config = new File("config");
            if(!config.exists()) {
                config.mkdirs();
            }
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
            if(prop.getProperty("minimize_to_tray") == null) { prop.setProperty("minimize_to_tray", "false"); }
            input.close();

        } catch (IOException e) {
            prop.setProperty("minimize_to_tray", "false");
            try {
                File config = new File("config");
                if(!config.exists()) {
                    config.mkdirs();
                }
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
            File config = new File("config");
            if(!config.exists()) {
                config.mkdirs();
            }
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



    public void createTrayIcon(final Stage stage) {
        if(SystemTray.isSupported()) {
            java.awt.Image image = null;
            try {
                URL url  = getClass().getClassLoader().getResource("image/ic_favicon@2x.png");

                image = ImageIO.read(url);
                image = image.getScaledInstance(16,16,0);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if(SystemTray.isSupported()) {
                                stage.hide();
                            } else {
                                System.exit(0);
                            }
                        }
                    });
                }
            });

            final ActionListener closeListener = new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    System.exit(0);
                }
            };

            ActionListener showListener = new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            stage.show();
                        }
                    });
                }
            };

            // Create a Popup Menu
            PopupMenu popupMenu = new PopupMenu();
            MenuItem showItem = new MenuItem("Show");
            MenuItem closeItem = new MenuItem("Close");

            showItem.addActionListener(showListener);
            closeItem.addActionListener(closeListener);

            popupMenu.add(showItem);
            popupMenu.add(closeItem);

            // Construct a TrayIcon
            try {
                TrayIcon trayIcon = new TrayIcon(image, "APIS", popupMenu);
                trayIcon.addActionListener(showListener);
                for(int i=0; i<SystemTray.getSystemTray().getTrayIcons().length; i++){
                    SystemTray.getSystemTray().remove(SystemTray.getSystemTray().getTrayIcons()[i]);
                }
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }

    public enum MnState {
        EMPTY_MASTERNODE(0),
        REQUEST_MASTERNODE(1),
        MASTERNODE(2),
        CANCEL_MASTERNODE(3);
        public int num;
        MnState(int num) {
            this.num = num;
        }
    }
}
