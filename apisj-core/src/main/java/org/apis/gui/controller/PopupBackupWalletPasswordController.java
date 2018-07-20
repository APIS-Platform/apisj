package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupBackupWalletPasswordController implements Initializable {

    @FXML
    private ApisTextFieldController passwordController;


    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(1);
    };


    public void change(){

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
                if (passwordController.getCheckBtnEnteredFlag()) {
                    passwordController.setText("");
                }

                String text = passwordController.getText();

                if (text == null || text.equals("")) {
                    passwordController.failedForm("Please enter your password.");
                } else if (text.length() < 8) {
                    passwordController.failedForm("Password must contain at least 8 characters.");
                } else if (!passwordController.pwValidate(text)) {
                    passwordController.failedForm("Password must contain a combination of letters, numbers, and special characters.");
                } else {
                    passwordController.succeededForm();
                }

            }
        });
    }
}
