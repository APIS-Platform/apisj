package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.KeyStoreManager;
import org.apis.gui.model.WalletItemModel;

import javax.swing.*;
import java.net.URL;
import java.util.ResourceBundle;

public class PopupBackupWalletPasswordController implements Initializable {
    private WalletItemModel model;

    @FXML
    private ApisTextFieldController passwordController;
    @FXML
    private Label yesBtn;

    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(0);
    };

    public void change(){

        if (passwordController.getCheckBtnEnteredFlag()) {
            passwordController.setText("");
        }

        String text = passwordController.getText();


        if (text == null || text.equals("")) {
            passwordController.failedForm("Please enter your password.");
        } else if(! KeyStoreManager.getInstance().matchPassword(model.getKstoreJsonData(),  passwordController.getText())){
            passwordController.failedForm("Please enter your password.");
        } else{
            passwordController.succeededForm();

            PopupBackupWalletController controller = (PopupBackupWalletController) AppManager.getInstance().guiFx.showMainPopup("popup_backup_wallet.fxml", 1);
            controller.setModel(this.model, passwordController.getText());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        passwordController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "At least 8 characters including letters, numbers, and special characters.");
        passwordController.setHandler(new ApisTextFieldController.ApisTextFieldControllerInterface() {
            @Override
            public void onFocusOut() {

            }

            @Override
            public void change(String old_text, String new_text) {

            }
        });

        succeededForm();
    }

    public void failedForm(){
        yesBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #d8d8d8 ;");
    }

    public void succeededForm(){
        yesBtn.setStyle("-fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-background-color: #910000 ;");
    }

    public void setModel(WalletItemModel model) {
        this.model = model;
    }
}
