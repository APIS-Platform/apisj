package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Ellipse;
import javafx.scene.control.*;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupContractReadWriteCreateController implements Initializable {

    @FXML
    private ImageView addrCircleImg;
    @FXML
    private TextField contractNameTextField;

    // Multilingual Support Labels
    @FXML
    private Label readWriteTitle, readWriteCreate, addrLabel, nameLabel, jsonInterfaceLabel, noBtn, createBtn;

    public void exit(){ AppManager.getInstance().guiFx.hideMainPopup(0); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        init();
    }

    public void init() {
        // Multilingual Support
        languageSetting();

        Ellipse ellipse = new Ellipse(12, 12);
        ellipse.setCenterX(12);
        ellipse.setCenterY(12);

        addrCircleImg.setClip(ellipse);
    }

    public void languageSetting() {
        readWriteTitle.textProperty().bind(StringManager.getInstance().contractPopup.readWriteTitle);
        readWriteCreate.textProperty().bind(StringManager.getInstance().contractPopup.readWriteCreate);
        addrLabel.textProperty().bind(StringManager.getInstance().contractPopup.addrLabel);
        nameLabel.textProperty().bind(StringManager.getInstance().contractPopup.nameLabel);
        contractNameTextField.promptTextProperty().bind(StringManager.getInstance().contractPopup.namePlaceholder);
        jsonInterfaceLabel.textProperty().bind(StringManager.getInstance().contractPopup.jsonInterfaceLabel);
        noBtn.textProperty().bind(StringManager.getInstance().contractPopup.noBtn);
        createBtn.textProperty().bind(StringManager.getInstance().contractPopup.createBtn);
    }

    public void createBtnClicked() {
        AppManager.getInstance().guiFx.hideMainPopup(0);
        AppManager.getInstance().guiFx.showMainPopup("popup_contract_read_write_select.fxml", 0);
    }
}
