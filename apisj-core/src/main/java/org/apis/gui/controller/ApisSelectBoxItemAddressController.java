package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.model.SelectBoxWalletItemModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxItemAddressController implements Initializable {
    private SelectBoxWalletItemModel itemModel;
    private ApisSelectBoxItemAddressController.SelectBoxItemAddressInterface handler;

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Label addressLabel;

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

    public ApisSelectBoxItemAddressController.SelectBoxItemAddressInterface getHandler() {
        return handler;
    }

    public void setHandler(ApisSelectBoxItemAddressController.SelectBoxItemAddressInterface handler) {
        this.handler = handler;
    }


    interface SelectBoxItemAddressInterface{
        void onMouseClicked(SelectBoxWalletItemModel itemModel);
    }
}
