package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupLedgerNoticeController extends BasePopupController {
    @FXML
    Label title, subTitle;

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
        title.textProperty().bind(StringManager.getInstance().popup.ledgerNoticeTitle);
        subTitle.textProperty().bind(StringManager.getInstance().popup.ledgerNoticeSubTitle);
    }
}
