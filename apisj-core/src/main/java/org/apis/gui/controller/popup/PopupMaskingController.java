package org.apis.gui.controller.popup;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.core.Transaction;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.module.AddressLabelController;
import org.apis.gui.controller.module.selectbox.ApisSelectBoxController;
import org.apis.gui.controller.module.GasCalculatorMiniController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.*;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.ApisUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.TimeZone;

public class PopupMaskingController extends BasePopupController {
    private String abi =  ContractLoader.readABI(ContractLoader.CONTRACT_ADDRESS_MASKING);
    private byte[] contractAddress = AppManager.getInstance().constants.getADDRESS_MASKING_ADDRESS();
    private CallTransaction.Contract contract = new CallTransaction.Contract(abi);
    private CallTransaction.Function setterFunction = contract.getByName("registerMask");
    private int cusorTabIndex = 0;
    private int cusorStepIndex = 0;

    private Image tab1On, tab1Off, tab2On, tab2Off;
    private Image introNavi,introNaviCircle;
    private Image checkGreen = ImageManager.icCheckGreen;;
    private Image errorRed = ImageManager.icErrorRed;

    @FXML private GridPane hintMessageLabel, hintAddressLabel;
    @FXML private AnchorPane rootPane;
    @FXML private Pane tab1Line, tab2Line;
    @FXML private ImageView tab1Icon, tab2Icon, idIcon;
    @FXML private Label tab1Label, tab2Label, totalBalance, idMsg, idMsg2, requestNextBtn, emailAddrLabel;
    @FXML private TabPane tabPane;
    @FXML private ImageView introNaviOne, introNaviTwo, introNaviThree, introNaviFour, addressMsgIcon;
    @FXML private TextField commercialDomainTextField, emailTextField, registerMaskingIdTextField;
    @FXML private TextArea commercialDomainMessage;
    @FXML private Label
            titleLabel, addressLabel, addressMsgLabel,
            domainLabel, domainMsgLabel,
            idLabel,
            walletAddressLabel, aliasLabel, totalFeeLabel, payerLabel,reCentPayerLabel, payMsg1, payMsg2,
            tab5TitleLabel, tab5SubTitleLabel,
            pDomainLabel, purposeDomainLabel, selectDomainLabel,
            backBtn1, backBtn2, backBtn3, backBtn4, backBtn8, nextBtn1, nextBtn2, nextBtn3, payBtn, requestBtn,
            maskId, maskValue, timeLabel, errorLabel
    ;

    @FXML private ApisSelectBoxController selectAddressController, selectDomainController, selectPayerController;
    @FXML private GasCalculatorMiniController gasCalculatorMiniController;
    @FXML private AddressLabelController selectWalletAddressController, totalPayerLabelController;

    public void languageSetting() {
        titleLabel.textProperty().bind(StringManager.getInstance().popup.maskingTitle);
        addressLabel.textProperty().bind(StringManager.getInstance().popup.maskingAddress);
        addressMsgLabel.textProperty().bind(StringManager.getInstance().popup.maskingAliasAddressMsg);
        tab1Label.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterMask);
        tab2Label.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterDomain);

        domainLabel.textProperty().bind(StringManager.getInstance().popup.maskingDomain);
        errorLabel.textProperty().bind(StringManager.getInstance().common.notEnoughBalance);

        idLabel.textProperty().bind(StringManager.getInstance().popup.maskingId);

        walletAddressLabel.textProperty().bind(StringManager.getInstance().popup.maskingWalletAddress);
        aliasLabel.textProperty().bind(StringManager.getInstance().receipt.mask);
        totalFeeLabel.textProperty().bind(StringManager.getInstance().popup.maskingTotalFee);
        payerLabel.textProperty().bind(StringManager.getInstance().popup.maskingPayer);
        reCentPayerLabel.textProperty().bind(StringManager.getInstance().popup.maskingPayer);
        payMsg1.textProperty().bind(StringManager.getInstance().popup.maskingPayMsg1);
        payMsg2.textProperty().bind(StringManager.getInstance().popup.maskingPayMsg2);
        registerMaskingIdTextField.promptTextProperty().bind(StringManager.getInstance().addressMasking.registerAddressMsg4);

        backBtn1.textProperty().bind(StringManager.getInstance().common.closeButton);
        backBtn2.textProperty().bind(StringManager.getInstance().common.prevButton);
        backBtn3.textProperty().bind(StringManager.getInstance().common.prevButton);
        backBtn4.textProperty().bind(StringManager.getInstance().common.prevButton);
        backBtn8.textProperty().bind(StringManager.getInstance().common.prevButton);
        nextBtn1.textProperty().bind(StringManager.getInstance().common.nextButton);
        nextBtn2.textProperty().bind(StringManager.getInstance().common.nextButton);
        nextBtn3.textProperty().bind(StringManager.getInstance().common.nextButton);
        payBtn.textProperty().bind(StringManager.getInstance().common.payButton);
        requestNextBtn.textProperty().bind(StringManager.getInstance().common.requestButton);

        tab5TitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterDomain);
        tab5SubTitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingRegisterDomainMsg);

        pDomainLabel.textProperty().bind(StringManager.getInstance().popup.maskingPublicRequestDomain2);
        purposeDomainLabel.textProperty().bind(StringManager.getInstance().popup.maskingPublicRequestPurposeDomain);

        requestBtn.textProperty().bind(StringManager.getInstance().common.requestButton);
        emailAddrLabel.textProperty().bind(StringManager.getInstance().addressMasking.emailAddrLabel);

        StyleManager.fontStyle(addressLabel, StyleManager.Standard.SemiBold12);

    }

    public void settingLayoutData(){

        // step 1. 변경하려는 지갑주소 선택
        String address = selectAddressController.getAddress();
        BigInteger balance = selectPayerController.getBalance();
        BigInteger mineral = selectPayerController.getMineral();
        String payerAddress = selectPayerController.getAddress();

        this.gasCalculatorMiniController.setMineral(mineral);
        this.totalPayerLabelController.setAddress(payerAddress);
        this.totalPayerLabelController.setTooltip(AppManager.getInstance().getMaskWithAddress(payerAddress));
        this.totalBalance.setText(ApisUtil.readableApis(balance.toString(),',',ApisUtil.Unit.aAPIS, true));

        String mask = AppManager.getInstance().getMaskWithAddress(address);
        if(mask != null && mask.length() > 0){
            //이미존재
            setAddressState(false);
        }else{
            setAddressState(true);
        }

        // step 2. 도메인 선택
        String domain = selectDomainController.getDomain();
        String apis = selectDomainController.getValueApis();
        setDomainMsgState(domain, apis);

        // step 3. 아이디 작성
        String maskingId = registerMaskingIdTextField.getText();
        String maskingAddress = AppManager.getInstance().getAddressWithMask(maskingId+domain);

        selectWalletAddressController.setAddress(address);
        selectWalletAddressController.setTooltip(null);
        selectDomainLabel.setText(domain);
        maskId.setText(maskingId+domain);
        maskValue.setText(apis+" APIS");

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd, YYYY HH:mm");
        int utc = TimeZone.getDefault().getRawOffset()/1000/3600;
        this.timeLabel.textProperty().setValue(dateFormat.format(new Date()).toUpperCase()+"(UTC+"+utc+")");

        if(maskingId.length() == 0){
            nextBtn3.setDisable(true);
            StyleManager.backgroundColorStyle(nextBtn3, StyleManager.AColor.Cd8d8d8);
            gasCalculatorMiniController.setDisable(true);
            hintMessageLabel.setVisible(false);
            hintMessageLabel.setPrefHeight(0);
        }else{
            hintMessageLabel.setVisible(true);
            hintMessageLabel.setPrefHeight(-1);

            if(maskingId.getBytes().length > 24) {
                nextBtn3.setDisable(true);
                StyleManager.backgroundColorStyle(nextBtn3, StyleManager.AColor.Cd8d8d8);
                gasCalculatorMiniController.setDisable(true);

                idIcon.setImage(ImageManager.icErrorRed);
                idMsg.setTextFill(Color.web("#910000"));
                idMsg.setText(maskingId+domain+" "+StringManager.getInstance().addressMasking.registerAddressMsg4.get()+"("+maskingId.getBytes().length+"/24)");

                hintAddressLabel.setVisible(true);
                hintAddressLabel.setPrefHeight(-1);
                idMsg2.setText(maskingAddress);

            }else if(maskingAddress != null){
                nextBtn3.setDisable(true);
                StyleManager.backgroundColorStyle(nextBtn3, StyleManager.AColor.Cd8d8d8);
                gasCalculatorMiniController.setDisable(true);

                idIcon.setImage(ImageManager.icErrorRed);
                idMsg.setTextFill(Color.web("#910000"));
                idMsg.setText(maskingId+domain+" "+StringManager.getInstance().addressMasking.isAlreadyInUse.get());

                hintAddressLabel.setVisible(true);
                hintAddressLabel.setPrefHeight(-1);
                idMsg2.setText(maskingAddress);
            }else{
                gasCalculatorMiniController.setDisable(false);

                idIcon.setImage(ImageManager.icCheckGreen);
                idMsg.setTextFill(Color.web("#36b25b"));
                idMsg.setText(maskingId+domain+" "+StringManager.getInstance().addressMasking.isAvailable.get());

                hintAddressLabel.setVisible(false);
                hintAddressLabel.setPrefHeight(0);
            }
        }

        if(!nextBtn3.isDisable()){

            errorLabel.setVisible(false);
            errorLabel.setPrefHeight(0);

            if(balance.compareTo(BigInteger.valueOf(10)) >= 0){
                if(gasCalculatorMiniController.getTotalFee().compareTo(BigInteger.ZERO) > 0){

                    BigInteger fee = balance.subtract(gasCalculatorMiniController.getTotalFee());
                    if(fee.compareTo(BigInteger.ZERO) < 0){
                        StyleManager.backgroundColorStyle(nextBtn3, StyleManager.AColor.Cd8d8d8);
                        nextBtn3.setDisable(true);

                        errorLabel.setVisible(true);
                        errorLabel.setPrefHeight(-1);
                    }
                }
            }else{
                StyleManager.backgroundColorStyle(nextBtn3, StyleManager.AColor.Cd8d8d8);
                nextBtn3.setDisable(true);

                errorLabel.setVisible(true);
                errorLabel.setPrefHeight(-1);
            }


        }

    }

    public void setSelectedTab(int index){
        this.cusorTabIndex = index;

        tab1Line.setVisible(false);
        tab1Icon.setImage(tab1Off);
        tab1Label.setStyle("-fx-font-family: 'Noto Sans KR Regular'; -fx-font-size:12px; ");
        tab1Label.setTextFill(Color.web("#999999"));

        tab2Line.setVisible(false);
        tab2Icon.setImage(tab2Off);
        tab2Label.setStyle("-fx-font-family: 'Noto Sans KR Regular'; -fx-font-size:12px; ");
        tab2Label.setTextFill(Color.web("#999999"));

        if(index == 0){
            tab1Icon.setImage(tab1On);
            tab1Line.setVisible(true);
            tab1Label.setStyle("-fx-font-family: 'Noto Sans KR Medium'; -fx-font-size:12px; ");
            tab1Label.setTextFill(Color.web("#b01e1e"));

            introNaviOne.setVisible(true);
            introNaviTwo.setVisible(true);
            introNaviThree.setVisible(true);
            introNaviFour.setVisible(true);

        }else if(index == 1){
            tab2Icon.setImage(tab2On);
            tab2Line.setVisible(true);
            tab2Label.setStyle("-fx-font-family: 'Noto Sans KR Medium'; -fx-font-size:12px; ");
            tab2Label.setTextFill(Color.web("#b01e1e"));

            introNaviOne.setVisible(false);
            introNaviTwo.setVisible(false);
            introNaviThree.setVisible(false);
            introNaviFour.setVisible(false);
        }

        setStep(0);
    }
    public void setStep(int step){
        this.cusorStepIndex = step;

        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(this.cusorTabIndex*4 + step);

        setNavi(this.cusorStepIndex );

        if(this.cusorTabIndex*4 + step < 0){
            exit();
        }
    }

    public void setNavi(int step){
        introNaviOne.setImage(introNaviCircle);
        introNaviTwo.setImage(introNaviCircle);
        introNaviThree.setImage(introNaviCircle);
        introNaviFour.setImage(introNaviCircle);

        introNaviOne.fitWidthProperty().setValue(6);
        introNaviTwo.fitWidthProperty().setValue(6);
        introNaviThree.fitWidthProperty().setValue(6);
        introNaviFour.fitWidthProperty().setValue(6);

        if(step == 0){
            introNaviOne.setImage(introNavi);
            introNaviOne.fitWidthProperty().setValue(24);
        }else if(step == 1){
            introNaviTwo.setImage(introNavi);
            introNaviTwo.fitWidthProperty().setValue(24);
        }else if(step == 2){
            introNaviThree.setImage(introNavi);
            introNaviThree.fitWidthProperty().setValue(24);
        }else if(step == 3){
            introNaviFour.setImage(introNavi);
            introNaviFour.fitWidthProperty().setValue(24);
        }
    }


    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("tab1")){
            setSelectedTab(0);
        }else if(id.equals("tab2")){
            setSelectedTab(1);
        }else if(id.indexOf("backBtn") >= 0){
            setStep(this.cusorStepIndex-1);
            if(id.equals("backBtn3")){

            }

        }else if(id.indexOf("nextBtn") >= 0){
            setStep(this.cusorStepIndex+1);
        }else if(id.equals("requestBtn")){

            String domain = commercialDomainTextField.getText().trim();
            String message = commercialDomainMessage.getText().trim();
            String email = emailTextField.getText().trim();

            if(domain.length() > 0 && message.length() > 0  && email.length() > 0) {
                try {
                    String response = HttpRequestManager.sendRequestPublicDomain(domain, message, email);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PopupManager.getInstance().showMainPopup(rootPane, "popup_success.fxml", 1);
            }

        }else if(id.equals("subTab1")){
            setSelectedTab(1);
            setStep(0);
        }else if(id.equals("requestNextBtn")){
            AppManager.getInstance().openBrowserRegisterDomain();

        }else if(id.equals("payBtn")){

            String faceAddress = selectAddressController.getAddress().trim();
            String name = registerMaskingIdTextField.getText().trim();
            String domainId = selectDomainController.getDomainId().trim();

            String address = selectPayerController.getAddress().trim();
            BigInteger value = selectDomainController.getValueApisToBigInt();
            String gasLimit = gasCalculatorMiniController.getGasLimit().toString().trim();
            String gasPrice = gasCalculatorMiniController.getGasPrice().toString().trim();

            Object[] args = new Object[3];
            args[0] = ByteUtil.hexStringToBytes(faceAddress);   //_faceAddress
            args[1] = name;   //_name
            args[2] = new BigInteger(domainId);   //_domainId

            byte[] functionCallBytes = setterFunction.encode(args);

            // 완료 팝업 띄우기
            PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup(rootPane, "popup_contract_warning.fxml", 1);
            controller.setData(address, value.toString(), gasPrice, gasLimit, contractAddress, new byte[0], functionCallBytes);
            controller.requestFocus();
            controller.setHandler(new PopupContractWarningController.PopupContractWarningImpl() {
                @Override
                public void success(Transaction tx) {
                }
                @Override
                public void fail(Transaction tx) {
                }
            });

        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        AppManager.settingTextFieldStyle(registerMaskingIdTextField);
        AppManager.keyPressedHandler(registerMaskingIdTextField);

        tab1On = new Image("image/ic_registeralias_red@2x.png");
        tab1Off = new Image("image/ic_registeralias_grey@2x.png");
        tab2On = new Image("image/ic_registerdomain_red@2x.png");
        tab2Off = new Image("image/ic_registerdomain_grey@2x.png");
        introNavi = new Image("image/ic_nav@2x.png");
        introNaviCircle = new Image("image/ic_nav_circle@2x.png");

        selectAddressController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ALIAS, true);
        selectAddressController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onSelectItem() {
                settingLayoutData();
            }

            @Override
            public void onMouseClick() {
            }
        });

        selectDomainController.init(ApisSelectBoxController.SELECT_BOX_TYPE_DOMAIN, true);
        selectDomainController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onSelectItem() {
                settingLayoutData();
            }

            @Override
            public void onMouseClick() {
            }
        });

        selectPayerController.init(ApisSelectBoxController.SELECT_BOX_TYPE_ALIAS, true);
        selectPayerController.setHandler(new ApisSelectBoxController.ApisSelectBoxImpl() {
            @Override
            public void onSelectItem() {

                StyleManager.backgroundColorStyle(nextBtn3, StyleManager.AColor.Cd8d8d8);
                nextBtn3.setDisable(true);
                gasCalculatorMiniController.setGasLimit("0");
                settingLayoutData();
            }

            @Override
            public void onMouseClick() {
            }
        });

        registerMaskingIdTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                registerMaskingIdTextField.setText(registerMaskingIdTextField.getText().replaceAll("@",""));

                if(newValue.length() > 64){
                    registerMaskingIdTextField.setText(oldValue);
                }

                if(newValue.length() == 0){
                    nextBtn3.setDisable(true);
                    StyleManager.backgroundColorStyle(nextBtn3, StyleManager.AColor.Cd8d8d8);
                    gasCalculatorMiniController.setDisable(true);
                }else{
                    gasCalculatorMiniController.setDisable(false);
                }

                settingLayoutData();
            }
        });

        // Tab Pane Direction Key Block
        tabPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.LEFT
                    || event.getCode() == KeyCode.RIGHT
                    || event.getCode() == KeyCode.UP
                    || event.getCode() == KeyCode.DOWN
                    || (event.getCode() == KeyCode.PAGE_DOWN && event.isControlDown())
                    || (event.getCode() == KeyCode.PAGE_UP && event.isControlDown())) {
                if(tabPane.isFocused()){
                    event.consume();
                }else{
                }
            }
        });



        AppManager.settingTextFieldStyle(commercialDomainTextField);
        AppManager.keyPressedHandler(commercialDomainTextField);
        commercialDomainTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String domain = commercialDomainTextField.getText().trim();
                String message = commercialDomainMessage.getText().trim();
                String email = emailTextField.getText().trim();

                if(domain.length() > 0 && message.length() > 0  && email.length() > 0) {
                    StyleManager.backgroundColorStyle(requestBtn, StyleManager.AColor.Cb01e1e);
                }else{
                    StyleManager.backgroundColorStyle(requestBtn, StyleManager.AColor.Cd8d8d8);
                }
            }
        });
        commercialDomainMessage.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String domain = commercialDomainTextField.getText().trim();
                String message = commercialDomainMessage.getText().trim();
                String email = emailTextField.getText().trim();

                if(domain.length() > 0 && message.length() > 0  && email.length() > 0) {
                    StyleManager.backgroundColorStyle(requestBtn, StyleManager.AColor.Cb01e1e);
                }else{
                    StyleManager.backgroundColorStyle(requestBtn, StyleManager.AColor.Cd8d8d8);
                }
            }
        });
        AppManager.keyPressedHandler(commercialDomainMessage);
        emailTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String domain = commercialDomainTextField.getText().trim();
                String message = commercialDomainMessage.getText().trim();
                String email = emailTextField.getText().trim();

                if(domain.length() > 0 && message.length() > 0  && email.length() > 0) {
                    StyleManager.backgroundColorStyle(requestBtn, StyleManager.AColor.Cb01e1e);
                }else{
                    StyleManager.backgroundColorStyle(requestBtn, StyleManager.AColor.Cd8d8d8);
                }
            }
        });
        AppManager.keyPressedHandler(emailTextField);

        selectWalletAddressController.setHandler(new AddressLabelController.AddressLabelImpl() {
            @Override
            public void onMouseClicked(String address) {
                AppManager.copyClipboard(address);
                PopupCopyController controller = (PopupCopyController)PopupManager.getInstance().showMainPopup(rootPane, "popup_copy.fxml", 0);
                controller.setCopyWalletAddress(address);
            }
        });

        totalPayerLabelController.setHandler(new AddressLabelController.AddressLabelImpl() {
            @Override
            public void onMouseClicked(String address) {
                PopupCopyController controller = (PopupCopyController)PopupManager.getInstance().showMainPopup(rootPane, "popup_copy.fxml", 0);
                controller.setCopyWalletAddress(address);
            }
        });

        gasCalculatorMiniController.setHandler(new GasCalculatorMiniController.GasCalculatorImpl() {
            @Override
            public void gasLimitTextFieldFocus(boolean isFocused) {

            }

            @Override
            public void gasLimitTextFieldChangeValue(String oldValue, String newValue) {

            }

            @Override
            public void gasPriceSliderChangeValue(int value) {

            }

            @Override
            public void changeGasPricePopup(boolean isVisible) {

            }

            @Override
            public void clickPreGasUsed() {
                nextBtn3.setDisable(false);
                StyleManager.backgroundColorStyle(nextBtn3, StyleManager.AColor.Cb01e1e);
                estimateGasLimit();
                settingLayoutData();
            }
        });

        setSelectedTab(0);
        setStep(0);
    }

    public void onMouseClickedShowCommercial(){
        PopupManager.getInstance().showMainPopup(rootPane, "popup_register_commercial_domain.fxml", zIndex + 1);
    }

    public void setSelectAddress(String address){
        selectAddressController.selectedItemWithAddress(address);
        settingLayoutData();
    }
    public void setSelectWalletId(String id) {
        selectAddressController.selectedItemWithWalletAddress(id);
        settingLayoutData();
    }

    private void setAddressState(boolean isAvailable){
        addressMsgLabel.textProperty().unbind();
        if(isAvailable){
            addressMsgLabel.textProperty().bind(StringManager.getInstance().popup.maskingAliasAddressMsg);
            this.addressMsgLabel.setTextFill(Color.web("#36b25b"));
            this.addressMsgIcon.setImage(checkGreen);
            this.nextBtn1.setStyle(new JavaFXStyle(nextBtn1.getStyle()).add("-fx-background-color","#b01e1e").toString());
            this.nextBtn1.setDisable(false);
        }else{
            addressMsgLabel.textProperty().bind(StringManager.getInstance().popup.maskingAliasAddressMsg2);
            this.addressMsgLabel.setTextFill(Color.web("#b01e1e"));
            this.addressMsgIcon.setImage(errorRed);
            this.nextBtn1.setStyle(new JavaFXStyle(nextBtn1.getStyle()).add("-fx-background-color","#d8d8d8").toString());
            this.nextBtn1.setDisable(true);
        }
    }

    private void setDomainMsgState(String domain, String apis){
        domainMsgLabel.setText(domain + " "+StringManager.getInstance().addressMasking.domainMsg2.get()+" "+apis+"APIS"+" "+StringManager.getInstance().addressMasking.domainMsg4.get());
    }

    private void estimateGasLimit(){
        String address = selectAddressController.getAddress();
        String maskingId = registerMaskingIdTextField.getText();
        BigInteger balance = selectPayerController.getBalance();
        BigInteger value = selectDomainController.getValueApisToBigInt();

        Object[] args = new Object[3];
        args[0] = ByteUtil.hexStringToBytes(address);   //_faceAddress
        args[1] = maskingId;   //_name
        args[2] = new BigInteger(selectDomainController.getDomainId());   //_domainId

        long checkGas = AppManager.getInstance().getPreGasUsed(abi, ByteUtil.hexStringToBytes(address), contractAddress, value, setterFunction.name, args);
        String preGasUsed = Long.toString(checkGas);
        if(checkGas < 0){
            preGasUsed = "0";
        }
        gasCalculatorMiniController.setGasLimit(preGasUsed);
    }
}
