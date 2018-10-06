package org.apis.gui.controller.module;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.base.BaseSelectBoxHeaderController;
import org.apis.gui.model.SelectBoxItemModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxHeadOnlyAddressController extends BaseSelectBoxHeaderController {

    @FXML
    private Label addressLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void setModel(BaseModel model) {
        this.itemModel = (SelectBoxItemModel)model;

        addressLabel.textProperty().setValue(itemModel.getAddress());
    }
}
