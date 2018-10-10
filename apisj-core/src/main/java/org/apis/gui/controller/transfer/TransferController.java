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
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.apis.core.Transaction;
import org.apis.db.sql.DBManager;
import org.apis.gui.common.JavaFXStyle;
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

    @FXML private GridPane sendBtn;
    @FXML private TextField recevingTextField;
    @FXML private ProgressBar progressBar;
    @FXML private Slider slider;
    @FXML private Label totalMineralNature, detailMineralNature, detailGasNature, totalFeeNature;
    @FXML private Label receiptTotalAmountNature, receiptTotalAmountDecimal, receiptAmountNature, receiptFeeNature, receiptTotalWithdrawalNature, receiptAfterNature;
    @FXML private AnchorPane hintMaskAddress, apisPane, tokenPane;
    @FXML private Label btnMyAddress, btnRecentAddress, hintMaskAddressLabel, sendBtnText;
    @FXML private ImageView hintIcon;
    @FXML
    private Label titleLabel, transferAmountLabel, feeLabel, feeCommentLabel,
                    totalMineralLabel, detailLabel1, detailLabel2, apisFeeLabel1, apisFeeLabel2,
                    lowLabel, highLabel, gaspriceComment1Label, gaspriceComment2Label, recevingAddressLabel,
                    detailTransferAmount, detailFee, detailTotalWithdrawal, detailAfterBalance, detailGaspriceComment1, detailGaspriceComment2
            ;
    @FXML private ApisWalletAndAmountController walletAndAmountController;
    @FXML private TransferSelectTokenController selectTokenController;
    @FXML private TransferTokenController transferTokenController;

    public void languageSetting() {
        this.titleLabel.textProperty().bind(StringManager.getInstance().transfer.title);
        this.transferAmountLabel.textProperty().bind(StringManager.getInstance().transfer.transferAmount);
        this.feeLabel.textProperty().bind(StringManager.getInstance().transfer.fee);
        this.feeCommentLabel.textProperty().bind(StringManager.getInstance().transfer.feeComment);
        this.totalMineralLabel.textProperty().bind(StringManager.getInstance().transfer.totalMineral);
        this.detailLabel1.textProperty().bind(StringManager.getInstance().transfer.detail);
        this.detailLabel2.textProperty().bind(StringManager.getInstance().transfer.detail);
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
        this.detailTransferAmount.textProperty().bind(StringManager.getInstance().transfer.detailTransferAmount);
        this.detailFee.textProperty().bind(StringManager.getInstance().transfer.detailFee);
        this.detailTotalWithdrawal.textProperty().bind(StringManager.getInstance().transfer.detailTotalWithdrawal);
        this.detailAfterBalance.textProperty().bind(StringManager.getInstance().transfer.detailAfterBalance);
        this.detailGaspriceComment1.textProperty().bind(StringManager.getInstance().transfer.detailGaspriceComment1);
        this.detailGaspriceComment2.textProperty().bind(StringManager.getInstance().transfer.detailGaspriceComment2);
        this.sendBtnText.textProperty().bind(StringManager.getInstance().transfer.transferButton);
    }

    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        id = (id != null) ? id : "";
        if(id.equals("rootPane")){
        }

        if(id.equals("sendBtn")){
            String sendAddr = walletAndAmountController.getAddress();
            String receivAddr = recevingTextField.getText().trim();
            String sendAmount = walletAndAmountController.getAmount().toString();
            String totalAmount = receiptTotalWithdrawalNature.getText();
            String aferBalance = receiptAfterNature.getText();
            String sBalance = walletAndAmountController.getBalance().toString();

            if(sendAddr == null || sendAddr.length() == 0
                    || receivAddr == null || receivAddr.length() == 0
                    || sendAmount == null || sendAmount.length() == 0
                    || totalAmount == null || totalAmount.length() == 0
                    || aferBalance == null || aferBalance.length() == 0
                    || new BigInteger(sBalance).subtract(new BigInteger(totalAmount.replaceAll("[,\\.]",""))).toString().indexOf("-") >=0 ){
                return;
            }

            PopupTransferSendController popupController = (PopupTransferSendController)PopupManager.getInstance().showMainPopup("popup_transfer_send.fxml", 0);
            popupController.init(sendAddr, receivAddr, sendAmount, totalAmount, aferBalance);
            popupController.setHandler(popupTransferSendHandler);
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


        detailMineralNature.textProperty().bind(totalMineralNature.textProperty());
        receiptFeeNature.textProperty().bind(totalFeeNature.textProperty());
        slider.setValue(0);
    }


    public void settingLayoutData(){
        if(selectTokenController.getSelectTokenAddress().equals("-1")){
            settingLayoutApisData();
        }else {
            settingLayoutTokenData();
        }
    }

    private void settingLayoutApisData(){
        // apis balance
        BigInteger balance = walletAndAmountController.getBalance();
        // amount
        BigInteger value = walletAndAmountController.getAmount();
        // gas
        BigInteger sGasPrice = gasPrice.multiply(new BigInteger(GAS_LIMIT));
        //mineral
        BigInteger mineral =walletAndAmountController.getMineral();
        String sMineral = mineral.toString();

        //fee
        BigInteger fee = gasPrice.multiply(new BigInteger(GAS_LIMIT)).subtract(mineral);
        fee = (fee.compareTo(BigInteger.ZERO) > 0) ? fee : BigInteger.ZERO;

        //total amount
        BigInteger totalAmount = value.add(fee);

        //after balance
        BigInteger afterBalance = balance.subtract(totalAmount);
        afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;

        detailGasNature.textProperty().setValue(ApisUtil.readableApis(sGasPrice,',',true));
        totalMineralNature.textProperty().setValue(ApisUtil.readableApis(new BigInteger(sMineral),',',true));
        totalFeeNature.textProperty().setValue(ApisUtil.readableApis(fee,',',true));

        receiptAmountNature.textProperty().setValue(ApisUtil.readableApis(value,',',true));
        receiptTotalWithdrawalNature.textProperty().setValue(ApisUtil.readableApis(totalAmount, ',',true));

        String[] receiptTotalAmount = ApisUtil.readableApis(totalAmount, ',',true).split("\\.");
        try {
            receiptTotalAmountNature.setText(receiptTotalAmount[0]);
            receiptTotalAmountDecimal.setText("."+receiptTotalAmount[1]);
        }catch (Exception e){
            receiptTotalAmountNature.setText("0");
            receiptTotalAmountDecimal.setText(".000000000000000000");
        }

        receiptAfterNature.textProperty().setValue(ApisUtil.readableApis(afterBalance, ',', true));


        // 전송버튼 색상 변경
        if(recevingTextField.getText() == null || recevingTextField.getText().trim().length() == 0
                || balance.compareTo(totalAmount) <0 ){
            sendBtn.setStyle(new JavaFXStyle(sendBtn.getStyle()).add("-fx-background-color","#d8d8d8").toString());
        }else{
            sendBtn.setStyle(new JavaFXStyle(sendBtn.getStyle()).add("-fx-background-color","#910000").toString());
        }
    }

    private void settingLayoutTokenData(){
        // token balance
        BigInteger tokenBalance = transferTokenController.getTokenBalance();
        // amount
        BigInteger value = transferTokenController.getAmount();
        // gas
        BigInteger sGasPrice = transferTokenController.getGasPrice();
        BigInteger gasLimit = transferTokenController.getGasLimit();
        //mineral
        BigInteger mineral =transferTokenController.getMineral();
        String sMineral = mineral.toString();

        //fee
        BigInteger fee = gasPrice.multiply(new BigInteger(GAS_LIMIT)).subtract(mineral);
        fee = (fee.compareTo(BigInteger.ZERO) > 0) ? fee : BigInteger.ZERO;

        //total amount
        BigInteger totalAmount = value.add(fee);

        //after balance
        BigInteger afterBalance = tokenBalance.subtract(totalAmount);
        afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;

        detailGasNature.textProperty().setValue(ApisUtil.readableApis(sGasPrice,',',true));
        totalMineralNature.textProperty().setValue(ApisUtil.readableApis(new BigInteger(sMineral),',',true));
        totalFeeNature.textProperty().setValue(ApisUtil.readableApis(fee,',',true));

        receiptAmountNature.textProperty().setValue(ApisUtil.readableApis(value,',',true));
        receiptTotalWithdrawalNature.textProperty().setValue(ApisUtil.readableApis(totalAmount, ',',true));

        String[] receiptTotalAmount = ApisUtil.readableApis(totalAmount, ',',true).split("\\.");
        try {
            receiptTotalAmountNature.setText(receiptTotalAmount[0]);
            receiptTotalAmountDecimal.setText("."+receiptTotalAmount[1]);
        }catch (Exception e){
            receiptTotalAmountNature.setText("0");
            receiptTotalAmountDecimal.setText(".000000000000000000");
        }

        receiptAfterNature.textProperty().setValue(ApisUtil.readableApis(afterBalance, ',', true));


        // 전송버튼 색상 변경
        if(recevingTextField.getText() == null || recevingTextField.getText().trim().length() == 0
                || tokenBalance.compareTo(totalAmount) <0 ){
            sendBtn.setStyle(new JavaFXStyle(sendBtn.getStyle()).add("-fx-background-color","#d8d8d8").toString());
        }else{
            sendBtn.setStyle(new JavaFXStyle(sendBtn.getStyle()).add("-fx-background-color","#910000").toString());
        }
    }

    private void refreshToApis(){
        apisPane.setVisible(true);
        tokenPane.setVisible(false);
    }
    private void refreshToToken(){
        apisPane.setVisible(false);
        tokenPane.setVisible(true);
        transferTokenController.setTokenSymbol(AppManager.getInstance().getTokenSymbol(selectTokenController.getSelectTokenAddress()));
        transferTokenController.setTokenAddress(selectTokenController.getSelectTokenAddress());
        transferTokenController.setTokenName(selectTokenController.getSelectTokenName());
    }

    private void init(){
        recevingTextField.textProperty().setValue("");
        totalMineralNature.textProperty().setValue("0.000000000000000000");
        receiptTotalWithdrawalNature.textProperty().setValue("0.000000000000000000");
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

    private void showHintMaskAddress(){
        this.hintMaskAddress.setVisible(true);
        this.hintMaskAddress.prefHeightProperty().setValue(-1);
    }
    private void hideHintMaskAddress(){
        this.hintMaskAddress.setVisible(false);
        this.hintMaskAddress.prefHeightProperty().setValue(0);
    }

    private PopupTransferSendController.PopupTransferSendImpl popupTransferSendHandler = new PopupTransferSendController.PopupTransferSendImpl() {
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
}
