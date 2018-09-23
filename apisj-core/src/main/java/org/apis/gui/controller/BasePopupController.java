package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.apis.gui.manager.PopupManager;

import java.net.URL;
import java.util.ResourceBundle;

public class BasePopupController implements Initializable {
    protected int zIndex = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    public void exit(){
        PopupManager.getInstance().hideMainPopup(zIndex);
    }

    public void setZIndex(int zIndex){
        this.zIndex = zIndex;
    }

}
