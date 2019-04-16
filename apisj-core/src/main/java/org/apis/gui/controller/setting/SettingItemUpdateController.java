package org.apis.gui.controller.setting;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import org.apis.gui.controller.popup.PopupCautionUpdateController;
import org.apis.gui.manager.PopupManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingItemUpdateController implements Initializable {
    @FXML private Label contents, latestVerLabel, latestVer, versionChk, updateBtn;

    public static final boolean VERSION_UPDATED = true;
    public static final boolean VERSION_NOT_UPDATED = false;
    private boolean versionStatus = VERSION_NOT_UPDATED;
    private PopupCautionUpdateController cautionUpdateController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        updateBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if(!versionStatus) {
                    cautionUpdateController = (PopupCautionUpdateController) PopupManager.getInstance().showMainPopup(null, "popup_caution_update.fxml", 0);
                    cautionUpdateController.setLatestVer(latestVer.getText());
                }
            }
        });

        updateBtn.setOnMouseEntered(event -> {
            if(!isVersionStatus()) {
                StyleManager.backgroundColorStyle(updateBtn, StyleManager.AColor.C910000);
            }
        });

        updateBtn.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!isVersionStatus()) {
                    StyleManager.backgroundColorStyle(updateBtn, StyleManager.AColor.Cb01e1e);
                }
            }
        });
    }

    private void languageSetting() {
        latestVerLabel.textProperty().bind(StringManager.getInstance().setting.latestVerLabel);
        updateBtn.textProperty().bind(StringManager.getInstance().setting.updateLabel);
    }

    public String getContents() {
        return this.contents.getText();
    }

    public void setContents(String contents) {
        this.contents.setText(contents);
    }

    public String getLatestVer() {
        return this.latestVer.getText();
    }

    public void setLatestVer(String latestVer) {
        this.latestVer.setText(latestVer);
    }

    public void setVersionChk(String versionChk) {
        this.versionChk.setText(versionChk);
    }

    public boolean isVersionStatus() {
        return this.versionStatus;
    }

    public void setVersionStatus(boolean versionStatus) {
        this.versionStatus = versionStatus;
        if(versionStatus) {
            StyleManager.backgroundColorStyle(updateBtn, StyleManager.AColor.Cd8d8d8);
        } else {
            StyleManager.backgroundColorStyle(updateBtn, StyleManager.AColor.Cb01e1e);
        }
    }

}
