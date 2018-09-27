package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.WalletItemModel;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupChangePasswordController extends BasePopupController {

    private WalletItemModel model;
    private boolean isChangeable = false;

    @FXML
    private Label changeBtn;
    @FXML
    private ApisTextFieldController currentFieldController, newFieldController, reFieldController;
    @FXML
    private Label title, subTitle, currentPasswordLabel, newPasswordLabel;

    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.changeWalletPasswordTitle);
        subTitle.textProperty().bind(StringManager.getInstance().popup.changeWalletPasswordSubTitle);
        currentPasswordLabel.textProperty().bind(StringManager.getInstance().popup.changeWalletPasswordCurrentPw);
        newPasswordLabel.textProperty().bind(StringManager.getInstance().popup.changeWalletPasswordNewPw);
        changeBtn.textProperty().bind(StringManager.getInstance().popup.changeWalletPasswordChange);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        currentFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());
        newFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());
        reFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, StringManager.getInstance().common.passwordPlaceholder.get());

        currentFieldController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text = currentFieldController.getText();

                if (text == null || text.equals("")) {
                    currentFieldController.failedForm("Please enter your password.");
                } else {
                    currentFieldController.succeededForm();
                }

                checkChangeNext();
            }

            @Override
            public void change(String old_text, String new_text) {

            }
        });
        newFieldController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if (newFieldController.getCheckBtnEnteredFlag()) {
                    newFieldController.setText("");
                }

                text = newFieldController.getText();

                if (text == null || text.equals("")) {
                    newFieldController.failedForm("Please enter your password.");
                } else if (text.length() < 8) {
                    newFieldController.failedForm("Password must contain at least 8 characters.");
                } else if (!newFieldController.pwValidate(text)) {
                    newFieldController.failedForm("Password must contain a combination of letters, numbers, and special characters.");
                } else {
                    newFieldController.succeededForm();
                }

                if (!newFieldController.getText().isEmpty()) {
                    if(reFieldController.getHandler() != null){
                        reFieldController.getHandler().onFocusOut();
                    }
                }else{

                }

                checkChangeNext();
            }

            @Override
            public void change(String old_text, String new_text) {
                String text;

                if (newFieldController.getCheckBtnEnteredFlag()) {
                    newFieldController.setText("");
                }

                text = newFieldController.getText();

                if (text == null || text.equals("")) {
                    newFieldController.failedForm("Please enter your password.");
                } else if (text.length() < 8) {
                    newFieldController.failedForm("Password must contain at least 8 characters.");
                } else if (!newFieldController.pwValidate(text)) {
                    newFieldController.failedForm("Password must contain a combination of letters, numbers, and special characters.");
                } else {
                    newFieldController.succeededForm();
                }

                if (!newFieldController.getText().isEmpty()) {
                    if(reFieldController.getHandler() != null){
                        reFieldController.getHandler().onFocusOut();
                    }
                }else{

                }

                checkChangeNext();
            }
        });

        reFieldController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {
                String text;

                if(reFieldController.getCheckBtnEnteredFlag()) {
                    reFieldController.setText("");
                }

                text = reFieldController.getText();

                if(text == null || text.equals("")) {
                    reFieldController.failedForm("Please check your password.");
                } else if(!text.equals(newFieldController.getText())) {
                    reFieldController.failedForm("Password does not match the confirm password.");
                } else {
                    reFieldController.succeededForm();
                }

                checkChangeNext();
            }

            @Override
            public void change(String old_text, String new_text) {
                String text;

                if(reFieldController.getCheckBtnEnteredFlag()) {
                    reFieldController.setText("");
                }

                text = reFieldController.getText();

                if(text == null || text.equals("")) {
                    reFieldController.failedForm("Please check your password.");
                } else if(!text.equals(newFieldController.getText())) {
                    reFieldController.failedForm("Password does not match the confirm password.");
                } else {
                    reFieldController.succeededForm();
                }

                checkChangeNext();
            }
        });
    }



    public void setModel(WalletItemModel model) {
        this.model = model;

    }

    public void checkChangeNext(){

        if( currentFieldController.getText().length() >= 8
            && currentFieldController.pwValidate(currentFieldController.getText())
            && newFieldController.getText().length() >= 8
            && newFieldController.getText().equals(reFieldController.getText())){
            succeededForm();
        }else{
            failedForm();
        }

    }

    public void failedForm(){
        changeBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #d8d8d8 ;");
        isChangeable = false;
    }

    public void succeededForm(){
        changeBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #910000 ;");
        isChangeable = true;
    }

    public void change(){

        if(isChangeable == false){
            return;
        }

        boolean isChanged = KeyStoreManager.getInstance().updateWalletPassword(this.model.getId(), this.model.getAlias(), currentFieldController.getText(), newFieldController.getText());
        if(isChanged){
            AppManager.getInstance().guiFx.getWallet().removeWalletCheckList();
            AppManager.getInstance().guiFx.getWallet().update(null);
            exit();
        }else{
            currentFieldController.failedForm("Please check your password.");
            failedForm();
        }
    }
}
