package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.PopupManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupLedgerNoticeController extends BasePopupController {

    @FXML
    public void exit() {
        PopupManager.getInstance().hideMainPopup(zIndex);
        parentRequestFocus();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();
    }

    public void languageSetting() {

    }
}
