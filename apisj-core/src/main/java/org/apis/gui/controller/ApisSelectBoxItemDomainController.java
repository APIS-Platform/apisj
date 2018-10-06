package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.model.SelectBoxDomainModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxItemDomainController extends BaseViewController {
    private SelectBoxDomainModel itemModel;
    private SelectBoxItemDomainInterface handler;

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Label domainLabel, priceLabel;

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
        this.itemModel = (SelectBoxDomainModel)model;

        this.domainLabel.textProperty().setValue(this.itemModel.getDomain());
        this.priceLabel.textProperty().setValue(this.itemModel.getApis()+" APIS");
    }

    public void setHandler(SelectBoxItemDomainInterface handler) {
        this.handler = handler;
    }

    public interface SelectBoxItemDomainInterface {
        void onMouseClicked(SelectBoxDomainModel itemModel);
    }
}
