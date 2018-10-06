package org.apis.gui.controller.base;

import javafx.fxml.FXML;
import org.apis.gui.manager.PopupManager;

import java.net.URL;
import java.util.ResourceBundle;

public class BasePopupController extends BaseViewController {
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
