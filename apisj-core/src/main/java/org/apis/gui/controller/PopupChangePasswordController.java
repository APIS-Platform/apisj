package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupChangePasswordController implements Initializable {

    @FXML
    private ApisTextFieldController currentFieldController, newFieldController, reFieldController;

    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "At least 8 characters including letters, numbers, and special characters.");
        newFieldController.init(ApisTextFieldController.TEXTFIELD_TYPE_PASS, "At least 8 characters including letters, numbers, and special characters.");
    }
}
