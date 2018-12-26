package org.apis.gui.controller.popup;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.config.SystemProperties;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.AppManager;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupCautionController extends BasePopupController {
    @FXML private Label cautionTitle, cancelWaiting, completeCancel, yesBtn, noBtn;

    public void exitAll(){
        PopupManager.getInstance().hideMainPopup(zIndex-1);
        PopupManager.getInstance().hideMainPopup(zIndex);
        parentRequestFocus();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();
    }

    public void languageSetting() {
        cautionTitle.textProperty().bind(StringManager.getInstance().popup.cautionTitle);
        cancelWaiting.textProperty().bind(StringManager.getInstance().popup.cancelWaiting);
        completeCancel.textProperty().bind(StringManager.getInstance().popup.completeCancel);
        yesBtn.textProperty().bind(StringManager.getInstance().popup.yesBtn);
        noBtn.textProperty().bind(StringManager.getInstance().popup.noBtn);
    }

    public void initMessageCancel() {
        cancelWaiting.setVisible(true);
        completeCancel.setVisible(false);
    }

    public void initMessageComplete() {
        cancelWaiting.setVisible(false);
        completeCancel.setVisible(true);
    }

    private void cancelWaiting() {
        AppManager.saveGeneralProperties("masternode_state", Integer.toString(AppManager.MnState.EMPTY_MASTERNODE.num));
        SystemProperties.getDefault().setMasternodePrivateKey(null);
        SystemProperties.getDefault().setMasternodeRecipient(null);
        exitAll();
    }

    private void completeCancel() {
        AppManager.saveGeneralProperties("masternode_state", Integer.toString(AppManager.MnState.CANCEL_MASTERNODE.num));
        SystemProperties.getDefault().setMasternodePrivateKey(null);
        SystemProperties.getDefault().setMasternodeRecipient(null);
        exitAll();
    }

    @FXML
    private void onMouseClicked(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("yesBtn")) {
            if(cancelWaiting.isVisible()) {
                cancelWaiting();
            } else if(completeCancel.isVisible()) {
                completeCancel();
            }
        } else if(fxid.equals("noBtn")) {
            exit();
        }
    }
}
