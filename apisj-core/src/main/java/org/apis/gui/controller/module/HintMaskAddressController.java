package org.apis.gui.controller.module;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.manager.StyleManager;

import java.net.URL;
import java.util.ResourceBundle;

public class HintMaskAddressController extends BaseViewController {
    @FXML private AnchorPane bgAnchor;
    @FXML private Label hintMaskAddressLabel;
    @FXML private ImageView hintIcon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setVisible(boolean isVisible) {
        bgAnchor.setVisible(isVisible);
    }

    public void setSuccessed(){
        StyleManager.fontColorStyle(hintMaskAddressLabel, StyleManager.AColor.C36b25b);
        hintIcon.setImage(ImageManager.icCheckGreen);
    }

    public void setSuccessed(SimpleStringProperty message){
        hintMaskAddressLabel.textProperty().unbind();
        hintMaskAddressLabel.textProperty().bind(message);
        StyleManager.fontColorStyle(hintMaskAddressLabel, StyleManager.AColor.C36b25b);
        hintIcon.setImage(ImageManager.icCheckGreen);
    }

    public void setFailed(SimpleStringProperty message){
        hintMaskAddressLabel.textProperty().unbind();
        hintMaskAddressLabel.textProperty().bind(message);
        StyleManager.fontColorStyle(hintMaskAddressLabel, StyleManager.AColor.C910000);
        hintIcon.setImage(ImageManager.icErrorRed);
    }

    public String getHintMaskAddressLabel() {
        return hintMaskAddressLabel.getText();
    }

    public void setHintMaskAddressLabel(String messageLabel) {
        this.hintMaskAddressLabel.textProperty().unbind();
        this.hintMaskAddressLabel.setText(messageLabel);
    }
}
