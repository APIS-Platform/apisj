package org.apis.gui.controller.addressmasking;

import javafx.animation.ScaleTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.core.Transaction;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.receipt.ReceiptController;
import org.apis.gui.controller.popup.PopupContractWarningController;
import org.apis.gui.manager.*;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;
import org.spongycastle.util.encoders.Hex;

import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.ResourceBundle;

public class AddressMaskingController extends BaseViewController {

    public static final int TAB_MENU = -1;
    public static final int TAB_REGISTER_MASK = 0;
    public static final int TAB_HAND_OVER_MASK = 1;
    public static final int TAB_REGISTER_DOMAIN = 2;
    public int tabIndex = TAB_REGISTER_MASK;

    private String abi = ContractLoader.readABI(ContractLoader.CONTRACT_ADDRESS_MASKING);
    private byte[] addressMaskingAddress = AppManager.getInstance().constants.getADDRESS_MASKING_ADDRESS();
    private CallTransaction.Contract contract = new CallTransaction.Contract(abi);
    private CallTransaction.Function functionRegisterMask = contract.getByName("registerMask");
    private CallTransaction.Function functionHandOverMask = contract.getByName("handOverMask");
    private CallTransaction.Function functionDefaultFee = contract.getByName("defaultFee");

    @FXML private AnchorPane tab1LeftPane, tab1RightPane, tabRightHandOverReceiptPane, tabLeftHandOfMask;
    @FXML private ScrollPane bodyScrollPane;
    @FXML private GridPane bodyScrollPaneContentPane;
    @FXML private GridPane cardRegisterMask, cardHandOverMask, cardRegisterDomain;
    @FXML private GridPane cardManuPane, bodyPane;
    @FXML private Label backButton;
    @FXML private Label titleRegisterMask, titleHandOverMask, titleRegisterDomain;
    @FXML private Label subTitleRegisterMask, subTitleHandOverMask, subTitleRegisterDomain;
    @FXML private Label subTitleRegisterMask2, subTitleHandOverMask2, subTitleRegisterDomain2;
    @FXML private Label enterRegisterMask, enterHandOverMask, enterRegisterDomain;
    @FXML private Pane aniLine1, aniLine2, aniLine3;

    @FXML private GridPane btnPay;
    private boolean isScrolling = false;

    @FXML private AddressMaskingRegisterController registerController;
    @FXML private AddressMaskingHandOverController handOverMaskController;
    @FXML private ReceiptController receiptController, handOverReceiptController;
    @FXML private ImageView imgRegisterMask, imgHandOverMask, imgRegisterDomain;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.getInstance().guiFx.setAddressMasking(this);

        // Multilingual Support
        languageSetting();
        initializeReceipt();
        initializeHandOverReceipt();

        registerController.setHandler(new AddressMaskingRegisterController.AddressMaskingRegisterImpl() {
            @Override
            public void settingLayoutData() {
                AddressMaskingController.this.settingLayoutData();
            }
        });
        handOverMaskController.setHandler(new AddressMaskingHandOverController.AddressMaskingHandOverImpl() {
            @Override
            public void settingLayoutData() {
                AddressMaskingController.this.settingLayoutData();
            }
        });

        bodyScrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                if(isScrolling){
                    isScrolling = false;
                }else{
                    isScrolling = true;

                    double w1w2 = bodyScrollPaneContentPane.getHeight() - bodyScrollPane.getHeight();

                    double oldV = Double.parseDouble(oldValue.toString());
                    double newV = Double.parseDouble(newValue.toString());
                    double moveV = 0;
                    double size = 20; // 이동하고 싶은 거리 (height)
                    double addNum = w1w2 / 100; // 0.01 vValue 당 이동거리(height)
                    double add = 0.01 * (size/addNum);  // size 민큼 이동하기 위해 필요한 vValue

                    // Down
                    if (oldV < newV) {
                        moveV = bodyScrollPane.getVvalue() + add;
                        if(moveV > bodyScrollPane.getVmax()){
                            moveV = bodyScrollPane.getVmax();
                        }
                    }

                    // Up
                    else if (oldV > newV) {
                        moveV = bodyScrollPane.getVvalue() - add;
                        if(moveV < bodyScrollPane.getVmin()){
                            moveV = bodyScrollPane.getVmin();
                        }
                    }

                    if(!bodyScrollPane.isPressed()) {
                        bodyScrollPane.setVvalue(moveV);
                    }
                }
            }
        });

        aniLine1.setVisible(false);
        aniLine2.setVisible(false);
        aniLine3.setVisible(false);

        initStyleTab(TAB_MENU);
    }

    public void startAnimation(Pane pane){
        pane.setVisible(true);
        ScaleTransition st = new ScaleTransition(Duration.millis(300), pane);
        st.setFromX(0.0f);
        st.setToX(1.0f);
        st.setCycleCount(1);
        st.setAutoReverse(true);
        st.play();
    }


    public void languageSetting() {

        titleRegisterMask.textProperty().bind(StringManager.getInstance().addressMasking.titleRegisterMask);
        titleHandOverMask.textProperty().bind(StringManager.getInstance().addressMasking.titleHandOverMask);
        titleRegisterDomain.textProperty().bind(StringManager.getInstance().addressMasking.titleRegisterDomain);

        subTitleRegisterMask.textProperty().bind(StringManager.getInstance().addressMasking.subTitleRegisterMask);
        subTitleRegisterMask2.textProperty().bind(StringManager.getInstance().addressMasking.subTitleRegisterMask2);
        subTitleHandOverMask.textProperty().bind(StringManager.getInstance().addressMasking.subTitleHandOverMask);
        subTitleHandOverMask2.textProperty().bind(StringManager.getInstance().addressMasking.subTitleHandOverMask2);
        subTitleRegisterDomain.textProperty().bind(StringManager.getInstance().addressMasking.subTitleRegisterDomain);
        subTitleRegisterDomain2.textProperty().bind(StringManager.getInstance().addressMasking.subTitleRegisterDomain2);

        backButton.textProperty().bind(StringManager.getInstance().common.backButton);
    }

    public void initializeReceipt(){

        receiptController.setHandler(new ReceiptController.ReceiptImpl() {
            @Override
            public void send() {
                String faceAddress = registerController.getAddress();
                String name = registerController.getMask();
                String domainId = registerController.getDomainId();

                if(name.length() <= 0){
                    return;
                }

                String payerAddress = registerController.getPayerAddress();
                BigInteger value = registerController.getValue();
                String gasLimit = registerController.getGasLimit().toString();
                String gasPrice = registerController.getGasPrice().toString();

                Object[] args = new Object[3];
                args[0] = ByteUtil.hexStringToBytes(faceAddress);   //_faceAddress
                args[1] = name;   //_name
                args[2] = new BigInteger(domainId);   //_domainId
                byte[] functionCallBytes = functionRegisterMask.encode(args);

                // 완료 팝업 띄우기
                PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup(null,"popup_contract_warning.fxml", 0);
                controller.setData(payerAddress, value.toString(), gasPrice, gasLimit, addressMaskingAddress, new byte[0], functionCallBytes);
                controller.setHandler(new PopupContractWarningController.PopupContractWarningImpl() {
                    @Override
                    public void success(Transaction tx) {
                    }
                    @Override
                    public void fail(Transaction tx){

                    }
                });
            }
        });

        receiptController.setTitle(StringManager.getInstance().receipt.chargedAmount);
        receiptController.setButtonTitle(StringManager.getInstance().receipt.transferButton);


        receiptController.addBeneficiaryAddress(0);
        receiptController.addVSpace(16);
        receiptController.addLineStyleDotted();
        receiptController.addVSpace(16);
        receiptController.addMaskAddress(0);
        receiptController.addVSpace(16);
        receiptController.addAmount(0);
        receiptController.addVSpace(16);
        receiptController.addLineStyleDotted();
        receiptController.addVSpace(16);
        receiptController.addPayerAddress(0);
        receiptController.addVSpace(16);
        receiptController.addChargedFee(0);
        receiptController.addFee(16);
        receiptController.addMineral(16);
        receiptController.addVSpace(16);
        receiptController.addLineStyleDotted();
        receiptController.addVSpace(16);
        receiptController.addChargedAmount(0);
        receiptController.addVSpace(16);
        receiptController.addLineStyleDotted();
        receiptController.addVSpace(16);
        receiptController.addAfterBalance(0);
        receiptController.setSuccessed(false);
    }

    public void initializeHandOverReceipt() {
        handOverReceiptController.setHandler(new ReceiptController.ReceiptImpl() {
            @Override
            public void send() {
                String fromAddress = handOverMaskController.getHandOverFromAddress();
                String toAddress = handOverMaskController.getHandOverToAddress();
                Object[] values = AppManager.getInstance().callConstantFunction(ByteUtil.toHexString(addressMaskingAddress), functionDefaultFee);
                BigInteger value = new BigInteger(""+values[0]);
                BigInteger gasPrice = handOverMaskController.getGasPrice();
                BigInteger gasLimit = handOverMaskController.getGasLimit();

                Object[] args = new Object[1];
                args[0] = ByteUtil.hexStringToBytes(toAddress);
                byte[] functionCallBytes = functionHandOverMask.encode(args);

                // 완료 팝업 띄우기
                PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup(null,"popup_contract_warning.fxml", 0);
                controller.setData(fromAddress, value.toString(), gasPrice.toString(), gasLimit.toString(), addressMaskingAddress, new byte[0], functionCallBytes);
                controller.setHandler(new PopupContractWarningController.PopupContractWarningImpl() {
                    @Override
                    public void success(Transaction tx) {
                    }
                    @Override
                    public void fail(Transaction tx){

                    }
                });
            }
        });

        handOverReceiptController.setTitle(StringManager.getInstance().receipt.chargedAmount);
        handOverReceiptController.setButtonTitle(StringManager.getInstance().receipt.transferButton);


        handOverReceiptController.addBeneficiaryAddress(0);
        handOverReceiptController.addVSpace(16);
        handOverReceiptController.addLineStyleDotted();
        handOverReceiptController.addVSpace(16);
        handOverReceiptController.addMaskAddress(0);
        handOverReceiptController.addVSpace(16);
        handOverReceiptController.addAmount(0);
        handOverReceiptController.addVSpace(16);
        handOverReceiptController.addLineStyleDotted();
        handOverReceiptController.addVSpace(16);
        handOverReceiptController.addPayerAddress(0);
        handOverReceiptController.addVSpace(16);
        handOverReceiptController.addChargedFee(0);
        handOverReceiptController.addFee(16);
        handOverReceiptController.addMineral(16);
        handOverReceiptController.addVSpace(16);
        handOverReceiptController.addLineStyleDotted();
        handOverReceiptController.addVSpace(16);
        handOverReceiptController.addChargedAmount(0);
        handOverReceiptController.addVSpace(16);
        handOverReceiptController.addLineStyleDotted();
        handOverReceiptController.addVSpace(16);
        handOverReceiptController.addAfterBalance(0);
        handOverReceiptController.setSuccessed(false);
    }




    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        if(id.equals("backButton")){
            initStyleTab(TAB_MENU);
        }

        if(id.equals("cardRegisterMask")){
            initStyleTab(TAB_REGISTER_MASK);
        }else if(id.equals("cardHandOverMask")){
            initStyleTab(TAB_HAND_OVER_MASK);
        }else if(id.equals("cardRegisterDomain")) {
            AppManager.getInstance().openBrowserRegisterDomain();
        }

    }

    @FXML
    public void onMouseEntered(InputEvent event){

        String id = ((Node)event.getSource()).getId();

        if(id.equals("backButton")){
            //StyleManager.backgroundColorStyle(backButton, StyleManager.AColor.Cd8d8d8);
            //StyleManager.borderColorStyle(backButton, StyleManager.AColor.Cd8d8d8);
            //StyleManager.fontColorStyle(backButton, StyleManager.AColor.C2b2b2b);
        }

        else if(id.equals("cardRegisterMask")){
            StyleManager.backgroundColorStyle(cardRegisterMask, StyleManager.AColor.Ce2e2e2);
            StyleManager.fontColorStyle(titleRegisterMask, StyleManager.AColor.C2b2b2b);
            StyleManager.fontColorStyle(subTitleRegisterMask, StyleManager.AColor.C2b2b2b);
            StyleManager.fontColorStyle(subTitleRegisterMask2, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(enterRegisterMask, StyleManager.AColor.C2b2b2b);
            imgRegisterMask.setImage(ImageManager.bgRegisterMaskHover);

            // start animation
            startAnimation(aniLine1);

        }else if(id.equals("cardHandOverMask")){
            StyleManager.backgroundColorStyle(cardHandOverMask, StyleManager.AColor.Ce2e2e2);
            StyleManager.fontColorStyle(titleHandOverMask, StyleManager.AColor.C2b2b2b);
            StyleManager.fontColorStyle(subTitleHandOverMask, StyleManager.AColor.C2b2b2b);
            StyleManager.fontColorStyle(subTitleHandOverMask2, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(enterHandOverMask, StyleManager.AColor.C2b2b2b);
            imgHandOverMask.setImage(ImageManager.bgHandOverMaskHover);

            // start animation
            startAnimation(aniLine2);

        }else if(id.equals("cardRegisterDomain")){
            StyleManager.backgroundColorStyle(cardRegisterDomain, StyleManager.AColor.Ce2e2e2);
            StyleManager.fontColorStyle(titleRegisterDomain, StyleManager.AColor.C2b2b2b);
            StyleManager.fontColorStyle(subTitleRegisterDomain, StyleManager.AColor.C2b2b2b);
            StyleManager.fontColorStyle(subTitleRegisterDomain2, StyleManager.AColor.C999999);
            StyleManager.fontColorStyle(enterRegisterDomain, StyleManager.AColor.C2b2b2b);
            imgRegisterDomain.setImage(ImageManager.bgRegisterDomainHover);

            // start animation
            startAnimation(aniLine3);

        }
    }
    @FXML
    public void onMouseExited(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        if(id.equals("backButton")){
            //StyleManager.backgroundColorStyle(backButton, StyleManager.AColor.Cffffff);
            //StyleManager.borderColorStyle(backButton, StyleManager.AColor.Cd8d8d8);
            //StyleManager.fontColorStyle(backButton, StyleManager.AColor.C999999);
        }


        else if(id.equals("cardRegisterMask")){
            StyleManager.backgroundColorStyle(cardRegisterMask, StyleManager.AColor.Cffffff);
            StyleManager.fontColorStyle(titleRegisterMask, StyleManager.AColor.Cc1c1c1);
            StyleManager.fontColorStyle(subTitleRegisterMask, StyleManager.AColor.Cc1c1c1);
            StyleManager.fontColorStyle(subTitleRegisterMask2, StyleManager.AColor.Cc1c1c1);
            StyleManager.fontColorStyle(enterRegisterMask, StyleManager.AColor.Cc1c1c1);
            imgRegisterMask.setImage(ImageManager.bgRegisterMask);

            aniLine1.setVisible(false);

        }else if(id.equals("cardHandOverMask")){
            StyleManager.backgroundColorStyle(cardHandOverMask, StyleManager.AColor.Cffffff);
            StyleManager.fontColorStyle(titleHandOverMask, StyleManager.AColor.Cc1c1c1);
            StyleManager.fontColorStyle(subTitleHandOverMask, StyleManager.AColor.Cc1c1c1);
            StyleManager.fontColorStyle(subTitleHandOverMask2, StyleManager.AColor.Cc1c1c1);
            StyleManager.fontColorStyle(enterHandOverMask, StyleManager.AColor.Cc1c1c1);
            imgHandOverMask.setImage(ImageManager.bgHandOverMask);

            aniLine2.setVisible(false);

        }else if(id.equals("cardRegisterDomain")){
            StyleManager.backgroundColorStyle(cardRegisterDomain, StyleManager.AColor.Cffffff);
            StyleManager.fontColorStyle(titleRegisterDomain, StyleManager.AColor.Cc1c1c1);
            StyleManager.fontColorStyle(subTitleRegisterDomain, StyleManager.AColor.Cc1c1c1);
            StyleManager.fontColorStyle(subTitleRegisterDomain2, StyleManager.AColor.Cc1c1c1);
            StyleManager.fontColorStyle(enterRegisterDomain, StyleManager.AColor.Cc1c1c1);
            imgRegisterDomain.setImage(ImageManager.bgRegisterDomain);

            aniLine3.setVisible(false);

        }


    }

    public void initStyleTab(int index){
        this.tabIndex = index;
        if(index == TAB_MENU) {
            this.cardManuPane.setVisible(true);
            this.bodyPane.setVisible(false);
        }else if(index == TAB_REGISTER_MASK) {
            this.cardManuPane.setVisible(false);
            this.bodyPane.setVisible(true);

            this.tab1LeftPane.setVisible(true);         this.tab1LeftPane.setPrefHeight(-1);
            this.tabLeftHandOfMask.setVisible(false);   this.tabLeftHandOfMask.setPrefHeight(0);

            this.tab1RightPane.setVisible(true);
            this.tabRightHandOverReceiptPane.setVisible(false);

            this.receiptController.setVisible(true);
            this.handOverReceiptController.setVisible(false);

        } else if(index == TAB_HAND_OVER_MASK) {
            this.cardManuPane.setVisible(false);
            this.bodyPane.setVisible(true);

            this.tab1LeftPane.setVisible(false);        this.tab1LeftPane.setPrefHeight(0);
            this.tabLeftHandOfMask.setVisible(true);    this.tabLeftHandOfMask.setPrefHeight(-1);
            this.handOverMaskController.update();

            this.tab1RightPane.setVisible(false);
            this.tabRightHandOverReceiptPane.setVisible(true);

            this.receiptController.setVisible(false);
            this.handOverReceiptController.setVisible(true);
        }

        settingLayoutData();
    }

    public void settingLayoutData() {
        if(this.tabIndex == TAB_REGISTER_MASK){
            settingLayoutDataRegisterMask();
        }else if(this.tabIndex == TAB_HAND_OVER_MASK){
            settingLayoutDataHandOverAddress();
        }

    }

    public void settingLayoutDataRegisterMask(){
        String address = registerController.getAddress();
        String payerAddress = registerController.getPayerAddress();
        String mask = registerController.getMask();
        String domain = registerController.getDomain();

        BigInteger chargedFee = registerController.getChargedFee();
        BigInteger fee = registerController.getFee();
        BigInteger mineral = registerController.getMineral();
        BigInteger amount = registerController.getAmount();
        BigInteger chargedAmount = registerController.getChargedAmount();
        BigInteger afterBalance = registerController.getAfterBalance();

        // charged fee
        chargedFee = (chargedFee.compareTo(BigInteger.ZERO) >=0 ) ? chargedFee : BigInteger.ZERO;

        // after balance
        afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;

        receiptController.setBeneficiaryAddress(address);
        receiptController.setMask(mask+domain);
        receiptController.setAmount(ApisUtil.readableApis(amount, ',', true));
        receiptController.setChargedFee(ApisUtil.readableApis(chargedFee, ',', true));
        receiptController.setFee(ApisUtil.readableApis(fee, ',', true));
        receiptController.setMineral(ApisUtil.readableApis(mineral, ',', true));
        receiptController.setPayerAddress(payerAddress);
        receiptController.setChargedAmount(ApisUtil.readableApis(chargedAmount, ',', true));
        receiptController.setAfterBalance(ApisUtil.readableApis(afterBalance, ',', true));
        receiptController.setSuccessed(registerController.isEnabled());
    }
    public void settingLayoutDataHandOverAddress(){
        String fromAddress = handOverMaskController.getHandOverFromAddress();
        String toAddress = handOverMaskController.getHandOverToAddress();
        String mask = handOverMaskController.getHandOverFromMask();

        BigInteger chargedFee = handOverMaskController.getChargedFee();
        BigInteger fee = handOverMaskController.getFee();
        BigInteger mineral = handOverMaskController.getMineral();
        BigInteger amount = handOverMaskController.getAmount();
        BigInteger chargedAmount = handOverMaskController.getChargedAmount();
        BigInteger afterBalance = handOverMaskController.getAfterBalance();

        // charged fee
        chargedFee = (chargedFee.compareTo(BigInteger.ZERO) >=0 ) ? chargedFee : BigInteger.ZERO;

        // after balance
        afterBalance = (afterBalance.compareTo(BigInteger.ZERO) >=0 ) ? afterBalance : BigInteger.ZERO;

        handOverReceiptController.setBeneficiaryAddress(toAddress);
        handOverReceiptController.setMask(mask);
        handOverReceiptController.setAmount(ApisUtil.readableApis(amount, ',', true));
        handOverReceiptController.setChargedFee(ApisUtil.readableApis(chargedFee, ',', true));
        handOverReceiptController.setFee(ApisUtil.readableApis(fee, ',', true));
        handOverReceiptController.setMineral(ApisUtil.readableApis(mineral, ',', true));
        handOverReceiptController.setPayerAddress(fromAddress);
        handOverReceiptController.setChargedAmount(ApisUtil.readableApis(chargedAmount, ',', true));
        handOverReceiptController.setAfterBalance(ApisUtil.readableApis(afterBalance, ',', true));
        handOverReceiptController.setSuccessed(handOverMaskController.isEnabled());
    }

    public void update() {
        registerController.update();
        handOverMaskController.update();
        settingLayoutData();
    }
}
