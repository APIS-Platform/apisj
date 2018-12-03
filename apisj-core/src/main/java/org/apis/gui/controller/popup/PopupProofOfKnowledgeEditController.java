package org.apis.gui.controller.popup;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
import org.apis.gui.controller.module.ApisTextFieldController;
import org.apis.gui.controller.module.ApisTextFieldGroup;
import org.apis.gui.controller.module.GasCalculatorMiniController;
import org.apis.gui.controller.module.TooltipTopController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;
import org.apis.util.ByteUtil;
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

public class PopupProofOfKnowledgeEditController extends BasePopupController {
    private String abi =  ContractLoader.readABI(ContractLoader.CONTRACT_PROOF_OF_KNOWLEDGE);
    private byte[] contractAddress = Hex.decode("1000000000000000000000000000000000037452");
    private CallTransaction.Contract contract = new CallTransaction.Contract(abi);
    private CallTransaction.Function functionRegisterProofKey = contract.getByName("registerProofKey");
    private CallTransaction.Function functionRemoveProofKey = contract.getByName("removeProofKey");
    private int cusorTabIndex = 0;
    private int cusorStepIndex = 0;
    private boolean isCheckedPreGasUsed = false;

    private Image introNavi,introNaviCircle;
    private Image checkGreen = ImageManager.icCheckGreen;;
    private Image errorRed = ImageManager.icErrorRed;
    private WalletItemModel model;

    @FXML private AnchorPane rootPane;
    @FXML private TabPane tabPane;
    @FXML private ImageView introNaviOne, introNaviTwo, addressIcon, deleteButton, icKnowledgekey;
    @FXML private Label address, title, subTitle, selectedAddressLabel, timeLabel, editPasswordLabel, transferAmountLabel,detailFeeLabel,withdrawalLabel,afterBalanceLabel,payMsg1,payMsg2 ;
    @FXML private ApisTextFieldController newFieldController, reFieldController;
    @FXML private Label backBtn1, backBtn2, nextBtn, payBtn, transferAmount, detailFee, withdrawal, afterBalance;
    @FXML private Label titleLabel, balance, errorLabel;

    @FXML private GasCalculatorMiniController gasCalculatorMiniController;
    @FXML private TooltipTopController deleteTooltipController;

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

        deleteTooltipController.hideTooltip();
        setStep(0);

        apisTextFieldGroup.add(newFieldController);
        apisTextFieldGroup.add(reFieldController);
    }

    private void preGasUsed(){
        byte[] sender = Hex.decode(model.getAddress());
        BigInteger value = BigInteger.ZERO;
        String functionName = functionRegisterProofKey.name;
        Object[] args = new Object[1];
        args[0] = AppManager.getInstance().getKnowledgeKey(newFieldController.getText().trim());

        long gasLimit = AppManager.getInstance().getPreGasUsed(abi, sender, contractAddress, value, functionName, args);
        gasCalculatorMiniController.setGasLimit(Long.toString(gasLimit));

        // 잔액 여부
        isCheckedPreGasUsed = true;
        errorLabel.setVisible(false);
        errorLabel.setPrefHeight(0);

        gasCalculatorMiniController.setMineral(model.getMineral());
        BigInteger totalFee = gasCalculatorMiniController.getTotalFee();
        BigInteger balace = model.getApis();
        if(totalFee.compareTo(BigInteger.ZERO) > 0){
            if(totalFee.compareTo(balace) > 0){
                isCheckedPreGasUsed = false;
                errorLabel.setVisible(true);
                errorLabel.setPrefHeight(-1);
            }
        }
        settingLayoutData();
    }

    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().proofKey.title);
        subTitle.textProperty().bind(StringManager.getInstance().proofKey.editSubTitle);
        selectedAddressLabel.textProperty().bind(StringManager.getInstance().proofKey.selectedAddressLabel);
        editPasswordLabel.textProperty().bind(StringManager.getInstance().proofKey.editPassword);
        payMsg1.textProperty().bind(StringManager.getInstance().proofKey.payMsg1);
        payMsg2.textProperty().bind(StringManager.getInstance().proofKey.payMsg2);

        transferAmountLabel.textProperty().bind(StringManager.getInstance().common.transferAmount);
        detailFeeLabel.textProperty().bind(StringManager.getInstance().common.transferDetailFee);
        withdrawalLabel.textProperty().bind(StringManager.getInstance().common.withdrawal);
        afterBalanceLabel.textProperty().bind(StringManager.getInstance().common.afterBalance);
        titleLabel.textProperty().bind(StringManager.getInstance().proofKey.total);
        errorLabel.textProperty().bind(StringManager.getInstance().common.notEnoughBalance);

        backBtn1.textProperty().bind(StringManager.getInstance().common.closeButton);
        backBtn2.textProperty().bind(StringManager.getInstance().common.prevButton);
        nextBtn.textProperty().bind(StringManager.getInstance().common.nextButton);
        payBtn.textProperty().bind(StringManager.getInstance().common.payButton);

        newFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.newPassword.get());
        reFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.confrimPassword.get());

        deleteTooltipController.getTooltipText().textProperty().bind(StringManager.getInstance().proofKey.deleteToolTip);
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
        }else if(id.equals("payBtn")){
            String address = this.model.getAddress();
            byte[] proofKey = AppManager.getInstance().getKnowledgeKey(this.newFieldController.getText());
            BigInteger value = BigInteger.ZERO;
            BigInteger gasPrice = this.gasCalculatorMiniController.getGasPrice();
            BigInteger gasLimit = this.gasCalculatorMiniController.getGasLimit();

            Object[] args = new Object[1];
            args[0] = proofKey;
            byte[] functionCallBytes = functionRegisterProofKey.encode(args);
            // 완료 팝업 띄우기
            PopupContractWarningController controller = (PopupContractWarningController) PopupManager.getInstance().showMainPopup(rootPane, "popup_contract_warning.fxml", 1);
            controller.setData(address, value.toString(), gasPrice.toString(), gasLimit.toString(), contractAddress, functionCallBytes);
            controller.requestFocus();
            controller.setHandler(new PopupContractWarningController.PopupContractWarningImpl() {
                @Override
                public void success(Transaction tx) {

                }

                @Override
                public void fail(Transaction tx) {

                }
            });

        }else if(id.equals("deleteButton")){
            PopupDeleteTypeBodyController controller = (PopupDeleteTypeBodyController)PopupManager.getInstance().showMainPopup(rootPane, "popup_delete_type_body.fxml", zIndex+1);
            controller.setModel(model);
            controller.setHandler(new PopupDeleteTypeBodyController.PopupDeleteTypeBodyImpl() {
                @Override
                public void delete(byte[] password, byte[] knowledgeKey, BigInteger gasLimit, BigInteger gasPrice){
                    String address = model.getAddress();
                    BigInteger value = BigInteger.ZERO;

                    byte[] functionCallBytes = functionRemoveProofKey.encodeSignature();

                    // 트랜잭션 생성
                    Transaction tx = AppManager.getInstance().ethereumGenerateTransaction(address, value.toString(), gasPrice.toString(), gasLimit.toString(), contractAddress, functionCallBytes, password, knowledgeKey);

                    // 트랜잭션 먼저 전송해보기
                    ContractLoader.ContractRunEstimate runEstimate = AppManager.getInstance().ethereumPreRunTransaction(tx);
                    if(runEstimate.isSuccess()){

                        // 트랜잭션 전송
                        AppManager.getInstance().ethereumSendTransactions(tx);
                        PopupSuccessController controller = (PopupSuccessController)PopupManager.getInstance().showMainPopup(rootPane, "popup_success.fxml",1);
                        controller.requestFocusYesButton();

                    }else{

                    }
                }
            });
            controller.requestFocus();
        }
    }

    @FXML
    public void onMouseExited(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        if(id.equals("deleteButton")){
            deleteButton.setImage(ImageManager.btnKeyDelete);
            deleteTooltipController.hideTooltip();
        }
    }

    @FXML
    public void onMouseEntered(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        if(id.equals("deleteButton")){
            deleteButton.setImage(ImageManager.btnKeyDeleteHover);
            deleteTooltipController.showTooltip();
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
