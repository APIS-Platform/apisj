package org.apis.gui.controller.module;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import org.apis.gui.controller.base.BaseSelectBoxItemController;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.model.SelectBoxItemModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxItemOnlyAddressController extends BaseSelectBoxItemController {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Label textLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void setModel(BaseModel model) {
        this.model = model;
        SelectBoxItemModel itemModel = (SelectBoxItemModel)this.model;

        this.textLabel.textProperty().setValue(itemModel.getAddress());
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        if(handler != null){
            handler.onMouseClicked((SelectBoxItemModel)this.model);
        }
        event.consume();
    }
    public void onMouseEntered(){ rootPane.setStyle("-fx-background-color: f2f2f2"); }
    public void onMouseExited(){
        rootPane.setStyle("-fx-background-color: transparent");
    }
}
