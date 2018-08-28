package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.model.SelectBoxDomainModel;
import org.apis.gui.model.SelectBoxWalletItemModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxHeadOnlyAddressController implements Initializable {
    private SelectBoxWalletItemModel itemModel;

    @FXML
    private Label addressLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setModel(SelectBoxWalletItemModel model) {
        this.itemModel = model;

        addressLabel.textProperty().setValue(itemModel.getAddress());
    }

    public String getAddress(){ return this.itemModel.getAddress(); }
    public String getKeystoreId() { return this.itemModel.getKeystoreId(); }
}
