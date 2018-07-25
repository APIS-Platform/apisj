package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupSuccessController implements Initializable {
    @FXML
    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(0);
        AppManager.getInstance().guiFx.hideMainPopup(1);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {


    }
}
