package org.apis.gui.controller.popup;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.core.Transaction;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.controller.module.*;
import org.apis.gui.manager.*;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;
import org.apis.util.AddressUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.blockchain.ApisUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.TimeZone;

public class PopupProofOfKnowledgeRegisterController extends BasePopupController {
    private String abi =  ContractLoader.readABI(ContractLoader.CONTRACT_PROOF_OF_KNOWLEDGE);
    private byte[] contractAddress = AppManager.getInstance().constants.getPROOF_OF_KNOWLEDGE();
    private CallTransaction.Contract contract = new CallTransaction.Contract(abi);
    private CallTransaction.Function functionRegisterProofKey = contract.getByName("registerProofKey");
    private int cusorTabIndex = 0;
    private int cusorStepIndex = 0;
    private boolean isCheckedPreGasUsed = false;

    private Image introNavi,introNaviCircle;
    private Image checkGreen = ImageManager.icCheckGreen;;
    private Image errorRed = ImageManager.icErrorRed;
    private WalletItemModel model;

    @FXML private AnchorPane rootPane;
    @FXML private TabPane tabPane;
    @FXML private ImageView introNaviOne, introNaviTwo, addressIcon;
    @FXML private Label address, title, subTitle, selectedAddressLabel, timeLabel, passwordLabel,transferAmountLabel,detailFeeLabel,withdrawalLabel,afterBalanceLabel,payMsg1,payMsg2 ;
    @FXML private ApisTextFieldController newFieldController, reFieldController;
    @FXML private Label backBtn1, backBtn2, nextBtn, payBtn, transferAmount, detailFee, withdrawal, afterBalance;
    @FXML private Label balance, titleLabel, errorLabel;

    @FXML private GasCalculatorMiniController gasCalculatorMiniController;

    private ApisTextFieldGroup apisTextFieldGroup = new ApisTextFieldGroup();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        // set a clip to apply rounded border to the original image.
        Rectangle clip = new Rectangle( this.addressIcon.getFitWidth()-0.5, this.addressIcon.getFitHeight()-0.5 );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        addressIcon.setClip(clip);

        introNavi = new Image("image/ic_nav@2x.png");
        introNaviCircle = new Image("image/ic_nav_circle@2x.png");

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
        newFieldController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                settingLayoutData();

                if (newFieldController.getCheckBtnEnteredFlag()) {
                    newFieldController.setText("");
                }

                String password = newFieldController.getText();

                if(password == null || password.equals("")) {
                    newFieldController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else if(!reFieldController.getText().equals("") && !password.equals(reFieldController.getText())) {
                    newFieldController.failedForm(StringManager.getInstance().common.walletPasswordNotMatch.get());
                } else {
                    newFieldController.succeededForm();
                }
            }

            @Override
            public void change(String old_text, String new_text) {
                settingLayoutData();
            }

            @Override
            public void onAction() {
                settingLayoutData();
                reFieldController.requestFocus();
            }

            @Override
            public void onKeyTab(){
                reFieldController.requestFocus();
            }
        });
        reFieldController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                settingLayoutData();

                if (reFieldController.getCheckBtnEnteredFlag()) {
                    reFieldController.setText("");
                }

                String password = reFieldController.getText();

                if(password == null || password.equals("")) {
                    reFieldController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
                } else if(!newFieldController.getText().equals("") && !password.equals(newFieldController.getText())) {
                    reFieldController.failedForm(StringManager.getInstance().common.walletPasswordNotMatch.get());
                } else {
                    reFieldController.succeededForm();
                }
            }

            @Override
            public void change(String old_text, String new_text) {
                settingLayoutData();
            }

            @Override
            public void onAction() {
                settingLayoutData();
                preGasUsed();
            }

            @Override
            public void onKeyTab(){
                newFieldController.requestFocus();
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
                preGasUsed();
            }
        });


        settingLayoutData();

        setStep(0);

        apisTextFieldGroup.add(newFieldController);
        apisTextFieldGroup.add(reFieldController);
    }

    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().proofKey.title);
        subTitle.textProperty().bind(StringManager.getInstance().proofKey.registerSubTitle);
        selectedAddressLabel.textProperty().bind(StringManager.getInstance().proofKey.selectedAddressLabel);
        passwordLabel.textProperty().bind(StringManager.getInstance().proofKey.password);
        payMsg1.textProperty().bind(StringManager.getInstance().proofKey.payMsg1);
        payMsg2.textProperty().bind(StringManager.getInstance().proofKey.payMsg2);
        titleLabel.textProperty().bind(StringManager.getInstance().proofKey.total);

        transferAmountLabel.textProperty().bind(StringManager.getInstance().common.transferAmount);
        detailFeeLabel.textProperty().bind(StringManager.getInstance().common.transferDetailFee);
        withdrawalLabel.textProperty().bind(StringManager.getInstance().common.withdrawal);
        afterBalanceLabel.textProperty().bind(StringManager.getInstance().common.afterBalance);
        errorLabel.textProperty().bind(StringManager.getInstance().common.notEnoughBalance);

        backBtn1.textProperty().bind(StringManager.getInstance().common.closeButton);
        backBtn2.textProperty().bind(StringManager.getInstance().common.prevButton);
        nextBtn.textProperty().bind(StringManager.getInstance().common.nextButton);
        payBtn.textProperty().bind(StringManager.getInstance().common.payButton);

        newFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.newPassword.get(), ApisTextFieldController.THEME_TYPE_MAIN, OnScreenKeyboardController.CARET_INTRO);
        reFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.confrimPassword.get(), ApisTextFieldController.THEME_TYPE_MAIN, OnScreenKeyboardController.CARET_INTRO);
    }

    private void preGasUsed(){
        byte[] sender = Hex.decode(model.getAddress());
        BigInteger value = BigInteger.ZERO;
        String functionName = functionRegisterProofKey.name;
        Object[] args = new Object[1];
        args[0] = AppManager.getInstance().getKnowledgeKey(newFieldController.getText().trim());

        long gasLimit = AppManager.getInstance().getPreGasUsed(abi, sender, contractAddress, value, functionName, args);
        gasCalculatorMiniController.setGasLimit(Long.toString(gasLimit));



        isCheckedPreGasUsed = true;
        errorLabel.setVisible(false);
        errorLabel.setPrefHeight(0);

        // 잔액 여부
        BigInteger totalFee = gasCalculatorMiniController.getTotalFee();
        BigInteger balace = this.model.getApis();
        if(totalFee.compareTo(BigInteger.ZERO) > 0){
            if(totalFee.compareTo(balace) > 0){
                isCheckedPreGasUsed = false;
                errorLabel.setVisible(true);
                errorLabel.setPrefHeight(-1);
            }
        }

        settingLayoutData();
    }

    public void settingLayoutData(){
        if(model != null) {

            gasCalculatorMiniController.setMineral(model.getMineral());
            BigInteger value = BigInteger.ZERO;
            BigInteger fee = gasCalculatorMiniController.getTotalFee();
            if(fee.compareTo(BigInteger.ZERO) < 0){
                fee = BigInteger.ZERO;
            }
            BigInteger totalValue = value.add(fee);
            BigInteger after = model.getApis().subtract(totalValue);

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, YYYY HH:mm");
            int utc = TimeZone.getDefault().getRawOffset() / 1000 / 3600;
            this.timeLabel.textProperty().setValue(dateFormat.format(new Date()).toUpperCase() + "(UTC+" + utc + ")");

            transferAmount.setText(ApisUtil.readableApis(value, ',', true) + " APIS");
            detailFee.setText(ApisUtil.readableApis(fee, ',', true) + " APIS");
            withdrawal.setText(ApisUtil.readableApis(totalValue, ',', true) + " APIS");
            afterBalance.setText(ApisUtil.readableApis(after, ',', true) + " APIS");

        }
        isNextStep();
    }

    @Override
    public void setModel(BaseModel model) {
        this.model = (WalletItemModel)model;

        this.addressIcon.setImage(ImageManager.getIdenticons(this.model.getAddress()));
        this.address.setText(this.model.getAddress());
        this.balance.setText(ApisUtil.readableApis(this.model.getApis(), ',', true));
        gasCalculatorMiniController.setMineral(this.model.getMineral());

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

        introNaviOne.fitWidthProperty().setValue(6);
        introNaviTwo.fitWidthProperty().setValue(6);

        if(step == 0){
            introNaviOne.setImage(introNavi);
            introNaviOne.fitWidthProperty().setValue(24);
        }else if(step == 1){
            introNaviTwo.setImage(introNavi);
            introNaviTwo.fitWidthProperty().setValue(24);
        }
    }


    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.indexOf("backBtn") >= 0){
            setStep(this.cusorStepIndex-1);
        }else if(id.indexOf("nextBtn") >= 0){

            if(!isNextStep()){
                return ;
            }

            setStep(this.cusorStepIndex+1);
        }

    }

    @FXML
    public void onMouseReleased(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("payBtn")){

            String address = this.model.getAddress();
            byte[] proofKey = AppManager.getInstance().getKnowledgeKey(this.newFieldController.getText());
            BigInteger value = BigInteger.ZERO;
            BigInteger gasPrice = this.gasCalculatorMiniController.getGasPrice();
            BigInteger gasLimit = this.gasCalculatorMiniController.getGasLimit();

            Object[] args = new Object[1];
            args[0] = proofKey;
            byte[] functionCallBytes = functionRegisterProofKey.encode(args);
            PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup(rootPane, "popup_contract_warning.fxml", 1);
            controller.setData(address, value.toString(), gasPrice.toString(), gasLimit.toString(), contractAddress, functionCallBytes);
            controller.setHandler(new PopupContractWarningController.PopupContractWarningImpl() {
                @Override
                public void success(Transaction tx) {

                }

                @Override
                public void fail(Transaction tx) {

                }
            });
            controller.getPasswordController().requestFocus();
            controller.requestFocus();

        }
    }

    public boolean isNextStep(){

        byte[] password = newFieldController.getText().getBytes(Charset.forName("UTF-8"));
        byte[] rePassword = reFieldController.getText().getBytes(Charset.forName("UTF-8"));

        boolean isNextStep = true;

        // 비밀번호 입력 여부 체크
        if(newFieldController.getText().length() == 0){
            isNextStep = false;
        }

        // 비밀번호 일치 여부 체크
        if(isNextStep) {
            isNextStep = FastByteComparisons.equal(password, rePassword);
        }
        gasCalculatorMiniController.setDisable(!isNextStep);

        // Gas Limit Check
        if(isNextStep) {
            newFieldController.succeededForm();
            reFieldController.succeededForm();
            isNextStep = isCheckedPreGasUsed;
        }

        if(isNextStep){
            nextBtn.setStyle(new JavaFXStyle(nextBtn.getStyle()).add("-fx-background-color", "#910000").toString());
        }else{
            nextBtn.setStyle(new JavaFXStyle(nextBtn.getStyle()).add("-fx-background-color", "#d8d8d8").toString());
        }

        return isNextStep;
    }


    public void requestFocus(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                newFieldController.requestFocus();
            }
        });
    }
}
