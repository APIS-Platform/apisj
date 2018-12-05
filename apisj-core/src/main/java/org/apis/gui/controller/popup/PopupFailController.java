package org.apis.gui.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupFailController extends BasePopupController {
    @FXML private AnchorPane bgAnchor;
    @FXML private Label title, subTitle, yesBtn, error;

    @FXML
    public void exit(){
        PopupManager.getInstance().hideMainPopup(zIndex-1);
        PopupManager.getInstance().hideMainPopup(zIndex);
        parentRequestFocus();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        bgAnchor.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.ENTER) {
                exit();
            }
        });
    }

    public void languageSetting() {
        title.textProperty().bind(StringManager.getInstance().popup.failTitle);
        subTitle.textProperty().bind(StringManager.getInstance().popup.failSubTitle);
        yesBtn.textProperty().bind(StringManager.getInstance().common.yesButton);
    }

    public void setError(String error) {
        this.error.setText(error);
    }
}
