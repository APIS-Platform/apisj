package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.model.SelectBoxWalletItemModel;
import org.apis.gui.model.base.BaseModel;

import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxHeadOnlyAddressController extends BaseViewController {
    private SelectBoxWalletItemModel itemModel;

    @FXML
    private Label addressLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void setModel(BaseModel model) {
        this.itemModel = (SelectBoxWalletItemModel)model;

        addressLabel.textProperty().setValue(itemModel.getAddress());
    }

    public String getAddress(){ return this.itemModel.getAddress(); }
    public String getKeystoreId() { return this.itemModel.getKeystoreId(); }
    public BigInteger getMineral() { return this.itemModel.getMineral(); }
}
