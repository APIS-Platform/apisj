package org.apis.gui.controller.module.receipt;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.base.BaseViewController;

import java.net.URL;
import java.util.ResourceBundle;

public class ReceiptItemController extends BaseViewController {

    @FXML private Label title, value;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setValue(String value){
        this.value.setText(value);
    }

    public void setTitle(SimpleStringProperty title) {
        this.title.textProperty().unbind();
        this.title.textProperty().bind(title);
    }
}
