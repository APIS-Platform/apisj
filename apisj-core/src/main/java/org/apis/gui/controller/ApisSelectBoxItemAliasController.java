package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.model.SelectBoxWalletItemModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxItemAliasController implements Initializable {
    private SelectBoxWalletItemModel itemModel;
    private SelectBoxItemAliasInterface handler;

    @FXML
    private AnchorPane rootPane;
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

    public void onMouseEntered(){
        rootPane.setStyle("-fx-background-color: f2f2f2");
    }

    public void onMouseExited(){
        rootPane.setStyle("-fx-background-color: transparent");
    }

    public void onMouseClicked(){
        if(handler != null){
            handler.onMouseClicked(this.itemModel);
        }
    }

    public SelectBoxItemAliasInterface getHandler() {
        return handler;
    }

    public void setHandler(SelectBoxItemAliasInterface handler) {
        this.handler = handler;
    }


    interface SelectBoxItemAliasInterface{
        void onMouseClicked(SelectBoxWalletItemModel itemModel);
    }
}
