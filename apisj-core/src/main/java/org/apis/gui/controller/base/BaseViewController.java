package org.apis.gui.controller.base;


import javafx.fxml.Initializable;
import org.apis.gui.model.base.BaseModel;

import java.net.URL;
import java.util.ResourceBundle;

public class BaseViewController implements Initializable {

    protected BaseModel model;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void update(){

    }

    public void setModel(BaseModel model){
        this.model = model;
    }

    public BaseModel getModel() {
        return this.model;
    }
}
