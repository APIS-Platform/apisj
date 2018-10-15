package org.apis.gui.controller.addressmasking;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.control.*;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.module.ApisSelectBoxController;
import org.apis.gui.controller.module.GasCalculatorController;
import org.apis.gui.controller.popup.PopupContractWarningController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.HttpRequestManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.util.blockchain.ApisUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AddressMaskingController extends BaseViewController {

    private String abi = ContractLoader.readABI(ContractLoader.CONTRACT_ADDRESS_MASKING);
    private byte[] contractAddress = Hex.decode("1000000000000000000000000000000000037449");
    private CallTransaction.Contract contract = new CallTransaction.Contract(abi);
    private CallTransaction.Function setterFunction = contract.getByName("registerMask");

    @FXML private Label tabLabel1, tabLabel2, sideTabLabel1, sideTabLabel2, apisLabel, warningLabel;
    @FXML private Pane tabLinePane1, tabLinePane2, sideTabLinePane1, sideTabLinePane2;
    @FXML private AnchorPane tab1LeftPane, tab1RightPane, tab2LeftPane1, tab2LeftPane2, tab2LeftPane3;
    @FXML private GridPane commercialDescGrid, publicDescGrid, tab2RightPane1;
    @FXML private ImageView domainDragDrop, domainRequestBtn, idIcon, registerAddressIcon;
    @FXML private Label idIcon2;
    @FXML private TextField addrMaskingIDTextField, commercialDomainTextField, publicDomainTextField, emailTextField;
    @FXML private TextArea publicTextArea;
    @FXML private Label selectedDomainLabel, totalFeeAliaValue, totalFeeValue, totalWalletAddressValue, totalPayerLabel, totalBalance;

    @FXML private ApisSelectBoxController selectAddressController, selectDomainController, selectPayerController;
    @FXML private GasCalculatorController gasCalculatorController;
    @FXML private GridPane btnPay, hintAddressLabel, hintMessageLabel;

    // Multilingual Support Label
    @FXML
    private Label tabTitle, registerAddressLabel, registerAddressDesc, registerAddressMsg, selectDomainLabel, selectDomainDesc, selectDomainMsg,
                  registerIdLabel, registerIdDesc, totalFeeTitle, totalFeeAddress, totalFeeAlias, totalFeeLabel, totalFeePayer, totalFeeDesc, totalFeePayBtn,
                  registerDomainLabel, registerDomainDesc, sideTab1Desc1, sideTab1Desc2, sideTab1Desc3, sideTab2Desc1, sideTab2Desc2, sideTab2Desc3, sideTab2Desc4,
                  commercialDomainTitle, commercialDomainDesc, commercialDomainDesc1, commercialDomainDesc2, commercialDomainDesc3, commercialDomainMsg, fileFormMsg,
                  emailAddrLabel, emailDesc1, emailDesc2, emailDesc3, requestBtnLabel, publicDomainTitle, publicDomainDesc, publicDomainDesc1, publicDomainDesc2,
                  publicDomainDesc3, publicDomainDesc4, publicDomainMsg, publicMessageTitle, publicMessageDesc, idMsg, idMsg2;

    private Image domainDragDropGrey, domainDragDropColor, domainDragDropCheck;
    private Image downGreen = new Image("image/ic_check_green@2x.png");
    private Image downRed = new Image("image/ic_error_red@2x.png");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.getInstance().guiFx.setAddressMasking(this);

        // Multilingual Support
        languageSetting();

        // Initialize Images
        domainDragDropGrey = new Image("image/bg_domain_dragdrop_grey@2x.png");
        domainDragDropColor = new Image("image/bg_domain_dragdrop_color@2x.png");
        domainDragDropCheck = new Image("image/bg_domain_dragdrop_check@2x.png");

        this.tab1LeftPane.setVisible(true);
        this.tab1RightPane.setVisible(true);
        this.tab2LeftPane1.setVisible(false);
        this.addrMaskingIDTextField.setText("");
        this.addrMaskingIDTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(newValue.length() > 64){
                    addrMaskingIDTextField.setText(oldValue);
                }

                // get pre gas
                String address = selectAddressController.getAddress();
                BigInteger value = selectDomainController.getValueApisToBigInt();
                String maskingId = addrMaskingIDTextField.getText();
                Object[] args = new Object[3];
                args[0] = Hex.decode(address);   //_faceAddress
                args[1] = maskingId;   //_name
                args[2] = new BigInteger(selectDomainController.getDomainId());   //_domainId
                long checkGas = AppManager.getInstance().getPreGasUsed(abi, Hex.decode(address), contractAddress, value, setterFunction.name, args);
                if(checkGas > 0) {
                    String preGasUsed = Long.toString(checkGas);
                    gasCalculatorController.setGasLimit(preGasUsed);
                }else{
                    gasCalculatorController.setGasLimit("0");
                }

                settingLayoutData();
            }
        });

        this.tabLabel1.setTextFill(Color.web("#910000"));
        this.tabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        this.tabLinePane1.setVisible(true);
        this.tabLabel2.setTextFill(Color.web("#999999"));
        this.tabLabel2.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
        this.tabLinePane2.setVisible(false);

        this.warningLabel.setVisible(false);

        this.commercialDomainTextField.focusedProperty().addListener(textFieldListener);
        this.publicDomainTextField.focusedProperty().addListener(textFieldListener);

        selectAddressController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ADDRESS);
        selectAddressController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onSelectItem() {
                settingLayoutData();
            }

            @Override
            public void onMouseClick() {

            }
        });

        selectDomainController.init(ApisSelectBoxController.SELECT_BOX_TYPE_DOMAIN);
        selectDomainController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onSelectItem() {

                // get pre gas
                String address = selectAddressController.getAddress();
                BigInteger value = selectDomainController.getValueApisToBigInt();
                String maskingId = addrMaskingIDTextField.getText();
                Object[] args = new Object[3];
                args[0] = Hex.decode(address);   //_faceAddress
                args[1] = maskingId;   //_name
                args[2] = new BigInteger(selectDomainController.getDomainId());   //_domainId
                long checkGas = AppManager.getInstance().getPreGasUsed(abi, Hex.decode(address), contractAddress, value, setterFunction.name, args);
                if(checkGas > 0) {
                    String preGasUsed = Long.toString(checkGas);
                    gasCalculatorController.setGasLimit(preGasUsed);
                }else{
                    gasCalculatorController.setGasLimit("0");
                }

                settingLayoutData();
            }

            @Override
            public void onMouseClick() {

            }
        });

        selectPayerController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ADDRESS);
        selectPayerController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onSelectItem() {

                settingLayoutData();
            }

            @Override
            public void onMouseClick() {

            }
        });
        selectPayerController.setStage( ApisSelectBoxController.STAGE_DEFAULT);

        initStyleTab(0);

        this.idMsg.setVisible(false);
        this.idMsg.setText("");
        this.hintMessageLabel.setVisible(false);
        this.hintMessageLabel.setPrefHeight(-1);

        this.idMsg2.setVisible(false);
        this.idMsg2.setText("");
        this.hintAddressLabel.setVisible(false);
        this.hintAddressLabel.setPrefHeight(1);
    }

    public void languageSetting() {
        tabTitle.textProperty().bind(StringManager.getInstance().addressMasking.tabTitle);
        tabLabel1.textProperty().bind(StringManager.getInstance().addressMasking.tabLabel1);
        tabLabel2.textProperty().bind(StringManager.getInstance().addressMasking.tabLabel2);
        registerAddressLabel.textProperty().bind(StringManager.getInstance().addressMasking.registerAddressLabel);
        registerAddressDesc.textProperty().bind(StringManager.getInstance().addressMasking.registerAddressDesc);
        registerAddressMsg.textProperty().bind(StringManager.getInstance().addressMasking.registerAddressMsg);
        selectDomainLabel.textProperty().bind(StringManager.getInstance().addressMasking.selectDomainLabel);
        selectDomainDesc.textProperty().bind(StringManager.getInstance().addressMasking.selectDomainDesc);
        registerIdLabel.textProperty().bind(StringManager.getInstance().addressMasking.registerIdLabel);
        registerIdDesc.textProperty().bind(StringManager.getInstance().addressMasking.registerIdDesc);
        addrMaskingIDTextField.promptTextProperty().bind(StringManager.getInstance().addressMasking.registerIdPlaceholder);
        totalFeeTitle.textProperty().bind(StringManager.getInstance().addressMasking.totalFeeTitle);
        totalFeeAddress.textProperty().bind(StringManager.getInstance().addressMasking.totalFeeAddress);
        totalFeeAlias.textProperty().bind(StringManager.getInstance().addressMasking.totalFeeAlias);
        totalFeeLabel.textProperty().bind(StringManager.getInstance().addressMasking.totalFeeLabel);
        totalFeePayer.textProperty().bind(StringManager.getInstance().addressMasking.totalFeePayer);
        totalFeeDesc.textProperty().bind(StringManager.getInstance().addressMasking.totalFeeDesc);
        totalFeePayBtn.textProperty().bind(StringManager.getInstance().addressMasking.totalFeePayBtn);
        registerDomainLabel.textProperty().bind(StringManager.getInstance().addressMasking.registerDomainLabel);
        registerDomainDesc.textProperty().bind(StringManager.getInstance().addressMasking.registerDomainDesc);
        sideTabLabel1.textProperty().bind(StringManager.getInstance().addressMasking.sideTabLabel1);
        sideTabLabel2.textProperty().bind(StringManager.getInstance().addressMasking.sideTabLabel2);
        sideTab1Desc1.textProperty().bind(StringManager.getInstance().addressMasking.sideTab1Desc1);
        sideTab1Desc2.textProperty().bind(StringManager.getInstance().addressMasking.sideTab1Desc2);
        sideTab1Desc3.textProperty().bind(StringManager.getInstance().addressMasking.sideTab1Desc3);
        sideTab2Desc1.textProperty().bind(StringManager.getInstance().addressMasking.sideTab2Desc1);
        sideTab2Desc2.textProperty().bind(StringManager.getInstance().addressMasking.sideTab2Desc2);
        sideTab2Desc3.textProperty().bind(StringManager.getInstance().addressMasking.sideTab2Desc3);
        sideTab2Desc4.textProperty().bind(StringManager.getInstance().addressMasking.sideTab2Desc4);
        commercialDomainTitle.textProperty().bind(StringManager.getInstance().addressMasking.commercialDomainTitle);
        commercialDomainDesc.textProperty().bind(StringManager.getInstance().addressMasking.commercialDomainDesc);
        commercialDomainDesc1.textProperty().bind(StringManager.getInstance().addressMasking.commercialDomainDesc1);
        commercialDomainDesc2.textProperty().bind(StringManager.getInstance().addressMasking.commercialDomainDesc2);
        commercialDomainDesc3.textProperty().bind(StringManager.getInstance().addressMasking.commercialDomainDesc3);
        commercialDomainTextField.promptTextProperty().bind(StringManager.getInstance().addressMasking.commercialDomainPlaceholder);
        commercialDomainMsg.textProperty().bind(StringManager.getInstance().addressMasking.commercialDomainMsg);
        fileFormMsg.textProperty().bind(StringManager.getInstance().addressMasking.fileFormMsg);
        emailAddrLabel.textProperty().bind(StringManager.getInstance().addressMasking.emailAddrLabel);
        emailTextField.promptTextProperty().bind(StringManager.getInstance().addressMasking.emailPlaceholder);
        emailDesc1.textProperty().bind(StringManager.getInstance().addressMasking.emailDesc1);
        emailDesc2.textProperty().bind(StringManager.getInstance().addressMasking.emailDesc2);
        emailDesc3.textProperty().bind(StringManager.getInstance().addressMasking.emailDesc3);
        requestBtnLabel.textProperty().bind(StringManager.getInstance().addressMasking.requestBtnLabel);
        publicDomainTitle.textProperty().bind(StringManager.getInstance().addressMasking.publicDomainTitle);
        publicDomainDesc.textProperty().bind(StringManager.getInstance().addressMasking.publicDomainDesc);
        publicDomainDesc1.textProperty().bind(StringManager.getInstance().addressMasking.publicDomainDesc1);
        publicDomainDesc2.textProperty().bind(StringManager.getInstance().addressMasking.publicDomainDesc2);
        publicDomainDesc3.textProperty().bind(StringManager.getInstance().addressMasking.publicDomainDesc3);
        publicDomainDesc4.textProperty().bind(StringManager.getInstance().addressMasking.publicDomainDesc4);
        publicDomainTextField.promptTextProperty().bind(StringManager.getInstance().addressMasking.publicDomainPlaceholder);
        publicMessageTitle.textProperty().bind(StringManager.getInstance().addressMasking.publicMessageTitle);
        publicMessageDesc.textProperty().bind(StringManager.getInstance().addressMasking.publicMessageDesc);
        publicTextArea.promptTextProperty().bind(StringManager.getInstance().addressMasking.publicTextareaPlaceholder);
    }

    private ChangeListener<Boolean> textFieldListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            // Focus in Function
            if(newValue) {
                if(tab2LeftPane2.isVisible()) {
                    commercialDomainTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                } else if(tab2LeftPane3.isVisible()) {
                    publicDomainTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                }
            }

            // Focus out Function
            else {
                if(tab2LeftPane2.isVisible()) {
                    commercialDomainTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                } else if(tab2LeftPane3.isVisible()) {
                    publicDomainTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                }
            }
        }
    };

    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        if(id.equals("tab1")) {
            initStyleTab(0);

        } else if(id.equals("tab2")) {
            initStyleTab(1);

        } else if(id.equals("sideTab1")) {
            initStyleSideTab(0);

        } else if(id.equals("sideTab2")) {
            initStyleSideTab(1);

        } else if(id.equals("domainRequestBtn")) {
            if(commercialDescGrid.isVisible()) {
                this.tab2LeftPane1.setVisible(false);
                this.tab2RightPane1.setVisible(true);
                this.tab2LeftPane2.setVisible(true);
                //리퀘스트 이후 입력 데이터 유지를 위해 주석처리
                //this.commercialDomainTextField.setText("");
                //this.emailTextField.setText("");
            } else {
                this.tab2LeftPane1.setVisible(false);
                this.tab2LeftPane3.setVisible(true);
                //리퀘스트 이후 입력 데이터 유지를 위해 주석처리
                //this.publicDomainTextField.setText("");
                //this.publicTextArea.setText("");

                //publicSendBtn
                // 오른쪽 뷰 보이
                this.tab2RightPane1.setVisible(true);
                //리퀘스트 이후 입력 데이터 유지를 위해 주석처리
                //this.emailTextField.setText("");
            }

        } else if(id.equals("commercialBackBtn")) {
            this.tab2LeftPane2.setVisible(false);
            this.tab2RightPane1.setVisible(false);
            this.tab2LeftPane1.setVisible(true);

        } else if(id.equals("publicBackBtn")) {
            this.tab2LeftPane3.setVisible(false);
            this.tab2RightPane1.setVisible(false);
            this.tab2LeftPane1.setVisible(true);

        } else if(id.equals("btnPay")){
            String faceAddress = selectAddressController.getAddress().trim();
            String name = addrMaskingIDTextField.getText().trim();
            String domainId = selectDomainController.getDomainId().trim();

            if(name.length() <= 0){
                return;
            }

            String address = selectPayerController.getAddress().trim();
            BigInteger value = selectDomainController.getValueApisToBigInt();
            String gasLimit = gasCalculatorController.getGasLimit().toString();
            String gasPrice = gasCalculatorController.getGasPrice().toString();

            System.out.println("gasLimit : "+gasLimit+", gasPrice : "+gasPrice);

            Object[] args = new Object[3];
            args[0] = Hex.decode(faceAddress);   //_faceAddress
            args[1] = name;   //_name
            args[2] = new BigInteger(domainId);   //_domainId
            byte[] functionCallBytes = setterFunction.encode(args);

            // 완료 팝업 띄우기
            PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup("popup_contract_warning.fxml", 0);
            controller.setData(address, value.toString(), gasPrice, gasLimit, contractAddress, functionCallBytes);
            controller.setHandler(new PopupContractWarningController.PopupContractWarningImpl() {
                @Override
                public void success() {
                }
                @Override
                public void fail(){

                }
            });
        }

    }

    public void initStyleTab(int index){
        if(index == 0) {
            //Register Mask
            this.tab1LeftPane.setVisible(true);
            this.tab1RightPane.setVisible(true);
            this.tab2LeftPane1.setVisible(false);
            this.tab2LeftPane2.setVisible(false);
            this.tab2LeftPane3.setVisible(false);
            this.tab2RightPane1.setVisible(false);
            this.tabLabel1.setTextFill(Color.web("#910000"));
            this.tabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
            this.tabLinePane1.setVisible(true);
            this.tabLabel2.setTextFill(Color.web("#999999"));
            this.tabLabel2.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
            this.tabLinePane2.setVisible(false);

        } else if(index == 1) {
            //Register Domain
            this.tab1LeftPane.setVisible(false);
            this.tab2LeftPane2.setVisible(false);
            this.tab2LeftPane3.setVisible(false);
            this.tab1RightPane.setVisible(false);
            this.tab2RightPane1.setVisible(false);
            this.tab2LeftPane1.setVisible(true);
            this.tabLabel2.setTextFill(Color.web("#910000"));
            this.tabLabel2.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
            this.tabLinePane2.setVisible(true);
            this.tabLabel1.setTextFill(Color.web("#999999"));
            this.tabLabel1.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
            this.tabLinePane1.setVisible(false);

            this.publicDescGrid.setVisible(false);
            this.commercialDescGrid.setVisible(true);
            this.sideTabLabel1.setTextFill(Color.web("#910000"));
            this.sideTabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 14px;");
            this.sideTabLinePane1.setVisible(true);
            this.sideTabLabel2.setTextFill(Color.web("#999999"));
            this.sideTabLabel2.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size: 14px;");
            this.sideTabLinePane2.setVisible(false);

            initStyleSideTab(0);
        }
    }
    public void initStyleSideTab(int index){
        if(index == 0) {
            //Commercial domain
            this.commercialDescGrid.setVisible(true);
            this.publicDescGrid.setVisible(false);
            this.sideTabLabel1.setTextFill(Color.web("#910000"));
            this.sideTabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 14px;");
            this.sideTabLinePane1.setVisible(true);
            this.sideTabLabel2.setTextFill(Color.web("#999999"));
            this.sideTabLabel2.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size: 14px;");
            this.sideTabLinePane2.setVisible(false);
            this.domainRequestBtn.setVisible(false);

        } else if(index == 1) {
            //Public domain
            this.commercialDescGrid.setVisible(false);
            this.publicDescGrid.setVisible(true);
            this.sideTabLabel2.setTextFill(Color.web("#910000"));
            this.sideTabLabel2.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 14px;");
            this.sideTabLinePane2.setVisible(true);
            this.sideTabLabel1.setTextFill(Color.web("#999999"));
            this.sideTabLabel1.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size: 14px;");

            this.sideTabLinePane1.setVisible(false);
            this.domainRequestBtn.setVisible(true);
        }
    }

    public void settingLayoutData() {
        String address = selectAddressController.getAddress().trim();
        String mask = AppManager.getInstance().getMaskWithAddress(address);
        String domain = selectDomainController.getDomain().trim();
        String maskingId = addrMaskingIDTextField.getText().trim();
        String valueApis = selectDomainController.getValueApis().trim();
        BigInteger value = selectDomainController.getValueApisToBigInt();
        BigInteger mineral = selectPayerController.getMineral();
        String payerAddress = selectPayerController.getAddress();

        totalBalance.setText(ApisUtil.readableApis(selectPayerController.getBalance(),',', true));
        totalPayerLabel.setText(payerAddress);

        gasCalculatorController.setMineral(mineral);

        this.selectedDomainLabel.setText(domain);
        this.selectDomainMsg.setText(domain+" is "+valueApis+"APIS");

        this.totalWalletAddressValue.setText(address);
        this.totalFeeAliaValue.setText(maskingId+domain);
        this.totalFeeValue.setText(valueApis+" APIS");
        this.apisLabel.setText(valueApis);

        // 도메인 체크
        if(mask != null && mask.length() > 0){
            this.registerAddressIcon.setVisible(true);
            this.registerAddressIcon.setImage(downRed);

            this.registerAddressMsg.textProperty().unbind();
            this.registerAddressMsg.textProperty().bind(StringManager.getInstance().addressMasking.registerAddressMsg2);
            this.registerAddressMsg.setTextFill(Color.web("#910000"));
        }else{
            this.registerAddressIcon.setVisible(true);
            this.registerAddressIcon.setImage(downGreen);

            this.registerAddressMsg.textProperty().unbind();
            this.registerAddressMsg.textProperty().bind(StringManager.getInstance().addressMasking.registerAddressMsg);
            this.registerAddressMsg.setTextFill(Color.web("#36b25b"));
        }

        if(maskingId != null && maskingId.length() > 0){

            String addressUsed = AppManager.getInstance().getAddressWithMask(maskingId+domain);
            if(addressUsed != null){
                // used
                this.idIcon.setVisible(true);
                this.idIcon.setImage(downRed);
                this.idMsg.setVisible(true);
                this.idMsg.setTextFill(Color.web("#910000"));
                this.idMsg.setText(maskingId+domain+" is already in use.");
                this.hintMessageLabel.setVisible(true);
                this.hintMessageLabel.setPrefHeight(-1);

                this.idIcon2.setVisible(true);
                this.idMsg2.setVisible(true);
                this.idMsg2.setText(address);
                this.hintAddressLabel.setVisible(true);
                this.hintAddressLabel.setPrefHeight(-1);
            }else{
                // not used
                this.idIcon.setVisible(true);
                this.idIcon.setImage(downGreen);
                this.idMsg.setVisible(true);
                this.idMsg.setTextFill(Color.web("#36b25b"));
                this.idMsg.setText(maskingId+domain+" is available");
                this.hintMessageLabel.setVisible(true);
                this.hintMessageLabel.setPrefHeight(-1);

                this.idIcon2.setVisible(false);
                this.idMsg2.setVisible(false);
                this.idMsg2.setText("");
                this.hintAddressLabel.setVisible(false);
                this.hintAddressLabel.setPrefHeight(0);
            }
        }else{
            this.idIcon.setVisible(false);
            this.idMsg.setVisible(false);
            this.hintMessageLabel.setVisible(false);
            this.hintMessageLabel.setPrefHeight(0);
            this.idIcon2.setVisible(false);
            this.idMsg2.setVisible(false);
            this.idMsg2.setText("");
            this.hintAddressLabel.setVisible(false);
            this.hintAddressLabel.setPrefHeight(0);
        }

        // pay버튼 색상 변경
        if(maskingId.length() <= 0){
            btnPay.setStyle(new JavaFXStyle(btnPay.getStyle()).add("-fx-background-color","#d8d8d8").toString());
        }else{
            btnPay.setStyle(new JavaFXStyle(btnPay.getStyle()).add("-fx-background-color","#910000").toString());
        }

    }

    public void domainDragDropMouseEntered() {
        this.domainDragDrop.setImage(domainDragDropColor);
    }

    public void domainDragDropMouseExited() {
        this.domainDragDrop.setImage(domainDragDropGrey);
    }

    public void domainRequestMouseClicked(){
        String domain = this.publicDomainTextField.getText().trim();
        String message = this.publicTextArea.getText().trim();
        String email = this.emailTextField.getText().trim();

        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("domain" , domain);
            params.put("message" , message);
            params.put("email" , email);

            String response = HttpRequestManager.sendRequestPublicDomain(params);
            System.out.println("response > \n" + response);

        }catch (MalformedURLException e){
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 완료 팝업
        PopupManager.getInstance().showMainPopup("popup_success.fxml",1);

        // 신청 후 보낸 데이터를 확인하기 위해 초기화 하지 않음.
//        this.publicDomainTextField.setText("");
//        this.publicTextArea.setText("");
//        this.emailTextField.setText("");
    }

    public void update() {
        selectAddressController.update();
        selectDomainController.update();
        selectPayerController.update();
        settingLayoutData();
    }
}
