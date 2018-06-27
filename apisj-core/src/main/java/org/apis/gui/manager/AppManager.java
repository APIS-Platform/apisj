package org.apis.gui.manager;

import com.google.gson.Gson;
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
import org.apis.keystore.KeyStoreData;
import org.apis.keystore.KeyStoreDataExp;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.net.eth.handler.Eth;
import org.apis.net.eth.message.StatusMessage;
import org.apis.net.server.Channel;
import org.apis.util.ByteUtil;
import org.apis.util.FastByteComparisons;
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


    private EthereumListener mListener = new EthereumListenerAdapter() {

        boolean isStartGenerateTx = false;

        @Override
        public void onSyncDone(SyncState state) {
            System.out.println("===================== [onSyncDone] =====================");
        }

        /**
         *  블록들을 전달받았으면 다른 노드들에게 현재의 RP를 전파해야한다.
         */
        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            System.out.println("===================== [onBlock] =====================");

            // apis, mineral
            AppManager.getInstance().keystoreFileReadAll();
            for(int i=0; i<AppManager.this.keyStoreDataExpList.size(); i++){
                BigInteger balance = AppManager.this.mEthereum.getRepository().getBalance( Hex.decode(AppManager.this.keyStoreDataExpList.get(i).address) );
                AppManager.this.keyStoreDataExpList.get(i).balance = balance.toString();
                System.out.println(balance.toString());

                BigInteger mineral = ((RepositoryImpl)AppManager.this.mEthereum.getRepository()).getMineral( Hex.decode(AppManager.this.keyStoreDataExpList.get(i).address), AppManager.this.mEthereum.getBlockchain().getBestBlock().getNumber() );
                AppManager.this.keyStoreDataExpList.get(i).mineral = mineral.toString();
                System.out.println(mineral.toString());
            }

            // block number
            long bestBlock = AppManager.this.mEthereum.getBlockchain().getBestBlock().getNumber();
        }

        @Override
        public void onPeerDisconnect(String host, long port) {
            System.out.println("===================== [onPeerDisconnect] =====================");

            // peer number
            int peerSize = AppManager.this.mEthereum.getChannelManager().getActivePeers().size();
        }

        @Override
        public void onPeerAddedToSyncPool(Channel peer) {
            System.out.println("===================== [onPeerAddedToSyncPool] =====================");

            // peer number
            int peerSize = AppManager.this.mEthereum.getChannelManager().getActivePeers().size();
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

        this.keyStoreDataList.clear();
        this.keyStoreDataExpList.clear();
        for(int i=0; i<keystoreList.length; i++){
            tempFile = keystoreList[i];
            if(tempFile.isFile()){
                try {
                    String allText = AppManager.fileRead(tempFile);
                    KeyStoreData keyStoreData = new Gson().fromJson(allText.toString().toLowerCase(), KeyStoreData.class);
                    this.keyStoreDataList.add(keyStoreData);

                    KeyStoreDataExp keyStoreDataExp = new Gson().fromJson(allText.toString().toLowerCase(), KeyStoreDataExp.class);
                    this.keyStoreDataExpList.add(keyStoreDataExp);
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

        /*
        final SystemProperties config = SystemProperties.getDefault();

        final boolean actionBlocksLoader = !config.blocksLoader().equals("");
        final boolean actionGenerateDag = !StringUtils.isEmpty(System.getProperty("ethash.blockNumber"));

        if (actionBlocksLoader || actionGenerateDag) {
            config.setSyncEnabled(false);
            config.setDiscoveryEnabled(false);
        }

        if (actionGenerateDag) {
            System.out.println("exit");
        } else {
            this.ethereum = EthereumFactory.createEthereum();
            ethereum.addListener(mListener);

            if (actionBlocksLoader) {
                //블록 불러오기
                System.out.println("==================== Load Block ====================");
                ethereum.getBlockLoader().loadBlocks();
            }
        }
        */
        final SystemProperties config = SystemProperties.getDefault();
        // Coinbase를 생성하기 위해 선택하도록 해야한다.
        // keystore 폴더가 존재하는지, 파일들이 존재하는지 확인한다.
        String keystoreDir = config.keystoreDir();

        final boolean actionBlocksLoader = !config.blocksLoader().equals("");

        if (actionBlocksLoader) {
            System.out.println("==================== actionBlocksLoader TRUE ====================");
            config.setSyncEnabled(false);
            config.setDiscoveryEnabled(false);
        }else{
            System.out.println("==================== actionBlocksLoader FALSE====================");
        }

        mEthereum = EthereumFactory.createEthereum();
        mEthereum.addListener(mListener);

        if (actionBlocksLoader) {
            System.out.println("==================== Load Block ====================");
            mEthereum.getBlockLoader().loadBlocks();
        }else{
            System.out.println("==================== Load Block Fail====================");
        }

    }//start

    public void ethereumCreateTransactions(String addr, String sGasPrice, String sGasLimit, String sToAddress, String sValue){

        BigInteger nonce = this.mEthereum.getRepository().getNonce(addr.getBytes());
        byte[] gasPrice = ByteUtil.bigIntegerToBytes(new BigInteger(sGasPrice));
        byte[] gasLimit = ByteUtil.bigIntegerToBytes(new BigInteger(sGasLimit));
        byte[] toAddress = Hex.decode(sToAddress);
        byte[] value = ByteUtil.bigIntegerToBytes(new BigInteger(sValue));


        System.out.println("addr : "+addr);
        System.out.println("sGasPrice : "+sGasPrice);
        System.out.println("sGasLimit : "+sGasLimit);
        System.out.println("sToAddress : "+sToAddress);
        System.out.println("sValue : "+sValue);
        System.out.println("nonce : "+nonce.toString());

        this.tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                gasPrice,
                gasLimit,
                toAddress,
                value,
                null, //new byte[0] ??
                this.mEthereum.getChainIdForNextBlock());

        ECKey senderKey = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));
        this.tx.sign(senderKey);

    }

    public void ethereumSendTransactions(){
        if(this.tx != null){
            //this.ethereum.submitTransaction(this.tx);
            this.mEthereum.getChannelManager().sendTransaction(Collections.singletonList(this.tx), null);
            System.err.println("Sending tx: " + Hex.toHexString(tx.getHash()));
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

}
