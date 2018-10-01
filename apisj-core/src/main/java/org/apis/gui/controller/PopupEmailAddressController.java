package org.apis.gui.controller;

import javafx.fxml.Initializable;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupEmailAddressController extends BasePopupController {

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void send(){
        PopupManager.getInstance().hideMainPopup(1);
        PopupManager.getInstance().showMainPopup("popup_success.fxml", 1);
    }
}
