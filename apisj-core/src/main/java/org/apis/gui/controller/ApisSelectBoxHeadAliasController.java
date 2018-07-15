package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.model.SelectBoxWalletItemModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxHeadAliasController implements Initializable {
    private SelectBoxWalletItemModel itemModel;

    @FXML
    private Label aliasLabel, addressLabel, maskLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setModel(SelectBoxWalletItemModel model) {
        this.itemModel = model;

        if(model != null) {
            aliasLabel.textProperty().unbind();
            addressLabel.textProperty().unbind();
            maskLabel.textProperty().unbind();

            aliasLabel.textProperty().bind(this.itemModel.aliasProperty());
            addressLabel.textProperty().bind(this.itemModel.addressProperty());
            maskLabel.textProperty().bind(this.itemModel.maskProperty());
        }
    }

    public String getAddress(){
        return this.addressLabel.getText();
    }
}
