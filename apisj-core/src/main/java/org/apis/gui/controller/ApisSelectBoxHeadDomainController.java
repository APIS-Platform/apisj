package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apis.gui.model.SelectBoxDomainModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ApisSelectBoxHeadDomainController implements Initializable {
    private SelectBoxDomainModel itemModel;

    @FXML
    private Label domainLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setModel(SelectBoxDomainModel model) {
        this.itemModel = model;

        domainLabel.textProperty().setValue(itemModel.getDomain());
    }

    public String getDomainId(){ return this.itemModel.getDomainId(); }
    public String getDomain(){ return this.itemModel.getDomain(); }
    public String getApis(){ return this.itemModel.getApis(); }
}
