package org.apis.gui.controller.transfer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.apis.core.Transaction;
import org.apis.db.sql.DBManager;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisSelectBoxController;
import org.apis.gui.controller.module.ApisWalletAndAmountController;
import org.apis.gui.controller.popup.PopupMyAddressController;
import org.apis.gui.controller.popup.PopupRecentAddressController;
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
    private final String GAS_LIMIT = "200000";
    private BigInteger gasPrice = new BigInteger("50000000000");

    private Image hintImageCheck, hintImageError;

    @FXML private TextField recevingTextField;
    @FXML private ProgressBar progressBar;
    @FXML private Slider slider;
    @FXML private Label totalMineralNature, detailMineralNature, detailGasNature, totalFeeNature;
    @FXML private AnchorPane hintMaskAddress, apisPane, tokenPane, apisReceiptPane, tokenReceiptPane;
    @FXML private Label btnMyAddress, btnRecentAddress, hintMaskAddressLabel;
    @FXML private ImageView hintIcon;
    @FXML
    private Label titleLabel, feeLabel, feeCommentLabel,
                    totalMineralLabel, detailLabel1, apisFeeLabel1, apisFeeLabel2,
                    lowLabel, highLabel, gaspriceComment1Label, gaspriceComment2Label, recevingAddressLabel
            ;
    @FXML private ApisWalletAndAmountController walletAndAmountController;
    @FXML private TransferSelectTokenController selectTokenController;
    @FXML private TransferTokenController transferTokenController;
    @FXML private TransferApisReceiptController apisReceiptController;
    @FXML private TransferTokenReceiptController tokenReceiptController;

    public void languageSetting() {
        this.titleLabel.textProperty().bind(StringManager.getInstance().transfer.title);
        this.feeLabel.textProperty().bind(StringManager.getInstance().transfer.fee);
        this.feeCommentLabel.textProperty().bind(StringManager.getInstance().transfer.feeComment);
        this.totalMineralLabel.textProperty().bind(StringManager.getInstance().transfer.totalMineral);
        this.detailLabel1.textProperty().bind(StringManager.getInstance().transfer.detail);
        this.apisFeeLabel1.textProperty().bind(StringManager.getInstance().transfer.apisFee);
        this.apisFeeLabel2.textProperty().bind(StringManager.getInstance().transfer.apisFee);
        this.lowLabel.textProperty().bind(StringManager.getInstance().transfer.low);
        this.highLabel.textProperty().bind(StringManager.getInstance().transfer.high);
        this.gaspriceComment1Label.textProperty().bind(StringManager.getInstance().transfer.gaspriceComment1);
        this.gaspriceComment2Label.textProperty().bind(StringManager.getInstance().transfer.gaspriceComment2);
        this.recevingAddressLabel.textProperty().bind(StringManager.getInstance().transfer.recevingAddress);
        this.btnMyAddress.textProperty().bind(StringManager.getInstance().transfer.myAddress);
        this.btnRecentAddress.textProperty().bind(StringManager.getInstance().transfer.recentAddress);
        this.recevingTextField.promptTextProperty().bind(StringManager.getInstance().transfer.recevingAddressPlaceHolder);
    }

    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        id = (id != null) ? id : "";
        if(id.equals("rootPane")){
        }

        else if(id.equals("btnRecentAddress")){
            PopupRecentAddressController controller = (PopupRecentAddressController)PopupManager.getInstance().showMainPopup("popup_recent_address.fxml", 0);
            controller.setHandler(new PopupRecentAddressController.PopupRecentAddressImpl() {
                @Override
                public void onMouseClickYes(String address) {
                    recevingTextField.setText(address);
                }
            });
        }else if(id.equals("btnMyAddress")){
            PopupMyAddressController controller = (PopupMyAddressController)PopupManager.getInstance().showMainPopup("popup_my_address.fxml", 0);
            controller.setHandler(new PopupMyAddressController.PopupMyAddressImpl() {
                @Override
                public void onClickYes(String address) {
                    recevingTextField.setText(address);
                }
            });
        }
    }
    @FXML
    private void onMouseEntered(InputEvent event){
        String id = ((Node)event.getSource()).getId();
    }
    @FXML
    private void onMouseExited(InputEvent event){
        String id = ((Node)event.getSource()).getId();
    }

    public void update(){
        walletAndAmountController.update();
        selectTokenController.update();
        transferTokenController.update();

        settingLayoutData();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.getInstance().guiFx.setTransfer(this);

        languageSetting();

        hintImageCheck = new Image("image/ic_check_green@2x.png");
        hintImageError = new Image("image/ic_error_red@2x.png");

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

        walletAndAmountController.setHandler(new ApisWalletAndAmountController.ApisAmountImpl() {
            @Override
            public void change(BigInteger value) {
                settingLayoutData();
            }
        });

        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                // min:50 * 10^9
                // max:500 * 10^9
                progressBar.setProgress((new_val.doubleValue()-slider.getMin()) / (slider.getMax()-slider.getMin()));
                gasPrice = new BigInteger(""+new_val.intValue()).multiply(new BigInteger("1000000000"));
                settingLayoutData();
            }
        });

        recevingTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                walletAndAmountController.setStage(ApisSelectBoxController.STAGE_DEFAULT);

                if(newValue) {
                    //onFocusIn();
                    String style = "";
                    style = style + "-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;";
                    style = style + "-fx-background-color : #ffffff; ";
                    style = style + "-fx-border-color : #999999; ";
                    recevingTextField.setStyle(style);
                } else {
                    //onFocusOut();
                    String style = "";
                    style = style + "-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; ";
                    style = style + "-fx-background-color : #f2f2f2; ";
                    style = style + "-fx-border-color : #d8d8d8; ";
                    recevingTextField.setStyle(style);

                    String mask = recevingTextField.getText();
                    if(mask.indexOf("@") >= 0){
                        //use masking address
                        String address = AppManager.getInstance().getAddressWithMask(mask);
                        if(address != null) {
                            hintMaskAddressLabel.textProperty().setValue(mask + " = " + address);
                            hintMaskAddressLabel.setTextFill(Color.web("#36b25b"));
                            hintIcon.setImage(hintImageCheck);

                        }else{
                            hintMaskAddressLabel.textProperty().setValue("No matching addresses found.");
                            hintMaskAddressLabel.setTextFill(Color.web("#910000"));
                            hintIcon.setImage(hintImageError);
                        }
                        showHintMaskAddress();
                    }else{
                        //use hex address
                        hideHintMaskAddress();
                    }
                }
                settingLayoutData();
            }
        });
        recevingTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                int maxlength = 40;
                if(recevingTextField.getText() != null && recevingTextField.getText().length() > maxlength){
                    recevingTextField.setText(recevingTextField.getText().substring(0, maxlength));
                }
                settingLayoutData();
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
                BigInteger balance = walletAndAmountController.getBalance();
                // amount
                BigInteger value = walletAndAmountController.getAmount();
                //mineral
                BigInteger mineral =walletAndAmountController.getMineral();

                //fee
                BigInteger fee = gasPrice.multiply(new BigInteger(GAS_LIMIT)).subtract(mineral);
                fee = (fee.compareTo(BigInteger.ZERO) > 0) ? fee : BigInteger.ZERO;

                //total amount
                BigInteger totalAmount = value.add(fee);
                String sTotalAmount = ApisUtil.readableApis(totalAmount, ',', true);

                //after balance
                BigInteger afterBalance = balance.subtract(totalAmount);
                afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;
                String sAfterBalance = ApisUtil.readableApis(afterBalance, ',', true);


                String sendAddr = walletAndAmountController.getAddress();
                String receivAddr = recevingTextField.getText().trim();
                if(!AddressUtil.isAddress(receivAddr)){
                    String address = AppManager.getInstance().getAddressWithMask(receivAddr);
                    if(address != null && receivAddr.length() > 0){
                        receivAddr = address;
                    }else{
                        receivAddr = null;
                    }
                }
                String sendAmount = ApisUtil.readableApis(walletAndAmountController.getAmount(), ',', true);

                if(sendAddr == null || sendAddr.length() == 0
                        || receivAddr == null || receivAddr.length() == 0
                        || sendAmount == null || sendAmount.length() == 0
                        || balance.compareTo(totalAmount) <=0 ){
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
                // gas
                BigInteger sGasPrice = transferTokenController.getGasPrice();
                //mineral
                BigInteger mineral =transferTokenController.getMineral();
                String sMineral = mineral.toString();

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
                String sAfterBalance = ApisUtil.readableApis(afterBalance, ',', true);

                detailGasNature.textProperty().setValue(ApisUtil.readableApis(sGasPrice,',',true));
                totalMineralNature.textProperty().setValue(ApisUtil.readableApis(new BigInteger(sMineral),',',true));
                totalFeeNature.textProperty().setValue(ApisUtil.readableApis(totalFee,',',true));

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


        detailMineralNature.textProperty().bind(totalMineralNature.textProperty());
        slider.setValue(0);
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
        BigInteger balance = walletAndAmountController.getBalance();
        // amount
        BigInteger value = walletAndAmountController.getAmount();
        String sValue = ApisUtil.readableApis(value, ',', true);
        // gas
        BigInteger sGasPrice = gasPrice.multiply(new BigInteger(GAS_LIMIT));
        //mineral
        BigInteger mineral =walletAndAmountController.getMineral();
        String sMineral = mineral.toString();

        //fee
        BigInteger fee = gasPrice.multiply(new BigInteger(GAS_LIMIT)).subtract(mineral);
        fee = (fee.compareTo(BigInteger.ZERO) > 0) ? fee : BigInteger.ZERO;
        String sFee = ApisUtil.readableApis(fee, ',', true);

        //total amount
        BigInteger totalAmount = value.add(fee);
        String sTotalAmount = ApisUtil.readableApis(totalAmount,',',false);
        String sTotalAmountSplit[] = sTotalAmount.split("\\.");

        //after balance
        BigInteger afterBalance = balance.subtract(totalAmount);
        afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;
        String sAfterBalace = ApisUtil.readableApis(afterBalance, ',', true);

        detailGasNature.textProperty().setValue(ApisUtil.readableApis(sGasPrice,',',true));
        totalMineralNature.textProperty().setValue(ApisUtil.readableApis(new BigInteger(sMineral),',',true));
        totalFeeNature.textProperty().setValue(ApisUtil.readableApis(fee,',',true));

        // 전송버튼 색상 변경
        String recevingAddress = recevingTextField.getText();
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
        // gas
        BigInteger sGasPrice = transferTokenController.getGasPrice();
        //mineral
        BigInteger mineral =transferTokenController.getMineral();
        String sMineral = mineral.toString();

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

        detailGasNature.textProperty().setValue(ApisUtil.readableApis(sGasPrice,',',true));
        totalMineralNature.textProperty().setValue(ApisUtil.readableApis(new BigInteger(sMineral),',',true));
        totalFeeNature.textProperty().setValue(ApisUtil.readableApis(totalFee,',',true));

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
        recevingTextField.textProperty().setValue("");
        totalMineralNature.textProperty().setValue("0.000000000000000000");
        initSlider();
        hideHintMaskAddress();
        settingLayoutData();
    }
    public void init(String id) {
        init();
        walletAndAmountController.selectedItemWithWalletId(id);
    }

    public void initSlider(){
        this.slider.valueProperty().setValue(0);
    }

    public void sendTransfer(String sPasswd){
        String sGasPrice = gasPrice.toString();
        BigInteger value = walletAndAmountController.getAmount();
        String sAddr = walletAndAmountController.getAddress();
        String sToAddress = recevingTextField.getText();

        BigInteger gas = new BigInteger(sGasPrice);
        Transaction tx = null;
        if(sAddr!= null && sAddr.length() > 0
                && sGasPrice != null && sGasPrice.length() > 0
                && sToAddress != null && sToAddress.length() > 0
                && value.compareTo(BigInteger.ZERO) >= 0){

            if (sToAddress.indexOf("@") >= 0) {
                tx = AppManager.getInstance().ethereumGenerateTransactionsWithMask(sAddr, value.toString(), gas.toString(), GAS_LIMIT, sToAddress, new byte[0], sPasswd);
            } else {
                tx = AppManager.getInstance().ethereumGenerateTransaction(sAddr, value.toString(), gas.toString(), GAS_LIMIT, Hex.decode(sToAddress), new byte[0], sPasswd);
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

    public void tokenSendTransfer(String sPasswd){

        String addr = transferTokenController.getSendAddress();
        String sValue = "0";
        String sGasPrice = transferTokenController.getGasPrice().toString();
        String sGasLimit = transferTokenController.getGasLimit().toString();
        String tokenAddress = selectTokenController.getSelectTokenAddress();
        String password = sPasswd;
        Object args[] = new Object[2];
        args[0] = transferTokenController.getReceveAddress(); // to address
        args[1] = transferTokenController.getAmount(); // token amount
        AppManager.getInstance().tokenSendTransfer(addr, sValue, sGasPrice, sGasLimit, tokenAddress, password, args);
    }

    private void showHintMaskAddress(){
        this.hintMaskAddress.setVisible(true);
        this.hintMaskAddress.prefHeightProperty().setValue(-1);
    }
    private void hideHintMaskAddress(){
        this.hintMaskAddress.setVisible(false);
        this.hintMaskAddress.prefHeightProperty().setValue(0);
    }

    private PopupTransferSendController.PopupTransferSendImpl popupTransferApisSendHandler = new PopupTransferSendController.PopupTransferSendImpl() {
        @Override
        public void send(PopupTransferSendController controller, String password) {

            String keystoreId = walletAndAmountController.getKeystoreId();
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
                        tokenSendTransfer(password);
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
}
