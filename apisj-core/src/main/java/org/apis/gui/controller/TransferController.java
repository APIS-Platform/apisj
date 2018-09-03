package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apis.core.Transaction;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.manager.StringManager;
import org.apis.keystore.KeyStoreData;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;


public class TransferController implements Initializable {
    private final String GAS_NUM= "200000";
    private BigInteger gasPrice = new BigInteger("50000000000");

    private Image hintImageCheck, hintImageError;

    @FXML
    private AnchorPane amountPane;
    @FXML
    private TextField amountTextField, recevingTextField;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Slider slider;
    @FXML
    private GridPane pSelectHead, pSelectItem100, pSelectItem75, pSelectItem50, pSelectItem25, pSelectItem10;
    @FXML
    private VBox pSelectList, pSelectChild;
    @FXML
    private Label pSelectHeadText;
    @FXML
    private Label totalBalanceNature, totalBalanceDecimal, totalMineralNature, totalMineralDecimal, detailMineralNature, detailMineralDecimal, detailGasNature, detailGasDecimal, totalFeeNature, totalFeeDecimal;
    @FXML
    private Label receiptTotalAmountNature, receiptTotalAmountDecimal, receiptAmountNature, receiptAmountDecimal, receiptFeeNature, receiptFeeDecimal, receiptTotalWithdrawalNature, receiptTotalWithdrawalDecimal, receiptAfterNature, receiptAfterDecimal;
    @FXML
    private AnchorPane hintMaskAddress;
    @FXML
    private Label btnMyAddress, btnRecentAddress, hintMaskAddressLabel, sendBtnText;
    @FXML
    private ImageView hintIcon;
    @FXML
    private Label titleLabel, selectWalletNameLabel, amountToSendLabel, transferAmountLabel, feeLabel, feeCommentLabel,
                    totalLabel, totalMineralLabel, detailLabel1, detailLabel2, apisFeeLabel1, apisFeeLabel2,
                    lowLabel, highLabel, gaspriceComment1Label, gaspriceComment2Label, recevingAddressLabel,
                    detailTransferAmount, detailFee, detailTotalWithdrawal, detailAfterBalance, detailGaspriceComment1, detailGaspriceComment2
            ;
    @FXML
    private ApisSelectBoxController walletSelectorController;

    private String cursorPane;

    public void languageSetting() {
        this.titleLabel.textProperty().bind(StringManager.getInstance().transfer.title);
        this.selectWalletNameLabel.textProperty().bind(StringManager.getInstance().transfer.selectWalletName);
        this.amountToSendLabel.textProperty().bind(StringManager.getInstance().transfer.amountToSend);
        this.transferAmountLabel.textProperty().bind(StringManager.getInstance().transfer.transferAmount);
        this.feeLabel.textProperty().bind(StringManager.getInstance().transfer.fee);
        this.feeCommentLabel.textProperty().bind(StringManager.getInstance().transfer.feeComment);
        this.totalLabel.textProperty().bind(StringManager.getInstance().transfer.total);
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
        String keystoreId = walletSelectorController.getKeystoreId();
        if(id.equals("rootPane")){
            if(cursorPane != null){
                if(! cursorPane.equals("selector")){
                    walletSelectorController.setVisibleItemList(false);
                }

                if(! cursorPane.equals("pSelectChild")){
                    hidePercentSelectBox();
                }
            }else{
                walletSelectorController.setVisibleItemList(false);
                hidePercentSelectBox();
            }
        }
        if(id.equals("sendBtn")){
            String sendAddr = walletSelectorController.getAddress();
            String receivAddr = recevingTextField.getText();
            String sendAmount = amountTextField.getText();
            String totalAmount = receiptTotalWithdrawalNature.getText() + receiptTotalWithdrawalDecimal.getText();
            String aferBalance = receiptAfterNature.getText() + receiptAfterDecimal.getText();

            //sendTransfer();
            PopupTransferSendController popupController = (PopupTransferSendController)AppManager.getInstance().guiFx.showMainPopup("popup_transfer_send.fxml", 0);
            popupController.init(sendAddr, receivAddr, sendAmount, totalAmount, aferBalance);
            popupController.setHandler(new PopupTransferSendController.PopupTransferSendInterface() {
                @Override
                public void send(String password) {
                    for(int i=0; i<AppManager.getInstance().getKeystoreList().size(); i++){
                        KeyStoreData data = AppManager.getInstance().getKeystoreList().get(i);
                        if(data.id.equals(keystoreId)){
                            KeyStoreManager.getInstance().setKeystoreJsonData(data.toString());
                            if(KeyStoreManager.getInstance().matchPassword(password)){
                                sendTransfer(password);
                                init();
                                AppManager.getInstance().guiFx.hideMainPopup(0);
                                popupController.succeededForm();
                                break;
                            }else{
                                popupController.failedForm("Please check your password.");
                            }
                        }
                    }
                }

                @Override
                public void close() {

                }
            });
        }

        // percent select box
        else if(id.equals("pSelectHead")){
            if(this.pSelectList.isVisible() == true){
                hidePercentSelectBox();
            }else{
                showPercentSelectBox();
            }
        }else if(id.equals("pSelectItem100")){
            pSelectHeadText.textProperty().setValue("100%");
            String sBalance = walletSelectorController.getBalance();
            BigInteger balance = new BigInteger(sBalance).multiply(new BigInteger("100")).divide(new BigInteger("100"));
            amountTextField.textProperty().setValue(AppManager.addDotWidthIndex(balance.toString()));
            pSelectHead.setStyle("-fx-border-radius : 0 4 4 0; -fx-background-radius: 0 4 4 0; -fx-background-color:#910000; ");
            hidePercentSelectBox();
            settingLayoutData();
        }else if(id.equals("pSelectItem75")){
            pSelectHeadText.textProperty().setValue("75%");
            String sBalance = walletSelectorController.getBalance();
            BigInteger balance = new BigInteger(sBalance).multiply(new BigInteger("75")).divide(new BigInteger("100"));
            amountTextField.textProperty().setValue(AppManager.addDotWidthIndex(balance.toString()));
            pSelectHead.setStyle("-fx-border-radius : 0 4 4 0; -fx-background-radius: 0 4 4 0; -fx-background-color:#910000; ");
            hidePercentSelectBox();
            settingLayoutData();
        }else if(id.equals("pSelectItem50")){
            pSelectHeadText.textProperty().setValue("50%");
            String sBalance = walletSelectorController.getBalance();
            BigInteger balance = new BigInteger(sBalance).multiply(new BigInteger("50")).divide(new BigInteger("100"));
            amountTextField.textProperty().setValue(AppManager.addDotWidthIndex(balance.toString()));
            pSelectHead.setStyle("-fx-border-radius : 0 4 4 0; -fx-background-radius: 0 4 4 0; -fx-background-color:#910000; ");
            hidePercentSelectBox();
            settingLayoutData();
        }else if(id.equals("pSelectItem25")){
            pSelectHeadText.textProperty().setValue("25%");
            String sBalance = walletSelectorController.getBalance();
            BigInteger balance = new BigInteger(sBalance).multiply(new BigInteger("25")).divide(new BigInteger("100"));
            amountTextField.textProperty().setValue(AppManager.addDotWidthIndex(balance.toString()));
            pSelectHead.setStyle("-fx-border-radius : 0 4 4 0; -fx-background-radius: 0 4 4 0; -fx-background-color:#910000; ");
            hidePercentSelectBox();
            settingLayoutData();
        }else if(id.equals("pSelectItem10")){
            pSelectHeadText.textProperty().setValue("10%");
            String sBalance = walletSelectorController.getBalance();
            BigInteger balance = new BigInteger(sBalance).multiply(new BigInteger("10")).divide(new BigInteger("100"));
            amountTextField.textProperty().setValue(AppManager.addDotWidthIndex(balance.toString()));
            pSelectHead.setStyle("-fx-border-radius : 0 4 4 0; -fx-background-radius: 0 4 4 0; -fx-background-color:#910000; ");
            hidePercentSelectBox();
            settingLayoutData();
        }else if(id.equals("btnRecentAddress")){
            AppManager.getInstance().guiFx.showMainPopup("popup_recent_address.fxml", 0);
        }else if(id.equals("btnMyAddress")){
            AppManager.getInstance().guiFx.showMainPopup("popup_my_address.fxml", 0);
        }
    }
    @FXML
    private void onMouseEntered(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        cursorPane = id;
        if(id != null) {
            if (id.equals("pSelectItem100")) {
                pSelectItem100.setStyle("-fx-background-color : #f2f2f2");
            } else if (id.equals("pSelectItem75")) {
                pSelectItem75.setStyle("-fx-background-color : #f2f2f2");
            } else if (id.equals("pSelectItem50")) {
                pSelectItem50.setStyle("-fx-background-color : #f2f2f2");
            } else if (id.equals("pSelectItem25")) {
                pSelectItem25.setStyle("-fx-background-color : #f2f2f2");
            } else if (id.equals("pSelectItem10")) {
                pSelectItem10.setStyle("-fx-background-color : #f2f2f2");
            }
        }

    }
    @FXML
    private void onMouseExited(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        cursorPane = null;
        if(id != null){
            if(id.equals("pSelectItem100")){
                pSelectItem100.setStyle("-fx-background-color : #ffffff");
            }else if(id.equals("pSelectItem75")){
                pSelectItem75.setStyle("-fx-background-color : #ffffff");
            }else if(id.equals("pSelectItem50")){
                pSelectItem50.setStyle("-fx-background-color : #ffffff");
            }else if(id.equals("pSelectItem25")){
                pSelectItem25.setStyle("-fx-background-color : #ffffff");
            }else if(id.equals("pSelectItem10")){
                pSelectItem10.setStyle("-fx-background-color : #ffffff");
            }
        }
    }

    public void update(){
        walletSelectorController.update();
        settingLayoutData();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.getInstance().guiFx.setTransfer(this);

        languageSetting();

        hintImageCheck = new Image("image/ic_check_green@2x.png");
        hintImageError = new Image("image/ic_error_red@2x.png");

        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                // min:50 * 10^9
                // max:500 * 10^9
                progressBar.setProgress((new_val.doubleValue()-slider.getMin()) / (slider.getMax()-slider.getMin()));
                gasPrice = new BigInteger(""+new_val.intValue()).multiply(new BigInteger("1000000000"));
                settingLayoutData();
            }
        });

        hidePercentSelectBox();
        walletSelectorController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ALIAS);
        walletSelectorController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl(){
            @Override
            public void onSelectItem() {
                settingLayoutData();
            }

            @Override
            public void onMouseClick() {
                hidePercentSelectBox();
            }
        });

        amountTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {


                if (!newValue.matches("[\\d\\.]*")) {
                    amountTextField.setText(newValue.replaceAll("[^\\d\\.]", ""));
                }
            }
        });
        amountTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                walletSelectorController.setStage(ApisSelectBoxController.STAGE_DEFAULT);

                if(newValue) {
                    //onFocusIn();
                    String style = "";
                    style = style + "-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;";
                    style = style + "-fx-background-color : #ffffff; ";
                    style = style + "-fx-border-color : #999999; ";
                    amountPane.setStyle(style);
                    walletSelectorController.setVisibleItemList(false);
                } else {
                    //onFocusOut();
                    String style = "";
                    style = style + "-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; ";
                    style = style + "-fx-background-color : #f2f2f2; ";
                    style = style + "-fx-border-color : #d8d8d8; ";
                    amountPane.setStyle(style);

                    String sAmount = amountTextField.getText();
                    String[] amountSplit = sAmount.split("\\.");
                    if(sAmount != null && !sAmount.equals("")){
                        if(amountSplit.length == 0){
                            sAmount = "0.000000000000000000";
                        }else if(amountSplit.length == 1){
                            sAmount = sAmount.replace(".","") + ".000000000000000000";
                        }else{
                            String decimal = amountSplit[1];
                            for(int i=0; i<18 - amountSplit[1].length(); i++){
                                decimal = decimal + "0";
                            }
                            amountSplit[1] = decimal;
                            sAmount = amountSplit[0] + "." + amountSplit[1];
                        }
                        amountTextField.textProperty().setValue(sAmount);
                    }

                    settingLayoutData();
                }
            }
        });

        recevingTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                walletSelectorController.setStage(ApisSelectBoxController.STAGE_DEFAULT);

                if(newValue) {
                    //onFocusIn();
                    String style = "";
                    style = style + "-fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4;";
                    style = style + "-fx-background-color : #ffffff; ";
                    style = style + "-fx-border-color : #999999; ";
                    recevingTextField.setStyle(style);
                    walletSelectorController.setVisibleItemList(false);
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
            }
        });


        detailMineralNature.textProperty().bind(totalMineralNature.textProperty());
        detailMineralDecimal.textProperty().bind(totalMineralDecimal.textProperty());

        receiptFeeNature.textProperty().bind(totalFeeNature.textProperty());
        receiptFeeDecimal.textProperty().bind(totalFeeDecimal.textProperty());

        receiptTotalAmountNature.textProperty().bind(receiptTotalWithdrawalNature.textProperty());
        receiptTotalAmountDecimal.textProperty().bind(receiptTotalWithdrawalDecimal.textProperty());

        slider.setValue(0);
    }


    public void settingLayoutData(){
        String sBalance =  walletSelectorController.getBalance();
        String[] balanceSplit = AppManager.addDotWidthIndex(sBalance).split("\\.");

        // amount
        String sAmount = amountTextField.getText();
        sAmount = (sAmount != null && !sAmount.equals("")) ? sAmount : AppManager.addDotWidthIndex("0");
        String[] amountSplit = sAmount.split("\\.");

        // gas
        String sGasPrice = AppManager.addDotWidthIndex(gasPrice.multiply(new BigInteger(GAS_NUM)).toString());
        String[] gasPriceSplit = sGasPrice.split("\\.");

        //mineral
        String sMineral = walletSelectorController.getMineral();
        String[] mineralSplit = AppManager.addDotWidthIndex(sMineral).split("\\.");
        BigInteger mineral = new BigInteger(sMineral);

        //fee
        BigInteger fee = gasPrice.multiply(new BigInteger(GAS_NUM)).subtract(mineral);
        fee = (fee.compareTo(new BigInteger("0")) > 0) ? fee : new BigInteger("0");
        String[] feeSplit = AppManager.addDotWidthIndex(fee.toString()).split("\\.");

        //total amount
        BigInteger totalAmount = new BigInteger(sAmount.replace(".","")).add(fee);
        String[] totalAmountSplit = AppManager.addDotWidthIndex(totalAmount.toString()).split("\\.");

        //after balance
        BigInteger afterBalance = new BigInteger(walletSelectorController.getBalance()).subtract(totalAmount);
        afterBalance = (afterBalance.compareTo(new BigInteger("0")) >=0 ) ? afterBalance : new BigInteger("0");
        String[] afterBalanceSplit = AppManager.addDotWidthIndex(afterBalance.toString()).split("\\.");

        totalBalanceNature.textProperty().setValue(balanceSplit[0]);
        totalBalanceDecimal.textProperty().setValue("."+balanceSplit[1]);

        detailGasNature.textProperty().setValue(gasPriceSplit[0]);
        detailGasDecimal.textProperty().setValue("."+gasPriceSplit[1]);

        totalMineralNature.textProperty().setValue(mineralSplit[0]);
        totalMineralDecimal.textProperty().setValue("."+mineralSplit[1]);

        totalFeeNature.textProperty().setValue(feeSplit[0]);
        totalFeeDecimal.textProperty().setValue("."+feeSplit[1]);

        receiptAmountNature.textProperty().setValue(amountSplit[0]);
        receiptAmountDecimal.textProperty().setValue("."+amountSplit[1]);

        receiptTotalWithdrawalNature.textProperty().setValue(totalAmountSplit[0]);
        receiptTotalWithdrawalDecimal.textProperty().setValue("."+totalAmountSplit[1]);

        receiptAfterNature.textProperty().setValue(afterBalanceSplit[0]);
        receiptAfterDecimal.textProperty().setValue("."+afterBalanceSplit[1]);
    }

    public void init(){
        amountTextField.textProperty().setValue("");
        recevingTextField.textProperty().setValue("");
        pSelectHeadText.textProperty().setValue("100%");
        pSelectHead.setStyle("-fx-border-radius : 0 4 4 0; -fx-background-radius: 0 4 4 0; -fx-background-color:#999999; ");
        totalMineralNature.textProperty().setValue("0");
        totalMineralDecimal.textProperty().setValue(".000000000000000000");
        receiptTotalWithdrawalNature.textProperty().setValue("0");
        receiptTotalWithdrawalDecimal.textProperty().setValue(".000000000000000000");
        initSlider();
        hideHintMaskAddress();
        settingLayoutData();
    }
    public void init(String id) {
        init();
        walletSelectorController.selectedItemWithWalletId(id);

        String sBalance =  walletSelectorController.getBalance();
        String percent = pSelectHeadText.getText().split("%")[0];
        BigInteger balance = new BigInteger(sBalance).multiply(new BigInteger(percent)).divide(new BigInteger("100"));
    }

    public void initSlider(){
        this.slider.valueProperty().setValue(0);
    }

    public void sendTransfer(String sPasswd){
        String sGasPrice = gasPrice.toString();
        String sValue = amountTextField.getText().replace(".","");
        String sAddr = walletSelectorController.getAddress();
        String sToAddress = recevingTextField.getText();

        BigInteger gas = new BigInteger(sGasPrice);
        BigInteger balance = new BigInteger(sValue);
        Transaction tx = null;
        if(sAddr!= null && sAddr.length() > 0
                && sGasPrice != null && sGasPrice.length() > 0
                && sToAddress != null && sToAddress.length() > 0
                && sValue != null && sValue.length() > 0){

            if(sToAddress.indexOf("@") >= 0){
                tx = AppManager.getInstance().ethereumGenerateTransactionsWithMask(sAddr, balance.toString(), gas.toString(), GAS_NUM, sToAddress,  sPasswd, new byte[0]);
            }else{
                tx = AppManager.getInstance().ethereumGenerateTransaction(sAddr, balance.toString(), gas.toString(), GAS_NUM, Hex.decode(sToAddress), sPasswd, new byte[0]);
            }

            if(tx != null) {
                AppManager.getInstance().ethereumSendTransactions(tx);
            }
        }
    }

    public void showPercentSelectBox(){
        this.pSelectList.setVisible(true);
        this.pSelectList.prefHeightProperty().setValue(-1);
        this.pSelectChild.prefHeightProperty().setValue(-1);
    }
    public void hidePercentSelectBox(){
        this.pSelectList.setVisible(false);
        this.pSelectList.prefHeightProperty().setValue(0);
        this.pSelectChild.prefHeightProperty().setValue(48);
    }
    public void showHintMaskAddress(){
        this.hintMaskAddress.setVisible(true);
        this.hintMaskAddress.prefHeightProperty().setValue(-1);
    }
    public void hideHintMaskAddress(){
        this.hintMaskAddress.setVisible(false);
        this.hintMaskAddress.prefHeightProperty().setValue(0);
    }
}
