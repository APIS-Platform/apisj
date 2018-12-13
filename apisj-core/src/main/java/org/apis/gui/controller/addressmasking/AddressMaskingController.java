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

    public static final String REGISTER_DOMAIN_URL = "https://goo.gl/forms/oytS76KKssTcosND3";

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
    @FXML private AddressMaskingReceiptController receiptController;
    @FXML private AddressMaskingHandOverController handOverMaskController;
    @FXML private AddressMaskingHandOverReceiptController handOverReceiptController;
    @FXML private ImageView imgRegisterMask, imgHandOverMask, imgRegisterDomain;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.getInstance().guiFx.setAddressMasking(this);

        // Multilingual Support
        languageSetting();

        receiptController.setHandler(new AddressMaskingReceiptController.AddressMaskingReceiptImpl() {
            @Override
            public void transfer() {
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
                args[0] = Hex.decode(faceAddress);   //_faceAddress
                args[1] = name;   //_name
                args[2] = new BigInteger(domainId);   //_domainId
                byte[] functionCallBytes = functionRegisterMask.encode(args);

                // 완료 팝업 띄우기
                PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup(null,"popup_contract_warning.fxml", 0);
                controller.setData(payerAddress, value.toString(), gasPrice, gasLimit, addressMaskingAddress, functionCallBytes);
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
        handOverReceiptController.setHandler(new AddressMaskingHandOverReceiptController.AddressMaskingHandOverReceiptImpl() {
            @Override
            public void transfer() {
                String fromAddress = handOverMaskController.getHandOverFromAddress();
                String toAddress = handOverMaskController.getHandOverToAddress();
                Object[] values = AppManager.getInstance().callConstantFunction(ByteUtil.toHexString(addressMaskingAddress), functionDefaultFee);
                BigInteger value = new BigInteger(""+values[0]);
                BigInteger gasPrice = handOverMaskController.getGasPrice();
                BigInteger gasLimit = handOverMaskController.getGasLimit();

                System.out.println("value : "+value);
                System.out.println("gasPrice : "+gasPrice);
                System.out.println("gasLimit : "+gasLimit);


                Object[] args = new Object[1];
                args[0] = Hex.decode(toAddress);
                byte[] functionCallBytes = functionHandOverMask.encode(args);

                // 완료 팝업 띄우기
                PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup(null,"popup_contract_warning.fxml", 0);
                controller.setData(fromAddress, value.toString(), gasPrice.toString(), gasLimit.toString(), addressMaskingAddress, functionCallBytes);
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
        receiptController.setEnabled(false);

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
            try {
                Desktop.getDesktop().browse(new URI(REGISTER_DOMAIN_URL));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
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

        } else if(index == TAB_HAND_OVER_MASK) {
            this.cardManuPane.setVisible(false);
            this.bodyPane.setVisible(true);

            this.tab1LeftPane.setVisible(false);        this.tab1LeftPane.setPrefHeight(0);
            this.tabLeftHandOfMask.setVisible(true);    this.tabLeftHandOfMask.setPrefHeight(-1);
            this.handOverMaskController.update();

            this.tab1RightPane.setVisible(false);
            this.tabRightHandOverReceiptPane.setVisible(true);

        }
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
        Object[] values = AppManager.getInstance().callConstantFunction(ByteUtil.toHexString(addressMaskingAddress), functionDefaultFee);
        BigInteger value = new BigInteger(""+values[0]);


        receiptController.setEnabled(registerController.isEnabled());
        receiptController.setAddress(address);
        receiptController.setPayerAddress(payerAddress);
        receiptController.setMaskId(mask);
        receiptController.setMaskDomain(domain);
        receiptController.setValue(ApisUtil.readableApis(value, ',', true) + " APIS");
    }
    public void settingLayoutDataHandOverAddress(){
        String fromAddress = handOverMaskController.getHandOverFromAddress();
        String toAddress = handOverMaskController.getHandOverToAddress();
        String mask = handOverMaskController.getHandOverFromMask();
        Object[] values = AppManager.getInstance().callConstantFunction(ByteUtil.toHexString(addressMaskingAddress), functionDefaultFee);
        BigInteger value = new BigInteger(""+values[0]);

        handOverReceiptController.setEnabled(handOverMaskController.isEnabled());
        handOverReceiptController.setFromAddress(fromAddress);
        handOverReceiptController.setToAddress(toAddress);
        handOverReceiptController.setMask(mask);
        handOverReceiptController.setValue(ApisUtil.readableApis(value, ',', true) + " APIS");
    }

    public void update() {
        registerController.update();
        handOverMaskController.update();
        settingLayoutData();
    }
}
