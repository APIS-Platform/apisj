package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.model.SelectBoxWalletItemModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxItemOnlyAddressController extends BaseViewController {
    private SelectBoxWalletItemModel itemModel;
    private SelectBoxItemOnlyAddressInterface handler;

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Label textLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    public void onMouseEntered(){ rootPane.setStyle("-fx-background-color: f2f2f2"); }
    public void onMouseExited(){
        rootPane.setStyle("-fx-background-color: transparent");
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        if(handler != null){
            handler.onMouseClicked(this.itemModel);
        }
        event.consume();
    }

    @Override
    public void setModel(BaseModel model) {
        this.itemModel = (SelectBoxWalletItemModel)model;

        this.textLabel.textProperty().setValue(this.itemModel.getAddress());
    }

    public void setHandler(SelectBoxItemOnlyAddressInterface handler) {
        this.handler = handler;
    }

    public interface SelectBoxItemOnlyAddressInterface {
        void onMouseClicked(SelectBoxWalletItemModel itemModel);
    }
}
