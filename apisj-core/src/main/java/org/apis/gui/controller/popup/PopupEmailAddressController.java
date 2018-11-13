package org.apis.gui.controller.popup;

import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.PopupManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupEmailAddressController extends BasePopupController {

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void send(){
        PopupManager.getInstance().hideMainPopup(1);
        PopupManager.getInstance().showMainPopup(null, "popup_success.fxml", 1);
    }
}
