package org.apis.gui.controller.popup;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.contract.ContractLoader;
import org.apis.core.Transaction;
import org.apis.db.sql.DBManager;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.module.ApisTextFieldController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.AppManager;
import javafx.scene.control.*;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.ResourceBundle;

public class PopupContractWarningController extends BasePopupController {

    @FXML private AnchorPane rootPane, knowledgeKeyPane;
    @FXML private Label warningTitle, warningDesc, walletPasswordLabel, generateTxBtn, rawTxLabel, signedTxLabel, noBtn, yesBtn, knowledgeKeyLabel;
    @FXML private ApisTextFieldController passwordController, knowledgeKeyController;
    @FXML private TextArea rawTxArea, signedTxArea;

    private String address, value, gasPrice, gasLimit;
    private byte[] data, toAddress;
    private Transaction tx;

    private PopupContractWarningImpl handler;

    public void setHandler(PopupContractWarningImpl handler) {
        this.handler = handler;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

        rootPane.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    passwordController.requestFocus();
                }
            }
        });

        passwordController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {

            }

            @Override
            public void change(String old_text, String new_text) {

            }

            @Override
            public void onAction() {
                // 보안키 여부 체크
                if(AppManager.getInstance().isUsedProofKey(Hex.decode(address))){
                    knowledgeKeyController.requestFocus();
                }else{
                    generateTx();
                }

            }

            @Override
            public void onKeyTab(){
                if(AppManager.getInstance().isUsedProofKey(Hex.decode(address))){
                    knowledgeKeyController.requestFocus();
                }
            }
        });

        knowledgeKeyController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {

            }

            @Override
            public void change(String old_text, String new_text) {

            }

            @Override
            public void onAction() {
                generateTx();
            }

            @Override
            public void onKeyTab(){
                passwordController.requestFocus();
            }
        });

        this.yesBtn.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if("ENTER".equals(event.getCode().toString())){
                    sendTx();
                }
            }
        });
    }


    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        if("generateTxBtn".equals(id)){
            generateTx();

        }else if("noBtn".equals(id)){
            exit();

        }else if("yesBtn".equals(id)){
            sendTx();

        }
    }

    public void generateTx() {
        byte[] password = passwordController.getText().getBytes(Charset.forName("UTF-8"));
        byte[] knowledgeKey = knowledgeKeyController.getText().getBytes(Charset.forName("UTF-8"));

        if (password == null || password.equals("")) {
            passwordController.failedForm("Please enter your password.");
        } else {
            passwordController.succeededForm();
            try {
                if (this.toAddress == null || this.toAddress.length <= 0) {
                    tx = AppManager.getInstance().ethereumGenerateTransaction(this.address, this.value, this.gasPrice, this.gasLimit, new byte[0], this.data, password, knowledgeKey);
                } else {
                    tx = AppManager.getInstance().ethereumGenerateTransaction(this.address, this.value, this.gasPrice, this.gasLimit, this.toAddress, this.data, password, knowledgeKey);
                }

                rawTxArea.setText(tx.toString());
                signedTxArea.setText(Hex.toHexString(tx.getEncoded()));
                yesBtn.setStyle(new JavaFXStyle(yesBtn.getStyle()).add("-fx-background-color", "#910000").toString());

                this.yesBtn.requestFocus();

            } catch (Exception e) {
                e.printStackTrace();
                passwordController.failedForm(StringManager.getInstance().common.walletPasswordCombination.get());
            }
        }
    }

    private void sendTx(){
        if(tx != null){
            ContractLoader.ContractRunEstimate runEstimate = AppManager.getInstance().ethereumPreRunTransaction(tx);
            if(runEstimate.isSuccess() || runEstimate.getReceipt().getError().toLowerCase().indexOf("invalid nonce") >= 0){
                AppManager.getInstance().ethereumSendTransactions(tx);
                PopupSuccessController controller = (PopupSuccessController)PopupManager.getInstance().showMainPopup(rootPane, "popup_success.fxml",this.zIndex);
                controller.requestFocusYesButton();

                // update recent address
                if(handler != null){
                    handler.success(tx);
                }

            }else{
                PopupFailController failController = (PopupFailController)PopupManager.getInstance().showMainPopup(rootPane ,"popup_fail.fxml", this.zIndex);
                failController.setError(runEstimate.getReceipt().getError());
                if(handler != null){
                    handler.fail(tx);
                }
            }
        }
    }

    public void languageSetting() {
        warningTitle.textProperty().bind(StringManager.getInstance().contractPopup.warningTitle);
        warningDesc.textProperty().bind(StringManager.getInstance().contractPopup.warningDesc);
        generateTxBtn.textProperty().bind(StringManager.getInstance().contractPopup.generateTxBtn);
        rawTxLabel.textProperty().bind(StringManager.getInstance().contractPopup.rawTxLabel);
        signedTxLabel.textProperty().bind(StringManager.getInstance().contractPopup.signedTxLabel);
        noBtn.textProperty().bind(StringManager.getInstance().common.noButton);
        yesBtn.textProperty().bind(StringManager.getInstance().common.yesButton);
        walletPasswordLabel.textProperty().bind(StringManager.getInstance().contractPopup.walletPasswordLabel);
        knowledgeKeyLabel.textProperty().bind(StringManager.getInstance().contractPopup.knowledgeKeyLabel);
    }

    public void setData(String address, String value, String gasPrice, String gasLimit, byte[] toAddress, byte[] data){
        this.address = address;
        this.value = value;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.toAddress = toAddress;
        this.data = data;

        // 보안키 여부 체크
        if(AppManager.getInstance().isUsedProofKey(Hex.decode(address))){
            this.knowledgeKeyPane.setVisible(true);
            this.knowledgeKeyPane.setPrefHeight(-1);
        }else{
            this.knowledgeKeyPane.setVisible(false);
            this.knowledgeKeyPane.setPrefHeight(0);
        }
    }

    public interface PopupContractWarningImpl{
        void success(Transaction tx);
        void fail(Transaction tx);
    }

    public ApisTextFieldController getPasswordController() {
         return passwordController;
    }

    public void requestFocus(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                rootPane.requestFocus();
                passwordController.requestFocus();
            }
        });
    }
}
