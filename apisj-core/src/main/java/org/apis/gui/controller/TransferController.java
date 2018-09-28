package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apis.core.Transaction;
import org.apis.db.sql.DBManager;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.keystore.InvalidPasswordException;
import org.apis.keystore.KeyStoreData;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;


public class TransferController implements Initializable {
    private final String GAS_NUM= "200000";
    private BigInteger gasPrice = new BigInteger("50000000000");

    private Image hintImageCheck, hintImageError;

    @FXML private AnchorPane amountPane, pSelectBox;
    @FXML private TextField amountTextField, recevingTextField;
    @FXML private ProgressBar progressBar;
    @FXML private Slider slider;
    @FXML private GridPane pSelectHead, pSelectItem100, pSelectItem75, pSelectItem50, pSelectItem25, pSelectItem10, sendBtn;
    @FXML private VBox pSelectList, pSelectChild;
    @FXML private Label pSelectHeadText;
    @FXML private Label totalBalanceNature,totalMineralNature, detailMineralNature, detailGasNature, totalFeeNature;
    @FXML private Label receiptTotalAmountNature, receiptTotalAmountDecimal, receiptAmountNature, receiptFeeNature, receiptTotalWithdrawalNature, receiptAfterNature;
    @FXML private AnchorPane hintMaskAddress;
    @FXML private Label btnMyAddress, btnRecentAddress, hintMaskAddressLabel, sendBtnText;
    @FXML private ImageView hintIcon;
    @FXML
    private Label titleLabel, selectWalletNameLabel, amountToSendLabel, transferAmountLabel, feeLabel, feeCommentLabel,
                    totalLabel, totalMineralLabel, detailLabel1, detailLabel2, apisFeeLabel1, apisFeeLabel2,
                    lowLabel, highLabel, gaspriceComment1Label, gaspriceComment2Label, recevingAddressLabel,
                    detailTransferAmount, detailFee, detailTotalWithdrawal, detailAfterBalance, detailGaspriceComment1, detailGaspriceComment2
            ;
    @FXML
    private ApisSelectBoxController walletSelectorController;
    @FXML private ApisSelectboxUnitController selectApisUnitController;

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
            String receivAddr = recevingTextField.getText().trim();
            String sendAmount = selectApisUnitController.getValue(amountTextField.getText().trim()).toString();
            String totalAmount = receiptTotalWithdrawalNature.getText();
            String aferBalance = receiptAfterNature.getText();

            if(sendAddr == null || sendAddr.length() == 0
                    || receivAddr == null || receivAddr.length() == 0
                    || sendAmount == null || sendAmount.length() == 0
                    || totalAmount == null || totalAmount.length() == 0
                    || aferBalance == null || aferBalance.length() == 0
                    || new BigInteger(walletSelectorController.getBalance()).subtract(new BigInteger(totalAmount.replaceAll("\\.",""))).toString().indexOf("-") >=0 ){
                return;
            }

            PopupTransferSendController popupController = (PopupTransferSendController)PopupManager.getInstance().showMainPopup("popup_transfer_send.fxml", 0);
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
                                PopupManager.getInstance().showMainPopup("popup_success.fxml",1);
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

                if(newValue.length() > 1 && newValue.indexOf(".") < 0 && newValue.indexOf("0") == 0){
                    amountTextField.setText(newValue.substring(1, newValue.length()));
                }

                if(newValue.indexOf(".") >= 0 && newValue.indexOf(".") != newValue.lastIndexOf(".")){
                    amountTextField.setText(newValue.substring(0, newValue.length()-1));
                }

                String sAmount = amountTextField.getText().replaceAll("[^0-9,.]", "");
                sAmount = (sAmount.length() == 0)?"0":sAmount;
                String sAmountSplit[] = sAmount.split("\\.");
                if(sAmountSplit.length > 1){
                    String sAmountDecimal = sAmount.split("\\.")[1];
                    System.out.println("sAmountDecimal : "+sAmountDecimal);
                    //소수점 18자리 넘지 않게 처리 (각 단위 자리를 넘지 않게 처리)
                    if(sAmountDecimal.length() > 18){
                        sAmountDecimal = sAmountDecimal.substring(0, 18);
                    }
                    System.out.println("sAmountDecimal : "+sAmountDecimal);
                }


                BigInteger amount = selectApisUnitController.getValue(sAmount);

                String sBalance =  walletSelectorController.getBalance().replaceAll("\\.","").replaceAll(",","");
                BigInteger balance = new BigInteger(sBalance);

                if(amount.compareTo(balance) > 0){
                    String result = ApisUtil.convert(sBalance, ApisUtil.Unit.aAPIS, selectApisUnitController.getSelectUnit(), ',', true);
                    amountTextField.setText(result);
                }

                settingLayoutData();
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

                    settingLayoutData();
                }
            }
        });

        selectApisUnitController.setHandler(new ApisSelectboxUnitController.ApisSelectboxUnitImpl() {
            @Override
            public void onChange(String name, BigInteger value) {
                String sAmount = amountTextField.getText().replaceAll("[^0-9,.]", "");
                sAmount = (sAmount.length() == 0)?"0":sAmount;
                BigInteger amount = selectApisUnitController.getValue(sAmount);

                String sBalance =  walletSelectorController.getBalance().replaceAll("\\.","").replaceAll(",","");
                BigInteger balance = new BigInteger(sBalance);

                if(amount.compareTo(balance) > 0){
                    String result = ApisUtil.convert(sBalance, ApisUtil.Unit.aAPIS, selectApisUnitController.getSelectUnit(), ',', true);
                    amountTextField.setText(result);
                }

                settingLayoutData();
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

        pSelectBox.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hidePercentSelectBox();
            }
        });


        detailMineralNature.textProperty().bind(totalMineralNature.textProperty());

        receiptFeeNature.textProperty().bind(totalFeeNature.textProperty());

        slider.setValue(0);
    }


    public void settingLayoutData(){
        String sBalance =  walletSelectorController.getBalance().replaceAll("\\.","").replaceAll(",","");

        // amount
        BigInteger amount = selectApisUnitController.getValue(amountTextField.getText());

        // gas
        BigInteger sGasPrice = gasPrice.multiply(new BigInteger(GAS_NUM));

        //mineral
        String sMineral = walletSelectorController.getMineral();
        BigInteger mineral = new BigInteger(sMineral);

        //fee
        BigInteger fee = gasPrice.multiply(new BigInteger(GAS_NUM)).subtract(mineral);
        fee = (fee.compareTo(BigInteger.ZERO) > 0) ? fee : BigInteger.ZERO;

        //total amount
        BigInteger totalAmount = amount.add(fee);

        //after balance
        BigInteger afterBalance = new BigInteger(walletSelectorController.getBalance()).subtract(totalAmount);
        afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;

        totalBalanceNature.textProperty().setValue(ApisUtil.readableApis(new BigInteger(sBalance),',',true));
        detailGasNature.textProperty().setValue(ApisUtil.readableApis(sGasPrice,',',true));
        totalMineralNature.textProperty().setValue(ApisUtil.readableApis(new BigInteger(sMineral),',',true));
        totalFeeNature.textProperty().setValue(ApisUtil.readableApis(fee,',',true));
        receiptAmountNature.textProperty().setValue(ApisUtil.readableApis(amount,',',true));
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
                || amountTextField.getText().length() == 0
                || new BigInteger(walletSelectorController.getBalance()).subtract(totalAmount).toString().indexOf("-") >=0 ){
            sendBtn.setStyle(new JavaFXStyle(sendBtn.getStyle()).add("-fx-background-color","#d8d8d8").toString());
        }else{
            sendBtn.setStyle(new JavaFXStyle(sendBtn.getStyle()).add("-fx-background-color","#910000").toString());
        }
    }

    private void init(){
        amountTextField.textProperty().setValue("");
        recevingTextField.textProperty().setValue("");
        pSelectHeadText.textProperty().setValue("100%");
        pSelectHead.setStyle("-fx-border-radius : 0 4 4 0; -fx-background-radius: 0 4 4 0; -fx-background-color:#999999; ");
        totalMineralNature.textProperty().setValue("0.000000000000000000");
        receiptTotalWithdrawalNature.textProperty().setValue("0.000000000000000000");
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
        String sValue = amountTextField.getText().replaceAll("\\.","");
        String sAddr = walletSelectorController.getAddress();
        String sToAddress = recevingTextField.getText();

        BigInteger gas = new BigInteger(sGasPrice);
        BigInteger value = new BigInteger(sValue);
        Transaction tx = null;
        if(sAddr!= null && sAddr.length() > 0
                && sGasPrice != null && sGasPrice.length() > 0
                && sToAddress != null && sToAddress.length() > 0
                && sValue != null && sValue.length() > 0){

            if (sToAddress.indexOf("@") >= 0) {
                tx = AppManager.getInstance().ethereumGenerateTransactionsWithMask(sAddr, value.toString(), gas.toString(), GAS_NUM, sToAddress, new byte[0], sPasswd);
            } else {
                tx = AppManager.getInstance().ethereumGenerateTransaction(sAddr, value.toString(), gas.toString(), GAS_NUM, Hex.decode(sToAddress), new byte[0], sPasswd);
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
