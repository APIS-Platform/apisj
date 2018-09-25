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
import org.apis.solidity.Abi;
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
    private BigInteger totalBalance = BigInteger.ZERO;
    private BigInteger totalMineral = BigInteger.ZERO;
    private BigInteger totalRewared = BigInteger.ZERO;
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


        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            System.out.println(String.format("===================== [onBlock %d] =====================", block.getNumber()));

            if(isSyncDone){
                // apis, mineral
                AppManager.getInstance().keystoreFileReadAll();
                BigInteger totalBalance = BigInteger.ZERO;
                BigInteger totalMineral = BigInteger.ZERO;
                BigInteger totalRewared = BigInteger.ZERO;
                for(int i=0; i<AppManager.this.keyStoreDataExpList.size(); i++){
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
                    DBManager.getInstance().updateAccount(Hex.decode(keyStoreDataExp.address), keyStoreDataExp.alias, new BigInteger(keyStoreDataExp.balance), keyStoreDataExp.mask, BigInteger.ZERO);
                }

                // DB Sync Start
                DBSyncManager.getInstance(mEthereum).syncThreadStart();

                // Create Contract check
                List<AbiRecord> abisList = DBManager.getInstance().selectAbis();
                List<TransactionRecord> transactionList = null;
                String contractAddress = null, title = null, mask = null, abi = null, canvasUrl = null;
                ArrayList<String> deleteContractAddressList = new ArrayList<>();
                for(int i=0; i<abisList.size(); i++){
                    transactionList = DBManager.getInstance().selectTransactions(abisList.get(i).getCreator());
                    for(int j=0; j<transactionList.size(); j++){
                        if(Hex.toHexString(abisList.get(i).getContractAddress()).equals(transactionList.get(j).getContractAddress())){
                            contractAddress = transactionList.get(j).getContractAddress();
                            title = abisList.get(i).getContractName();
                            mask = getMaskWithAddress(contractAddress);
                            abi = abisList.get(i).getAbi();
                            canvasUrl = null;
                            if(DBManager.getInstance().updateContract(Hex.decode(contractAddress), title, mask, abi, canvasUrl)){
                                deleteContractAddressList.add(contractAddress);
                            }

                            break;
                        }
                    }
                }

                // Delete Abi list
                for(int i=0; i<deleteContractAddressList.size(); i++){
                    DBManager.getInstance().deleteAbi(Hex.decode(deleteContractAddressList.get(i)));
                }

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
                            this.keyStoreDataExpList.get(k).mask = getMaskWithAddress(keyStoreData.address);
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
}
