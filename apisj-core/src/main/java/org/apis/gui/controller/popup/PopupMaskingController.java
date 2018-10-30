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
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.core.Transaction;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.module.AddressLabelController;
import org.apis.gui.controller.module.ApisSelectBoxController;
import org.apis.gui.controller.module.GasCalculatorMiniController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.HttpRequestManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
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
    private byte[] contractAddress = Hex.decode("1000000000000000000000000000000000037449");
    private CallTransaction.Contract contract = new CallTransaction.Contract(abi);
    private CallTransaction.Function setterFunction = contract.getByName("registerMask");
    private int cusorTabIndex = 0;
    private int cusorStepIndex = 0;

    private Image tab1On, tab1Off, tab2On, tab2Off;
    private Image introNavi,introNaviCircle;
    private Image downGreen = new Image("image/ic_check_green@2x.png");
    private Image downRed = new Image("image/ic_error_red@2x.png");

    @FXML private Pane tab1Line, tab2Line;
    @FXML private ImageView tab1Icon, tab2Icon;
    @FXML private Label tab1Label, tab2Label, warningLabel, totalBalance;
    @FXML private TabPane tabPane;
    @FXML private ImageView introNaviOne, introNaviTwo, introNaviThree, introNaviFour, addressMsgIcon;
    @FXML private TextField commercialDomainTextField, emailTextField, registerMaskingIdTextField;
    @FXML private TextArea commercialDomainMessage;
    @FXML private Label
            titleLabel, addressLabel, addressMsgLabel,
            domainLabel, domainMsgLabel,
            idLabel,
            walletAddressLabel, aliasLabel, totalFeeLabel, payerLabel, payMsg1, payMsg2,
            tab5TitleLabel, tab5SubTitleLabel, tab7TitleLabel, tab7SubTitleLabel, tabComercialDomain1, tabPublicDomain1, tabComercialDomain2, tabPublicDomain2,
            cDomainMsg1, cDomainMsg2, cDomainMsg3, cDomainMsg4,
            pDomainMsg1, pDomainMsg2, pDomainMsg3, pDomainMsg4,
            cDomainLabel,
            pDomainLabel, purposeDomainLabel, selectDomainLabel,
            backBtn1, backBtn2, backBtn3, backBtn4, backBtn6, backBtn8, nextBtn1, nextBtn2, nextBtn3, payBtn, suggestingBtn, requestBtn,
            maskId, maskValue, timeLabel
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

        idLabel.textProperty().bind(StringManager.getInstance().popup.maskingId);

        walletAddressLabel.textProperty().bind(StringManager.getInstance().popup.maskingWalletAddress);
        aliasLabel.textProperty().bind(StringManager.getInstance().popup.maskingAlias);
        totalFeeLabel.textProperty().bind(StringManager.getInstance().popup.maskingTotalFee);
        payerLabel.textProperty().bind(StringManager.getInstance().popup.maskingPayer);
        payMsg1.textProperty().bind(StringManager.getInstance().popup.maskingPayMsg1);
        payMsg2.textProperty().bind(StringManager.getInstance().popup.maskingPayMsg2);

        backBtn1.textProperty().bind(StringManager.getInstance().common.backButton);
        backBtn2.textProperty().bind(StringManager.getInstance().common.backButton);
        backBtn3.textProperty().bind(StringManager.getInstance().common.backButton);
        backBtn4.textProperty().bind(StringManager.getInstance().common.backButton);
        backBtn6.textProperty().bind(StringManager.getInstance().common.backButton);
        backBtn8.textProperty().bind(StringManager.getInstance().common.backButton);
        nextBtn1.textProperty().bind(StringManager.getInstance().common.nextButton);
        nextBtn2.textProperty().bind(StringManager.getInstance().common.nextButton);
        nextBtn3.textProperty().bind(StringManager.getInstance().common.nextButton);
        payBtn.textProperty().bind(StringManager.getInstance().common.payButton);

        tab5TitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterDomain);
        tab5SubTitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingRegisterDomainMsg);
        tab7TitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingTabRegisterDomain);
        tab7SubTitleLabel.textProperty().bind(StringManager.getInstance().popup.maskingRegisterDomainMsg);
        tabComercialDomain1.textProperty().bind(StringManager.getInstance().popup.maskingCommercialDomain);
        tabPublicDomain1.textProperty().bind(StringManager.getInstance().popup.maskingPublicDomain);
        tabComercialDomain2.textProperty().bind(StringManager.getInstance().popup.maskingCommercialDomain);
        tabPublicDomain2.textProperty().bind(StringManager.getInstance().popup.maskingPublicDomain);
        cDomainMsg1.textProperty().bind(StringManager.getInstance().popup.maskingCommercialDomainMsg1);
        cDomainMsg2.textProperty().bind(StringManager.getInstance().popup.maskingCommercialDomainMsg2);
        cDomainMsg3.textProperty().bind(StringManager.getInstance().popup.maskingCommercialDomainMsg3);
        cDomainMsg4.textProperty().bind(StringManager.getInstance().popup.maskingCommercialDomainMsg4);
        pDomainMsg1.textProperty().bind(StringManager.getInstance().popup.maskingPublicDomainMsg1);
        pDomainMsg2.textProperty().bind(StringManager.getInstance().popup.maskingPublicDomainMsg2);
        pDomainMsg3.textProperty().bind(StringManager.getInstance().popup.maskingPublicDomainMsg3);
        pDomainMsg4.textProperty().bind(StringManager.getInstance().popup.maskingPublicDomainMsg4);

        cDomainLabel.textProperty().bind(StringManager.getInstance().popup.maskingRequestCommercialDomain2);
        pDomainLabel.textProperty().bind(StringManager.getInstance().popup.maskingPublicRequestDomain2);
        purposeDomainLabel.textProperty().bind(StringManager.getInstance().popup.maskingPublicRequestPurposeDomain);

        suggestingBtn.textProperty().bind(StringManager.getInstance().common.suggestingButton);
        requestBtn.textProperty().bind(StringManager.getInstance().common.requestButton);

        warningLabel.setVisible(false);
    }
    public void settingLayoutData(){

        // step 1. 변경하려는 지갑주소 선택
        String address = selectAddressController.getAddress();
        BigInteger balance = selectPayerController.getBalance();
        BigInteger mineral = selectPayerController.getMineral();
        String payerAddress = selectPayerController.getAddress();
        this.totalPayerLabelController.setAddress(payerAddress);
        this.totalPayerLabelController.setTooltip(AppManager.getInstance().getMaskWithAddress(payerAddress));

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
        BigInteger value = selectDomainController.getValueApisToBigInt();
        setDomainMsgState(domain, apis);

        // step 3. 아이디 작성
        String maskingId = registerMaskingIdTextField.getText();
        Object[] args = new Object[3];
        args[0] = Hex.decode(address);   //_faceAddress
        args[1] = maskingId;   //_name
        args[2] = new BigInteger(selectDomainController.getDomainId());   //_domainId
        long checkGas = AppManager.getInstance().getPreGasUsed(abi, Hex.decode(address), contractAddress, value, setterFunction.name, args);
        String preGasUsed = Long.toString(checkGas);
        if(checkGas < 0){
            preGasUsed = "0";
            warningLabel.setVisible(true);
        }else{
            warningLabel.setVisible(false);
        }
        totalBalance.setText(ApisUtil.readableApis(balance.toString(),',',ApisUtil.Unit.aAPIS, true));
        gasCalculatorMiniController.setMineral(mineral);
        gasCalculatorMiniController.setGasLimit(preGasUsed);


        selectWalletAddressController.setAddress(address);
        selectWalletAddressController.setTooltip(null);
        selectDomainLabel.setText(domain);
        maskId.setText(maskingId+domain);
        maskValue.setText(apis+"APIS");

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, YYYY HH:mm");
        int utc = TimeZone.getDefault().getRawOffset()/1000/3600;
        this.timeLabel.textProperty().setValue(dateFormat.format(new Date()).toUpperCase()+"(UTC+"+utc+")");
    }

    public void setSelectedTab(int index){
        this.cusorTabIndex = index;

        tab1Line.setVisible(false);
        tab1Icon.setImage(tab1Off);
        tab1Label.setStyle("-fx-font-family: 'Open Sans Regular'; -fx-font-size:12px; ");
        tab1Label.setTextFill(Color.web("#999999"));

        tab2Line.setVisible(false);
        tab2Icon.setImage(tab2Off);
        tab2Label.setStyle("-fx-font-family: 'Open Sans Regular'; -fx-font-size:12px; ");
        tab2Label.setTextFill(Color.web("#999999"));

        if(index == 0){
            tab1Icon.setImage(tab1On);
            tab1Line.setVisible(true);
            tab1Label.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px; ");
            tab1Label.setTextFill(Color.web("#910000"));

            introNaviOne.setVisible(true);
            introNaviTwo.setVisible(true);
            introNaviThree.setVisible(true);
            introNaviFour.setVisible(true);

        }else if(index == 1){
            tab2Icon.setImage(tab2On);
            tab2Line.setVisible(true);
            tab2Label.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px; ");
            tab2Label.setTextFill(Color.web("#910000"));

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
        }else if(id.indexOf("nextBtn") >= 0){
            setStep(this.cusorStepIndex+1);
        }else if(id.equals("suggestingBtn")){
            PopupManager.getInstance().showMainPopup("popup_email_address.fxml", 1);
        }else if(id.equals("requestBtn")){

            String domain = commercialDomainTextField.getText().trim();
            String message = commercialDomainMessage.getText().trim();
            String email = emailTextField.getText().trim();

            try {
                String response = HttpRequestManager.sendRequestPublicDomain(domain, message, email);
            }catch (MalformedURLException e){
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            PopupManager.getInstance().showMainPopup("popup_success.fxml", 1);
        }else if(id.equals("subTab1")){
            setSelectedTab(1);
            setStep(0);
        }else if(id.equals("subTab2")){
            setSelectedTab(1);
            setStep(2);
        }else if(id.equals("payBtn")){

            String faceAddress = selectAddressController.getAddress().trim();
            String name = registerMaskingIdTextField.getText().trim();
            String domainId = selectDomainController.getDomainId().trim();

            String address = selectPayerController.getAddress().trim();
            BigInteger value = selectDomainController.getValueApisToBigInt();
            String gasLimit = gasCalculatorMiniController.getGasLimit().toString().trim();
            String gasPrice = gasCalculatorMiniController.getGasPrice().toString().trim();

            Object[] args = new Object[3];
            args[0] = Hex.decode(faceAddress);   //_faceAddress
            args[1] = name;   //_name
            args[2] = new BigInteger(domainId);   //_domainId

            byte[] functionCallBytes = setterFunction.encode(args);

            // 완료 팝업 띄우기
            PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup("popup_contract_warning.fxml", 1);
            controller.setData(address, value.toString(), gasPrice, gasLimit, contractAddress, functionCallBytes);
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

        tab1On = new Image("image/ic_registeralias_red@2x.png");
        tab1Off = new Image("image/ic_registeralias_grey@2x.png");
        tab2On = new Image("image/ic_registeralias_red@2x.png");
        tab2Off = new Image("image/ic_registeralias_grey@2x.png");
        introNavi = new Image("image/ic_nav@2x.png");
        introNaviCircle = new Image("image/ic_nav_circle@2x.png");

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

        registerMaskingIdTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(newValue.length() > 64){
                    registerMaskingIdTextField.setText(oldValue);
                }
                settingLayoutData();
            }
        });

        // Tab Pane Direction Key Block
        tabPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.LEFT
                    || event.getCode() == KeyCode.RIGHT
                    || event.getCode() == KeyCode.UP
                    || event.getCode() == KeyCode.DOWN) {
                if(tabPane.isFocused()){
                    event.consume();
                }else{
                }
            }
        });

        this.commercialDomainTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // Focus in Function
                if(newValue) {
                    commercialDomainTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                }
                // Focus out Function
                else {
                    commercialDomainTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                }
            }
        });

        selectWalletAddressController.setHandler(new AddressLabelController.AddressLabelImpl() {
            @Override
            public void onMouseClicked(String address) {
                AppManager.copyClipboard(address);
                PopupCopyTxHashController controller = (PopupCopyTxHashController)PopupManager.getInstance().showMainPopup("popup_copy_tx_hash.fxml",zIndex+1);
                controller.setHash(address);
            }
        });

        totalPayerLabelController.setHandler(new AddressLabelController.AddressLabelImpl() {
            @Override
            public void onMouseClicked(String address) {
                AppManager.copyClipboard(address);
                PopupCopyTxHashController controller = (PopupCopyTxHashController)PopupManager.getInstance().showMainPopup("popup_copy_tx_hash.fxml",zIndex+1);
                controller.setHash(address);
            }
        });

        setSelectedTab(0);
        setStep(0);
    }

    public void setSelectAddress(String address){
        selectAddressController.selectedItemWithAddress(address);
        settingLayoutData();
    }
    public void setSelectWalletId(String id) {
        selectAddressController.selectedItemWithWalletId(id);
        settingLayoutData();
    }

    private void setAddressState(boolean isAvailable){
        addressMsgLabel.textProperty().unbind();
        if(isAvailable){
            addressMsgLabel.textProperty().bind(StringManager.getInstance().popup.maskingAliasAddressMsg);
            this.addressMsgLabel.setTextFill(Color.web("#36b25b"));
            this.addressMsgIcon.setImage(downGreen);
            this.nextBtn1.setStyle(new JavaFXStyle(nextBtn1.getStyle()).add("-fx-background-color","#910000").toString());
            this.nextBtn1.setDisable(false);
        }else{
            addressMsgLabel.textProperty().bind(StringManager.getInstance().popup.maskingAliasAddressMsg2);
            this.addressMsgLabel.setTextFill(Color.web("#910000"));
            this.addressMsgIcon.setImage(downRed);
            this.nextBtn1.setStyle(new JavaFXStyle(nextBtn1.getStyle()).add("-fx-background-color","#d8d8d8").toString());
            this.nextBtn1.setDisable(true);
        }
    }

    private void setDomainMsgState(String domain, String apis){
        domainMsgLabel.setText(domain + " is "+apis+"APIS");
    }
}
