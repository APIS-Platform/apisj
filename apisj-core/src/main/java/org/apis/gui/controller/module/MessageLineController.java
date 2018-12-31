package org.apis.gui.controller.module;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.manager.ImageManager;
import org.apis.gui.manager.StringManager;
import org.apis.gui.manager.StyleManager;

import java.net.URL;
import java.util.ResourceBundle;

public class MessageLineController extends BaseViewController {

    @FXML private AnchorPane rootPane;
    @FXML private ImageView icon;
    @FXML private Label messageLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setVisible(boolean isVisible) {
        rootPane.setVisible(isVisible);
    }

    public void setSuccessed(SimpleStringProperty message){
        messageLabel.textProperty().unbind();
        messageLabel.textProperty().bind(message);
        StyleManager.fontColorStyle(messageLabel, StyleManager.AColor.C36b25b);
        icon.setImage(ImageManager.icCheckGreen);
    }

    public void setFailed(SimpleStringProperty message){
        messageLabel.textProperty().unbind();
        messageLabel.textProperty().bind(message);
        StyleManager.fontColorStyle(messageLabel, StyleManager.AColor.C910000);
        icon.setImage(ImageManager.icErrorRed);
    }

    public String getMessageLabel() {
        return messageLabel.getText();
    }

    public void setMessageLabel(String messageLabel) {
        this.messageLabel.setText(messageLabel);
    }
}
