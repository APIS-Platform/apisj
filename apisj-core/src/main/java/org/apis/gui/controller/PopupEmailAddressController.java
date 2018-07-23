package org.apis.gui.controller;

import javafx.fxml.Initializable;
import org.apis.gui.manager.AppManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupEmailAddressController implements Initializable {

    public void exit(){
        AppManager.getInstance().guiFx.hideMainPopup(1);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void send(){
        AppManager.getInstance().guiFx.hideMainPopup(1);
        AppManager.getInstance().guiFx.showMainPopup("popup_success.fxml", 1);
    }
}
