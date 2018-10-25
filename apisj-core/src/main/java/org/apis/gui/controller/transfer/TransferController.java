package org.apis.gui.controller.transfer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.apis.contract.ContractLoader;
import org.apis.core.Transaction;
import org.apis.db.sql.DBManager;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.popup.PopupFailController;
import org.apis.gui.controller.popup.PopupTransferSendController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.keystore.KeyStoreData;
import org.apis.util.AddressUtil;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;


public class TransferController extends BaseViewController {

    @FXML private AnchorPane apisPane, tokenPane, apisReceiptPane, tokenReceiptPane;
    @FXML private Label titleLabel;
    @FXML private TransferSelectTokenController selectTokenController;

    @FXML private TransferApisController transferApisController;
    @FXML private TransferTokenController transferTokenController;
    @FXML private TransferApisReceiptController apisReceiptController;
    @FXML private TransferTokenReceiptController tokenReceiptController;

    public void languageSetting() {
        this.titleLabel.textProperty().bind(StringManager.getInstance().transfer.title);
    }

    public void update(){
        selectTokenController.update();
        transferApisController.update();
        transferTokenController.update();

        settingLayoutData();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.getInstance().guiFx.setTransfer(this);

        languageSetting();

        selectTokenController.setHeader(new TransferSelectTokenController.TransferSelectTokenImpl() {
            @Override
            public void onChange(String tokenName, String tokenAddress) {
                if(tokenAddress == null || tokenAddress.length() == 0 || tokenAddress.equals("-1") || tokenAddress.equals("-2")){
                    refreshToApis();
                }else{
                    refreshToToken();
                }

                settingLayoutData();
            }
        });


        transferApisController.setHandler(new TransferApisController.TransferApisImpl() {
            @Override
            public void settingLayoutData() {
                TransferController.this.settingLayoutData();
            }
        });
        transferTokenController.setHandler(new TransferTokenController.TransferTokenImpl() {
            @Override
            public void settingLayoutData() {
                TransferController.this.settingLayoutData();
            }
        });
        apisReceiptController.setHandler(new TransferApisReceiptController.TransferApisReceiptImpl() {
            @Override
            public void onMouseClickTransfer() {
                // apis balance
                BigInteger balance = transferApisController.getBalance();

                // amount
                BigInteger value = transferApisController.getAmount();

                //fee
                BigInteger fee = transferApisController.getFee();

                //total amount
                BigInteger totalAmount = value.add(fee);
                String sTotalAmount = ApisUtil.readableApis(totalAmount, ',', true);

                //after balance
                BigInteger afterBalance = balance.subtract(totalAmount);
                afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;
                String sAfterBalance = ApisUtil.readableApis(afterBalance, ',', true);


                String sendAddr = transferApisController.getAddress();
                String receivAddr = transferApisController.getReceiveAddress();

                if(!AddressUtil.isAddress(receivAddr)){
                    String address = AppManager.getInstance().getAddressWithMask(receivAddr);
                    if(address != null && receivAddr.length() > 0){
                        receivAddr = address;
                    }else{
                        receivAddr = null;
                    }
                }
                String sendAmount = ApisUtil.readableApis(value, ',', true);

                if(sendAddr == null || sendAddr.length() == 0
                        || receivAddr == null || receivAddr.length() == 0
                        || sendAmount == null || sendAmount.length() == 0
                        || balance.compareTo(totalAmount) < 0 ){
                    return;
                }

                PopupTransferSendController popupController = (PopupTransferSendController)PopupManager.getInstance().showMainPopup("popup_transfer_send.fxml", 0);
                popupController.init(sendAddr, receivAddr, sendAmount, sTotalAmount, sAfterBalance);
                popupController.setHandler(popupTransferApisSendHandler);
            }
        });

        tokenReceiptController.setHandler(new TransferTokenReceiptController.TransferTokenReceiptImpl() {
            @Override
            public void onMouseClickTransfer() {
                // apis
                BigInteger balance = transferTokenController.getBalance();
                // token balance
                BigInteger tokenBalance = transferTokenController.getTokenBalance();
                // amount
                BigInteger value = transferTokenController.getAmount();
                String sValue = ApisUtil.readableApis(value,',', true);

                //fee
                BigInteger totalFee = transferTokenController.getTotalFee();
                totalFee = (totalFee.compareTo(BigInteger.ZERO) > 0) ? totalFee : BigInteger.ZERO;

                //total amount
                BigInteger totalAmount = value;

                //after balance
                BigInteger afterBalance = tokenBalance.subtract(totalAmount);
                afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;
                String sAfterBalance = ApisUtil.readableApis(afterBalance, ',', true);

                // 전송버튼 색상 변경
                if(transferTokenController.getReceveAddress() == null || transferTokenController.getReceveAddress().trim().length() == 0
                        || balance.compareTo(totalFee) < 0
                        || tokenBalance.compareTo(totalAmount) < 0){
                    return ;
                }

                String sendAddr = transferTokenController.getSendAddress();
                String receivAddr = transferTokenController.getReceveAddress();
                String sendAmount = sValue;

                PopupTransferSendController popupController = (PopupTransferSendController)PopupManager.getInstance().showMainPopup("popup_transfer_send.fxml", 0);
                popupController.initToken(sendAddr, receivAddr, sendAmount, ApisUtil.readableApis(totalAmount, ',', true), sAfterBalance, selectTokenController.getTokenSymbol());
                popupController.setHandler(popupTransferTokenSendHandler);
            }
        });

    }


    public void settingLayoutData(){
        if(selectTokenController.getSelectTokenAddress().equals("")
                || selectTokenController.getSelectTokenAddress().equals("-1")){

            apisReceiptPane.setVisible(true);
            tokenReceiptPane.setVisible(false);
            settingLayoutApisData();
        }else {

            apisReceiptPane.setVisible(false);
            tokenReceiptPane.setVisible(true);
            settingLayoutTokenData();
        }
    }

    private void settingLayoutApisData(){
        // apis balance
        BigInteger balance = transferApisController.getBalance();

        // amount
        BigInteger value = transferApisController.getAmount();
        String sValue = ApisUtil.readableApis(value, ',', true);

        //fee
        BigInteger fee = transferApisController.getFee();
        String sFee = ApisUtil.readableApis(fee, ',', true);

        //total amount
        BigInteger totalAmount = value.add(fee);
        String sTotalAmount = ApisUtil.readableApis(totalAmount,',',false);
        String sTotalAmountSplit[] = sTotalAmount.split("\\.");

        //after balance
        BigInteger afterBalance = balance.subtract(totalAmount);
        afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;
        String sAfterBalace = ApisUtil.readableApis(afterBalance, ',', true);

        // 전송버튼 색상 변경
        String recevingAddress = transferApisController.getReceiveAddress();
        if(!AddressUtil.isAddress(recevingAddress)){
            String address = AppManager.getInstance().getAddressWithMask(recevingAddress);
            if(address != null && address.length() > 0){
                recevingAddress = address;
            }else{
                recevingAddress = null;
            }
        }
        if(recevingAddress == null || recevingAddress.length() == 0
                || balance.compareTo(totalAmount) <0 ){
            apisReceiptController.transferButtonDefault();
        }else{
            apisReceiptController.transferButtonActive();
        }

        apisReceiptController.setAfterBalance(sAfterBalace);
        apisReceiptController.setAmount(sValue);
        apisReceiptController.setFee(sFee);
        apisReceiptController.setTotalAmount(sTotalAmountSplit[0], sTotalAmountSplit[1]);
        apisReceiptController.setWithdrawal(ApisUtil.readableApis(totalAmount,',',true));
    }

    private void settingLayoutTokenData(){

        // apis
        BigInteger balance = transferTokenController.getBalance();
        // token balance
        BigInteger tokenBalance = transferTokenController.getTokenBalance();
        // amount
        BigInteger value = transferTokenController.getAmount();
        String sValue = ApisUtil.readableApis(value,',', true);

        //fee
        BigInteger totalFee = transferTokenController.getTotalFee();
        totalFee = (totalFee.compareTo(BigInteger.ZERO) > 0) ? totalFee : BigInteger.ZERO;
        String sTotalFee = ApisUtil.readableApis(totalFee, ',', true);

        //total amount
        BigInteger totalAmount = value;
        String sTotalAmount = ApisUtil.readableApis(totalAmount, ',', false);
        String sTotalAmountSplit[] = sTotalAmount.split("\\.");

        //after balance
        BigInteger afterBalance = tokenBalance.subtract(totalAmount);
        afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;
        String sAfterBalace = ApisUtil.readableApis(afterBalance, ',', true);

        // 전송버튼 색상 변경
        if(transferTokenController.getReceveAddress() == null || transferTokenController.getReceveAddress().trim().length() == 0
                || balance.compareTo(totalFee) < 0
                || tokenBalance.compareTo(totalAmount) < 0){
            tokenReceiptController.transferButtonDefault();
        }else{
            tokenReceiptController.transferButtonActive();
        }

        tokenReceiptController.setTokenSymbol(selectTokenController.getTokenSymbol());
        tokenReceiptController.setAfterBalance(ApisUtil.readableApis(balance.subtract(totalFee),',',true));
        tokenReceiptController.setAfterTokenBalance(sAfterBalace);
        tokenReceiptController.setAmount(sValue);
        tokenReceiptController.setFee(sTotalFee);
        tokenReceiptController.setTotalAmount(sTotalAmountSplit[0], sTotalAmountSplit[1]);
        tokenReceiptController.setWithdrawal(ApisUtil.readableApis(totalAmount, ',', true));
    }

    private void refreshToApis(){
        apisPane.setVisible(true);
        apisPane.setPrefHeight(-1);
        tokenPane.setVisible(false);
        tokenPane.setPrefHeight(0);

    }
    private void refreshToToken(){
        apisPane.setVisible(false);
        apisPane.setPrefHeight(0);
        tokenPane.setVisible(true);
        tokenPane.setPrefHeight(-1);
        transferTokenController.setTokenSymbol(AppManager.getInstance().getTokenSymbol(selectTokenController.getSelectTokenAddress()));
        transferTokenController.setTokenAddress(selectTokenController.getSelectTokenAddress());
        transferTokenController.setTokenName(selectTokenController.getSelectTokenName());
        transferTokenController.update();
    }

    private void init(){
        settingLayoutData();
    }
    public void init(String id, String tokenAddress) {
        init();
        if(tokenAddress != null && !tokenAddress.equals("-1") && !tokenAddress.equals("-2")){
            transferTokenController.selectedItemWithWalletId(id);
        }else{
            transferApisController.selectedItemWithWalletId(id);
        }
        selectTokenController.setSelectedToken(tokenAddress);

        if(tokenAddress == null || tokenAddress.length() == 0 || tokenAddress.equals("-1") || tokenAddress.equals("-2")){
            refreshToApis();
        }else{
            refreshToToken();
        }
        settingLayoutData();
    }

    public void sendTransfer(String sPasswd){
        String sGasPrice = transferApisController.getGasPrice().toString();
        String sGasLimit = transferApisController.getGasLimit().toString();
        BigInteger value = transferApisController.getAmount();
        String sAddr = transferApisController.getAddress();
        String sToAddress = transferApisController.getReceiveAddress();

        BigInteger gas = new BigInteger(sGasPrice);
        Transaction tx = null;
        if(sAddr!= null && sAddr.length() > 0
                && sGasPrice != null && sGasPrice.length() > 0
                && sToAddress != null && sToAddress.length() > 0
                && value.compareTo(BigInteger.ZERO) >= 0){

            if (sToAddress.indexOf("@") >= 0) {
                tx = AppManager.getInstance().ethereumGenerateTransactionsWithMask(sAddr, value.toString(), gas.toString(), sGasLimit, sToAddress, new byte[0], sPasswd);
            } else {
                tx = AppManager.getInstance().ethereumGenerateTransaction(sAddr, value.toString(), gas.toString(), sGasLimit, Hex.decode(sToAddress), new byte[0], sPasswd);
            }

            if(tx != null) {
                byte[] txHash = tx.getHash();
                byte[] address = null;
                String mask = null, alias = null;
                if(sToAddress.indexOf("@") > 0){
                    address = Hex.decode(AppManager.getInstance().getAddressWithMask(sToAddress));
                    mask = sToAddress;
                    alias = AppManager.getInstance().getAliasWithAddress(ByteUtil.toHexString(address));
                }else{
                    address = Hex.decode(sToAddress);
                    mask = AppManager.getInstance().getMaskWithAddress(sToAddress);
                    alias = AppManager.getInstance().getAliasWithAddress(sToAddress);
                }

                if(alias == null || alias.length() == 0){
                    alias = "Unnamed";
                }else{
                    if(mask != null && mask.length() > 0){
                        alias = alias + " ("+mask+")";
                    }
                }
                DBManager.getInstance().updateRecentAddress(txHash, address, alias);
                AppManager.getInstance().ethereumSendTransactions(tx);
            }
        }
    }

    public boolean tokenSendTransfer(String sPasswd){

        String addr = transferTokenController.getSendAddress();
        String sValue = "0";
        String sGasPrice = transferTokenController.getGasPrice().toString();
        String sGasLimit = transferTokenController.getGasLimit().toString();
        String tokenAddress = selectTokenController.getSelectTokenAddress();
        String password = sPasswd;
        Object args[] = new Object[2];
        args[0] = transferTokenController.getReceveAddress(); // to address
        args[1] = transferTokenController.getAmount(); // token amount

        byte[] toAddress = org.spongycastle.util.encoders.Hex.decode(tokenAddress);
        byte[] functionCallBytes = AppManager.getInstance().getTokenSendTransferData(args);
        Transaction tx = AppManager.getInstance().ethereumGenerateTransaction(addr, sValue, sGasPrice, sGasLimit, toAddress, functionCallBytes,  password);

        // 미리 트랜잭션 발생시켜 보기
        ContractLoader.ContractRunEstimate runEstimate = AppManager.getInstance().ethereumPreRunTransaction(tx);

        if(runEstimate.isSuccess()){
            AppManager.getInstance().tokenSendTransfer(addr, sValue, sGasPrice, sGasLimit, tokenAddress, password, args);
            return true;
        }else {
            PopupFailController failController = (PopupFailController)PopupManager.getInstance().showMainPopup("popup_fail.fxml", 1);
            failController.setError(runEstimate.getReceipt().getError());
            return false;
        }

    }

    private PopupTransferSendController.PopupTransferSendImpl popupTransferApisSendHandler = new PopupTransferSendController.PopupTransferSendImpl() {
        @Override
        public void send(PopupTransferSendController controller, String password) {

            String keystoreId = transferApisController.getKeystoreId();
            for(int i=0; i<AppManager.getInstance().getKeystoreList().size(); i++){
                KeyStoreData data = AppManager.getInstance().getKeystoreList().get(i);
                if(data.id.equals(keystoreId)){
                    KeyStoreManager.getInstance().setKeystoreJsonData(data.toString());
                    if(KeyStoreManager.getInstance().matchPassword(password)){
                        sendTransfer(password);
                        init();
                        PopupManager.getInstance().showMainPopup("popup_success.fxml",1);
                        break;
                    }else{
                        controller.failedForm("Please check your password.");
                    }
                }
            }
        }

        @Override
        public void close() {

        }
    };

    private PopupTransferSendController.PopupTransferSendImpl popupTransferTokenSendHandler = new PopupTransferSendController.PopupTransferSendImpl() {
        @Override
        public void send(PopupTransferSendController controller, String password) {

            String keystoreId = transferTokenController.getKeystoreId();
            for(int i=0; i<AppManager.getInstance().getKeystoreList().size(); i++){
                KeyStoreData data = AppManager.getInstance().getKeystoreList().get(i);
                if(data.id.equals(keystoreId)){
                    KeyStoreManager.getInstance().setKeystoreJsonData(data.toString());
                    if(KeyStoreManager.getInstance().matchPassword(password)){
                        init();
                        if(tokenSendTransfer(password)) {
                            PopupManager.getInstance().showMainPopup("popup_success.fxml", 1);
                        }
                        break;
                    }else{
                        controller.failedForm("Please check your password.");
                    }
                }
            }
        }

        @Override
        public void close() {

        }
    };
}
