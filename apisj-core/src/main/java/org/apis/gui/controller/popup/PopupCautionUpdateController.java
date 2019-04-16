package org.apis.gui.controller.popup;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.config.SystemProperties;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupCautionUpdateController extends BasePopupController implements PopupUpdateController.PopupProcessCallback {
    @FXML private AnchorPane bgAnchor;
    @FXML private Label cautionTitle, cautionUpdateFirst, cautionUpdateSecond, cautionUpdateThird, yesBtn, noBtn;

    private String latestVer;
    private PopupUpdateController updateController;
    private PopupSuccessController successController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        bgAnchor.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ENTER) {
                    versionUpdate();
                }
            }
        });

        yesBtn.setOnMouseClicked(event -> {
            versionUpdate();
        });

        noBtn.setOnMouseClicked(event -> {
            exit();
        });
    }

    public void exitAll(){
        PopupManager.getInstance().hideMainPopup(zIndex-1);
        PopupManager.getInstance().hideMainPopup(zIndex);
        parentRequestFocus();
    }

    private void languageSetting() {
        cautionTitle.textProperty().bind(StringManager.getInstance().popup.cautionTitle);
        cautionUpdateFirst.textProperty().bind(StringManager.getInstance().popup.cautionUpdateFirst);
        cautionUpdateSecond.textProperty().bind(StringManager.getInstance().popup.cautionUpdateSecond);
        yesBtn.textProperty().bind(StringManager.getInstance().popup.yesBtn);
        noBtn.textProperty().bind(StringManager.getInstance().popup.noBtn);
    }

    private void versionUpdate() {
        // Activate update popup
        updateController = (PopupUpdateController) PopupManager.getInstance().showMainPopup(null, "popup_update.fxml", 0);
        updateController.setLatestVer(latestVer);
        updateController.callback = this;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateController.versionUpdate();
            }
        });
        thread.start();
    }

    public void setLatestVer(String latestVer) {
        this.latestVer = latestVer;
    }

    @Override
    public void exitPopupProcessCallback(String zipFileName) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                successController = (PopupSuccessController) PopupManager.getInstance().showMainPopup(null, "popup_success.fxml", 0);
                successController.setSubTitle(StringManager.getInstance().popup.successSubTitleUpdate);
                successController.requestFocusYesButton();
                successController.setRestartApp(zipFileName);
            }
        });
    }
}
