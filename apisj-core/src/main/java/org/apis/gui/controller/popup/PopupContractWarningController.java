package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
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
import org.spongycastle.util.encoders.Hex;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupContractWarningController extends BasePopupController {

    // Multilingual Support Label
    @FXML
    private Label warningTitle, warningDesc, walletPasswordLabel, generateTxBtn, rawTxLabel, signedTxLabel, noBtn, yesBtn;

    @FXML
    private ApisTextFieldController passwordController;

    @FXML private TextArea rawTxArea, signedTxArea;

    private String address, value, gasPrice, gasLimit, contractName, abi;
    private byte[] data, toAddress;
    private Transaction tx;
    private boolean isDeploy;

    private PopupContractWarningImpl handler;

    public void setHandler(PopupContractWarningImpl handler) {
        this.handler = handler;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();
    }


    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        if(isDeploy){
            // Deploy

            if("generateTxBtn".equals(id)){

                String password = passwordController.getText();

                if (password == null || password.equals("")) {
                    passwordController.failedForm("Please enter your password.");
                //} else if (password.length() < 8) {
                //    passwordController.failedForm("Password must contain at least 8 characters.");
                //} else if (!passwordController.pwValidate(password)) {
                //    passwordController.failedForm("Password must contain a combination of letters, numbers, and special characters.");
                } else {
                    passwordController.succeededForm();

                    try{
                        tx = AppManager.getInstance().ethereumGenerateTransaction(this.address, this.value, this.gasPrice, this.gasLimit, new byte[0], this.data,  password);
                        rawTxArea.setText(tx.toString());
                        signedTxArea.setText(Hex.toHexString(tx.getEncoded()));

                        System.out.println("tx.getHash() : "+Hex.toHexString(tx.getHash()));
                        System.out.println("tx.getContractAddress() : " + Hex.toHexString(tx.getContractAddress()));

                        yesBtn.setStyle(new JavaFXStyle(yesBtn.getStyle()).add("-fx-background-color","#910000").toString());
                    }catch (Exception e){
                        passwordController.failedForm("Password must contain a combination of letters, numbers, and special characters.");
                    }

                }
            }else if("noBtn".equals(id)){
                exit();
            }else if("yesBtn".equals(id)){
                if(tx != null){

                    ContractLoader.ContractRunEstimate runEstimate = AppManager.getInstance().ethereumPreRunTransaction(tx);
                    if(runEstimate.isSuccess()){
                        AppManager.getInstance().ethereumSendTransactions(tx);
                        PopupManager.getInstance().showMainPopup("popup_success.fxml",1);

                        byte[] address = tx.getSender();
                        byte[] contractAddress = tx.getContractAddress();
                        String abi = this.abi;
                        String name = this.contractName;

                        DBManager.getInstance().updateAbi(address, contractAddress, abi, name);
                        // 컨트렉트를 직접 저장하지 않고, 우선 abi만 저장 후,
                        // 컨트렉트가 블록에 씌워졌을 때,비로소 컨트렉트를 저장한다.
                        // DBManager.getInstance().updateContract(address, title, mask, abi, canvas_url);

                        if(handler != null){
                            handler.success();
                        }

                    }else{
                        PopupFailController failController = (PopupFailController)PopupManager.getInstance().showMainPopup("popup_fail.fxml", this.zIndex+1);
                        failController.setError(runEstimate.getReceipt().getError());
                        if(handler != null){
                            handler.fail();
                        }
                    }
                }
            }

        }else{
            // Write

            if("generateTxBtn".equals(id)){

                String password = passwordController.getText();

                if (password == null || password.equals("")) {
                    passwordController.failedForm("Please enter your password.");
                //} else if (password.length() < 8) {
                //    passwordController.failedForm("Password must contain at least 8 characters.");
                //} else if (!passwordController.pwValidate(password)) {
                //    passwordController.failedForm("Password must contain a combination of letters, numbers, and special characters.");
                } else {
                    passwordController.succeededForm();
                    try{
                        tx = AppManager.getInstance().ethereumGenerateTransaction(this.address, this.value, this.gasPrice, this.gasLimit, this.toAddress, this.data,  password);


                        rawTxArea.setText(tx.toString());
                        signedTxArea.setText(Hex.toHexString(tx.getEncoded()));
                        yesBtn.setStyle(new JavaFXStyle(yesBtn.getStyle()).add("-fx-background-color", "#910000").toString());

                    }catch (Exception e){
                        passwordController.failedForm("Password must contain a combination of letters, numbers, and special characters.");
                    }

                }

            }else if("noBtn".equals(id)){
                exit();
            }else if("yesBtn".equals(id)){
                if(tx != null){
                    // 미리 트랜잭션 발생시켜 보기
                    ContractLoader.ContractRunEstimate runEstimate = AppManager.getInstance().ethereumPreRunTransaction(tx);

                    if(runEstimate.isSuccess()) {
                        AppManager.getInstance().ethereumSendTransactions(tx);
                        PopupManager.getInstance().showMainPopup("popup_success.fxml", 1);
                        if (handler != null) {
                            handler.success();
                        }
                    }else{
                        PopupFailController failController = (PopupFailController)PopupManager.getInstance().showMainPopup("popup_fail.fxml", this.zIndex+1);
                        failController.setError(runEstimate.getReceipt().getError());
                        if (handler != null) {
                            handler.fail();
                        }
                    }

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
        noBtn.textProperty().bind(StringManager.getInstance().contractPopup.noBtn);
        yesBtn.textProperty().bind(StringManager.getInstance().contractPopup.yesBtn);
        walletPasswordLabel.textProperty().bind(StringManager.getInstance().contractPopup.walletPasswordLabel);
    }

    public void setData(String address, String value, String gasPrice, String gasLimit, byte[] toAddress, byte[] data){
        this.isDeploy = false;
        this.address = address;
        this.value = value;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.toAddress = toAddress;
        this.data = data;
    }

    public void setData(String address, String value, String gasPrice, String gasLimit, String contractName, String abi, byte[] data) {
        this.isDeploy = true;
        this.address = address;
        this.value = value;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.contractName = contractName;
        this.abi = abi;
        this.data = data;
    }

    public interface PopupContractWarningImpl{
        void success();
        void fail();
    }


    public void requestFocus(){
        passwordController.requestFocus();
    }
}
