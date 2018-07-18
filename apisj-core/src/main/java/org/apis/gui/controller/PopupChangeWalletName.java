package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupChangeWalletName implements Initializable {

    @FXML
    private ApisTextFieldController textFieldController;

    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_TEXT, "Wallet Name");
    }

    public void setWalletName(String alias) {
        textFieldController.setText(alias);
    }
}
