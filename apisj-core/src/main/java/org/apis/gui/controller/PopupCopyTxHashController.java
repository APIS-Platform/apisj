package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupCopyTxHashController implements Initializable {
    @FXML
    private Label hashLabel;

    public void exit(){ AppManager.getInstance().guiFx.hideMainPopup(0); };

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setHash(String hash){
        this.hashLabel.textProperty().setValue(hash);
    }
}
