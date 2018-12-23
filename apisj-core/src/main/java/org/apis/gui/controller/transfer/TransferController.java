package org.apis.gui.controller.transfer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.apis.contract.EstimateTransactionResult;
import org.apis.core.Transaction;
import org.apis.db.sql.DBManager;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.receipt.ReceiptController;
import org.apis.gui.controller.popup.PopupContractWarningController;
import org.apis.gui.controller.popup.PopupFailController;
import org.apis.gui.controller.popup.PopupTransferSendController;
import org.apis.gui.manager.*;
import org.apis.keystore.KeyStoreData;
import org.apis.keystore.KeyStoreManager;
import org.apis.util.AddressUtil;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ResourceBundle;


public class TransferController extends BaseViewController {

    @FXML private AnchorPane apisPane, tokenPane, apisReceiptPane, tokenReceiptPane;
    @FXML private Label titleLabel;
    @FXML private TransferSelectTokenController selectTokenController;

    @FXML private TransferApisController transferApisController;
    @FXML private TransferTokenController transferTokenController;
    @FXML private ReceiptController apisReceiptController;
    @FXML private ReceiptController tokenReceiptController;

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
        initializeApisReceipt();
        initializeTokenReceipt();

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

        settingLayoutData();
    }

    public void initializeApisReceipt(){

        apisReceiptController.setHandler(new ReceiptController.ReceiptImpl() {
            @Override
            public void send() {
                // apis balance
                BigInteger balance = transferApisController.getBalance();

                // amount
                BigInteger value = transferApisController.getAmount();

                //fee
                BigInteger fee = transferApisController.getFee();

                //total amount
                BigInteger totalAmount = value.add(fee);

                String fromAddress = transferApisController.getAddress();
                String toAddress = transferApisController.getReceiveAddress();
                byte[] toMask = new byte[0];

                if(fromAddress == null || fromAddress.length() == 0
                        || toAddress == null || toAddress.length() == 0
                        || balance.compareTo(totalAmount) < 0 ){
                    return;
                }

                if(!AddressUtil.isAddress(toAddress)){
                    String address = AppManager.getInstance().getAddressWithMask(toAddress);
                    if(address != null && toAddress.length() > 0){
                        toMask = toAddress.getBytes(Charset.forName("UTF-8"));
                        toAddress = address;
                    }
                }

                BigInteger gasPrice = transferApisController.getGasPrice();
                BigInteger gasLimit = transferApisController.getGasLimit();

                // 완료 팝업 띄우기
                PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup(null, "popup_contract_warning.fxml", 0);
                controller.setData(fromAddress, value.toString(), gasPrice.toString(), gasLimit.toString(), Hex.decode(toAddress), toMask, null);
                controller.setHandler(new PopupContractWarningController.PopupContractWarningImpl() {
                    @Override
                    public void success(Transaction tx) {
                        DBManager.getInstance().updateRecentAddress(tx.getHash(), Hex.decode(transferApisController.getReceiveAddress()) , AppManager.getInstance().getAliasWithAddress(transferApisController.getReceiveAddress() ));
                    }
                    @Override
                    public void fail(Transaction tx){

                    }
                });
            }
        });

        apisReceiptController.setTitle(StringManager.getInstance().receipt.chargedAmount);
        apisReceiptController.setButtonTitle(StringManager.getInstance().receipt.transferButton);

        apisReceiptController.addAmount(0);
        apisReceiptController.addVSpace(16);
        apisReceiptController.addLineStyleDotted();
        apisReceiptController.addVSpace(16);
        apisReceiptController.addChargedFee(0);
        apisReceiptController.addFee(16);
        apisReceiptController.addMineral(16);
        apisReceiptController.addVSpace(16);
        apisReceiptController.addLineStyleDotted();
        apisReceiptController.addVSpace(16);
        apisReceiptController.addChargedAmount(0);
        apisReceiptController.addVSpace(16);
        apisReceiptController.addLineStyleDotted();
        apisReceiptController.addVSpace(16);
        apisReceiptController.addAfterBalance(0);
        apisReceiptController.setSuccessed(false);
    }

    public void initializeTokenReceipt(){

        tokenReceiptController.setHandler(new ReceiptController.ReceiptImpl() {
            @Override
            public void send() {
                String tokenAddress = selectTokenController.getSelectTokenAddress();
                // apis
                BigInteger balance = transferTokenController.getBalance();
                // token balance
                BigInteger tokenBalance = transferTokenController.getTokenBalance();
                // amount
                BigInteger value = BigInteger.ZERO;

                //fee
                BigInteger totalFee = transferTokenController.getTotalFee();
                totalFee = (totalFee.compareTo(BigInteger.ZERO) > 0) ? totalFee : BigInteger.ZERO;

                //total amount
                BigInteger totalAmount = value;

                // 전송버튼 색상 변경
                if(transferTokenController.getReceveAddress() == null || transferTokenController.getReceveAddress().trim().length() == 0
                        || balance.compareTo(totalFee) < 0
                        || tokenBalance.compareTo(totalAmount) < 0){
                    return ;
                }

                String sendAddr = transferTokenController.getSendAddress();
                BigInteger gasPrice = transferTokenController.getGasPrice();
                BigInteger gasLimit = transferTokenController.getGasLimit();

                Object args[] = new Object[2];
                args[0] = transferTokenController.getReceveAddress(); // to address
                args[1] = transferTokenController.getTokenAmount(); // token amount

                byte[] functionCallBytes = AppManager.getInstance().getTokenSendTransferData(args);

                // 완료 팝업 띄우기
                PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup(null,"popup_contract_warning.fxml", 0);
                controller.setData(sendAddr, value.toString(), gasPrice.toString(), gasLimit.toString(), Hex.decode(tokenAddress), new byte[0], functionCallBytes);
                controller.setHandler(new PopupContractWarningController.PopupContractWarningImpl() {
                    @Override
                    public void success(Transaction tx) {
                        DBManager.getInstance().updateRecentAddress(tx.getHash(), Hex.decode(transferTokenController.getReceveAddress()) , AppManager.getInstance().getAliasWithAddress(transferTokenController.getReceveAddress() ));
                    }
                    @Override
                    public void fail(Transaction tx){

                    }
                });
            }
        });

        tokenReceiptController.setTitle(StringManager.getInstance().receipt.chargedAmount);
        tokenReceiptController.setButtonTitle(StringManager.getInstance().receipt.transferButton);

        tokenReceiptController.addAmount(0);
        tokenReceiptController.addVSpace(16);
        tokenReceiptController.addTokenAmount();
        tokenReceiptController.addVSpace(16);
        tokenReceiptController.addLineStyleDotted();
        tokenReceiptController.addVSpace(16);
        tokenReceiptController.addChargedFee(0);
        tokenReceiptController.addFee(16);
        tokenReceiptController.addMineral(16);
        tokenReceiptController.addVSpace(16);
        tokenReceiptController.addLineStyleDotted();
        tokenReceiptController.addVSpace(16);
        tokenReceiptController.addChargedAmount(0);
        tokenReceiptController.addVSpace(16);
        tokenReceiptController.addLineStyleDotted();
        tokenReceiptController.addVSpace(16);
        tokenReceiptController.addAfterBalance(0);
        tokenReceiptController.addVSpace(16);
        tokenReceiptController.addAfterTokenBalance();
        tokenReceiptController.setSuccessed(false);
    }


    public void settingLayoutData(){
        if(selectTokenController.getSelectTokenAddress().equals("")
                || selectTokenController.getSelectTokenAddress().equals("-1")){

            apisReceiptPane.setVisible(true);
            apisReceiptPane.setPrefHeight(-1);
            tokenReceiptPane.setVisible(false);
            settingLayoutApisData();
        }else {

            apisReceiptPane.setVisible(false);
            apisReceiptPane.setPrefHeight(0);
            tokenReceiptPane.setVisible(true);
            settingLayoutTokenData();
        }
    }

    private void settingLayoutApisData(){
        BigInteger amount = transferApisController.getAmount();
        BigInteger fee = transferApisController.getFee();
        BigInteger mineral = transferApisController.getMineral();
        BigInteger chargedFee = transferApisController.getChargedFee();
        BigInteger chargedAmount = transferApisController.getChargedAmount();
        BigInteger afterBalance = transferApisController.getAfterBalance();

        // charged fee
        chargedFee = (chargedFee.compareTo(BigInteger.ZERO) >=0 ) ? chargedFee : BigInteger.ZERO;

        //after balance
        afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;

        apisReceiptController.setTitleValue(chargedAmount);
        apisReceiptController.setAmount(ApisUtil.readableApis(amount, ',', true));
        apisReceiptController.setChargedFee(ApisUtil.readableApis(chargedFee,',',true));
        apisReceiptController.setFee(ApisUtil.readableApis(fee,',',true));
        apisReceiptController.setMineral(ApisUtil.readableApis(mineral,',',true));
        apisReceiptController.setChargedAmount(ApisUtil.readableApis(chargedAmount,',',true));
        apisReceiptController.setAfterBalance(ApisUtil.readableApis(afterBalance,',',true));
        apisReceiptController.setSuccessed(transferApisController.isReadyTransfer());
    }

    private void settingLayoutTokenData(){

        BigInteger amount = transferTokenController.getAmount();
        BigInteger tokenAmount = transferTokenController.getTokenAmount();
        BigInteger fee = transferTokenController.getFee();
        BigInteger mineral = transferTokenController.getMineral();
        BigInteger chargedFee = transferTokenController.getChargedFee();
        BigInteger chargedAmount = transferTokenController.getChargedAmount();
        BigInteger afterBalance = transferTokenController.getAfterBalance();
        BigInteger afterTokenBalance = transferTokenController.getAfterTokenBalance();

        if(afterBalance.compareTo(BigInteger.ZERO) >=0
                && afterTokenBalance.compareTo(BigInteger.ZERO) >= 0){
            tokenReceiptController.setSuccessed(true);
        }else{
            tokenReceiptController.setSuccessed(true);
        }

        // charged fee
        chargedFee = (chargedFee.compareTo(BigInteger.ZERO) >=0 ) ? chargedFee : BigInteger.ZERO;

        //after balance
        afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;

        //after token balance
        afterTokenBalance = (afterTokenBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterTokenBalance : BigInteger.ZERO;

        tokenReceiptController.setTokenSymbol(selectTokenController.getTokenSymbol());
        tokenReceiptController.setAmount(ApisUtil.readableApis(amount, ',', true));
        tokenReceiptController.setTokenAmount(ApisUtil.readableApis(tokenAmount, ',', true));
        tokenReceiptController.setFee(ApisUtil.readableApis(fee, ',', true));
        tokenReceiptController.setMineral(ApisUtil.readableApis(mineral, ',', true));
        tokenReceiptController.setChargedFee(ApisUtil.readableApis(chargedFee, ',', true));
        tokenReceiptController.setChargedAmount(ApisUtil.readableApis(chargedAmount, ',', true));
        tokenReceiptController.setAfterBalance(ApisUtil.readableApis(afterBalance, ',', true));
        tokenReceiptController.setAfterTokenBalance(ApisUtil.readableApis(afterTokenBalance, ',', true));
        tokenReceiptController.setSuccessed(transferTokenController.isReadyTransfer());
    }

    private void refreshToApis(){
        apisPane.setVisible(true);
        apisPane.setPrefHeight(-1);
        tokenPane.setVisible(false);
        tokenPane.setPrefHeight(0);
        apisReceiptController.setVisible(true);
        tokenReceiptController.setVisible(false);

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
        apisReceiptController.setVisible(false);
        tokenReceiptController.setVisible(true);
    }

    private void init(){
        settingLayoutData();
    }
    public void init(String address, String tokenAddress) {
        init();
        if(tokenAddress != null && !tokenAddress.equals("-1") && !tokenAddress.equals("-2")){
            transferTokenController.selectedItemWithWalletAddress(address);
        }else{
            transferApisController.selectedItemWithWalletAddress(address);
        }
        selectTokenController.setSelectedToken(tokenAddress);

        if(tokenAddress == null || tokenAddress.length() == 0 || tokenAddress.equals("-1") || tokenAddress.equals("-2")){
            refreshToApis();
        }else{
            refreshToToken();
        }
        settingLayoutData();
    }

    public void sendTransfer(char[] password, char[] knowledgeKey){
        String sGasPrice = transferApisController.getGasPrice().toString();
        String sGasLimit = transferApisController.getGasLimit().toString();
        BigInteger value = transferApisController.getAmount();
        String sAddr = transferApisController.getAddress();
        String sToAddress = transferApisController.getReceiveAddress();
        byte[] toMask = new byte[0];

        BigInteger gas = new BigInteger(sGasPrice);
        Transaction tx = null;
        if(sAddr!= null && sAddr.length() > 0
                && sGasPrice != null && sGasPrice.length() > 0
                && sToAddress != null && sToAddress.length() > 0
                && value.compareTo(BigInteger.ZERO) >= 0){

            if (sToAddress.indexOf("@") >= 0) {
                toMask = AppManager.getInstance().getMaskWithAddress(sToAddress).getBytes(Charset.forName("UTF-8"));
            }
            tx = AppManager.getInstance().generateTransaction(sAddr, value.toString(), gas.toString(), sGasLimit, Hex.decode(sToAddress), toMask, new byte[0], password, knowledgeKey);

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

    public boolean tokenSendTransfer(char[] password, char[] knowledgeKey){

        String addr = transferTokenController.getSendAddress();
        String sValue = "0";
        String sGasPrice = transferTokenController.getGasPrice().toString();
        String sGasLimit = transferTokenController.getGasLimit().toString();
        String tokenAddress = selectTokenController.getSelectTokenAddress();
        Object args[] = new Object[2];
        args[0] = transferTokenController.getReceveAddress(); // to address
        args[1] = transferTokenController.getTokenAmount(); // token amount

        byte[] toAddress = Hex.decode(tokenAddress);
        byte[] functionCallBytes = AppManager.getInstance().getTokenSendTransferData(args);
        Transaction tx = AppManager.getInstance().generateTransaction(addr, sValue, sGasPrice, sGasLimit, toAddress, new byte[0], functionCallBytes,  password, knowledgeKey);

        // 미리 트랜잭션 발생시켜 보기
        EstimateTransactionResult runEstimate = AppManager.getInstance().estimateTransaction(tx);

        if(runEstimate.isSuccess()){
            AppManager.getInstance().tokenSendTransfer(addr, sValue, sGasPrice, sGasLimit, tokenAddress, password, knowledgeKey, args);
            return true;
        }else {
            PopupFailController failController = (PopupFailController)PopupManager.getInstance().showMainPopup(null,"popup_fail.fxml", 1);
            failController.setError(runEstimate.getReceipt().getError());
            return false;
        }

    }

    private PopupTransferSendController.PopupTransferSendImpl popupTransferApisSendHandler = new PopupTransferSendController.PopupTransferSendImpl() {
        @Override
        public void send(PopupTransferSendController controller, char[] password, char[] knowledgeKey) {

            String address = transferApisController.getAddress();
            for(int i=0; i<AppManager.getInstance().getKeystoreList().size(); i++){
                KeyStoreData data = AppManager.getInstance().getKeystoreList().get(i);
                if(data.address.equals(address)){
                    if(KeyStoreManager.matchPassword(data.toString(), password)){
                        sendTransfer(password, knowledgeKey);
                        init();
                        PopupManager.getInstance().showMainPopup(null,"popup_success.fxml",1);
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
        public void send(PopupTransferSendController controller, char[] password, char[] knowledgeKey) {

            String address = transferTokenController.getSendAddress();
            for(int i=0; i<AppManager.getInstance().getKeystoreList().size(); i++){
                KeyStoreData data = AppManager.getInstance().getKeystoreList().get(i);
                if(data.address.equals(address)){
                    if(KeyStoreManager.getInstance().matchPassword(data.toString(), password)){
                        init();
                        if(tokenSendTransfer(password, knowledgeKey)) {
                            PopupManager.getInstance().showMainPopup(null,"popup_success.fxml", 1);
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
