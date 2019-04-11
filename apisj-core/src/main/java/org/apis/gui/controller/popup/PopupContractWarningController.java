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
import org.apis.contract.EstimateTransactionResult;
import org.apis.core.Transaction;
import org.apis.gui.common.JavaFXStyle;
import org.apis.gui.controller.module.MessageLineController;
import org.apis.gui.controller.module.textfield.ApisTextFieldController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.controller.module.textfield.ApisTextFieldGroup;
import org.apis.gui.controller.module.OnScreenKeyboardController;
import org.apis.gui.manager.AppManager;
import javafx.scene.control.*;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;
import org.apis.hid.HIDDevice;
import org.apis.hid.HIDModule;
import org.apis.hid.template.DeviceData;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.spongycastle.util.encoders.Hex;

import javax.usb.UsbException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class PopupContractWarningController extends BasePopupController implements PopupLedgerNoticeController.PopupLedgerNoticeCallback {

    @FXML private AnchorPane rootPane, walletPasswordPane, messagePane, knowledgeKeyPane;
    @FXML private Label warningTitle, warningDesc, walletPasswordLabel, generateTxBtn, rawTxLabel, signedTxLabel, noBtn, yesBtn, knowledgeKeyLabel;
    @FXML private ApisTextFieldController passwordController, knowledgeKeyController;
    @FXML private TextArea rawTxArea, signedTxArea;
    @FXML private MessageLineController ledgerConnectionMessageController;

    private String address, value, gasPrice, gasLimit, path;
    private byte[] data, toAddress, toMask;
    private char[] knowledgeKey;
    private Transaction tx;
    private HIDDevice ledger;

    private PopupContractWarningImpl handler;

    public void setHandler(PopupContractWarningImpl handler) {
        this.handler = handler;
    }

    private ApisTextFieldGroup apisTextFieldGroup = new ApisTextFieldGroup();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

        passwordController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "", ApisTextFieldController.THEME_TYPE_MAIN, OnScreenKeyboardController.CARET_INTRO);
        knowledgeKeyController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "", ApisTextFieldController.THEME_TYPE_MAIN, OnScreenKeyboardController.CARET_INTRO);

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
                yesBtn.setStyle(new JavaFXStyle(yesBtn.getStyle()).add("-fx-background-color", "#d8d8d8").toString());
                tx = null;
                rawTxArea.setText("");
            }

            @Override
            public void onAction() {
                // 보안키 여부 체크
                if(AppManager.getInstance().isUsedProofKey(ByteUtil.hexStringToBytes(address))){
                    knowledgeKeyController.requestFocus();
                }else{
                    generateTx();
                }

            }

            @Override
            public void onKeyTab(){
                if(AppManager.getInstance().isUsedProofKey(ByteUtil.hexStringToBytes(address))){
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
                ledgerConnectionMessageController.setVisible(false);
                yesBtn.setStyle(new JavaFXStyle(yesBtn.getStyle()).add("-fx-background-color", "#d8d8d8").toString());
                tx = null;
                rawTxArea.setText("");
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

        apisTextFieldGroup.add(passwordController);
        apisTextFieldGroup.add(knowledgeKeyController);
        this.messagePane.setVisible(false);
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
    PopupLedgerNoticeController ledgerNoticeController;
    public void generateTx() {
        // Ledger address
        if(AppManager.getInstance().isLedger(address)) {
            HIDModule module = HIDModule.getInstance();
            ledger = AppManager.getInstance().getLedger();
            module.loadDeviceList();

            List<DeviceData> devices = module.getDeviceList();

            ledgerConnectionMessageController.setVisible(false);
            StyleManager.backgroundColorStyle(yesBtn, StyleManager.AColor.Cd8d8d8);
            tx = null;
            rawTxArea.setText("");

            if(devices.size() == 1) {
                // Check knowledge key
                knowledgeKey = knowledgeKeyController.getText().toCharArray();;

                if (AppManager.getInstance().isUsedProofKey(ByteUtil.hexStringToBytes(address))) {
                    byte[] proofKey = AppManager.getInstance().getProofKey(ByteUtil.hexStringToBytes(address));
                    if (!Arrays.equals(proofKey, AppManager.getInstance().getKnowledgeKey(knowledgeKeyController.getText()))) {
                        knowledgeKeyController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
                        return;
                    } else {
                        knowledgeKeyController.succeededForm();
                    }
                }

                // Check ledger connection
                if(ledger == null) {
                    try {
                        AppManager.getInstance().closeLedger();
                        AppManager.getInstance().setLedger(new HIDDevice(devices.get(0).getDevice()));
                        ledger = AppManager.getInstance().getLedger();
                    } catch (UsbException e) {
                        e.printStackTrace();
                    }
                }

                path = AppManager.getInstance().getDerivationPath(address);
                if(path.length() != 0) {
                    byte[] tempAddress = null;
                    tempAddress = ledger.getAddress(path, false, false);

                    // Exception handling for disconnecting while proceeding
                    if(tempAddress == null) {
                        ledgerConnectionMessageController.setFailed(StringManager.getInstance().intro.checkConnectionFailedDisconn);
                        ledgerConnectionMessageController.setVisible(true);
                        AppManager.getInstance().closeLedger();
                        AppManager.getInstance().setLedger(null);
                        return;
                    }

                    // Correct ledger address
                    if(ByteUtil.toHexString(tempAddress).equals(address)) {
                        ledgerConnectionMessageController.setSuccessed(StringManager.getInstance().intro.checkConnectionSuccess);
                        ledgerConnectionMessageController.setVisible(true);
                        ledgerNoticeController = (PopupLedgerNoticeController)PopupManager.getInstance().showMainPopup(rootPane, "popup_ledger_notice.fxml",this.zIndex + 1);
                        ledgerNoticeController.callback = this;

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ledgerNoticeController.getLedgerTx(address, value, gasPrice, gasLimit, toAddress, toMask, data, path);
                            }
                        });
                        thread.start();
                    }
                }

            } else if(devices.size() > 1) {
                ledgerConnectionMessageController.setFailed(StringManager.getInstance().intro.checkConnectionFailedMulti);
                ledgerConnectionMessageController.setVisible(true);
                AppManager.getInstance().closeLedger();
                AppManager.getInstance().setLedger(null);

            } else {
                ledgerConnectionMessageController.setFailed(StringManager.getInstance().intro.checkConnectionFailedEmpty);
                ledgerConnectionMessageController.setVisible(true);
                AppManager.getInstance().closeLedger();
                AppManager.getInstance().setLedger(null);
            }

        // Non-Ledger Address
        } else {
            char[] password = passwordController.getText().toCharArray();
            char[] knowledgeKey = knowledgeKeyController.getText().toCharArray();

            yesBtn.setStyle(new JavaFXStyle(yesBtn.getStyle()).add("-fx-background-color", "#d8d8d8").toString());
            tx = null;
            rawTxArea.setText("");

            if (AppManager.getInstance().isUsedProofKey(ByteUtil.hexStringToBytes(address))) {
                byte[] proofKey = AppManager.getInstance().getProofKey(ByteUtil.hexStringToBytes(address));
                if (!Arrays.equals(proofKey, AppManager.getInstance().getKnowledgeKey(knowledgeKeyController.getText()))) {
                    knowledgeKeyController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
                    return;
                } else {
                    knowledgeKeyController.succeededForm();
                }
            }

            if (password == null || password.equals("")) {
                passwordController.failedForm(StringManager.getInstance().common.walletPasswordNull.get());
            } else {
                passwordController.succeededForm();
                try {
                    if (this.toAddress == null || this.toAddress.length <= 0) {
                        tx = AppManager.getInstance().generateTransaction(this.address, this.value, this.gasPrice, this.gasLimit, new byte[0], new byte[0], this.data, password, knowledgeKey);
                    } else {
                        tx = AppManager.getInstance().generateTransaction(this.address, this.value, this.gasPrice, this.gasLimit, this.toAddress, this.toMask, this.data, password, knowledgeKey);
                    }

//                    System.out.println("RAW TX : " + Hex.toHexString(tx.getEncodedRaw()));
//                    System.out.println("SIGNED TX : " + Hex.toHexString(tx.getEncoded()));
                    rawTxArea.setText(tx.toString());
                    signedTxArea.setText(Hex.toHexString(tx.getEncoded()));
                    yesBtn.setStyle(new JavaFXStyle(yesBtn.getStyle()).add("-fx-background-color", "#b01e1e").toString());

                    this.yesBtn.requestFocus();

                } catch (Exception e) {
                    passwordController.failedForm(StringManager.getInstance().common.walletPasswordCheck.get());
                }
            }
        }
    }

    @Override
    public void exitLedgerNoticeCallback(Transaction ledgerTx, Transaction signedTx) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                rawTxArea.setText(ledgerTx.toString());
                ConsoleUtil.printlnPurple(ledgerTx.toString());

                tx = signedTx;
                Transaction newTx = new Transaction(signedTx.getEncoded());
                if(AppManager.getInstance().isUsedProofKey(ByteUtil.hexStringToBytes(address))) {
                    tx = AppManager.getInstance().apisGenerateTransactionWithLedgerTx(signedTx, knowledgeKey);
                }
                signedTxArea.setText(String.format("%s\n------\n%s\n------\n%s", ByteUtil.toHexString(signedTx.getEncoded()), signedTx.toString().replaceAll(" ", "\n"), newTx.toString().replaceAll(" ", "\n")));

                yesBtn.setStyle(new JavaFXStyle(yesBtn.getStyle()).add("-fx-background-color", "#b01e1e").toString());
                yesBtn.requestFocus();

                ledgerNoticeController.exit();
            }
        });
    }

    @Override
    public void errorLedgerCallback(Exception e) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ledgerConnectionMessageController.setFailed(StringManager.getInstance().intro.checkConnectionFailedCancel);
                ledgerConnectionMessageController.setVisible(true);
                AppManager.getInstance().closeLedger();
                AppManager.getInstance().setLedger(null);
                signedTxArea.setText(e.getMessage());
                tx = null;

                yesBtn.setStyle(new JavaFXStyle(yesBtn.getStyle()).add("-fx-background-color", "#d8d8d8").toString());
                ledgerNoticeController.exit();
            }
        });
    }

    private void sendTx(){
        if(tx != null){
            EstimateTransactionResult runEstimate = AppManager.getInstance().estimateTransaction(tx);
            if(runEstimate.isSuccess() || runEstimate.getReceipt().getError().toLowerCase().contains("invalid nonce")){
                AppManager.getInstance().apisSendTransactions(tx);
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

    public void setData(String address, String value, String gasPrice, String gasLimit, byte[] toAddress, byte[] toMask, byte[] data){
        this.address = address;
        this.value = value;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.toAddress = toAddress;
        this.toMask = toMask;


        this.data = data;

        // 렛저 여부 체크
        if(AppManager.getInstance().isLedger(address)) {
            this.walletPasswordPane.setVisible(false);
            this.walletPasswordPane.setPrefHeight(0);
            this.messagePane.setVisible(true);
            this.ledgerConnectionMessageController.setVisible(false);
            this.messagePane.setPrefHeight(-1);
        } else {
            this.walletPasswordPane.setVisible(true);
            this.walletPasswordPane.setPrefHeight(-1);
            this.messagePane.setVisible(false);
            this.messagePane.setPrefHeight(0);
        }

        // 보안키 여부 체크
        if(AppManager.getInstance().isUsedProofKey(ByteUtil.hexStringToBytes(address))){
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
