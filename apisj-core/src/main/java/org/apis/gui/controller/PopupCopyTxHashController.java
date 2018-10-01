package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupCopyTxHashController extends BasePopupController {
    @FXML
    private Label hashLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setHash(String hash){
        this.hashLabel.textProperty().setValue(hash);
    }
}
