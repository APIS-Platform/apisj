package org.apis.gui.controller.module.receipt;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.base.BaseViewController;

import java.net.URL;
import java.util.ResourceBundle;

public class ReceiptAddressController extends BaseViewController {

    @FXML private Label title, address;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setAddress(String address){
        this.address.setText(address);
    }

    public void setTitle(SimpleStringProperty address) {
        this.title.textProperty().unbind();
        this.title.textProperty().bind(address);
    }
}
