package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingItemBtnController implements Initializable {
    @FXML
    private Label contents;
    @FXML
    private SlideButtonController slideBtnController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setContents(String contents) {
        this.contents.setText(contents);
    }

    public String getContents() {
        return this.contents.getText();
    }

    public void setSelected(boolean status) {
        this.slideBtnController.setSelected(status);
    }

    public boolean isSelected() {
        return this.slideBtnController.isSelected();
    }
}
