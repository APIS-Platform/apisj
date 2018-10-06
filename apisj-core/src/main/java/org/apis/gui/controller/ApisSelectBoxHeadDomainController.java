package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.model.SelectBoxDomainModel;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxHeadDomainController extends BaseViewController {
    private SelectBoxDomainModel itemModel;

    @FXML
    private Label domainLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void setModel(BaseModel model) {
        this.itemModel = (SelectBoxDomainModel)model;

        domainLabel.textProperty().setValue(itemModel.getDomain());
    }

    public String getDomainId(){ return this.itemModel.getDomainId(); }
    public String getDomain(){ return this.itemModel.getDomain(); }
    public String getApis(){ return this.itemModel.getApis(); }
}
