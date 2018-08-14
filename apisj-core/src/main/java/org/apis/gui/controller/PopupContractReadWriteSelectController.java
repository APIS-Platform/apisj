package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupContractReadWriteSelectController implements Initializable {

    // Multilingual Support Label
    @FXML
    private Label readWriteTitle, readWriteSelect, addrLabel, newLabel, listLabel, editLabel, deleteLabel, selectLabel, noBtn, yesBtn;

    public void exit(){ AppManager.getInstance().guiFx.hideMainPopup(0); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Multilingual Support
        languageSetting();
    }

    public void languageSetting() {
        readWriteTitle.textProperty().bind(StringManager.getInstance().contractPopup.readWriteTitle);
        readWriteSelect.textProperty().bind(StringManager.getInstance().contractPopup.readWriteSelect);
        addrLabel.textProperty().bind(StringManager.getInstance().contractPopup.addrLabel);
        newLabel.textProperty().bind(StringManager.getInstance().contractPopup.newLabel);
        listLabel.textProperty().bind(StringManager.getInstance().contractPopup.listLabel);
        editLabel.textProperty().bind(StringManager.getInstance().contractPopup.editLabel);
        deleteLabel.textProperty().bind(StringManager.getInstance().contractPopup.deleteLabel);
        selectLabel.textProperty().bind(StringManager.getInstance().contractPopup.selectLabel);
        noBtn.textProperty().bind(StringManager.getInstance().contractPopup.noBtn);
        yesBtn.textProperty().bind(StringManager.getInstance().contractPopup.yesBtn);
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("yesBtn")) {
            AppManager.getInstance().guiFx.hideMainPopup(0);
            AppManager.getInstance().guiFx.showMainPopup("popup_contract_read_write_modify.fxml",0);
        }
    }

}
