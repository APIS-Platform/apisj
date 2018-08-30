package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.apis.gui.manager.AppManager;
import javafx.scene.control.*;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupContractWarningController implements Initializable {

    // Multilingual Support Label
    @FXML
    private Label warningTitle, warningDesc, walletPasswordLabel, generateTxBtn, rawTxLabel, signedTxLabel, noBtn, yesBtn;

    public void exit() { AppManager.getInstance().guiFx.hideMainPopup(0); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

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

    public void yesBtnClicked() {
        AppManager.getInstance().guiFx.hideMainPopup(0);
        AppManager.getInstance().guiFx.showMainPopup("popup_token_add_edit.fxml",0);
    }
}
