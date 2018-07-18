package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupCopyWalletAddress implements Initializable {
    @FXML
    private Label addressLabel;

    public void exit(){ AppManager.getInstance().guiFx.hideMainPopup(0); };

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setAddress(String address){
        this.addressLabel.textProperty().setValue(address);
    }
}
