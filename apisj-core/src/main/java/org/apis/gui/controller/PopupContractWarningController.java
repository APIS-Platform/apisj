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

    @FXML
    private TextField amountTextField, gasLimitTextField;

    // Multilingual Support Label
    @FXML
    private Label warningTitle, warningDesc, amountToSendLabel, gasLimitLabel, gasLimitLabel1, generateTxBtn, rawTxLabel, noBtn, yesBtn;

    public void exit() { AppManager.getInstance().guiFx.hideMainPopup(0); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

        this.amountTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue) {
                    amountTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-font-family: 'Roboto Mono Regular'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                } else {
                    amountTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-font-family: 'Roboto Mono Regular'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                }
            }
        });

        this.gasLimitTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue) {
                    gasLimitTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-font-family: 'Roboto Mono Regular'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                } else {
                    gasLimitTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-font-family: 'Roboto Mono Regular'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                }
            }
        });
    }

    public void languageSetting() {
        warningTitle.textProperty().bind(StringManager.getInstance().contractPopup.warningTitle);
        warningDesc.textProperty().bind(StringManager.getInstance().contractPopup.warningDesc);
        amountToSendLabel.textProperty().bind(StringManager.getInstance().contractPopup.amountToSendLabel);
        gasLimitLabel.textProperty().bind(StringManager.getInstance().contractPopup.gasLimitLabel);
        gasLimitLabel1.textProperty().bind(StringManager.getInstance().contractPopup.gasLimitLabel);
        generateTxBtn.textProperty().bind(StringManager.getInstance().contractPopup.generateTxBtn);
        rawTxLabel.textProperty().bind(StringManager.getInstance().contractPopup.rawTxLabel);
        noBtn.textProperty().bind(StringManager.getInstance().contractPopup.noBtn);
        yesBtn.textProperty().bind(StringManager.getInstance().contractPopup.yesBtn);
    }

    public void yesBtnClicked() {
        AppManager.getInstance().guiFx.hideMainPopup(0);
        AppManager.getInstance().guiFx.showMainPopup("popup_token_add_edit.fxml",0);
    }
}
