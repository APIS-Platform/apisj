package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import org.apis.contract.ContractLoader;
import org.apis.core.CallTransaction;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.controller.module.ApisTextFieldController;
import org.apis.gui.controller.module.ApisTextFieldGroup;
import org.apis.gui.controller.module.GasCalculatorMiniController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.WalletItemModel;
import org.apis.gui.model.base.BaseModel;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.ResourceBundle;

public class PopupDeleteTypeBodyController extends BasePopupController {
    private String abi =  ContractLoader.readABI(ContractLoader.CONTRACT_PROOF_OF_KNOWLEDGE);
    private byte[] contractAddress = Hex.decode("1000000000000000000000000000000000037452");
    private CallTransaction.Contract contract = new CallTransaction.Contract(abi);
    private CallTransaction.Function functionRemoveProofKey = contract.getByName("removeProofKey");

    @FXML private Label title, subTitle, passwordLabel, knowledgeKeyLabel, btnNo, btnDelete, errorLabel;
    @FXML private ApisTextFieldController passwordController, knowledgeKeyController;
    @FXML private GasCalculatorMiniController gasCalculatorMiniController;

    private boolean isCheckedPreGasUsed = false;

    private WalletItemModel model;

    private ApisTextFieldGroup apisTextFieldGroup = new ApisTextFieldGroup();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        passwordController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
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
                knowledgeKeyController.requestFocus();
            }

            @Override
            public void onKeyTab(){
                knowledgeKeyController.requestFocus();
            }
        });
        knowledgeKeyController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
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
                passwordController.requestFocus();
            }

            @Override
            public void onKeyTab(){
                passwordController.requestFocus();
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
                byte[] sender = Hex.decode(model.getAddress());
                BigInteger value = BigInteger.ZERO;
                String functionName = functionRemoveProofKey.name;

                long gasLimit = AppManager.getInstance().getPreGasUsed(abi, sender, contractAddress, value, functionName, new Object[0]);
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
        });

        settingLayoutData();

        apisTextFieldGroup.add(passwordController);
        apisTextFieldGroup.add(knowledgeKeyController);
    }

    private void languageSetting(){
        title.textProperty().bind(StringManager.getInstance().deleteTypeBody.title);
        subTitle.textProperty().bind(StringManager.getInstance().deleteTypeBody.subTitle);
        passwordLabel.textProperty().bind(StringManager.getInstance().deleteTypeBody.passwordLabel);
        knowledgeKeyLabel.textProperty().bind(StringManager.getInstance().deleteTypeBody.knowledgeKeyLabel);
        btnNo.textProperty().bind(StringManager.getInstance().common.noButton);
        btnDelete.textProperty().bind(StringManager.getInstance().common.deleteButton);
        errorLabel.textProperty().bind(StringManager.getInstance().common.notEnoughBalance);
    }

    @Override
    public void setModel(BaseModel model) {
        this.model = (WalletItemModel)model;
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("btnNo")){
            exit();
        }else if(id.equals("btnDelete")){
            if(!isNextStep()){
                return ;
            }

            byte[] password = passwordController.getText().getBytes(Charset.forName("UTF-8"));
            byte[] knowledgeKey = knowledgeKeyController.getText().getBytes(Charset.forName("UTF-8"));
            byte[] proofKey = AppManager.getInstance().getProofKey(Hex.decode(this.model.getAddress()));
            byte[] pk = KeyStoreManager.getInstance().getPrivateKey(this.model.getKeystoreJsonData(), passwordController.getText());
            if(pk == null){
                passwordController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
                return ;
            }else{
                passwordController.succeededForm();
            }

            if(!Arrays.equals(proofKey,AppManager.getInstance().getKnowledgeKey(knowledgeKeyController.getText()))){
                knowledgeKeyController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
                return ;
            }else{
                knowledgeKeyController.succeededForm();
            }

            if(handler != null){

                BigInteger gasLimit = gasCalculatorMiniController.getGasLimit();
                BigInteger gasPrice = gasCalculatorMiniController.getGasPrice();
                handler.delete(password, knowledgeKey, gasLimit, gasPrice );
            }

            PopupManager.getInstance().hideMainPopup(zIndex-1);
            PopupManager.getInstance().hideMainPopup(zIndex);
        }
    }

    public void settingLayoutData(){
        if(model != null) {

            gasCalculatorMiniController.setMineral(model.getMineral());

        }
        isNextStep();
    }

    public void requestFocus (){
        passwordController.requestFocus();
    }

    public boolean isNextStep(){
        boolean isNextStep = true;

        // 비밀번호 입력 여부 체크
        if(passwordController.getText().length() == 0){
            isNextStep = false;
        }
        // 비밀번호 입력 여부 체크
        if(knowledgeKeyController.getText().length() == 0){
            isNextStep = false;
        }

        gasCalculatorMiniController.setDisable(!isNextStep);

        // Gas Limit Check
        if(isNextStep) {
            isNextStep = isCheckedPreGasUsed;
        }

        if(isNextStep){
            btnDelete.setStyle(new JavaFXStyle(btnDelete.getStyle()).add("-fx-background-color", "#910000").toString());
        }else{
            btnDelete.setStyle(new JavaFXStyle(btnDelete.getStyle()).add("-fx-background-color", "#d8d8d8").toString());
        }

        return isNextStep;
    }

    private PopupDeleteTypeBodyImpl handler;
    public void setHandler(PopupDeleteTypeBodyImpl handler){
        this.handler = handler;
    }
    public interface PopupDeleteTypeBodyImpl{
        void delete(byte[] password, byte[] knowledgeKey, BigInteger gasLimit, BigInteger gasPrice);
    }
}
