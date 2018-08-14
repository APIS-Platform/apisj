package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Ellipse;
import javafx.scene.control.TextField;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupEditTokenController implements Initializable {

    @FXML
    private ImageView addrCircleImg;
    @FXML
    private TextField nameTextField;

    // Multilingual Support Label
    @FXML
    private Label editTokenTitle, editTokenDesc, contractAddrLabel, nameLabel, minNumLabel, previewLabel, noBtn, editBtn;

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
        editTokenTitle.textProperty().bind(StringManager.getInstance().popup.tokenEditEditTokenTitle);
        editTokenDesc.textProperty().bind(StringManager.getInstance().popup.tokenEditEditTokenDesc);
        contractAddrLabel.textProperty().bind(StringManager.getInstance().popup.tokenEditContractAddrLabel);
        nameLabel.textProperty().bind(StringManager.getInstance().popup.tokenEditNameLabel);
        nameTextField.promptTextProperty().bind(StringManager.getInstance().popup.tokenEditNamePlaceholder);
        minNumLabel.textProperty().bind(StringManager.getInstance().popup.tokenEditMinNumLabel);
        previewLabel.textProperty().bind(StringManager.getInstance().popup.tokenEditPreviewLabel);
        noBtn.textProperty().bind(StringManager.getInstance().popup.tokenEditNoBtn);
        editBtn.textProperty().bind(StringManager.getInstance().popup.tokenEditEditBtn);
    }

    public void editBtnClicked() {
        AppManager.getInstance().guiFx.hideMainPopup(0);
        AppManager.getInstance().guiFx.showMainPopup("popup_contract_warning.fxml", 0);
    }
}
