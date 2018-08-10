package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Ellipse;
import javafx.scene.control.*;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupContractReadWriteModifyController implements Initializable {

    @FXML
    private TextField contractNameTextField;
    @FXML
    private ImageView addrCircleImg;

    // Multilingual Support Label
    @FXML
    private Label readWriteTitle, readWriteModify, addrLabel, nameLabel, jsonInterfaceLabel, noBtn, modifyBtn;

    public void exit() { AppManager.getInstance().guiFx.hideMainPopup(0); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();

        Ellipse ellipse = new Ellipse(12, 12);
        ellipse.setCenterX(12);
        ellipse.setCenterY(12);

        addrCircleImg.setClip(ellipse);
    }

    public void languageSetting() {
        readWriteTitle.textProperty().bind(StringManager.getInstance().contractPopup.readWriteTitle);
        readWriteModify.textProperty().bind(StringManager.getInstance().contractPopup.readWriteModify);
        addrLabel.textProperty().bind(StringManager.getInstance().contractPopup.addrLabel);
        nameLabel.textProperty().bind(StringManager.getInstance().contractPopup.nameLabel);
        contractNameTextField.promptTextProperty().bind(StringManager.getInstance().contractPopup.namePlaceholder);
        jsonInterfaceLabel.textProperty().bind(StringManager.getInstance().contractPopup.jsonInterfaceLabel);
        noBtn.textProperty().bind(StringManager.getInstance().contractPopup.noBtn);
        modifyBtn.textProperty().bind(StringManager.getInstance().contractPopup.modifyBtn);
    }

    public void modifyBtnClicked() {
        AppManager.getInstance().guiFx.hideMainPopup(0);
        AppManager.getInstance().guiFx.showMainPopup("popup_edit_token.fxml", 0);
    }

}
