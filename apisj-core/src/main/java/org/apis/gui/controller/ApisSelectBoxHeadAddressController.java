package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.model.SelectBoxWalletItemModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxHeadAddressController implements Initializable {
    private SelectBoxWalletItemModel itemModel;

    @FXML
    private Label  addressLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setModel(SelectBoxWalletItemModel model) {
        this.itemModel = model;

        if(model != null) {
            addressLabel.textProperty().unbind();

            addressLabel.textProperty().bind(this.itemModel.addressProperty());
        }
    }

    public String getAddress(){
        return this.addressLabel.getText();
    }
    public String getKeystoreId() { return this.itemModel.getKeystoreId(); }

    public String getBalance() { return this.itemModel.getBalance(); }

    public String getMineral() { return this.itemModel.getMineral(); }
}
