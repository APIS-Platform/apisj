package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupSuccessController implements Initializable {
    @FXML
    private Label title, subTitle, yesBtn;
    @FXML
    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(0);
        AppManager.getInstance().guiFx.hideMainPopup(1);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

    }

    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.successTitle);
        subTitle.textProperty().bind(StringManager.getInstance().popup.successSubTitle);
        yesBtn.textProperty().bind(StringManager.getInstance().popup.successYes);
    }
}
