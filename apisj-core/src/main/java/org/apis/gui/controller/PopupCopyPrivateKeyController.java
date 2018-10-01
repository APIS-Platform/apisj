package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupCopyPrivateKeyController extends BasePopupController {
    @FXML
    private Label pkLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setPk(String pk){
        this.pkLabel.textProperty().setValue(pk);
    }
}
